package cer.drtec.utils

import auxlib.log.Logging

/**
  * @author Alexandros Mavrommatis
  */
trait SerializedLogging extends Logging{

  override protected lazy val loggerInstance: SerializedLogger = SerializedLogger(getClass.getName)
}
