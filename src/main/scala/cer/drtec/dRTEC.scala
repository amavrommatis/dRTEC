package cer.drtec

import auxlib.opt.OptionParser
import cer.drtec.IO.{MQTTConnector, InputHandler}
import cer.drtec.engine.{EngineExecutor, Theory}
import cer.drtec.utils.SerializedLogging
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Alexandros Mavrommatis
 */
class dRTEC private (_domain: Theory,
            _streamingContext: StreamingContext,
            _sqlContext: SQLContext,
            _inputIp: String,
            _inputPort: Int,
            _enableLog: Boolean,
            _enableConsole: Boolean,
            _mqtt: Boolean,
            _mqttConnectionOp: Option[MQTTConnector],
            _inputFilePathOp: Option[String],
            _outputFilePathOp: Option[String]) {

  val domain = _domain
  val streamingContext = _streamingContext
  val sqlContext = _sqlContext
  val inputIp = _inputIp
  val inputPort = _inputPort
  val enableLog = _enableLog
  val enableConsole = _enableConsole
  val mqtt = _mqtt
  val mqttConnectionOp = _mqttConnectionOp
  val inputFilePathOp = _inputFilePathOp
  val outputFilePathOp = _outputFilePathOp

}

object dRTEC extends SerializedLogging with OptionParser {


  /**
    * Creates an instance of the dRTEC engine
    * @param domain input Theory
    * @param inputFilePath path of the input data file
    * @param outputFilePath path of the output data (in case enableLog = true)
    * @param master spark master's url (default: local[*])
    *               {{{local 	Run Spark locally with one worker thread (i.e. no parallelism at all).
  local[K] 	Run Spark locally with K worker threads (ideally, set this to the number of cores on your machine).
  local[*] 	Run Spark locally with as many worker threads as logical cores on your machine.
  spark://HOST:PORT 	Connect to the given Spark standalone cluster master. The port must be whichever one your master is configured to use, which is 7077 by default.
  mesos://HOST:PORT 	Connect to the given Mesos cluster. The port must be whichever one your is configured to use, which is 5050 by default. Or, for a Mesos cluster using ZooKeeper, use mesos://zk://....
  yarn-client 	Connect to a YARN cluster in client mode. The cluster location will be found based on the HADOOP_CONF_DIR or YARN_CONF_DIR variable.
  yarn-cluster 	Connect to a YARN cluster in cluster mode. The cluster location will be found based on the HADOOP_CONF_DIR or YARN_CONF_DIR variable. }}}
    * @param appName application name (default: dRTEC)
    * @param inputIp master's url for spark in order to receive input data (default: localhost)
    * @param inputPort port for input data (default: 9999)
    * @param enableLog enable output logging (default: true)
    * @param enableConsole enable logging to console (default: true)
    * @return
    */
  def apply(domain: Theory,
            inputFilePath: String,
            outputFilePath: String,
            master: String = "local[*]",
            appName: String = "dRTEC",
            inputIp: String = "127.0.0.1",
            inputPort: Int = 9999,
            enableLog: Boolean = true,
            enableConsole: Boolean = true): dRTEC = {

    //configure spark
    println(logo)
    val properties = Vector(("spark.master", master), ("spark.app.name", appName))
    val conf = new SparkConf().setAll(properties)
    val ssc = new StreamingContext(conf, Milliseconds(domain.slide))
    val sqlContext = new SQLContext(ssc.sparkContext)
    new dRTEC(domain, ssc, sqlContext, inputIp, inputPort, enableLog, enableConsole, _mqtt = false, None,
      Some(inputFilePath), Some(outputFilePath))
  }

  /**
    * Creates an instance of the dRTEC engine
    * @param domain input Theory
    * @param inputFilePath path of the input data file,
    * @param outputFilePath path of the output data (in case enableLog = true)
    * @param properties spark properties
    * @param inputIp master's url for spark in order to receive input data
    * @param inputPort port for input data
    * @param enableLog enables logging to file
    * @param enableConsole enables logging to console
    * @return
    */
  def apply(domain: Theory,
            inputFilePath: String,
            outputFilePath: String,
            properties: Vector[(String, String)],
            inputIp: String,
            inputPort: Int,
            enableLog: Boolean,
            enableConsole: Boolean): dRTEC = {

    //configure spark
    println(logo)
    val conf = new SparkConf().setAll(properties)
    val ssc = new StreamingContext(conf, Milliseconds(domain.slide))
    val sqlContext = new SQLContext(ssc.sparkContext)
    new dRTEC(domain, ssc, sqlContext, inputIp, inputPort, enableLog, enableConsole, _mqtt = false, None,
      Some(inputFilePath), Some(outputFilePath))
  }

  /**
    * Creates an instance of the dRTEC engine
    * @param domain input Theory
    * @param mqttBrokerUrl mqtt broker url
    * @param inputTopic broker input topic
    * @param outputTopic broker output topic
    * @param username mqtt username for authentication (optional)
    * @param password mqtt password for authentication (optional)
    * @param sparkProperties spark properties
    * @param inputIp master's url for spark in order to receive input data
    * @param inputPort port for input data
    * @param enableLog enable logging to output topic
    * @param enableConsole enable logging to console
    * @return
    */
  def apply(domain: Theory,
            mqttBrokerUrl: String,
            inputTopic: String,
            outputTopic: String,
            username: Option[String],
            password: Option[String],
            sparkProperties: Vector[(String, String)],
            inputIp: String,
            inputPort: Int,
            enableLog: Boolean,
            enableConsole: Boolean): dRTEC = {

    //configure spark
    println(logo)
    val conf = new SparkConf().setAll(sparkProperties)
    val ssc = new StreamingContext(conf, Milliseconds(domain.slide))
    val sqlContext = new SQLContext(ssc.sparkContext)
    val mqttConnection = new MQTTConnector(mqttBrokerUrl, inputTopic, outputTopic, username, password)
    new dRTEC(domain, ssc, sqlContext, inputIp, inputPort, enableLog, enableConsole, _mqtt = true, Some(mqttConnection),
      None, None)
  }

  /**
    * Starts the engine execution
    * @param drtec engine instance
    */
  def start(drtec: dRTEC) {

    val domain = drtec.domain
    val inputIp = drtec.inputIp
    val inputPort = drtec.inputPort
    val inputPathOp = drtec.inputFilePathOp
    val mqtt = drtec.mqtt
    val mqttConnectionOp = drtec.mqttConnectionOp

    //future for data input
    Future {
      if(mqtt) InputHandler.receiveFromMQTT(domain, mqttConnectionOp.get, inputIp, inputPort)
      else InputHandler.sendFileDataToSpark(domain, inputPathOp.get, inputIp, inputPort)
    }

    //future for execution
    Future {
      EngineExecutor.start(drtec)
    }
  }

  /**
    * Awaits termination of the engine
    * @param drtec engine instance
    */
  def awaitTermination(drtec: dRTEC) = drtec.streamingContext.awaitTermination()

  /**
    * Awaits termination of the engine
    * @param drtec engine instance
    * @param millis milliseconds to wait
    */
  def awaitTermination(drtec: dRTEC, millis: Long) = {
    Thread.sleep(millis)
    EngineExecutor.stop(drtec)
  }

  /**
    * Terminates engine
    * @param drtec engine instance
    */
  def terminate(drtec: dRTEC) = EngineExecutor.stop(drtec)

}
