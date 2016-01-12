package cer.drtec.IO

import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import org.eclipse.paho.client.mqttv3.{MqttClientPersistence, MqttClient}

/**
  * @author Alexandros Mavrommatis
  */
private[drtec] class MQTTClient private[drtec](serverURI: String, clientId: String, persistence: MqttClientPersistence)
  extends MqttClient(serverURI, clientId, persistence) with Serializable{

  private[drtec] def this(serverURI: String, clientId: String) = this(serverURI, clientId, new MqttDefaultFilePersistence())
}
