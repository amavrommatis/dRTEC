package examples.aminess

import cer.drtec.engine.{Fluent, Rule, Entity, Theory}
import cer.drtec.engine.Predicate._

/**
  * @author Alexandros Mavrommatis
  */
case class Domain(slide: Long, window: Long) extends Theory {

  override val startTime: Long = 1243814474
  override val maxNumOfIds: Int = 1
  override val inputSchema: Map[String, (Int, Int)] = Map("nearPorts" -> (1, 0), "heading2Vessels" -> (1, 0),
    "gapEnd" -> (1, 0), "coord" -> (1, 2), "velocity" -> (1, 2), "turn" -> (1, 0),
    "inArea" -> (1, 0), "gapStart" -> (1, 0), "speedChange" -> (1, 0), "stopped" -> (1, 0),
    "lowSpeedStart" -> (1, 0), "lowSpeedEnd" -> (1, 0)
  )
  override val outputEntities: Vector[Entity] = Vector(
    Entity(happensAt, Fluent("illegalShipping", Vector(), Vector())),
    Entity(happensAt, Fluent("fastApproach", Vector(), Vector())),
    Entity(holdsFor, Fluent("suspiciousDelay", Vector(), Vector("true")))
  )
  override val complexEntities: Map[Int, Vector[Entity]] = Map(
    1 -> Vector(
      Entity(happensAt, Fluent("illegalShipping", Vector(), Vector())),
      Entity(happensAt, Fluent("fastApproach", Vector(), Vector())),
      Entity(holdsFor, Fluent("suspiciousDelay", Vector(), Vector("true")))
    )
  )
  override val declarations: Map[Entity, Vector[Rule]] = Map(
    Entity(initiatedAt, Fluent("lowSpeed", Vector(), Vector("true"))) -> Vector(InitiatedAtLowSpeedTrue()),
    Entity(terminatedAt, Fluent("lowSpeed", Vector(), Vector("true"))) -> Vector(TerminatedAtLowSpeedTrue()),
    Entity(holdsFor, Fluent("lowSpeed", Vector(), Vector("true"))) -> Vector(HoldsForLowSpeedTrue()),
    Entity(start, Fluent("stopped", Vector(), Vector("true"))) -> Vector(StartStoppedTrue()),
    Entity(initiatedAt, Fluent("gap", Vector(), Vector("true"))) -> Vector(InitiatedAtGapTrue()),
    Entity(terminatedAt, Fluent("gap", Vector(), Vector("true"))) -> Vector(TerminatedAtGapTrue()),
    Entity(holdsFor, Fluent("gap", Vector(), Vector("true"))) -> Vector(HoldsForGapTrue()),
    Entity(holdsFor, Fluent("stoppedNIP", Vector(), Vector("true"))) -> Vector(HoldsForStoppedNIPTrue()),
    Entity(start, Fluent("stoppedNIP", Vector(), Vector("true"))) -> Vector(StartStoppedNIPTrue()),
    Entity(start, Fluent("gap", Vector(), Vector("true"))) -> Vector(StartGapTrue()),
    Entity(end, Fluent("gap", Vector(), Vector("true"))) -> Vector(EndGapTrue()),
    Entity(start, Fluent("lowSpeed", Vector(), Vector("true"))) -> Vector(StartLowSpeedTrue()),
    Entity(end, Fluent("lowSpeed", Vector(), Vector("true"))) -> Vector(EndLowSpeedTrue()),
    Entity(happensAt, Fluent("illegalShipping", Vector(), Vector())) -> Vector(HappensAtIllegalShipping()),
    Entity(happensAt, Fluent("fastApproach", Vector(), Vector())) -> Vector(HappensAtFastApproach()),
    Entity(holdsFor, Fluent("suspiciousDelay", Vector(), Vector("true"))) -> Vector(HoldsForSuspiciousDelayTrue())
  )
  override val simpleEntities: Vector[Entity] = Vector(
    Entity(happensAt, Fluent("illegalShipping", Vector(), Vector())),
    Entity(happensAt, Fluent("fastApproach", Vector(), Vector())),
    Entity(holdsFor, Fluent("suspiciousDelay", Vector(), Vector("true")))
  )
  override val step: Long = 1
}
