package examples.aminess

import cer.drtec.dRTEC
/**
  * @author Alexandros Mavrommatis
  */
object Executor {

  def main(args: Array[String]) {

    val properties = Vector(
      ("spark.master", "local[*]"),
      ("spark.app.name", "AMINESS"),
      ("spark.executor.memory", "10g")
    )


    val drtec = dRTEC.apply(new Domain(3600, 3600), "./../Data/aminess.txt", "./../aminess-results/" + window + "-" + window + ".txt" , properties, "localhost", 9999, enableConsole = true, enableLog = true)
    dRTEC.start(drtec)
    dRTEC.awaitTermination(drtec, 1800000)
    Thread.sleep(10000)
  }

}
