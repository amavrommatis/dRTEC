package cer.drtec

import healpix.core.base.set.LongRangeSet

/**
  * @author Alexandros Mavrommatis
  */
package object engine {

  type Interval = LongRangeSet
  final val Infinity = Long.MaxValue - 1
}
