package cer.drtec.engine

/**
 * @author Alexandros Mavrommatis
 */
case class Fluent(symbol: String, args: Vector[String], values: Vector[String]) extends Serializable{

  override def equals(other: Any): Boolean = other match {
    case that: Fluent =>
      that.symbol == this.symbol && this.args == that.args && this.values == that.values
    case _ => false
  }

  override def toString = "("+symbol+","+args.mkString(";")+","+values.mkString(";")+")"
}
