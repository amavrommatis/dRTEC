## Classes and Data Types

#### dRTEC

The main class for creating an engine instance and executing it.
It contains three *apply()* functions for creating a dRTEC instance.
For the execution of the engine we can use the following functions:

```scala
def start(drtec: dRTEC)
```
For starting the engine execution - pass the dRTEC instance as an argument

```scala
def terminate(drtec: dRTEC)
```
For terminating the engine execution

```scala
def awaitTermination(drtec: dRTEC)
```
For awaiting the engine execution to terminate

```scala
def awaitTermination(drtec: dRTEC, millis: Long)
```
For awaiting the engine execution to terminate for a specific time period

#### Reasoning

The object that contatins the basic functions of the engine inference. For more details see [Reasoning](2_2_reasoning.md) section

#### Predicate 

The enumeration representing a fluent/event predicate. It has the following values:
  - *holdsFor* - for recognized fluents or input fluents in the current window (in dRTEC, fluents are represented only in durative form)
  - *happensAt* - for events
  - *holdsForProcessedIE* - for input fluents from the beginning of their occurrence.
  - *start* - for an event which defines that a fluent starts having a value
  - *end* - for an event which defines that a fluent ends having a value
  - *initiatedAt* - for the initiation points of a simple fluent
  - *terminatedAt* - for the termination points of a simple fluent

#### Fluent

The class that defines a fluent or an event. It contains the following arguments:
- *symbol* - fluent/event name - *String*
- *args* - fluent/event arguments - *Vector[String]* (ids not included)
- *values* - fluent values - *Vector[String]* (empty vector in case of an event)

#### Entity
 
The class that defines an entity of the engine. It contains a *Fluent* and its *Predicate*

#### Interval 

The data type that defines a list of left-closed, right-open time intervals. It contains the following functions:

```scala
def intersect_all(rs: Interval)
```
For the intersection of *this* list of intervals with *rs*

```scala
def union_all(rs: Interval)
```
For the union of *this* list of intervals with *rs*
```scala
def relative_complement_all(rs: Interval)
```
For the relative complement of *this* list of intervals with *rs*
```scala
def complement_all(window: Interval)
```
For the complement of *this* in a specific *window*

#### Occurrences

The class that contains the collection of entities pointing to their list of intervals for a specific window
