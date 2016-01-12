## Input data

dRTEC engine is able to receive the data stream from a file or a MQTT topic.
The data syntax should be agreed with the [RTEC syntax](http://users.iit.demokritos.gr/~a.artikis/EC.html) and represented in the following format,
 in order to be parsed from the engine.

**predicate, event/fluent, args, values, time**

predicate:
* happensAt - for events
* holdsAt - for instantaneous fluents
* holdsFor - for durative fluents

args - list of arguments separated by semicolon (;) - the first arguments in the list MUST be the sensor's/device's id(s)

values - list of values separated by semicolon (;)

time may be a:
* time stamp - one number representing time in milliseconds
* time interval - two numbers representing the start and the end of an interval in miliiseconmds (separated by semicolon (;)) (left-closed, right-open interval)


The main differences between events and fluents are the following:
* An event has no values but a fluent is always equal to at least one value
* Events are always instantaneous unlike fluents that can be also durative
