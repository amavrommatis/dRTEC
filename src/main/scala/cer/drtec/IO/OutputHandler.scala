package cer.drtec.IO

import java.io.{File, FileWriter, BufferedWriter}
import cer.drtec.engine._
import cer.drtec.utils.SerializedLogging
import org.apache.spark.streaming.dstream.DStream

/**
 * @author Alexandros Mavrommatis
 */
private[drtec] object OutputHandler extends SerializedLogging {

  private var bw: BufferedWriter = null
  /**
   * Prints the result of the event recognition to console and file
   * @param stream the stream with the results
   * @param path the path of the output file
   * @param enableLog whether to write to file or not
   * @param enableConsole whether to write to console or not
   */
  private[drtec] def printStream(stream: DStream[(Vector[String], (Vector[String], Map[Vector[String], Occurrences], Int))],
                  path: String, enableLog: Boolean, enableConsole: Boolean)(implicit domain: Theory) {

    if(enableLog) bw = new BufferedWriter(new FileWriter(new File(path)))

    stream.foreachRDD { rdd =>

      rdd.collect.foreach { case (id1, (id2, map, window)) =>
        map.get(id2) match {
          case Some(occ) =>

            occ.map.filter { case (entity, interval) =>
              domain.outputEntities.contains(entity)
            }.foreach { case (entity, (interval, calc)) =>

              var intervals = Vector[(String, String)]()
              val it = interval.rangeIterator()

              //create intervals
              while (it.moveToNext()) {

                val first = it.first() * domain.step
                val last = it.last() match {
                  case Infinity => "inf"
                  case _ => ((it.last() + 1) * domain.step).toString
                }

                intervals = intervals.:+(first.toString, last)

              }

              //print
              if (entity.fluent.values.length > 1) {
                if(enableConsole)
                  info("dRTEC INFO: " + entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=(" + entity.fluent.values.mkString(",") + ")" +
                    ",[" + intervals.mkString(",") + "])")
                if (enableLog)
                  bw.write(entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=(" + entity.fluent.values.mkString(",") + ")" +
                    ",[" + intervals.mkString(",") + "])\n")
              }
              else if (entity.fluent.values.length == 1) {
                if(enableConsole)
                  info("dRTEC INFO: " + entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=" + entity.fluent.values.head +
                    ",[" + intervals.mkString(",") + "])")
                if (enableLog)
                  bw.write(entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=" + entity.fluent.values.head +
                    ",[" + intervals.mkString(",") + "])\n")
              }
              else {
                if(enableConsole)
                  info("dRTEC INFO: " + entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")" + ",[" + intervals.mkString(",") + "])")
                if (enableLog)
                  bw.write(entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")" + ",[" + intervals.mkString(",") + "])\n")
              }
            }
          case None =>
        }
      }
    }
  }

  /**
    * Prints the result of the event recognition to console or sends to mqtt broker
    * @param stream the stream with the results
    * @param mqttClient the client for publishing the data
    * @param enableLog whether to write to file or not
    * @param enableConsole whether to write to console or not
    */
  private[drtec] def sendToMQTT(stream: DStream[(Vector[String], (Vector[String], Map[Vector[String], Occurrences], Int))],
                  mqttClient: MQTTConnector, enableLog: Boolean, enableConsole: Boolean)(implicit domain: Theory) {

    stream.foreachRDD { rdd =>

      rdd.collect.foreach { case (id1, (id2, map, window)) =>
        map.get(id2) match {
          case Some(occ) =>

            occ.map.filter { case (entity, interval) =>
              domain.outputEntities.contains(entity)
            }.foreach { case (entity, (interval, calc)) =>

              var intervals = Vector[(String, String)]()
              val it = interval.rangeIterator()

              //create intervals
              while (it.moveToNext()) {

                val first = it.first() * domain.step
                val last = it.last() match {
                  case Infinity => "inf"
                  case _ => ((it.last() + 1) * domain.step).toString
                }

                intervals = intervals.:+(first.toString, last)

              }

              //print
              if (entity.fluent.values.length > 1) {
                if(enableConsole)
                  info("dRTEC INFO: " + entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=(" + entity.fluent.values.mkString(",") + ")" +
                    ",[" + intervals.mkString(",") + "])")
                if (enableLog)
                  mqttClient.publish(entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=(" + entity.fluent.values.mkString(",") + ")" +
                    ",[" + intervals.mkString(",") + "])")
              }
              else if (entity.fluent.values.length == 1) {
                if(enableConsole)
                  info("dRTEC INFO: " + entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=" + entity.fluent.values.head +
                    ",[" + intervals.mkString(",") + "])")
                if (enableLog)
                  mqttClient.publish(entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")=" + entity.fluent.values.head +
                    ",[" + intervals.mkString(",") + "])")
              }
              else {
                if(enableConsole)
                  info("dRTEC INFO: " + entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")" + ",[" + intervals.mkString(",") + "])")
                if (enableLog)
                  mqttClient.publish(entity.predicate + "(" + entity.fluent.symbol + "(" +
                    (id2 ++ entity.fluent.args).mkString(",") + ")" + ",[" + intervals.mkString(",") + "])")
              }
            }
          case None =>
        }
      }
    }
  }

  /**
    * closes file streams
    */
  private[drtec] def closeFileStreams() {

    if(bw != null) {
      info("dRTEC INFO: File streams closed...")
      bw.close()
    }
  }

  /**
    * closes mqtt connection
    */
  private[drtec] def disconnectFromMqtt(mqttConnection: Option[MQTTConnector]) {

    mqttConnection match {
      case Some(conn) =>
        if(conn.client != null) {
          conn.client.disconnect()
          conn.client.close()
          info("dRTEC INFO: MQTT disconnected...")
        }
      case None =>
    }

  }
}
