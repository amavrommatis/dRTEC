## Running an example

The final step is to run our example.
Create an object for the execution:

```scala
scala> object Executor {

  def main(args: Array[String]) {

    val drtec = dRTEC.apply(MyDomain(), "input.txt", "output.txt")
    dRTEC.start(drtec)
    dRTEC.awaitTermination(drtec, 3600000)
  }
}
```

We create our dRTEC instance using the ```apply()``` function with default parameters.
We pass as parameters an instance of the domain class, the input and the output file path.
The application waits the termination of the engine for one hour. At the end of the execution, the results will be printed in the output file.

In case you need to pass other parameters (e.g., Apache Spark parameters), or connect to a MQTT broker, see the declaration of the ```apply()``` functions.
