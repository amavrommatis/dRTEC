package cer.drtec.engine

/**
 * @author Alexandros Mavrommatis
 */
object Predicate extends Enumeration{

  type Predicate = Value
  val holdsFor, happensAt, holdsForProcessedIE, initiatedAt, terminatedAt, start, end = Value
}
