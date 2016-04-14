package examples.caviar

import cer.drtec.dRTEC

/**
  * @author Alexandros Mavrommatis
  */
object Executor {

  def main(args: Array[String]) {

    val properties = Vector(
      ("spark.master", "local[*]"),
      ("spark.app.name", "CAVIAR"),
      ("spark.executor.memory", "6g")
    )

    val drtec = dRTEC.apply(new Domain(10000, 10000), "./../Data/caviar.txt", "./../caviar-results/" + window + "-" + window + ".txt", properties, "localhost", 9999, enableConsole = true, enableLog = true)
    dRTEC.start(drtec)
    dRTEC.awaitTermination(drtec, 1100000)
    Thread.sleep(10000)

  }

}
