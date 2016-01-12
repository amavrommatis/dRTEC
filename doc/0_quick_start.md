## Quick Start

dRTEC is an open-source library that consists of a distributed event recognition engine. This engine uses RTEC, which is an Event Calculus dialect
with novel implementation and 'windowing' techniques that allow for efficient event recognition, scalable to large data streams.
dRTEC is written in [Scala programming language](http://www.scala-lang.org) (v 2.11.7) with [Apache Spark Streaming](http://spark.apache.org/streaming/) (v 1.5.2).

dRTEC accepts as input a stream of time-stamped simple derived events (SDE) from sensor and other computational devices. It receives the input data and seeks to
 identify high-level composite events (CE), collection of events that satisfy a rule. The definition of a CE imposes temporal and, possibly, 
 atemporal constraints on its subevents, that is, SDEs or other CEs. dRTEC also includes a window mechanism, 
that deals with applications where event data arrives with a (variable) delay from, and are revised by, the underlying sources.
 dRTEC can update already recognized events and recognize new events when data arrives with a delay or following data revision.
 It performs run-time CE recognition by querying, computing and storing the maximal intervals of fluents and the time-points in which events occur. CE recognition takes places at
 specified query times. At each query time, only SDEs that fall within a specified time window are taken into consideration. All SDEs that took place before the window are discarded.
 The window is defined by two parameters; its duration and its slide. The duration must be a multiple of the slide and greater than it. If the slide is equal to the duration,
 then the windows are non-overlapping.
 
 For more information about the RTEC dialect see http://users.iit.demokritos.gr/~a.artikis/publications/artikis-TKDE14.pdf.
