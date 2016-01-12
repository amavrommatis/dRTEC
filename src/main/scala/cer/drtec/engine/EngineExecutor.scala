package cer.drtec.engine

import java.io.File
import cer.drtec.IO.{InputHandler, OutputHandler}
import cer.drtec.dRTEC
import cer.drtec.utils.SerializedLogging
import org.apache.commons.io.FileUtils
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.Milliseconds

/**
  * @author Alexandros Mavrommatis
  */
private[drtec] object EngineExecutor extends SerializedLogging{

  private[drtec] def start(drtec: dRTEC){

    //input parameters
    implicit val ssc = drtec.streamingContext
    implicit val sc = ssc.sparkContext
    implicit val sqlc = drtec.sqlContext
    implicit val domain = drtec.domain
    val mqtt = drtec.mqtt
    val mqttConnectionOp = drtec.mqttConnectionOp
    val outputFilePathOp = drtec.outputFilePathOp
    val enableLog = drtec.enableLog
    val enableConsole = drtec.enableConsole
    val ip = drtec.inputIp
    val port = drtec.inputPort
    val tableName = "state"

    //clear and create checkpoint
    val checkpointFolder = new File(System.getProperty("java.io.tmpdir") + "/CER")
    if(checkpointFolder.isDirectory) FileUtils.deleteQuietly(checkpointFolder)
    ssc.checkpoint(System.getProperty("java.io.tmpdir") + "/CER")
    if(enableConsole) sc.setLogLevel("INFO")
    else sc.setLogLevel("ERROR")

    //initialize ids table
    StreamHandler.initializeIdsState(tableName)

    info("dRTEC INFO: dRTEC execution started")

    //data directory
    val stream = ssc.socketTextStream(ip, port, StorageLevel.MEMORY_AND_DISK)
      .window(Milliseconds(domain.window), Milliseconds(domain.slide))

    //parse input data
    val parsedStream = StreamHandler.parsing(stream)

    //index input data according to one id
    val indexedStream = StreamHandler.indexing(parsedStream)

    //simple event recognition
    val simpleStream = indexedStream.updateStateByKey(StateHandler.windowMechanismSimple)

    //retrieve former ids
    val stateStream = StreamHandler.retrieveFormerState(simpleStream, tableName)

    //pair ids
    val pairedStream = StreamHandler.dynamicGrounding(stateStream)

    //event recognition
    val recognitionStream = pairedStream.updateStateByKey(StateHandler.windowMechanismComplex _,
      scala.math.pow(sc.defaultParallelism, domain.maxNumOfIds).toInt)

    //checkpoint to reduce long lineage
    recognitionStream.checkpoint(Milliseconds(domain.slide * 10))

    //save ids to table
    StreamHandler.saveState(recognitionStream, tableName)

    //print results
    if(mqtt) OutputHandler.sendToMQTT(recognitionStream, mqttConnectionOp.get, enableLog, enableConsole)
    else OutputHandler.printStream(recognitionStream, outputFilePathOp.get, enableLog, enableConsole)

    ssc.start()
  }

  /**
    * Stops engine execution
    */
  private[drtec] def stop(drtec: dRTEC){

    info("dRTEC INFO: Terminating execution...")
    OutputHandler.closeFileStreams()
    OutputHandler.disconnectFromMqtt(drtec.mqttConnectionOp)
    InputHandler.closeSocket()
    if(drtec.streamingContext != null) drtec.streamingContext.stop()
    info("dRTEC INFO: Spark streaming context stopped...")
    info("dRTEC INFO: dRTEC engine is shutting down")
  }
}
