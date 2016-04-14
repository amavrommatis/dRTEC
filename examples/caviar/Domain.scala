package examples.caviar

import cer.drtec.engine._
import cer.drtec.engine.Predicate._

/**
 * @author Alexandros Mavrommatis
 */
case class Domain(slide: Long, window: Long) extends Theory{

  override val step: Long = 40
  override val startTime: Long = 680
  override val maxNumOfIds = 2

  //fluent/event -> num of ids, args, values
  override val inputSchema = Map("walking" -> (1, 0), "running" -> (1, 0), "abrupt" -> (1, 0),
    "active" -> (1, 0), "inactive" -> (1, 0), "coord" -> (1, 2), "disappear" -> (1, 0), "appear" -> (1, 0))

  override val simpleEntities: Vector[Entity] = Vector(
    Entity(holdsFor, Fluent("activeOrInactivePerson", Vector(), Vector("true")))
  )

  override val complexEntities: Map[Int, Vector[Entity]] =
    Map(2 -> Vector(
      Entity(holdsFor, Fluent("leaving_object", Vector(), Vector("true"))),
      Entity(holdsFor, Fluent("meeting", Vector(), Vector("true"))),
      Entity(holdsFor, Fluent("moving", Vector(), Vector("true"))),
      Entity(holdsFor, Fluent("fighting", Vector(), Vector("true")))
    ))

  override val outputEntities: Vector[Entity] = Vector(
    Entity(holdsFor, Fluent("leaving_object", Vector(), Vector("true"))),
    Entity(holdsFor, Fluent("meeting", Vector(), Vector("true"))),
    Entity(holdsFor, Fluent("moving", Vector(), Vector("true"))),
    Entity(holdsFor, Fluent("fighting", Vector(), Vector("true")))
  )

  override val declarations: Map[Entity, Vector[Rule]] = Map(
    Entity(start, Fluent("walking", Vector(), Vector("true"))) -> Vector(StartWalkingTrue()),
    Entity(start, Fluent("running", Vector(), Vector("true"))) -> Vector(StartRunningTrue()),
    Entity(start, Fluent("abrupt", Vector(), Vector("true"))) -> Vector(StartAbruptTrue()),
    Entity(start, Fluent("active", Vector(), Vector("true"))) -> Vector(StartActiveTrue()),
    Entity(initiatedAt, Fluent("person", Vector(), Vector("true"))) -> Vector(InitiatedAtPersonTrue()),
    Entity(terminatedAt, Fluent("person", Vector(), Vector("true"))) -> Vector(InitiatedAtPersonFalse()),
    Entity(holdsFor, Fluent("person", Vector(), Vector("true"))) -> Vector(HoldsForPersonTrue()),
    Entity(holdsFor, Fluent("activeOrInactivePerson", Vector(), Vector("true"))) ->
      Vector(HoldsForActiveOrInactivePersonTrue()),
    Entity(holdsFor, Fluent("distance", Vector("24"), Vector("true"))) -> Vector(Distance()),
    Entity(holdsFor, Fluent("close", Vector("24"), Vector("true"))) -> Vector(HoldsForClose24True()),
    Entity(holdsFor, Fluent("distance", Vector("25"), Vector("true"))) -> Vector(Distance()),
    Entity(holdsFor, Fluent("close", Vector("25"), Vector("true"))) -> Vector(HoldsForClose25True()),
    Entity(holdsFor, Fluent("distance", Vector("30"), Vector("true"))) -> Vector(Distance()),
    Entity(holdsFor, Fluent("close", Vector("30"), Vector("true"))) -> Vector(HoldsForClose30True()),
    Entity(holdsFor, Fluent("distance", Vector("34"), Vector("true"))) -> Vector(Distance()),
    Entity(holdsFor, Fluent("close", Vector("34"), Vector("true"))) -> Vector(HoldsForClose34True()),
    Entity(holdsFor, Fluent("closeSymmetric", Vector("30"), Vector("true"))) -> Vector(HoldsForCloseSymmetricTrue()),
    Entity(initiatedAt, Fluent("leaving_object", Vector(), Vector("true"))) -> Vector(InitiatedAtLeaving_objectTrue()),
    Entity(terminatedAt, Fluent("leaving_object", Vector(), Vector("true"))) -> Vector(InitiatedAtLeaving_objectFalse()),
    Entity(start, Fluent("greeting1", Vector(), Vector("true"))) -> Vector(StartGreeting1True()),
    Entity(start, Fluent("greeting2", Vector(), Vector("true"))) -> Vector(StartGreeting2True()),
    Entity(start, Fluent("close", Vector("34"), Vector("false"))) -> Vector(StartCloseFalse()),
    Entity(initiatedAt, Fluent("meeting", Vector(), Vector("true"))) -> Vector(InitiatedAtMeetingTrue()),
    Entity(terminatedAt, Fluent("meeting", Vector(), Vector("true"))) -> Vector(InitiatedAtMeetingFalse()),
    Entity(holdsFor, Fluent("greeting1", Vector(), Vector("true"))) -> Vector(HoldsForGreeting1True()),
    Entity(holdsFor, Fluent("greeting2", Vector(), Vector("true"))) -> Vector(HoldsForGreeting2True()),
    Entity(holdsFor, Fluent("close", Vector("34"), Vector("false"))) -> Vector(HoldsForCloseFalse()),
    Entity(holdsFor, Fluent("leaving_object", Vector(), Vector("true"))) -> Vector(HoldsForLeaving_objectTrue()),
    Entity(holdsFor, Fluent("meeting", Vector(), Vector("true"))) -> Vector(HoldsForMeetingTrue()),
    Entity(holdsFor, Fluent("moving", Vector(), Vector("true"))) -> Vector(HoldsForMovingTrue()),
    Entity(holdsFor, Fluent("fighting", Vector(), Vector("true"))) -> Vector(HoldsForFightingTrue())
  )

}
