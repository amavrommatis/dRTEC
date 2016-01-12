package cer.drtec.IO

import java.io._
import java.net._
import java.util.concurrent.PriorityBlockingQueue
import cer.drtec.engine.Theory
import cer.drtec.utils.SerializedLogging
import scala.io.Source

/**
 * @author Alexandros Mavrommatis
 */
private[drtec] object InputHandler extends SerializedLogging{

  private var serverSocket: ServerSocket = null
  val mqttData = new PriorityBlockingQueue[String]()

  /**
    * starts the spark receiver of input data from mqtt
    * @param domain Theory
    * @param inputIp master's url for spark in order to receive input data (default: localhost)
    * @param inputPort port for input data
    * @param mqttClient mqtt broker's connection
    */
  private[drtec] def receiveFromMQTT(domain: Theory, mqttClient: MQTTConnector, inputIp: String, inputPort: Int) {

    mqttClient.connect()
    info("dRTEC INFO: Connected to MQTT broker\ndRTEC INFO: URL -> " + mqttClient.brokerUrl)
    sendMqttDataToSpark(domain, inputIp, inputPort)
  }

  /**
    * sends input data from mqtt topic to spark receiver (window index added)
    * @param domain Theory
    * @param ip socket ip
    * @param port port number
    */
  private[drtec] def sendMqttDataToSpark(domain: Theory, ip: String, port: Int) {

    serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip))
    val clientSocket = serverSocket.accept()
    val out = new PrintStream(clientSocket.getOutputStream)
    var window: Long = 1
    var firstWindow = 1
    val windowDur = domain.window
    val slide = domain.slide

    //send data
    while (true) {
      while (!mqttData.isEmpty) {
        val data = mqttData.poll()
        data.split("\n").foreach{ line =>
          out.println(line + "," + window)
        }

        out.flush()
        if(firstWindow < windowDur / slide) firstWindow += 1
        else window += 1
      }
    }
  }

  /**
    * sends input data from file to spark receiver (window index added)
    * @param domain Theory
    * @param inputPath input data file path
    * @param ip socket ip
    * @param port port number
    */
  private[drtec] def sendFileDataToSpark(domain: Theory, inputPath: String, ip: String, port: Int) {

    val data = Source.fromFile(inputPath).getLines().toVector.map{line =>
      val time = line.substring(line.lastIndexOf(",")+1)

      if(time.contains(";")) {
        val startEnd = time.split(";")
        (line, startEnd(1).toLong)
      }
      else (line, time.toLong)
    }.sortBy(_._2)

    val startTime: Long = domain.startTime
    serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip))

    //send data
    while (true) {
      val clientSocket = serverSocket.accept()
      val out = new PrintStream(clientSocket.getOutputStream)

      info("dRTEC INFO: Streaming data from file \"" + inputPath + "\"")

      var curTime = startTime + domain.slide
      var windowEnd = startTime + domain.window
      var window = 1
      var sendTime = System.currentTimeMillis()

      data.foreach{ case (line, time) =>

        if(time < curTime) {
          out.println(line + "," + window)
        }
        else {
          curTime += domain.slide
          if(curTime > windowEnd) {
            window += 1
            windowEnd += domain.slide
          }
          Thread.sleep(domain.slide - (System.currentTimeMillis() - sendTime))
          sendTime = System.currentTimeMillis()
          out.println(line + "," + window)
        }
      }

      out.flush()
      out.close()
      clientSocket.close()
      serverSocket.close()
      info("dRTEC INFO: End of file stream")
    }
  }

  /**
    * closes streaming socket
    */
  private[drtec] def closeSocket() = {
    if(serverSocket != null) serverSocket.close()
    info("dRTEC INFO: Socket closed...")
  }
}
