package cer.drtec.utils

import cer.drtec.engine.Interval
import healpix.core.base.set.LongRangeSetBuilder

/**
 * @author Alexandros Mavrommatis
 */
object IntervalFactory {

  /**
   * Creates an empty Interval
   * @return Interval instance
   */
  def apply() = new LongRangeSetBuilder().build()

  /**
   * Creates an Interval
   * @param points array of points
   * @return Interval instance
   */
  def apply(points: Array[Long]) : Interval = new Interval(points, points.length)

  /**
   * Creates an Interval
   * @param first initiation point
   * @param last termination point
   * @return Interval instance
   */
  def apply(first: Long, last: Long) = {
    val builder = new LongRangeSetBuilder()
    builder.appendRange(first, last)
    builder.build()
  }

  /**
   * Creates an Interval (with one timepoint)
   * @param timepoint initiation point
   * @return Interval instance
   */
  def apply(timepoint: Long) = {
    val builder = new LongRangeSetBuilder()
    builder.append(timepoint)
    builder.build()
  }
}
