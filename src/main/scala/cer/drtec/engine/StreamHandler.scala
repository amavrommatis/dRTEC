package cer.drtec.engine

import cer.drtec.utils.{SerializedLogging, IntervalFactory}
import cer.drtec.utils.IntervalWrapperObj._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import cer.drtec.engine.Predicate._

/**
 * @author Alexandros Mavrommatis
 *
 * Data parsing and indexing
 */
private[drtec] object StreamHandler extends SerializedLogging {

  /**
   * Converts input data in the representation based on the engine
   * @return the list of the input instances
   */
  private[drtec] def parsing(stream: DStream[String])(implicit domain: Theory) = {

    //parsing
    stream.flatMap { line =>
      parseLine(line)
    }
  }

  /**
   * Parses each line of the stream
   * @param line input line
   * @return the parsed line
   */
  private def parseLine(line: String)(implicit domain: Theory) = {

    val step = domain.step
    val fields = line.split(",")  //split elements

    if(fields.length != 6) None
    else {
      val predicate = fields(0).trim
      val symbol = fields(1).trim
      var args = fields(2).trim.split(";").filter(_.nonEmpty).toVector
      val values = fields(3).trim.split(";").filter(_.nonEmpty).toVector
      val timeStr = fields(4).trim.split(";").filter(_.nonEmpty).toVector
      val window = fields(5).trim.toInt
      var id = Vector[String]()
      var time: Interval = null

      domain.inputSchema.get(symbol) match {
        case Some((idsSize, argsSize)) =>

          //check if args are correct
          if (args.length == argsSize + idsSize) {

            //pass ids
            for (i <- 0 until idsSize) {
              id = id :+ args(i)
            }

            //pass rest of args
            args = args.drop(idsSize)


            //pass time (init/step, (term/step) + 1)
            if (timeStr.length == 1) {
              val timepoint = timeStr.head.trim.toLong
              time = IntervalFactory(timepoint / step, timepoint / step)
            }
            else time = IntervalFactory(timeStr.head.trim.toLong / step, (timeStr.last.trim.toLong / step) - 1)

            //form entry
            if (predicate == "happensAt" && values.isEmpty)
              Some(id, (Entity(happensAt, Fluent(symbol, args, values)), time, window))
            else if (predicate == "happensAt" && values.nonEmpty)
              None
            else
              Some(id, (Entity(holdsFor, Fluent(symbol, args, values)), time, window))
          }
          else None
        case None => None
      }
    }
  }

  /**
   * Indexes the input data according to the id
   * @param stream the input stream
   * @return stream grouped by id
   */
  private[drtec] def indexing(stream: DStream[(Vector[String], (Entity, Interval, Int))]) = {

    //copy the input data that has more than one ids to the corresponding ids
    stream.flatMap{ case (key, (entity, interval, window)) =>

      //relax vector of ids to each id separately
      var relaxedEntries = Vector[(Vector[String], (Vector[String], Vector[String], Entity, Interval, Int))]()

      key.foreach{ id =>
        relaxedEntries = relaxedEntries :+ (Vector(id), (Vector(id), key, entity, interval, window))
      }
      relaxedEntries

    }.groupByKey().mapValues{iter1 =>
      val relaxedId = iter1.head._1
      val relaxedWindow = iter1.map(_._5).max

      //group by the input ids
      val relaxedMap = iter1.map{ case (relId, id, entity, interval, window) => (id, entity, interval)}.groupBy(_._1)
      .mapValues{ iter2 =>
        //groupby the entity for each id  - filter out none entities
        Occurrences(iter2.map{ case (id, entity, interval) => (entity, interval)}.filterNot{ case (entity, interval) =>
            entity.fluent.symbol == "none"
        }.groupBy(_._1).mapValues{ iter3 =>
          (iter3.map(_._2).fold(IntervalFactory())((range1, range2) => range1.union_all(range2)), false)
        }.map(identity))
      }.map(identity)

      (relaxedId, relaxedMap, relaxedWindow)
    }
  }

  /**
   * Joins the input stream in order to create combinations of ids
   * @param stream input stream
   * @param domain Theory
   * @return the joined stream
   */
  private[drtec] def dynamicGrounding(stream: DStream[(Vector[String], (Vector[String], Map[Vector[String], Occurrences], Int))])
             (implicit domain: Theory, ssc: StreamingContext) = {

    var previousStreams = Vector[DStream[(Vector[String], (Vector[String], Map[Vector[String], Occurrences], Int))]]()

    var lastStream = stream

    //add stream with one id keys if there are output entities for one id
    if(domain.complexEntities.keySet.contains(1))
      previousStreams = previousStreams :+ lastStream

    for(i <- 1 until domain.maxNumOfIds){

      //cartesian to create pair of ids
      lastStream = lastStream.transformWith(stream, { (rdd1, rdd2: RDD[(Vector[String], (Vector[String], Map[Vector[String], Occurrences], Int))]) =>

        rdd1.cartesian(rdd2).flatMap{ case ( (id1, (idIn1, map1, window1)), (id2, (idIn2, map2, window2)) ) =>
          val newId = id1 ++ id2
          if(newId == newId.distinct) Some(newId, (newId, map1 ++ map2, scala.math.max(window1, window2)))
          else None
        }
      })

      //add stream to the list if needed
      if(domain.complexEntities.keySet.contains(i + 1))
        previousStreams = previousStreams :+ lastStream
    }

    //merge streams of the list
    //remove unnecessary entries
    ssc.union(previousStreams).mapValues{ case (id, map, window) =>
        val filteredMap = map.filter{ case (idIn, occ) => idIn.diff(id).isEmpty}
      (id, filteredMap, window)
    }
  }

  /**
   * initializes table for storing ids
   * @param tableName table name
   * @param sc spark context
   */
  private[drtec] def initializeIdsState(tableName: String)(implicit sc: SparkContext, sqlc: SQLContext) {

    import sqlc.implicits._
    val df = sc.parallelize(Array[State]()).toDF()
    df.registerTempTable(tableName)
  }

  /**
   * returns the former state from the previous window
   * @param stream the stream for storing state
   * @param tableName state table name
   * @param sqlc SQLContext
   * @return stream with previous state
   */
  private[drtec] def retrieveFormerState(stream: DStream[(Vector[String], (Vector[String], Map[scala.Vector[String], Occurrences], Int))],
                        tableName: String)(implicit sqlc: SQLContext) = {

    val idsStream = stream.transform{ rdd =>
      sqlc.sql(s"SELECT * FROM $tableName").map{tuple =>
        val id = tuple.getAs[String]("id")
        val window = tuple.getAs[Int]("window")
        (Vector(id), (Vector(id), Map[Vector[String], Occurrences](), window))
      }
    }

    stream.union(idsStream).reduceByKey{(entry1, entry2) =>
      if(entry1._2.isEmpty) entry2
      else entry1
    }
  }

  /**
   * stores previous state to the appropriate table
   * @param stream recognition stream
   * @param tableName table name
   * @param sqlc SQLContext
   */
  private[drtec] def saveState(stream: DStream[(Vector[String], (Vector[String], Map[Vector[String], Occurrences], Int))],
              tableName: String)(implicit sqlc: SQLContext) {


    import sqlc.implicits._
    stream.flatMap{ case (idOut, (idIn, map, window)) =>
      idOut.map(id => (id, window))
    }.transform(rdd => rdd.distinct()).map{ case (id, window) =>
      State(id, window)
    }.foreachRDD{ rdd =>
      val dataFrame = rdd.toDF()
      dataFrame.registerTempTable(tableName)
    }
  }
}


