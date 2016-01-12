package cer.drtec.engine

/**
 * @author Alexandros Mavrommatis
 */
trait Theory extends Serializable{

  val startTime: Long //execution start time
  val window: Long  //window duration
  val slide: Long //slide duration
  val step: Long  //application time step
  val maxNumOfIds : Int //maximum number ids in output fluents/events

  val inputSchema: Map[String, (Int, Int)] //input fluent/event -> num of ids, args, values
  val simpleEntities: Vector[Entity] //list with entities for simple recognition
  val complexEntities: Map[Int, Vector[Entity]] //the complex entities for each specific number of ids
  val outputEntities: Vector[Entity] //list with entities for output
  val declarations: Map[Entity, Vector[Rule]] //for each entity a list of rules
}
