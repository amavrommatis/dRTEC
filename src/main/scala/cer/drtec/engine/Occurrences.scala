package cer.drtec.engine

/**
 * Contains a map with key, the entity of an occurrence,
 * and value the interval of the occurrence as well as a boolean that indicates if this interval has been calculated
 * in the current window
 * @author Alexandros Mavrommatis
 */
case class Occurrences(map: Map[Entity, (Interval, Boolean)])
