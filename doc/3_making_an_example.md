## Making an example

Once you feel familiar with the dRTEC classes and *Reasoning* functions, you can proceed to an example that will help you create your own definitions in the future. 
We will implement an example of a dry weather detection. We receive an input data stream from a file, containing information about temperature and humidity level
 from appropriate sensors. The data is received in the following form:

holdsAt,temperaure,idX,high/low,timepoint

holdsAt,humidity,idY,high/low,timepoint

We are assuming that the sensors send data in every second. A dry weather could be defined with the following RTEC definition:

```prolog
initiatedAt(dry_weather(Id1, Id2)=true, T) :-
	happensAt(start(temperature(Id1)=high), T),
	happensAt(start(humidity(Id2)=low), T).
	
terminatedAt(dry_weather(Id1, _Id2)=true, T) :-
	holdsAt(temperature(Id1)=low, T).
	
terminatedAt(dry_weather(_Id1, Id2)=true, T) :-
	holdsAt(humidity(Id2)=high, T).
```

First we need to create the appropriate definitions for our application (see [Creating a Definition](3_1_creating_a_definition.md)).
