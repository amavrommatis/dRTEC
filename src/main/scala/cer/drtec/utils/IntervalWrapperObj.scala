package cer.drtec.utils

import cer.drtec.engine.{Interval, Theory}

/**
 * @author Alexandros Mavrommatis
 */
object IntervalWrapperObj {

  implicit class IntervalWrapper(val self: Interval) extends AnyVal {

    /**
     * Construct new Interval with intersection of values from original set and
     * parameter set.
     * <p>
     * This operation is FAST. Requires only one traversal of ranges.
     * It does not decompress RangeSet to pixels.
     * <p>
     * This operation does not modify original collection.

     * @param rs The set with which to intersect with this set.
     * @return new set that represents the intersect of original and parameter set
     */
    def intersect_all(rs: Interval) = {
      if(self.isEmpty || rs.isEmpty) new Interval(new Array[Long](0), 0)
      else self.intersect(rs)
    }

    /**
     * Create new Interval which contains union of values from
     * original set and parameter
     * <p>
     * This operation is FAST. Requires only one traversal of ranges.
     * It does not decompress RangeSet to pixels.
     * <p>
     * This operation does not modify original collection.
     *
     *
     * @param rs Interval to make union with
     * @return Interval contains union of original set and parameter set
     */
    def union_all(rs: Interval) = self.union(rs)

    /**
     *
     * Construct new Interval with values which are in original set, but not in parameter.
     * <p>
     * [1-5].substract[4-6] == [1-3]
     * <p>
     * This operation is FAST. Requires only one traversal of ranges.
     * It does not decompress RangeSet to pixels.

     * <p>
     * This operation does not modify original collection.

     * <p> substract this set from original
     * @return result of substraction
     */
    def relative_complement_all(rs: Interval) = self.substract(rs)

    /**
     * complement is defined in terms of relative complement
     * given the list of lists of intervals List
     * we compute the list of intervals NewI
     * such that  union_all([NewI|List], WM)  and  intersect_all([NewI|List], [])
     * @param window the current window
     * @return result of complement
     */
    def complement_all(window: Interval)(implicit domain: Theory) = window.substract(self)

  }

}