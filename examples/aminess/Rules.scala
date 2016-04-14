package examples.aminess

import cer.drtec.engine._
import cer.drtec.engine.Predicate._
import cer.drtec.utils.IntervalFactory
import cer.drtec.utils.IntervalWrapperObj._

/**
  * @author Alexandros Mavrommatis
  */

/**
  * {{{initiatedAt(lowSpeed(Vessel)=true, T) :-
	happensAt(lowSpeedStart(Vessel), T),
	\+happensAt(nearPorts(Vessel), T).}}}
  */
case class InitiatedAtLowSpeedTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, lowSpeedStartI) = Reasoning
      .getInterval(occ, id, Entity(happensAt, Fluent("lowSpeedStart", Vector(), Vector())))
    val (occ2, nearPortsI) = Reasoning.getInterval(occ1, id, Entity(happensAt, Fluent("nearPorts", Vector(), Vector())))

    val i = lowSpeedStartI.relative_complement_all(nearPortsI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{terminatedAt(lowSpeed(Vessel)=true, T) :-
	happensAt(lowSpeedEnd(Vessel), T).

terminatedAt(lowSpeed(Vessel)=true, T) :-
	happensAt(start(stopped(Vessel)=true), T).}}}
  */
case class TerminatedAtLowSpeedTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, lowSpeedEndI) = Reasoning
      .getInterval(occ, id, Entity(happensAt, Fluent("lowSpeedEnd", Vector(), Vector())))
    val (occ2, startStoppedTrueI) = Reasoning
      .getInterval(occ1, id, Entity(start, Fluent("stopped", Vector(), Vector("true"))))

    val i = lowSpeedEndI.union_all(startStoppedTrueI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{holdsFor(lowSpeed(Vessel)=true, T)}}}
  */
case class HoldsForLowSpeedTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, initiatedAtLowSpeedTrueI) = Reasoning
      .getInterval(occ, id, Entity(initiatedAt, Fluent("lowSpeed", Vector(), Vector("true"))))
    val (occ2, terminatedAtLowSpeedTrueI) = Reasoning
      .getInterval(occ1, id, Entity(terminatedAt, Fluent("lowSpeed", Vector(), Vector("true"))))

    val i = Reasoning.makeIntervalsFromPoints(initiatedAtLowSpeedTrueI, terminatedAtLowSpeedTrueI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = true)
  }
}

/**
  * {{{happensAt(start(stopped(Vessel)=true), T)}}}
  */
case class StartStoppedTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, stoppedTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("stopped", Vector(), Vector("true"))))

    val i = Reasoning.start(stoppedTrueI)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
  * initiatedAt(gap(Vessel)=true, T) :-
	happensAt(gapStart(Vessel), T),
	\+happensAt(nearPorts(Vessel), T).
  */
case class InitiatedAtGapTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, gapStartI) = Reasoning.getInterval(occ, id, Entity(happensAt, Fluent("gapStart", Vector(), Vector())))
    val (occ2, nearPortsI) = Reasoning.getInterval(occ1, id, Entity(happensAt, Fluent("nearPorts", Vector(), Vector())))

    val i = gapStartI.relative_complement_all(nearPortsI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{terminatedAt(gap(Vessel)=true, T) :-
	happensAt(gapEnd(Vessel), T).

terminatedAt(gap(Vessel)=true, T) :-
	happensAt(start(stopped(Vessel)=true), T).

terminatedAt(gap(Vessel)=true, T) :-
	happensAt(lowSpeedStart(Vessel), T).

terminatedAt(gap(Vessel)=true, T) :-
	happensAt(turn(Vessel), T).

terminatedAt(gap(Vessel)=true, T) :-
	happensAt(speedChange(Vessel), T).}}}
  */
case class TerminatedAtGapTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, gapEndI) = Reasoning.getInterval(occ, id, Entity(happensAt, Fluent("gapEnd", Vector(), Vector())))
    val (occ2, startStoppedTrueI) = Reasoning
      .getInterval(occ1, id, Entity(start, Fluent("stopped", Vector(), Vector("true"))))
    val (occ3, lowSpeedStartI) = Reasoning
      .getInterval(occ2, id, Entity(happensAt, Fluent("lowSpeedStart", Vector(), Vector())))
    val (occ4, turnI) = Reasoning.getInterval(occ3, id, Entity(happensAt, Fluent("turn", Vector(), Vector())))
    val (occ5, speedChangeI) = Reasoning
      .getInterval(occ4, id, Entity(happensAt, Fluent("speedChange", Vector(), Vector())))

    val i = gapEndI.union_all(startStoppedTrueI).union_all(lowSpeedStartI).union_all(turnI).union_all(speedChangeI)
    Reasoning.amalgamate(occ5, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{holdsFor(gap(Vessel)=true, T)}}}
  */
case class HoldsForGapTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, initiatedAtGapTrueI) = Reasoning.
      getInterval(occ, id, Entity(initiatedAt, Fluent("gap", Vector(), Vector("true"))))
    val (occ2, terminatedAtGapTrueI) = Reasoning.
      getInterval(occ1, id, Entity(terminatedAt, Fluent("gap", Vector(), Vector("true"))))

    val i = Reasoning.makeIntervalsFromPoints(initiatedAtGapTrueI, terminatedAtGapTrueI)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = true)
  }
}

/**
  * {{{holdsFor(stoppedNIP(Vessel)=true, I) :-
		holdsFor(stopped(Vessel)=true, I2),
		\+happensAt(nearPorts(Vessel), I3).
		intersect_all([I2, I3], I)}}}
  */
case class HoldsForStoppedNIPTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i2) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("stopped", Vector(), Vector("true"))))
    val (occ2, i3) = Reasoning.getInterval(occ1, id, Entity(happensAt, Fluent("nearPorts", Vector(), Vector())))

    val i = i2.relative_complement_all(i3)
    Reasoning.amalgamate(occ2, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(start(stoppedNIP(Vessel)=true), T)}}}
  */
case class StartStoppedNIPTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, stoppedNIPTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("stoppedNIP", Vector(), Vector("true"))))

    val i = Reasoning.start(stoppedNIPTrueI)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(start(gap(Vessel)=true), T)}}}
  */
case class StartGapTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, gapTrueI) = Reasoning.getInterval(occ, id, Entity(holdsFor, Fluent("gap", Vector(), Vector("true"))))

    val i = Reasoning.start(gapTrueI)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(end(gap(Vessel)=true), T)}}}
  */
case class EndGapTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, gapTrueI) = Reasoning.getInterval(occ, id, Entity(holdsFor, Fluent("gap", Vector(), Vector("true"))))

    val i = Reasoning.end(gapTrueI)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(start(lowSpeed(Vessel)=true), T)}}}
  */
case class StartLowSpeedTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, lowSpeedTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("lowSpeed", Vector(), Vector("true"))))

    val i = Reasoning.start(lowSpeedTrueI)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(end(lowSpeed(Vessel)=true), T)}}}
  */
case class EndLowSpeedTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, lowSpeedTrueI) = Reasoning
      .getInterval(occ, id, Entity(holdsFor, Fluent("lowSpeed", Vector(), Vector("true"))))

    val i = Reasoning.end(lowSpeedTrueI)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(illegalShipping(Vessel), T) :-
	happensAt(start(stoppedNIP(Vessel)=true), T),
	happensAt(inArea(Vessel), T).

happensAt(illegalShipping(Vessel), T) :-
	happensAt(start(gap(Vessel)=true), T),
	happensAt(inArea(Vessel), T).

happensAt(illegalShipping(Vessel), T) :-
	happensAt(end(gap(Vessel)=true), T),
	happensAt(inArea(Vessel), T).

happensAt(illegalShipping(Vessel), T) :-
	happensAt(start(lowSpeed(Vessel)=true), T),
	happensAt(inArea(Vessel), T).

happensAt(illegalShipping(Vessel), T) :-
	happensAt(end(lowSpeed(Vessel)=true), T),
	happensAt(inArea(Vessel), T).

happensAt(illegalShipping(Vessel), T) :-
	happensAt(turn(Vessel), T),
	happensAt(inArea(Vessel), T),
	\+happensAt(nearPorts(Vessel), T).

happensAt(illegalShipping(Vessel), T) :-
	happensAt(speedChange(Vessel), T),
	happensAt(inArea(Vessel), T),
	\+happensAt(nearPorts(Vessel), T).}}}
  */
case class HappensAtIllegalShipping() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, startStoppedNIPTrueI) = Reasoning
      .getInterval(occ, id, Entity(start, Fluent("stoppedNIP", Vector(), Vector("true"))))
    val (occ2, inAreaI) = Reasoning.getInterval(occ1, id, Entity(happensAt, Fluent("inArea", Vector(), Vector())))
    val (occ3, startGapTrueI) = Reasoning.getInterval(occ2, id, Entity(start, Fluent("gap", Vector(), Vector("true"))))
    val (occ4, endGapTrueI) = Reasoning.getInterval(occ3, id, Entity(end, Fluent("gap", Vector(), Vector("true"))))
    val (occ5, startLowSpeedTrueI) = Reasoning
      .getInterval(occ4, id, Entity(start, Fluent("lowSpeed", Vector(), Vector("true"))))
    val (occ6, endLowSpeedTrueI) = Reasoning
      .getInterval(occ5, id, Entity(end, Fluent("lowSpeed", Vector(), Vector("true"))))
    val (occ7, turnI) = Reasoning.getInterval(occ6, id, Entity(happensAt, Fluent("turn", Vector(), Vector())))
    val (occ8, nearPortsI) = Reasoning.getInterval(occ7, id, Entity(happensAt, Fluent("nearPorts", Vector(), Vector())))
    val (occ9, speedChangeI) = Reasoning
      .getInterval(occ8, id, Entity(happensAt, Fluent("sppedChange", Vector(), Vector())))

    val i6 = turnI.relative_complement_all(nearPortsI)
    val i7 = speedChangeI.relative_complement_all(nearPortsI)
    val i = startStoppedNIPTrueI.union_all(startGapTrueI).union_all(endGapTrueI).union_all(startLowSpeedTrueI)
      .union_all(endLowSpeedTrueI).union_all(i6).union_all(i7).intersect_all(inAreaI)
    Reasoning.amalgamate(occ9, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{happensAt(fastApproach(Vessel), T) :-
	happensAt(turn(Vessel), T),
	happensAt(velocity(Vessel,Speed,_Heading), T),
	Speed \= '-1',
	Speed >= 20.0,
	happensAt(coord(Vessel,Lon,Lat), T),
	\+happensAt(nearPorts(Vessel), T),
	happensAt(heading2Vessels(Vessel), T).

happensAt(fastApproach(Vessel), T) :-
	happensAt(start(gap(Vessel)=true), T),
	happensAt(velocity(Vessel,Speed,_Heading), T),
	Speed \= '-1',
	Speed >= 20.0,
	%happensAt(coord(Vessel,Lon,Lat), T),
	happensAt(heading2Vessels(Vessel), T).

happensAt(fastApproach(Vessel), T) :-
	happensAt(speedChange(Vessel), T),
	happensAt(velocity(Vessel,Speed,_Heading), T),
	Speed \= '-1',
	Speed >= 20.0,
	happensAt(coord(Vessel,Lon,Lat), T),
	\+happensAt(nearPorts(Vessel), T),
	happensAt(heading2Vessels(Vessel), T).}}}
  */
case class HappensAtFastApproach() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity
                        )(implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, turnI) = Reasoning.getInterval(occ, id, Entity(happensAt, Fluent("turn", Vector(), Vector())))
    val velocityI = occ1.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map
      .filter{ case (inEntity, (inInterval, inCase)) =>
          inEntity.predicate == happensAt && inEntity.fluent.symbol == "velocity" &&
            inEntity.fluent.args.head.toDouble >= 20
      }.map(_._2._1).fold(IntervalFactory())((range1, range2) => range1.union_all(range2))
    val coordI = occ1.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map
      .filter{ case (inEntity, (inInterval, inCase)) =>
        inEntity.predicate == happensAt && inEntity.fluent.symbol == "coord"
      }.map(_._2._1).fold(IntervalFactory())((range1, range2) => range1.union_all(range2))
    val (occ2, nearPortsI) = Reasoning.getInterval(occ1, id, Entity(happensAt, Fluent("nearPorts", Vector(), Vector())))
    val (occ3, heading2VesselsI) = Reasoning
      .getInterval(occ2, id, Entity(happensAt, Fluent("heading2Vessels", Vector(), Vector())))
    val (occ4, startGapTrueI) = Reasoning.getInterval(occ3, id, Entity(start, Fluent("gap", Vector(), Vector("true"))))
    val (occ5, speedChangeI) = Reasoning.getInterval(occ4, id, Entity(happensAt, Fluent("speedChange", Vector(), Vector())))

    val i1 = turnI.intersect_all(velocityI).intersect_all(coordI).relative_complement_all(nearPortsI)
      .intersect_all(heading2VesselsI)
    val i2 = startGapTrueI.intersect_all(velocityI).intersect_all(heading2VesselsI)
    val i3 = speedChangeI.intersect_all(velocityI).intersect_all(coordI).relative_complement_all(nearPortsI)
      .intersect_all(heading2VesselsI)
    val i = i1.union_all(i2).union_all(i3)
    Reasoning.amalgamate(occ5, id, entity, i, simpleFluent = false)
  }
}

/**
  * {{{holdsFor(suspiciousDelay(Vessel)=true, I) :-
		holdsFor(gap(Vessel)=true, I2),
		extendedDelays(Vessel, I2, I).}}}
  */
case class HoldsForSuspiciousDelayTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, i2) = Reasoning.getInterval(occ, id, Entity(holdsFor, Fluent("gap", Vector(), Vector("true"))))
    val i = extendedDelays(occ1, id, i2)
    Reasoning.amalgamate(occ1, id, entity, i, simpleFluent = false)
  }

  /**
    * {{{extendedDelays(Vessel,[],[]) :- !.
extendedDelays(Vessel,InIntervals,OutIntervals) :-
		%writeln('Vessel'),writeln(Vessel),
		%writeln('InIntervals'),writeln(InIntervals),
		findall(I,
				(
				member(I, InIntervals),
				estimatedSpeed(Vessel,I,Speed),
				Speed < 5.0
				%writeln(Speed)
				),
				OutIntervals).}}}
    */
  private def extendedDelays(occ: Map[Vector[String], Occurrences], id: Vector[String], i2: Interval) = {

    var newInterval = IntervalFactory()
    val it = i2.rangeIterator()
    while (it.moveToNext()) {
      val start = it.first()
      val end = it.last()
      val SpeedOp = estimatedSpeed(occ, id, start, end + 1)
      SpeedOp match {
        case Some(speed) => if(speed < 5) newInterval = newInterval.union_all(IntervalFactory(start, end))
        case None =>
      }

    }
    newInterval
  }

  /**
    * {{{estimatedSpeed(Vessel,(S,inf),0.0) :- !.
estimatedSpeed(Vessel,(S,E),Speed) :-
	%writeln('S'),writeln(S),writeln('Vessel'),writeln(Vessel),
	S1 is S-1,
	E1 is E-1,
	happensAtIE(coord(Vessel,LonS,LatS), S1),
	%writeln('happensAtIE11111'),
	happensAtIE(coord(Vessel,LonE,LatE), E1),
	%writeln('happensAtIE22222'),
	harvesineDistance((LonS,LatS),(LonE,LatE),SpatialDist),
	%writeln('euclDistance'),
	TemporalDist is (E - S)*3600,
	%writeln('Spatial'),writeln(SpatialDist),
	%writeln('Temporal'),writeln(TemporalDist),
	Speed is (SpatialDist/TemporalDist).
	%writeln('Speed'),writeln(Speed).}}}
    */
  private def estimatedSpeed(occ: Map[Vector[String], Occurrences], id: Vector[String], S: Long, E: Long) = {

    val S1 = S - 1
    val E1 = S - 1

    if(E1 == Infinity) Some(0.0)
    else {

      val coordSOp = occ.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map
        .find{ case (inEntity, (inInterval, inCase)) =>
          inEntity.predicate == happensAt && inEntity.fluent.symbol == "coord" && inInterval.contains(S1)
        }

      val coordEOp = occ.getOrElse(id, Occurrences(Map[Entity, (Interval, Boolean)]())).map
        .find{ case (inEntity, (inInterval, inCase)) =>
          inEntity.predicate == happensAt && inEntity.fluent.symbol == "coord" && inInterval.contains(E1)
        }

      coordSOp match {
        case Some(coordS) =>
          coordEOp match {
            case Some(coordE) =>
              val argsS = coordS._1.fluent.args
              val argsE = coordE._1.fluent.args

              val LonS = argsS.head.toDouble
              val LatS = argsS(1).toDouble

              val LonE = argsE.head.toDouble
              val LatE = argsE(1).toDouble

              val SpatialDist = harvesineDistance(LonS, LatS, LonE, LatE)
              val TemporalDist = (E - S) * 3600

              Some(SpatialDist/TemporalDist)
            case None => None
          }
        case None => None
      }

    }
  }

  /**
    * {{{harvesineDistance((Lon1,Lat1),(Lon2,Lat2),Dist) :-
	Lon1Rad is Lon1 * (pi/180),
	Lat1Rad is Lat1 * (pi/180),
	Lon2Rad is Lon2 * (pi/180),
	Lat2Rad is Lat2 * (pi/180),
	Dlon is Lon2Rad - Lon1Rad,
	Dlat is Lat2Rad - Lat1Rad,
	A is (sin(Dlat/2))^2 + cos(Lat1Rad)*cos(Lat2Rad)*(sin(Dlon/2))^2,
	C is 2*atan2(sqrt(A),sqrt(1-A)),
	% Earth radius: 3961 miles / 6373 km
	Dist is 3961*C.
	%Dist is sqrt( abs(Lon2-Lon1)^2 +abs(Lat2-Lat1)^2 ).}}}
    */
  private def harvesineDistance(Lon1: Double, Lat1: Double, Lon2: Double, Lat2: Double) = {

    import scala.math._
    val Lon1Rad = Lon1 * (Pi / 180)
    val Lat1Rad = Lat1 * (Pi / 180)
    val Lon2Rad = Lon2 * (Pi / 180)
    val Lat2Rad = Lat2 * (Pi / 180)
    val Dlon = Lon2Rad - Lon1Rad
    val Dlat = Lat2Rad - Lat1Rad
    val A = pow(sin(Dlat / 2), 2) + cos(Lat1Rad) * cos(Lat2Rad) * pow(sin(Dlon / 2), 2)
    val C = 2 * atan2(sqrt(A), sqrt(1 - A))
    3961 * C
  }
}
