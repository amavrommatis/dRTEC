## Creating a Theory

Once you have completed the definitions implementation, it's time to write the application domain.
Import the appropriate classes and packages:

```scala
scala> import cer.drtec.engine.Predicate._
import cer.drtec.engine._
```

Create a new class that extends the dRTEC trait *Theory*:

```scala
scala> case class MyDomain() extends Theory{
  override val startTime: Long = 0
  override val maxNumOfIds: Int = 2
  override val window: Long = 10000
  override val slide: Long = 1000
  override val step: Long = 1000
  override val inputSchema: Map[String, (Int, Int)] = Map(
    "temperature" -> (1, 0),
    "humidity" -> (1, 0)
  )
  override val declarations: Map[Entity, Vector[Rule]] = Map(
    Entity(holdsFor, Fluent("dry_weather", Vector(), Vector("true"))) -> Vector(HoldsForDry_weatherTrue()),
    Entity(initiatedAt, Fluent("dry_weather", Vector(), Vector("true"))) -> Vector(InitiatedAtDry_weatherTrue()),
    Entity(terminatedAt, Fluent("dry_weather", Vector(), Vector("true"))) -> Vector(TerminatedAtDry_weatherTrue()),
    Entity(start, Fluent("temperature", Vector(), Vector("high"))) -> Vector(StartTemperatureHigh()),
    Entity(start, Fluent("humidity", Vector(), Vector("low"))) -> Vector(StartHumidityLow())
  )
  override val simpleEntities: Vector[Entity] = Vector(
    Entity(start, Fluent("temperature", Vector(), Vector("high"))),
    Entity(start, Fluent("humidity", Vector(), Vector("low")))
  )
  override val complexEntities: Map[Int, Vector[Entity]] = Map(
    2 -> Vector(
    Entity(holdsFor, Fluent("dry_weather", Vector(), Vector("true")))
    )
  )
  override val outputEntities: Vector[Entity] = Vector(
    Entity(holdsFor, Fluent("dry_weather", Vector(), Vector("true")))
  )
}
```

This class needs to override the trait's variables:
- *startTime* - the initial time point. This time point must agree with the initial time point of input data.
- *maxNumOfIds* - the maximum number of ids in the complex events. In our case it's 2.
- *window* - window duration in milliseconds.
- *slide* - window slide in milliseconds.
- *step* - time in milliseconds between two consecutive time points.
- *inputSchema* - a map that describes the input data. Each input fluent/event has a tuple with two integers. The first one is the fluent/event number of ids.
The second one is the number of arguments (without the ids).
- *declarations* - a map that defines all the definitions for each entity. We may have more than one definitions describing an entity.
- *simpleEntities* - a vector that defines the top-level simple entities. Simple entities are the ones that have only one id in their declaration. 
By the term top-level, we mean the entities which are not created by any other entity.
- *complexEntities* - a map that defines the top-level complex entities. The key is the number of ids, and the value is a vector of top-level entities. In our case, 
```Entity(holdsFor, Fluent("dry_weather", Vector(), Vector("true")))``` is the only top level entity. The initiation and termination entities will be created by the holdsFor entity.
**In case that you need any simple entity to the output, you have also to add it in the simple entities map.**
- *outputEntities* - a vector of entities that need to be printed in the output.

<br />
The entities declaration is quite flexible. You may have numerous definitions for an entity, as well as, a definition may participate in the recognition of more than one entities.
For example, we may use the definition ```StartTemperatureHigh()``` for recognizing the entity ```Entity(end, Fluent("temperature", Vector(), Vector("low")))```.
