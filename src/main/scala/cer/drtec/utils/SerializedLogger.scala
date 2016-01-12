package cer.drtec.utils

import auxlib.log.Logger
import org.slf4j.{LoggerFactory, MarkerFactory}
import scala.reflect._

/**
  * @author Alexandros Mavrommatis
  */
object SerializedLogger {

  protected val FATAL_ERROR_MARKER = MarkerFactory.getMarker("FATAL")

  /**
    * Get a new logger where its name is given by: org.slf4j.Logger.ROOT_LOGGER_NAME
    *
    * @return logger instance
    * @see org.slf4j.Logger.ROOT_LOGGER_NAME
    */
  def apply(): SerializedLogger = apply(org.slf4j.Logger.ROOT_LOGGER_NAME)

  /**
    * Get a new logger with the specified name.
    *
    * @param name: the name of the logger
    * @return logger instance
    */
  def apply(name: String): SerializedLogger = new SerializedLogger(LoggerFactory.getLogger(name))

  /**
    * Get a new logger where its name is given by the name of the specified class
    *
    * @param clazz class type
    * @return logger instance
    */
  def apply(clazz: Class[_]): SerializedLogger = this.apply(clazz.getName)

  /**
    * Get a new logger where its name is given by the name of the specified class tag type
    *
    * @tparam T: class tag type
    * @return logger instance
    */
  def apply[T: ClassTag](): SerializedLogger = this.apply(classTag[T].runtimeClass.getName)

}

class SerializedLogger protected(val myInstance: org.slf4j.Logger) extends Logger(myInstance) with Serializable
