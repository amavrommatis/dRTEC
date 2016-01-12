package cer.drtec.engine

import cer.drtec.utils.IntervalFactory
import cer.drtec.utils.IntervalWrapperObj._
import cer.drtec.engine.Predicate._

/**
 * @author Alexandros Mavrommatis
 */
private[drtec] object StateHandler {

  /**
   * Executes the window operation and event recognition for simple events
   * @param newValues input data
   * @param state former state
   * @param domain Theory
   * @return the recognized data
   */
  private[drtec] def windowMechanismSimple(newValues: Seq[(Vector[String], Map[Vector[String], Occurrences], Int)],
                         state: Option[(Vector[String], Map[Vector[String], Occurrences], Int)])(implicit domain: Theory)
  = {

    //merge input entities in one map
    val mergedInput = inputDataConcat(newValues)
    val inputId = mergedInput._1
    val inputEntities = mergedInput._2
    val inputWindow = mergedInput._3

    state match {

      case Some(oldState) =>

        val oldId = oldState._1
        val oldEntities = oldState._2
        val oldWindow = oldState._3

        //****trick*****
        val newWindow = {
          if (inputWindow == 1) 1
          else oldWindow + 1
        }

        //calculate window parameters
        implicit val (windowStart, windowEnd, windowInterval) = calculateWindowInterval(newWindow)

        //stage 1 - merge input entities to create processedIEs
        // (amalgamation of input entities with previous processedIEs)
        val processedIEs = createProcessedIEs(oldEntities, inputEntities)

        //stage 2 - filter output entities from previous state
        val outputEntities = filterOutputEntities(oldEntities)

        //stage 3 - window operation for input entities
        val inputEntitiesWM = windowInputEntities(inputEntities, windowInterval)

        //stage 4 - window operation for output entities
        val outputEntitiesWM = windowOutputEntities(outputEntities, windowStart, windowEnd)

        //stage 5 - window operation for processedIEs
        val processedIEsWM = windowProcessedIEs(processedIEs, windowStart, windowEnd)

        //stage 6 - merge input entities with output entities and processedIEs for the current WM
        val newEntities = mergeNewEntities(inputEntitiesWM, outputEntitiesWM, processedIEsWM)

        //stage 7 - event recognition
        val recognizedEntities = simpleEventRecognition(newEntities, oldId)

        /*if(oldId == Vector("id4") || oldId == Vector("id6"))
          enableDebugging(windowStart, windowEnd, processedIEsWM, inputEntitiesWM, outputEntitiesWM, recognizedEntities)*/

        //stage 8 - clear recognized entities
        val clearEntities = clearRecognizedEntities(recognizedEntities)

        if (clearEntities.isEmpty) None
        else Some(oldId, clearEntities, newWindow)

      case None =>

        //calculate window parameters
        implicit val (_, _, windowInterval) = calculateWindowInterval(inputWindow)

        //stage 1 - create processedIEs
        val processedIEs = createProcessedIEs(inputEntities)

        //stage 2 - merge input entities with processedIEs for the current WM
        val newEntities = mergeNewEntities(inputEntities, processedIEs)

        //stage 3 - event recognition
        val recognizedEntities = simpleEventRecognition(newEntities, inputId)

        //stage 4 - clear recognized entities
        val clearEntities = clearRecognizedEntities(recognizedEntities)

        if (clearEntities.isEmpty) None
        else Some(inputId, clearEntities, inputWindow)

    }

  }

  /**
   * Executes the window operation and event recognition for complex events
   * @param newValues input data
   * @param state former state
   * @param domain Theory
   * @return the recognized data
   */
  private[drtec] def windowMechanismComplex(newValues: Seq[(Vector[String], Map[Vector[String], Occurrences], Int)],
                      state: Option[(Vector[String], Map[Vector[String], Occurrences], Int)])(implicit domain: Theory)
  = {

    //merge input entities in one map
    val mergedInput = inputDataConcat(newValues)
    val inputId = mergedInput._1
    val inputEntities = mergedInput._2
    val inputWindow = mergedInput._3

    state match {

      case Some(oldState) =>

        val oldId = oldState._1
        val oldEntities = oldState._2
        val oldWindow = oldState._3

        //pass simple events
        if(oldId.length != 1) {

          //****trick*****
          val newWindow = {
            if (inputWindow == 1) 1
            else oldWindow + 1
          }

          //calculate window parameters
          implicit val (windowStart, windowEnd, windowInterval) = calculateWindowInterval(newWindow)

          //stage 1 - filter output entities from previous state
          val outputEntities = filterOutputEntities(oldEntities)

          //stage 2 - window operation for input entities
          val inputEntitiesWM = windowInputEntities(inputEntities, windowInterval)

          //stage 3 - window operation for output entities
          val outputEntitiesWM = windowOutputEntities(outputEntities, windowStart, windowEnd)

          //stage 4 - merge input entities with output entities for the current WM
          val newEntities = mergeNewEntities(inputEntitiesWM, outputEntitiesWM)

          //stage 5 - event recognition
          val recognizedEntities = complexEventRecognition(newEntities, oldId)

          /*if(oldId == Vector("id4", "id6"))
        enableDebugging(windowStart, windowEnd, Map[Vector[String], Occurrences](), inputEntitiesWM, outputEntitiesWM, recognizedEntities)*/

          //stage 6 - clear recognized entities
          val clearEntities = clearRecognizedEntities(recognizedEntities)

          if (clearEntities.isEmpty) None
          else Some(oldId, clearEntities, newWindow)
        }
        else Some(oldId, inputEntities, inputWindow)

      case None =>

        //pass simple events
        if(inputId.length != 1) {

          //calculate window parameters
          implicit val (_, _, windowInterval) = calculateWindowInterval(inputWindow)

          //stage 1 - event recognition
          val recognizedEntities = complexEventRecognition(inputEntities, inputId)

          //stage 4 - clear recognized entities
          val clearEntities = clearRecognizedEntities(recognizedEntities)

          if (clearEntities.isEmpty) None
          else Some(inputId, clearEntities, inputWindow)
        }
        else Some(inputId, inputEntities, inputWindow)
    }

  }

  /**
   * For debugging
   * @param windowStart window start
   * @param windowEnd window end
   * @param processedIEsWM processed IEs
   * @param inputEntitiesWM input entities
   * @param outputEntitiesWM output entities
   * @param recognizedEntities recognized entities
   */
  private def enableDebugging(windowStart: Long, windowEnd: Long, processedIEsWM: Map[Vector[String], Occurrences],
                              inputEntitiesWM: Map[Vector[String], Occurrences],
                              outputEntitiesWM: Map[Vector[String], Occurrences],
                              recognizedEntities: Map[Vector[String], Occurrences]) = {

    print(Console.RESET + s"WINDOW[$windowStart-$windowEnd]\n\n")
    processedIEsWM.foreach{ case (id, occur) =>
      print(id + " processedIEs\t" + occur.map.filter{ case (entity, value) =>
        entity == Entity(holdsForProcessedIE, Fluent("walking", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("active", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("running", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("abrupt", Vector(), Vector("true")))
      } + "\n")
    }

    inputEntitiesWM.foreach{ case (id, occur) =>
      print(id + " inputEntities\t" + occur.map.filter{ case (entity, value) =>
        entity == Entity(holdsFor, Fluent("walking", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("active", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("running", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("abrupt", Vector(), Vector("true"))) ||
          entity == Entity(happensAt, Fluent("disappear", Vector(), Vector()))
      } + "\n")
    }

    outputEntitiesWM.foreach{ case (id, occur) =>
      print(id + " outputEntities\t" + occur.map.filter{ case (entity, value) =>
        entity == Entity(holdsForProcessedIE, Fluent("walking", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("active", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("running", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("abrupt", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("walking", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("active", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("running", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("abrupt", Vector(), Vector("true"))) ||
          entity == Entity(happensAt, Fluent("disappear", Vector(), Vector())) ||
          entity == Entity(initiatedAt, Fluent("person", Vector(), Vector("true"))) ||
          entity == Entity(terminatedAt, Fluent("person", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("person", Vector(), Vector("true"))) ||
          entity == Entity(initiatedAt, Fluent("leaving_object", Vector(), Vector("true"))) ||
          entity == Entity(terminatedAt, Fluent("leaving_object", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("leaving_object", Vector(), Vector("true")))
      } + "\n")
    }

    recognizedEntities.foreach{ case (id, occur) =>
      print(id + " recognizedEntities\t" + occur.map.filter{ case (entity, value) =>
        entity == Entity(holdsForProcessedIE, Fluent("walking", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("active", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("running", Vector(), Vector("true"))) ||
          entity == Entity(holdsForProcessedIE, Fluent("abrupt", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("walking", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("active", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("running", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("abrupt", Vector(), Vector("true"))) ||
          entity == Entity(happensAt, Fluent("disappear", Vector(), Vector())) ||
          entity == Entity(initiatedAt, Fluent("person", Vector(), Vector("true"))) ||
          entity == Entity(terminatedAt, Fluent("person", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("person", Vector(), Vector("true"))) ||
          entity == Entity(initiatedAt, Fluent("leaving_object", Vector(), Vector("true"))) ||
          entity == Entity(terminatedAt, Fluent("leaving_object", Vector(), Vector("true"))) ||
          entity == Entity(holdsFor, Fluent("leaving_object", Vector(), Vector("true")))
      } + "\n")
    }

    println()
  }

  /**
   * Executes rules for simple event recognition
   * @param inputEntities input entities
   * @param id id
   * @param domain Theory
   * @return recognized entities
   */
  private def simpleEventRecognition(inputEntities: Map[Vector[String], Occurrences], id: Vector[String])
                                     (implicit domain: Theory, windowInterval: Interval) = {

    var recognizedEntities = inputEntities
    domain.simpleEntities.foreach { entity =>
      //execute rules
      domain.declarations.get(entity).get.foreach(rule =>
        recognizedEntities = rule.recognize(recognizedEntities, id, entity))
    }
    recognizedEntities
  }

  /**
   * Executes rules for complex event recognition
   * @param inputEntities input entities
   * @param id id
   * @param domain Theory
   * @return recognized entities
   */
  private def complexEventRecognition(inputEntities: Map[Vector[String], Occurrences], id: Vector[String])
                              (implicit domain: Theory, windowInterval: Interval) = {

    var recognizedEntities = inputEntities

    domain.complexEntities.filter { case (numOfIds, rules) => numOfIds == id.length }
      .foreach { case (numOfIds, entities) =>

        entities.foreach { entity =>

          //execute rules
          domain.declarations.get(entity).get.foreach(rule =>
            recognizedEntities = rule.recognize(recognizedEntities, id, entity))
        }
      }
    recognizedEntities
  }

  /**
   * Calculates the parameters for the current window
   * @param window window number
   * @param domain Theory
   * @return the window parameters
   */
  private def calculateWindowInterval(window: Int)(implicit domain: Theory) = {

    val windowStart = scala.math.ceil((((window - 1) * domain.slide) + domain.startTime).toDouble
      / domain.step.toDouble).toLong
    val windowEnd = scala.math.ceil((((window - 1) * domain.slide) + domain.window + domain.startTime -
      domain.step).toDouble / domain.step.toDouble).toLong
    val windowInterval = IntervalFactory(windowStart, windowEnd)
    (windowStart, windowEnd, windowInterval)
  }

  /**
   * Clear recognized entities from empty intervals
   * @param map recognized entities
   * @return clear entities
   */
  private def clearRecognizedEntities(map: Map[Vector[String], Occurrences]) = {

    map.flatMap{ case (id, occur) =>
      val occurMap = occur.map.filter{ case (entity, (interval, calc)) => !interval.isEmpty}
      if(occurMap.isEmpty) None
      else Some(id, Occurrences(occurMap))
    }
  }

  /**
   * Merges the three maps with entities for recognition
   * @param inputEntities input entities map
   * @param processedIEs_outputEntities processedIEs or outputEntities map
   * @param domain theory
   * @return the merged map
   */
  private def mergeNewEntities(inputEntities: Map[Vector[String], Occurrences],
                               processedIEs_outputEntities : Map[Vector[String], Occurrences])(implicit domain: Theory) = {

    inputEntities ++ processedIEs_outputEntities.map{ case (id, processedOccur) =>

      inputEntities.get(id) match {
        case Some(inputOccur) =>
          val processedOccurMap = processedOccur.map
          val inputOccurMap = inputOccur.map
          (id, Occurrences(processedOccurMap ++ inputOccurMap))
        case None => (id, processedOccur)
      }
    }

  }

  /**
   * Merges the three maps with entities for recognition
   * @param inputEntitiesWM input entities map
   * @param outputEntitiesWM output entities map
   * @param processedIEsWM processedIEs map
   * @param domain theory
   * @return the merged map
   */
  private def mergeNewEntities(inputEntitiesWM: Map[Vector[String], Occurrences],
                               outputEntitiesWM: Map[Vector[String], Occurrences],
                               processedIEsWM: Map[Vector[String], Occurrences]) (implicit domain: Theory) = {

    val tempEntities = inputEntitiesWM ++ outputEntitiesWM.map{ case (id, outputOccur) =>

      inputEntitiesWM.get(id) match {
        case Some(inputOccur) =>
          val outputOccurMap = outputOccur.map
          val inputOccurMap = inputOccur.map
          (id, Occurrences(outputOccurMap ++ inputOccurMap))
        case None => (id, outputOccur)
      }
    }

    tempEntities ++ processedIEsWM.map{ case (id, processedOccur) =>

        tempEntities.get(id) match {
          case Some(tempOccur) =>
            val processedOccurMap = processedOccur.map
            val tempOccurMap = tempOccur.map
            (id, Occurrences(processedOccurMap ++ tempOccurMap))
          case None => (id, processedOccur)
        }
    }

  }

  /**
   * Executes window operation for processedIEs
   * @param map processedIEs
   * @param windowStart window start
   * @param windowEnd window end
   * @param domain theory
   * @return the processedIEs for the specific WM
   */
  private def windowProcessedIEs(map: Map[Vector[String], Occurrences], windowStart: Long, windowEnd: Long)
                                  (implicit domain: Theory) = {

    map.mapValues{ occur =>

      val occurMap = occur.map
      Occurrences(occurMap.flatMap{ case (entity, (interval, calc)) =>

        //keep only processedIEs that end in the WM
        var newInterval = IntervalFactory()
        val it = interval.rangeIterator()
        while (it.moveToNext()){
          if(it.last() >= windowStart) newInterval = newInterval.union_all(IntervalFactory(it.first(), it.last()))
        }

        if(newInterval.isEmpty) None
        else Some(entity, (newInterval,false))
      })
    }
  }

  /**
   * Executes window operation for output entities
   * @param map output entities
   * @param windowStart window start
   * @param windowEnd window end
   * @param domain theory
   * @return the output entities for the specific WM
   */
  private def windowOutputEntities(map: Map[Vector[String], Occurrences], windowStart: Long, windowEnd: Long)
                                (implicit domain: Theory) = {

    map.mapValues{ occur =>

      val occurMap = occur.map
      Occurrences(occurMap.flatMap { case (entity, (interval, calc)) =>

        entity.predicate match {

          //keep only the fluents that start before the window and ends at the end of the previous window or
          // after the start of the current window
          //keep only the part before the WM
          case Predicate.holdsFor =>

            var break = false
            var newInterval = IntervalFactory()
            val it = interval.rangeIterator()
            while (it.moveToNext() && !break) {
              if(it.first() < windowStart && it.last() >= windowStart - 1) {
                newInterval = IntervalFactory(it.first(), windowStart - 1)
                break = true
              }
            }

            if(newInterval.isEmpty) None
            else Some(entity, (newInterval, false))

          //find the first initiation point before WM, if there are no termination points before WM and after that
          case Predicate.initiatedAt =>

            //keep the last termination point before WM or -1 if there are none
            val terminationEntity = Entity(terminatedAt, entity.fluent)
            val lastTermPointBeforeWM = occurMap.get(terminationEntity) match {
              case Some((termInterval, termCalc)) =>

                var lastTermPoint = -1L
                var break = false
                val termIt = termInterval.rangeIterator()
                while(termIt.moveToNext() && !break) {
                  if( termIt.first() < windowStart ){
                    lastTermPoint = termIt.first()
                  }
                  else break = true
                }

                lastTermPoint

              case None => -1L
            }

            //keep the first initiation point before WM and after the last termination before WM or -1 if there are none
            val firstInitPointBeforeWM = {

              var firstInitPoint = -1L
              var break = false
              val initIt = interval.rangeIterator()
              while(initIt.moveToNext() && !break) {
                if(initIt.first() < windowStart && initIt.first() > lastTermPointBeforeWM){
                  firstInitPoint = initIt.first()
                  break = true
                }
                else if(initIt.first() >= windowStart) break = true
              }

              firstInitPoint
            }

            if(firstInitPointBeforeWM > lastTermPointBeforeWM)
              Some(entity, (IntervalFactory(firstInitPointBeforeWM), false))
            else None
          case _ => None
        }
      })
    }
  }

  /**
   * Executes window operation for input data
   * @param map input data
   * @param windowInterval window interval
   * @param domain theory
   * @return the input data from the specific WM
   */
  private def windowInputEntities(map: Map[Vector[String], Occurrences], windowInterval: Interval)
                                 (implicit domain: Theory) = {

    map.mapValues { occur =>

      val occurMap = occur.map
      Occurrences(occurMap.flatMap { case (entity, (interval, calc)) =>
        val newInterval = interval.intersect_all(windowInterval)
        if (newInterval.isEmpty) None
        else Some(entity, (newInterval, false))
      })
    }
  }

  /**
   * Filters only the output entities from previous state
   * @param map previous state
   * @param domain theory
   * @return output entities
   */
  private def filterOutputEntities(map: Map[Vector[String], Occurrences])(implicit domain: Theory) = {

    map.mapValues{ occur =>

      val occurMap = occur.map
      Occurrences(occurMap.filter{ case (entity, value) =>
          !domain.inputSchema.keySet.contains(entity.fluent.symbol)
      })
    }
  }

  /**
   * Merges the input entites of the input data with the input entites of the previous state
   * @param oldMap old state map
   * @param newMap new values map
   * @param domain theory
   * @return the merged map
   */
  private def createProcessedIEs(oldMap: Map[Vector[String], Occurrences], newMap: Map[Vector[String], Occurrences])
                                  (implicit domain: Theory) = {

    //change input entities to processedIEs
    val newMapProcessedIEs = newMap.mapValues{ newOccur =>
      Occurrences(newOccur.map.flatMap{ case (entity, value) =>
        entity.predicate match {
          case Predicate.holdsFor => Some(Entity(holdsForProcessedIE, entity.fluent), value)
          case _ => None
        }
      })
    }

    newMapProcessedIEs ++ oldMap.map{ case (id, oldOccur) =>

      //filter old state and keep processedIEs only
      val oldOccurMap = oldOccur.map.filter { case (entity, interval) =>
        entity.predicate == holdsForProcessedIE
      }

      newMapProcessedIEs.get(id) match {
        case Some(newOccur) =>
          val newOccurMap = newOccur.map

          (id, Occurrences(newOccurMap ++ oldOccurMap.map{ case (entity, (oldInterval, oldCalc)) =>

              newOccurMap.get(entity) match {
                case Some((newInterval, newCalc)) => (entity, (oldInterval.union_all(newInterval), false))
                case None => (entity, (oldInterval, false))
              }
          }))
        case None => (id, Occurrences(oldOccurMap))
      }
    }
  }

  /**
   * Converts input entities to processedIEs
   * @param map new values map
   * @param domain theory
   * @return the processedIEs map
   */
  private def createProcessedIEs(map: Map[Vector[String], Occurrences])(implicit domain: Theory) = {

    map.mapValues{ occur =>

      val occurMap = occur.map
      Occurrences(occurMap.flatMap{ case (entity, value) =>
        entity.predicate match {
          case Predicate.holdsFor => Some(Entity(holdsForProcessedIE, entity.fluent), value)
          case _ => None
        }
      })
    }
  }

  /**
   * Merges the input data sequence of maps in one map
   * @param newValues the input sequence
   * @return the output map
   */
  private def inputDataConcat(newValues: Seq[(Vector[String], Map[Vector[String], Occurrences], Int)]) = {

    var inputId = Vector[String]()
    var inputValues = Map[Vector[String], Occurrences]()
    var inputWindow = 0

    //concatenate new values in one entry
    newValues.foreach{ case (newId, newMap, newWindow) =>

      inputId = newId
      inputWindow = scala.math.max(inputWindow, newWindow)
      inputValues = inputValues ++ newMap.map{ case (id, newOcc) =>
        inputValues.get(id) match {
          case Some(oldOcc) =>
            val newInMap = newOcc.map
            val oldInMap = oldOcc.map

            (id, Occurrences(oldInMap ++ newInMap.map{ case (entity, (newInterval, newCalc)) =>
              oldInMap.get(entity) match {
                case Some((oldInterval, oldCalc)) => (entity, (oldInterval.union_all(newInterval), newCalc))
                case None => (entity, (newInterval, newCalc))
              }
            }))
          case None => (id, newOcc)
        }
      }
    }

    (inputId, inputValues, inputWindow)
  }
}
