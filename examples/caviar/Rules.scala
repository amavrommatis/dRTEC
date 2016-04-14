package examples.caviar

import cer.drtec.engine._
import cer.drtec.utils.IntervalFactory
import cer.drtec.utils.IntervalWrapperObj._
import cer.drtec.engine.Predicate._

/**
* @author Alexandros Mavrommatis
*
* Caviar rules
*/

//==============================================ONE ID RULES==========================================================

/**
 * {{{initiatedAt(person(Id)=true, T) :-
	happensAt(start(walking(Id)=true), T),
	\+ happensAt(disappear(Id), T).

  initiatedAt(person(Id)=true, T) :-
	happensAt(start(running(Id)=true), T),
	\+ happensAt(disappear(Id), T).

  initiatedAt(person(Id)=true, T) :-
	happensAt(start(active(Id)=true), T),
	\+ happensAt(disappear(Id), T).

  initiatedAt(person(Id)=true, T) :-
	happensAt(start(abrupt(Id)=true), T),
	\+ happensAt(disappear(Id), T).}}}
 */
case class InitiatedAtPersonTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, startWalkingI) = Reasoning
      .getInterval(occ, id, Entity(start, Fluent("walking", Vector(), Vector("true"))))
    val (occ2, startActiveI) = Reasoning
      .getInterval(occ1, id, Entity(start, Fluent("active", Vector(), Vector("true"))))
    val (occ3, startRunningI) = Reasoning
      .getInterval(occ2, id, Entity(start, Fluent("running", Vector(), Vector("true"))))
    val (occ4, startAbruptI) = Reasoning
      .getInterval(occ3, id, Entity(start, Fluent("abrupt", Vector(), Vector("true"))))
    val (occ5, disappearI) = Reasoning
      .getInterval(occ4, id, Entity(happensAt, Fluent("disappear", Vector(), Vector())))

    val initiatedAtPersonTrueI = startWalkingI.union_all(startActiveI).union_all(startRunningI)
      .union_all(startAbruptI).relative_complement_all(disappearI)
    Reasoning.amalgamate(occ5, id, entity, initiatedAtPersonTrueI, simpleFluent = false)
  }
}

/**
 * {{{initiatedAt(person(Id)=false, T) :-
	happensAt(disappear(Id), T).}}}
 */
case class InitiatedAtPersonFalse() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, initiatedAtPersonFalseI) = Reasoning
      .getInterval(occ, id, Entity(happensAt, Fluent("disappear", Vector(), Vector())))
    Reasoning.amalgamate(occ1, id, entity, initiatedAtPersonFalseI, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(person(p)=true, I)}}}
 */
case class HoldsForPersonTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, initiatedAtPersonTrueI) = Reasoning
      .getInterval(occ, id, Entity(initiatedAt, Fluent("person", Vector(), Vector("true"))))
    val (occ2, initiatedAtPersonFalseI) = Reasoning
      .getInterval(occ1, id, Entity(terminatedAt, Fluent("person", Vector(), Vector("true"))))
    val personI = Reasoning.makeIntervalsFromPoints(initiatedAtPersonTrueI, initiatedAtPersonFalseI)
    Reasoning.amalgamate(occ2, id, entity, personI, simpleFluent = true)
  }
}

/**
 * {{{holdsFor(activeOrInactivePerson(P)=true, I) :-
     holdsFor(active(P)=true, IA),
     holdsFor(inactive(P)=true, In),
     holdsFor(person(P)=true, IP),
     intersect_all([In,IP], InP),
     union_all([IA,InP], I).}}}
 */
case class HoldsForActiveOrInactivePersonTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, ia) = Reasoning.
      getInterval(occ, id, Entity(holdsFor, Fluent("active", Vector(), Vector("true"))))
    val (occ2, in) = Reasoning
      .getInterval(occ1, id, Entity(holdsFor, Fluent("inactive", Vector(), Vector("true"))))
    val (occ3, ip) = Reasoning
      .getInterval(occ2, id, Entity(holdsFor, Fluent("person", Vector(), Vector("true"))))
    val InP = in.intersect_all(ip)
    val I = ia.union_all(InP)
    Reasoning.amalgamate(occ3, id, entity, I, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(walking(id)=true), T)}}}
 */
case class StartWalkingTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, walkingTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("walking", Vector(), Vector("true"))))
    val startWalkingTrueI = Reasoning.start(walkingTrueI)
    Reasoning.amalgamate(occ1, id, entity, startWalkingTrueI, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(running(id)=true), T)}}}
 */
case class StartRunningTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, runningTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("running", Vector(), Vector("true"))))
    val startRunningTrueI = Reasoning.start(runningTrueI)
    Reasoning.amalgamate(occ1, id, entity, startRunningTrueI, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(abrupt(id)=true), T)}}}
 */
case class StartAbruptTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, abruptTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("abrupt", Vector(), Vector("true"))))
    val startAbruptTrueI = Reasoning.start(abruptTrueI)
    Reasoning.amalgamate(occ1, id, entity, startAbruptTrueI, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(active(id)=true), T)}}}
 */
case class StartActiveTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, activeTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("active", Vector(), Vector("true"))))
    val startActiveTrueI = Reasoning.start(activeTrueI)
    Reasoning.amalgamate(occ1, id, entity, startActiveTrueI, simpleFluent = false)
  }
}

//==============================================TWO IDs RULES=========================================================

/**
 *{{{holdsFor(close(Id1,Id2,Threshold)=false, I) :-
     holdsFor(close(Id1,Id2,Threshold)=true, I1),
     complement_all([I1], I).}}}
 */
case class HoldsForCloseFalse() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val threshold = entity.fluent.args.head
    val (occ1, i1) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("close", Vector(threshold), Vector("true"))))
    val i = i1.complement_all(windowInterval)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(close(Id1,Id2,24)=true, I) :-
	holdsFor(distance(Id1,Id2,24)=true, I).}}}
 */
case class HoldsForClose24True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("distance", Vector("24"), Vector("true"))))
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(close(Id1,Id2,25)=true, I) :-
	holdsFor(close(Id1,Id2,24)=true, I1),
	holdsFor(distance(Id1,Id2,25)=true, I2),
	union_all([I1,I2], I).}}}
 */
case class HoldsForClose25True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i1) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("close", Vector("24"), Vector("true"))))
    val (occ2, i2) = Reasoning
      .getInterval(occ1, id, Entity(holdsFor, Fluent("distance", Vector("25"), Vector("true"))))
    val i = i1.union_all(i2)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(close(Id1,Id2,30)=true, I) :-
	holdsFor(close(Id1,Id2,25)=true, I1),
	holdsFor(distance(Id1,Id2,30)=true, I2),
	union_all([I1,I2], I).}}}
 */
case class HoldsForClose30True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i1) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("close", Vector("25"), Vector("true"))))
    val (occ2, i2) = Reasoning
      .getInterval(occ1, id, Entity(holdsFor, Fluent("distance", Vector("30"), Vector("true"))))
    val i = i1.union_all(i2)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(close(Id1,Id2,34)=true, I) :-
	holdsFor(close(Id1,Id2,30)=true, I1),
	holdsFor(distance(Id1,Id2,34)=true, I2),
	union_all([I1,I2], I).}}}
 */
case class HoldsForClose34True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i1) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("close", Vector("30"), Vector("true"))))
    val (occ2, i2) = Reasoning
      .getInterval(occ1, id, Entity(holdsFor, Fluent("distance", Vector("34"), Vector("true"))))
    val i = i1.union_all(i2)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(closeSymmetric(Id1,Id2,Threshold)=true, I) :-
	holdsFor(close(Id1,Id2,Threshold)=true, I1),
	holdsFor(close(Id2,Id1,Threshold)=true, I2),
	union_all([I1,I2], I).}}}
 */
case class HoldsForCloseSymmetricTrue() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val threshold = entity.fluent.args.head
    val (occ1, i1) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("close", Vector(threshold), Vector("true"))))
    val (occ2, i2) = Reasoning
      .getInterval(occ1, Vector(id(1), id.head), Entity(holdsFor, Fluent("close", Vector(threshold), Vector("true"))))
    val i = i1.union_all(i2)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{initiatedAt(leaving_object(Person,Object)=true, T) :-
	happensAt(appear(Object), T),
	holdsAt(inactive(Object)=true, T),
	holdsAt(person(Person)=true, T),
	% leaving_object is not symmetric in the pair of ids
	% and thus we need closeSymmetric here as opposed to close
	holdsAt(closeSymmetric(Person,Object,30)=true, T).}}}
 */
case class InitiatedAtLeaving_objectTrue() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, appearI) = Reasoning
      .getInterval(occ, Vector(id(1)), Entity(happensAt, Fluent("appear", Vector(), Vector())))
    val (occ2, inactiveI) = Reasoning
      .getInterval(occ1, Vector(id(1)), Entity(holdsFor, Fluent("inactive", Vector(), Vector("true"))))
    val (occ3, personI) = Reasoning.
      getInterval(occ2, Vector(id.head), Entity(holdsFor, Fluent("person", Vector(), Vector("true"))))
    val (occ4, closeSymmetricI) = Reasoning
      .getInterval(occ3, id, Entity(holdsFor, Fluent("closeSymmetric", Vector("30"), Vector("true"))))
    val I = appearI.intersect_all(inactiveI).intersect_all(personI).intersect_all(closeSymmetricI)
    Reasoning.amalgamate(occ4, id, entity, I, simpleFluent = false)
  }
}

/**
 * {{{initiatedAt(leaving_object(_Person,Object)=false, T) :-
	happensAt(disappear(Object), T).}}}
 */
case class InitiatedAtLeaving_objectFalse() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i) = Reasoning
      .getInterval(occ, Vector(id(1)), Entity(happensAt, Fluent("disappear", Vector(), Vector())))
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(leaving_object(id1,id2)=true, I)}}}
 */
case class HoldsForLeaving_objectTrue() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, initiatedAtLeaving_objectTrueI) = Reasoning
      .getInterval(occ, id, Entity(initiatedAt, Fluent("leaving_object", Vector(), Vector("true"))))
    val (occ2, terminatedAtLeaving_objectTrueI) = Reasoning
      .getInterval(occ1, id, Entity(terminatedAt, Fluent("leaving_object", Vector(), Vector("true"))))
    val i = Reasoning.makeIntervalsFromPoints(initiatedAtLeaving_objectTrueI, terminatedAtLeaving_objectTrueI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = true)
  }
}

/**
 * {{{initiatedAt(meeting(P1,P2)=true, T) :-
	happensAt(start(greeting1(P1,P2)=true), T),
	\+ happensAt(disappear(P1), T),
	\+ happensAt(disappear(P2), T).

  initiatedAt(meeting(P1,P2)=true, T) :-
	happensAt(start(greeting2(P1,P2)=true), T),
	\+ happensAt(disappear(P1), T),
	\+ happensAt(disappear(P2), T).}}}
 */
case class InitiatedAtMeetingTrue() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, startGreeting1I) = Reasoning
      .getInterval(occ, id, Entity(start, Fluent("greeting1", Vector(), Vector("true"))))
    val (occ2, startGreeting2I) = Reasoning
      .getInterval(occ1, id, Entity(start, Fluent("greeting2", Vector(), Vector("true"))))
    val (occ3, disappear1I) = Reasoning
      .getInterval(occ2, Vector(id.head), Entity(happensAt, Fluent("disappear", Vector(), Vector())))
    val (occ4, disappear2I) = Reasoning
      .getInterval(occ3, Vector(id(1)), Entity(happensAt, Fluent("disappear", Vector(), Vector())))
    val i = startGreeting1I.union_all(startGreeting2I).relative_complement_all(disappear1I)
      .relative_complement_all(disappear2I)
    Reasoning.amalgamate(occ4, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{% run
initiatedAt(meeting(P1,_P2)=false, T) :-
	happensAt(start(running(P1)=true), T).

initiatedAt(meeting(_P1,P2)=false, T) :-
	happensAt(start(running(P2)=true), T).

% move abruptly
initiatedAt(meeting(P1,_P2)=false, T) :-
	happensAt(start(abrupt(P1)=true), T).

initiatedAt(meeting(_P1,P2)=false, T) :-
	happensAt(start(abrupt(P2)=true), T).

% move away from each other
initiatedAt(meeting(P1,P2)=false, T) :-
	happensAt(start(close(P1,P2,34)=false), T).}}}
 */
case class InitiatedAtMeetingFalse() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, startRunning1I) = Reasoning
      .getInterval(occ, Vector(id.head), Entity(start, Fluent("running", Vector(), Vector("true"))))
    val (occ2, startRunning2I) = Reasoning
      .getInterval(occ1, Vector(id(1)), Entity(start, Fluent("running", Vector(), Vector("true"))))
    val (occ3, startAbrupt1I) = Reasoning
      .getInterval(occ2, Vector(id.head), Entity(start, Fluent("abrupt", Vector(), Vector("true"))))
    val (occ4, startAbrupt2I) = Reasoning
      .getInterval(occ3, Vector(id(1)), Entity(start, Fluent("abrupt", Vector(), Vector("true"))))
    val (occ5, startClose34FalseI) = Reasoning
      .getInterval(occ4, id, Entity(start, Fluent("close", Vector("34"), Vector("false"))))
    val i = startRunning1I.union_all(startRunning2I).union_all(startAbrupt1I).union_all(startAbrupt2I)
      .union_all(startClose34FalseI)
    Reasoning.amalgamate(occ5, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(meeting(id1,id2)=true, I)}}}
 */
case class HoldsForMeetingTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, initiatedAtMeetingTrue) = Reasoning
      .getInterval(occ, id, Entity(initiatedAt, Fluent("meeting", Vector(), Vector("true"))))
    val (occ2, terminatedAtMeetingTrueI) = Reasoning.
      getInterval(occ1, id, Entity(terminatedAt, Fluent("meeting", Vector(), Vector("true"))))
    val i = Reasoning.makeIntervalsFromPoints(initiatedAtMeetingTrue, terminatedAtMeetingTrueI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = true)
  }
}

/**
 * {{{holdsFor(greeting1(P1,P2)=true, I) :-
	holdsFor(activeOrInactivePerson(P1)=true, IAI),
	holdsFor(person(P2)=true, IP2),
	holdsFor(close(P1,P2,25)=true, IC),
	intersect_all([IAI, IC, IP2], ITemp),
	holdsFor(running(P2)=true, IR2),
	holdsFor(abrupt(P2)=true, IA2),
	relative_complement_all(ITemp, [IR2,IA2], I).}}}
 */
case class HoldsForGreeting1True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, iai) = Reasoning
      .getInterval(occ, Vector(id.head), Entity(holdsFor, Fluent("activeOrInactivePerson", Vector(), Vector("true"))))
    val (occ2, ip2) = Reasoning
      .getInterval(occ1, Vector(id(1)), Entity(holdsFor, Fluent("person", Vector(), Vector("true"))))
    val (occ3, ic) = Reasoning
      .getInterval(occ2, id, Entity(holdsFor, Fluent("close", Vector("25"), Vector("true"))))
    val itemp = iai.intersect_all(ic).intersect_all(ip2)
    val (occ4, ir2) = Reasoning
      .getInterval(occ3, Vector(id(1)), Entity(holdsFor, Fluent("running", Vector(), Vector("true"))))
    val (occ5, ia2) = Reasoning
      .getInterval(occ4, Vector(id(1)), Entity(holdsFor, Fluent("abrupt", Vector(), Vector("true"))))
    val i = itemp.relative_complement_all(ir2.union_all(ia2))
    Reasoning.amalgamate(occ5, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(greeting2(P1,P2)=true, I) :-
	% if P1 were active or inactive
	% then meeting would have been initiated by greeting1
	holdsFor(walking(P1)=true, IW1),
	holdsFor(activeOrInactivePerson(P2)=true, IAI2),
	holdsFor(close(P1,P2,25)=true, IC),
	intersect_all([IW1, IAI2, IC], I).}}}
 */
case class HoldsForGreeting2True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, iw1) = Reasoning
      .getInterval(occ, Vector(id.head), Entity(holdsFor, Fluent("walking", Vector(), Vector("true"))))
    val (occ2, iai2) = Reasoning
      .getInterval(occ1, Vector(id(1)), Entity(holdsFor, Fluent("activeOrInactivePerson", Vector(), Vector("true"))))
    val (occ3, ic) = Reasoning
      .getInterval(occ2, id, Entity(holdsFor, Fluent("close", Vector("25"), Vector("true"))))
    val i = iw1.intersect_all(iai2).intersect_all(ic)
    Reasoning.amalgamate(occ3, id, entity, i, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(greeting1(id1,id2)=true), T)}}}
 */
case class StartGreeting1True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, greeting1TrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("greeting1", Vector(), Vector("true"))))
    val startGreeting1TrueI = Reasoning.start(greeting1TrueI)
    Reasoning.amalgamate(occ1, id, entity, startGreeting1TrueI, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(greeting2(id1,id2)=true), T)}}}
 */
case class StartGreeting2True() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, greeting2TrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("greeting2", Vector(), Vector("true"))))
    val startGreeting2TrueI = Reasoning.start(greeting2TrueI)
    Reasoning.amalgamate(occ1, id, entity, startGreeting2TrueI, simpleFluent = false)
  }
}

/**
 * {{{happensAt(start(close(id1,id2,34)=false), T)}}}
 */
case class StartCloseFalse() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val threshold = entity.fluent.args.head
    val (occ1, closeFalseI) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("close", Vector(threshold), Vector("false"))))
    val startCloseFalseI = Reasoning.start(closeFalseI)
    Reasoning.amalgamate(occ1, id, entity, startCloseFalseI, simpleFluent = false)
  }
}

/**
 *
 * {{{holdsFor(moving(P1,P2)=true, MI) :-
     holdsFor(walking(P1)=true, WP1),
     holdsFor(walking(P2)=true, WP2),
     intersect_all([WP1,WP2], WI),
     holdsFor(close(P1,P2,34)=true, CI),
     intersect_all([WI,CI], MI).}}}
 */
case class HoldsForMovingTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, wp1) = Reasoning
      .getInterval(occ, Vector(id.head), Entity(holdsFor, Fluent("walking", Vector(), Vector("true"))))
    val (occ2, wp2) = Reasoning
      .getInterval(occ1, Vector(id(1)), Entity(holdsFor, Fluent("walking", Vector(), Vector("true"))))
    val wi = wp1.intersect_all(wp2)
    val (occ3, ci) = Reasoning
      .getInterval(occ2, id, Entity(holdsFor, Fluent("close", Vector("34"), Vector("true"))))
    val mi = wi.intersect_all(ci)
    Reasoning.amalgamate(occ3, id, entity, mi, simpleFluent = false)
  }
}

/**
 * {{{holdsFor(fighting(P1,P2)=true, FightingI) :-
	holdsFor(abrupt(P1)=true, AbruptP1I),
	holdsFor(abrupt(P2)=true, AbruptP2I),
	union_all([AbruptP1I,AbruptP2I], AbruptI),
	holdsFor(close(P1,P2,24)=true, CloseI),
	intersect_all([AbruptI,CloseI], AbruptCloseI),
	holdsFor(inactive(P1)=true, InactiveP1I),
	holdsFor(inactive(P2)=true, InactiveP2I),
	union_all([InactiveP1I,InactiveP2I], InactiveI),
	relative_complement_all(AbruptCloseI, [InactiveI], FightingI).}}}
 */
case class HoldsForFightingTrue() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, abruptP1I) = Reasoning.
      getInterval(occ, Vector(id.head), Entity(holdsFor, Fluent("abrupt", Vector(), Vector("true"))))
    val (occ2, abruptP2I) = Reasoning.
      getInterval(occ1, Vector(id(1)), Entity(holdsFor, Fluent("abrupt", Vector(), Vector("true"))))
    val abruptI = abruptP1I.union_all(abruptP2I)
    val (occ3, closeI) = Reasoning.
      getInterval(occ2, id, Entity(holdsFor, Fluent("close", Vector("24"), Vector("true"))))
    val abruptCloseI = abruptI.intersect_all(closeI)
    val (occ4, inactiveP1I) = Reasoning
      .getInterval(occ3, Vector(id.head), Entity(holdsFor, Fluent("inactive", Vector(), Vector("true"))))
    val (occ5, inactiveP2I) = Reasoning
      .getInterval(occ4, Vector(id(1)), Entity(holdsFor, Fluent("inactive", Vector(), Vector("true"))))
    val inactiveI = inactiveP1I.union_all(inactiveP2I)
    val fightingI = abruptCloseI.relative_complement_all(inactiveI)
    Reasoning.amalgamate(occ5, id, entity, fightingI, simpleFluent = false)
  }
}

/**
 * <b>Application-dependent rule</b>
 * {{{h(distance(Id1,Id2,Dist)=true, T) :-
	holdsAtIE(coord(Id1,X1,Y1)=true, T),
	holdsAtIE(coord(Id2,X2,Y2)=true, T),
	XDiff is abs(X1-X2),
	YDiff is abs(Y1-Y2),
	SideA is XDiff*XDiff,
	SideB is YDiff*YDiff,
	Temp is SideA+SideB,
	D is sqrt(Temp),
	compareWithDistanceThresholds(D, Dist).


   compareWithDistanceThresholds(D, Threshold) :-
	threshold(_, Threshold),
	D=<Threshold, !.}}}
 */
case class Distance() extends Rule{

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val dist = entity.fluent.args.head
    val coord1 = occ.getOrElse(Vector(id.head), Occurrences(Map[Entity, (Interval, Boolean)]())).map
      .filter{ case (entity1, (interval, calc)) =>
      entity1.predicate == holdsFor && entity1.fluent.symbol == "coord" && entity1.fluent.values == Vector("true")
    }.mapValues(_._1).toVector

    val coord2 = occ.getOrElse(Vector(id(1)), Occurrences(Map[Entity, (Interval, Boolean)]())).map
      .filter{ case (entity2, (interval, calc)) =>
      entity2.predicate == holdsFor && entity2.fluent.symbol == "coord" && entity2.fluent.values == Vector("true")
    }.mapValues(_._1).toVector

    val t = coord1.flatMap{ case (entity1, interval1) =>
      coord2.map{ case (entity2, interval2) => ((entity1, entity2), interval1.intersect_all(interval2))}
    }.filter{ case (key, intersection) => !intersection.isEmpty}.map{ case ((entity1, entity2), intersection) =>
      val args1 = entity1.fluent.args
      val args2 = entity2.fluent.args
      val x1 = args1.head.toInt; val x2 = args2.head.toInt
      val y1 = args1(1).toInt; val y2 = args2(1).toInt
      val d = scala.math.sqrt(scala.math.pow(scala.math.abs(x1-x2), 2) + scala.math.pow(scala.math.abs(y1-y2), 2))
      (d, intersection)
    }.filter{ case (d, intersection) => d <= dist.toInt}.map(_._2)
      .fold(IntervalFactory())((range1, range2) => range1.union_all(range2))

    Reasoning.amalgamate(occ, id, Entity(holdsFor, Fluent("distance", Vector(dist), Vector("true"))), t,
      simpleFluent = false)
  }
}
