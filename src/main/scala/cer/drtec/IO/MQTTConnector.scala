package cer.drtec.IO

import cer.drtec.utils.SerializedLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3._

/**
  * @author Alexandros Mavrommatis
  */
private[drtec] case class MQTTConnector(brokerUrl: String, inputTopic: String, outputTopic: String,
                                        username: Option[String], password: Option[String])
  extends MqttCallback with SerializedLogging with Serializable{

  var client: MQTTClient = null

  private[drtec] def connect() {
    try {
      client = new MQTTClient(brokerUrl, MqttClient.generateClientId(), new MemoryPersistence())

      val properties = new MqttConnectOptions()
      username match {
        case Some(user) => properties.setUserName(user)
        case None =>
      }
      password match {
        case Some(pass) => properties.setPassword(pass.toCharArray)
        case None =>
      }
      client.connect(properties)
      client.setCallback(this)
      client.subscribe(inputTopic)
    }
    catch{
      case e:Throwable =>
        error("trying to reconnect to MQTT broker\nURL -> " + brokerUrl + "...")
        Thread.sleep(5000)
        connect()
    }
  }

  override private[drtec] def connectionLost(cause: Throwable) {
    error("trying to reconnect to MQTT broker...")
    Thread.sleep(5000)
    connect()
  }

  override private[drtec] def messageArrived(topic: String, message: MqttMessage) = {
    val batch = new String(message.getPayload,"utf-8")
    InputHandler.mqttData.offer(batch)
  }

  override private[drtec] def deliveryComplete(token: IMqttDeliveryToken) {}

  private[drtec] def publish(message: String){
    val mqttMessage = new MqttMessage()
    mqttMessage.setPayload(message.getBytes)
    client.publish(outputTopic, mqttMessage)
  }
}

