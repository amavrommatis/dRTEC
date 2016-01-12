package cer.drtec.engine

/**
 * @author Alexandros Mavrommatis
 */
trait Rule {

  /**
   * Function that implements a rule
   * @param occ input entities
   * @param id rule id
   * @param entity entity for recognition
   * @param domain Theory
   * @param windowInterval window
   * @return input entities merged with the recognized entity for this rule
   */
  def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
               (implicit domain: Theory, windowInterval: Interval): Map[Vector[String], Occurrences]
}
