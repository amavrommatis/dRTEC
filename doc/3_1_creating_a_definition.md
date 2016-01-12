## Creating a definition

Import the appropriate classes and packages:

```scala
scala> import cer.drtec.engine.Predicate._
import cer.drtec.engine._
import cer.drtec.utils.IntervalFactory
import cer.drtec.utils.IntervalWrapperObj._
```

Create a new class for the definition of ```start(temperature(Id1)=high)```:

```scala
scala> case class StartTemperatureHigh() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, tI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("temperature", Vector(), Vector("high"))))
    val startTI = Reasoning.start(tI)
    Reasoning.amalgamate(occ1, id, entity, startTI, simpleFluent = false)
  }
}
```

Each definition has to extend the dRTEC trait *Rule* and implement the body of the ```recognize()``` function.
Within this function, you may write the code that executes the recognition of the specific definition. In the case of ```start(temperature(Id1)=high)```, 
we get the list of intervals for the fluent *temperature*, with no arguments (the ids are not included in the arguments anymore), 
and ```value = Vector("high")```. Then, we calculate the start points, using the ```start()```function of the *Reasoning* object, 
and we store them in the collection of entities, using the ```amalgamate()``` function. 
Significant details that needs to be mentioned:
- Be careful - the entities collection is being revised whenever a *Reasoning* function is called.
In our case, we pass the result collection of ```getInterval()``` as an argument to the ```amalgamate()``` function.
- The *simpleFluent* argument in that case is equal to false
- We use the predicate *holdsForProcessedIE* instead of *holdsFor*, because *temperature* is an input fluent. We need to calculate its start points for the beginning of 
its occurrence, and not only for the current window. **In any other case**, *holdsFor* is the appropriate predicate.

Now, implement the rest of the rules:

```scala
scala> case class StartHumidityLow() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val (occ1, hI) = Reasoning
      .getInterval(occ, id, Entity(holdsForProcessedIE, Fluent("humidity", Vector(), Vector("low"))))
    val startHI = Reasoning.start(hI)
    Reasoning.amalgamate(occ1, id, entity, startHI, simpleFluent = false)
  }
}

case class InitiatedAtDry_weatherTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val id1 = Vector(id.head)
    val id2 = Vector(id(1))
    val (occ1, startTI) = Reasoning
      .getInterval(occ, id1, Entity(start, Fluent("temperature", Vector(), Vector("high"))))
    val (occ2, startHI) = Reasoning
      .getInterval(occ1, id2, Entity(start, Fluent("humidity", Vector(), Vector("low"))))
    val I = startTI.intersect_all(startHI)
    Reasoning.amalgamate(occ2, id, entity, I, simpleFluent = false)
  }
}

case class TerminatedAtDry_weatherTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {

    val id1 = Vector(id.head)
    val id2 = Vector(id(1))
    val (occ1, tI) = Reasoning
      .getInterval(occ, id1, Entity(holdsFor, Fluent("temperature", Vector(), Vector("low"))))
    val (occ2, hI) = Reasoning
      .getInterval(occ1, id2, Entity(holdsFor, Fluent("humidity", Vector(), Vector("high"))))
    val I = tI.union_all(hI)
    Reasoning.amalgamate(occ2, id, entity, I, simpleFluent = false)
  }
}

case class HoldsForDry_weatherTrue() extends Rule {

  override def recognize(occ: Map[Vector[String], Occurrences], id: Vector[String], entity: Entity)
                        (implicit domain: Theory, windowInterval: Interval) = {
    
    val (occ1, initI) = Reasoning
      .getInterval(occ, id, Entity(initiatedAt, Fluent("dry_weather", Vector(), Vector("true"))))
    val (occ2, termI) = Reasoning
      .getInterval(occ1, id, Entity(terminatedAt, Fluent("dry_weather", Vector(), Vector("true"))))
    val I = Reasoning.makeIntervalsFromPoints(initI, termI)
    Reasoning.amalgamate(occ2, id, entity, I, simpleFluent = true)
  }
}
```
Details to be mentioned:
- *simpleFluent* is equal to true in the ```amalgamate()``` function, only in the case of *HoldsForDry_weatherTrue*
- We need to implement a holdsFor definition to create the list of intervals that are produced from the initiation and termination points (use of ```makeIntervalsFromPoints()``` function)
- The sequence of the ids MUST be concrete according to the RTEC definition. In any other case, the recognition will be erroneous. 

<br />
The aformentioned definitions use the main *Reasoning* methods which agree with the RTEC dialect. 
In case you want to build a definition beyond the dialect's rules (for instance - the retrieval of a fluent's values), 
feel free to transform the collection of entities in any way that might help you (filtering/mapping/reducing etc).
