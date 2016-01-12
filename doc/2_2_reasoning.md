## Reasoning

The *Reasoning* object contains the basic functions of the dRTEC inference. This functions can be used to build any particular definitions.

```scala
def amalgamate(map: Map[Vector[String], Occurrences], 
				id: Vector[String], 
				entity: Entity, 
				newInterval: Interval,
				simpleFluent: Boolean): Map[Vector[String], Occurrences]
```
Inserts a new list of intervals - *newInterval*, to an existing or non-existing *entity* in the collection *map* for a specific list of ids - *id*. *simpleFluent* is only 
true for *holdsFor* entities that their list of intervals have been calculated from *initiatedAt* and *terminatedAt* entities (simple fluent case).

Returns the collection of recognized entites with the revised entity.

<br />

```scala
def getInterval(occ: Map[Vector[String], Occurrences], 
				id: Vector[String], 
				entity: Entity): (Map[Vector[String], Occurrences], Interval)
```
Calculates in collection *map*, the list of intervals for a specific list of ids - *id* and *entity*.

Returns a tuple containing the revised collection of recognized entities and the list of intervals.

<br />
```scala
def start(set: Interval): Interval
```
Calculates the start points of the list of intervals *set*. 

Returns a list of intervals containing the start points

<br />
```scala
def end(set: Interval): Interval
```
Calculates the end points of the list of intervals *set*. 

Returns a list of intervals containing the end points

<br />
```scala
def makeIntervalsFromPoints(initRanges: Interval, termRanges: Interval)
```
Calculates the list of intervals for a simple fluent, according to its initiation points - *initRanges* and termination points - *termRanges*.

Returns the list of intervals.

<br />

For mοre details of the functions' use, see [Making an Example](3_making_an_example.md) section.
