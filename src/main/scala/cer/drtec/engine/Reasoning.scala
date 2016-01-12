package cer.drtec.engine

import cer.drtec.utils.IntervalFactory
import healpix.core.base.set.LongRangeSetBuilder
import cer.drtec.utils.IntervalWrapperObj._

/**
 * @author Alexandros Mavrommatis
 *
 * DRTEC engine rules
 */
object Reasoning {

  /**
   * Adds an Interval into an existing or not fluent of the map
   * @param map the input map
   * @param entity the specific fluent
   * @param newInterval the new interval
   * @return the updated map
   */
  def amalgamate(map: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity, newInterval: Interval,
                  simpleFluent: Boolean)(implicit domain: Theory, windowInterval: Interval) = {

    //if it is a statically determined fluent intersect the interval with the current window
    var modifiedNewInterval: Interval = null
    if(simpleFluent) modifiedNewInterval = newInterval
    else modifiedNewInterval = newInterval.intersect_all(windowInterval)

    map + (map.get(id) match {
      //if the id exists
      case Some(occ) =>
        val occMap = occ.map
        (id, Occurrences(occMap + (occMap.get(entity) match {
          //if there is a previous interval for this entity
          //boolean is turned to true because this calculates takes place in the current window
          case Some((oldInterval, oldCalc)) => (entity, (oldInterval.union_all(modifiedNewInterval), true))
          case None => (entity, (modifiedNewInterval, true))
        })))
      case None =>
        (id, Occurrences(Map(entity -> (modifiedNewInterval, true))))
    })
  }


  /**
   * Locates the Interval for a specific Fluent or executes its rule if the Interval does not exist
   * @param occ the input map
   * @param id the specific id to retrieve the data from
   * @param entity the requested fluent
   * @return the Interval or empty Interval in case there is no Interval found
   */
  def getInterval(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                 (implicit domain: Theory, windowInterval: Interval) = {

    occ.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map.get(entity) match {
      //if the interval exists
      case Some((interval, calc)) =>
        //if there has not been a calculation for the specific fluent in the current window so far
        if(!calc){
          val newOcc = domain.declarations.get(entity) match {
            //if the fluent has a rule
            case Some(rules) =>
              var tempOcc = occ
              rules.foreach(rule => tempOcc = rule.recognize(tempOcc, id, entity))
              tempOcc
            //if the fluent doesn't have a rule (input entity)
            case None => occ
          }
          (newOcc, newOcc.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map
            .getOrElse(entity, (IntervalFactory(), true))._1)
        }
        else (occ, interval)
      case None =>
        val newOcc = domain.declarations.get(entity) match {
          //if the fluent has a rule
          case Some(rules) =>
            var tempOcc = occ
            rules.foreach(rule => tempOcc = rule.recognize(tempOcc, id, entity))
            tempOcc
          //if the fluent doesn't have a rule (input entity)
          case None => occ
        }
        (newOcc, newOcc.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map
          .getOrElse(entity, (IntervalFactory(), true))._1)
    }
  }

  /**
   * Finds the instances of a fluent that starts having a value
   * @param set the Interval of the fluent
   * @param domain Theory
   * @param windowInterval the current window
   * @return a LongRangSet that contains the timepoints
   */
  def start(set: Interval)(implicit domain: Theory, windowInterval: Interval) = {

    val builder = new LongRangeSetBuilder()
    val it = set.rangeIterator()
    while(it.moveToNext()) if(windowInterval.contains(it.first())) builder.append(it.first())
    builder.build()
  }

  /**
   * Finds the instances of a fluent that ends having a value
   * @param set the Interval of the fluent
   * @param domain Theory
   * @param windowInterval the current window
   * @return a LongRangSet that contains the timepoints
   */
  def end(set: Interval)(implicit domain: Theory, windowInterval: Interval) = {

    val builder = new LongRangeSetBuilder()
    val it = set.rangeIterator()
    while(it.moveToNext()) if(windowInterval.contains(it.last() + 1)) builder.append(it.last() + 1)
    builder.build()
  }

  /**
   * Creates a list of intervals from timepoints
   * @param initRanges initiation points
   * @param termRanges termination points
   * @return an Interval containing the list
   */
  def makeIntervalsFromPoints(initRanges: Interval, termRanges: Interval) = {

    val it1 = initRanges.rangeIterator()
    val it2 = termRanges.rangeIterator()
    var timepoints = List[(Long, Int)]()
    val builder = new LongRangeSetBuilder()

    //pass timepoints in the list
    //id=1 for initiation points
    //id=2 for termination points
    while(it1.moveToNext()) timepoints = (it1.first(), 1) :: timepoints
    while(it2.moveToNext()) timepoints = (it2.first(), 2) :: timepoints

    //sort timepoints and find the maximal intervals
    var tempInit: Long = -1
    timepoints.sortBy(_._1).foreach{case (timepoint, ttype) =>
      if(ttype == 1 && tempInit == -1) tempInit = timepoint
      if(ttype == 2 && tempInit != -1){
        builder.appendRange(tempInit+1, timepoint)
        tempInit = -1
      }
    }
    if(tempInit != -1){
      builder.appendRange(tempInit+1, Infinity)
    }

    builder.build()
  }
}
