package com.rallyhealth.weejson.v0



import com.rallyhealth.weepickle.v0.core.Util
import com.rallyhealth.weepickle.v0.core.Visitor

import scala.collection.compat._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

sealed trait Value extends Readable {
  def value: Any

  /**
    * Returns the `String` value of this [[Value]], fails if it is not
    * a [[Str]]
    */
  def str: String = this match{
    case Str(value) => value
    case _ => throw Value.InvalidData(this, "Expected com.rallyhealth.weejson.v0.Str")
  }

  /**
    * Returns an Optional `String` value of this [[Value]] in case this [[Value]] is a 'String'.
    */
  def strOpt: Option[String] = this match{
    case Str(value) => Some(value)
    case _ => None
  }

  /**
    * Returns the key/value map of this [[Value]], fails if it is not
    * a [[Obj]]
    */
  def obj: mutable.Map[String, Value] = this match{
    case Obj(value) => value
    case _ => throw Value.InvalidData(this, "Expected com.rallyhealth.weejson.v0.Obj")
  }
  /**
    * Returns an Optional key/value map of this [[Value]] in case this [[Value]] is a 'Obj'.
    */
  def objOpt: Option[mutable.Map[String, Value]] = this match{
    case Obj(value) => Some(value)
    case _ => None
  }
  /**
    * Returns the elements of this [[Value]], fails if it is not
    * a [[Arr]]
    */
  def arr: ArrayBuffer[Value] = this match{
    case Arr(value) => value
    case _ => throw Value.InvalidData(this, "Expected com.rallyhealth.weejson.v0.Arr")
  }
  /**
    * Returns The optional elements of this [[Value]] in case this [[Value]] is a 'Arr'.
    */
  def arrOpt: Option[ArrayBuffer[Value]] = this match{
    case Arr(value) => Some(value)
    case _ => None
  }
  /**
    * Returns the `Double` value of this [[Value]], fails if it is not
    * a [[Num]]
    */
  def num: Double = this match{
    case Num(value) => value
    case _ => throw Value.InvalidData(this, "Expected com.rallyhealth.weejson.v0.Num")
  }
  /**
    * Returns an Option[Double] in case this [[Value]] is a 'Num'.
    */
  def numOpt: Option[Double] = this match{
    case Num(value) => Some(value)
    case _ => None
  }
  /**
    * Returns the `Boolean` value of this [[Value]], fails if it is not
    * a [[Bool]]
    */
  def bool = this match{
    case Bool(value) => value
    case _ => throw Value.InvalidData(this, "Expected com.rallyhealth.weejson.v0.Bool")
  }
  /**
    * Returns an Optional `Boolean` value of this [[Value]] in case this [[Value]] is a 'Bool'.
    */
  def boolOpt: Option[Boolean] = this match{
    case Bool(value) => Some(value)
    case _ => None
  }
  /**
    * Returns true if the value of this [[Value]] is com.rallyhealth.weejson.v0.Null, false otherwise
    */
  def isNull: Boolean = this match {
    case Null => true
    case _ => false
  }

  def apply(s: Value.Selector): Value = s(this)
  def update(s: Value.Selector, v: Value): Unit = s(this) = v

  /**
    * Update a value in-place. Takes an `Int` or a `String`, through the
    * implicitly-constructe [[Value.Selector]] type.
    *
    * We cannot just overload `update` on `s: Int` and `s: String` because
    * of type inference problems in Scala 2.11.
    */
  def update(s: Value.Selector, f: Value => Value): Unit = s(this) = f(s(this))

  def transform[T](f: Visitor[_, T]): T = Value.transform(this, f)
  override def toString = render()
  def render(indent: Int = -1, escapeUnicode: Boolean = false): String = this.transform(StringRenderer(indent, escapeUnicode)).toString
}

/**
* A very small, very simple JSON AST that weepickle uses as part of its
* serialization process. A common standard between the Jawn AST (which
* we don't use so we don't pull in the bulk of Spire) and the Javascript
* JSON AST.
*/
object Value extends AstTransformer[Value]{
  type Value = com.rallyhealth.weejson.v0.Value
  sealed trait Selector{
    def apply(x: Value): Value
    def update(x: Value, y: Value): Unit
  }
  object Selector{
    implicit class IntSelector(i: Int) extends Selector{
      def apply(x: Value): Value = x.arr(i)
      def update(x: Value, y: Value) = x.arr(i) = y
    }
    implicit class StringSelector(i: String) extends Selector{
      def apply(x: Value): Value = x.obj(i)
      def update(x: Value, y: Value) = x.obj(i) = y
    }
  }

  implicit def JsonableSeq[T](items: TraversableOnce[T])
                             (implicit f: T => Value): Arr = Arr.from(items.map(f))
  implicit def JsonableDict[T](items: TraversableOnce[(String, T)])
                              (implicit f: T => Value): Obj = Obj.from(items.map(x => (x._1, f(x._2))))
  implicit def JsonableBoolean(i: Boolean): Bool = if (i) True else False
  implicit def JsonableByte(i: Byte): Num = Num(i)
  implicit def JsonableShort(i: Short): Num = Num(i)
  implicit def JsonableInt(i: Int): Num = Num(i)
  implicit def JsonableLong(i: Long): Str = Str(i.toString)
  implicit def JsonableFloat(i: Float): Num = Num(i)
  implicit def JsonableDouble(i: Double): Num = Num(i)
  implicit def JsonableNull(i: Null): Null.type = Null
  implicit def JsonableString(s: CharSequence): Str = Str(s.toString)


  def transform[T](j: Value, f: Visitor[_, T]): T = {
    j match{
      case Null => f.visitNull(-1)
      case True => f.visitTrue(-1)
      case False => f.visitFalse(-1)
      case Str(s) => f.visitString(s, -1)
      case Num(d) => f.visitFloat64(d, -1)
      case Arr(items) => transformArray(f, items)
      case Obj(items) => transformObject(f, items)
    }
  }

  def visitArray(length: Int, index: Int) = new AstArrVisitor[ArrayBuffer](xs => Arr(xs))

  def visitObject(length: Int, index: Int) = new AstObjVisitor[mutable.LinkedHashMap[String, Value]](xs => Obj(xs))

  def visitNull(index: Int) = Null

  def visitFalse(index: Int) = False

  def visitTrue(index: Int) = True


  override def visitFloat64StringParts(s: CharSequence, decIndex: Int, expIndex: Int, index: Int): Num = {
    Num(
      if (decIndex != -1 || expIndex != -1) s.toString.toDouble
      else Util.parseIntegralNum(s, decIndex, expIndex, index)
    )
  }

  override def visitFloat64(d: Double, index: Int): Num = Num(d)

  def visitString(s: CharSequence, index: Int) = Str(s.toString)

  /**
    * Thrown when weepickle tries to convert a JSON blob into a given data
    * structure but fails because part the blob is invalid
    *
    * @param data The section of the JSON blob that weepickle tried to convert.
    *             This could be the entire blob, or it could be some subtree.
    * @param msg Human-readable text saying what went wrong
    */
  case class InvalidData(data: Value, msg: String)
    extends Exception(s"$msg (data: $data)")
}

case class Str(value: String) extends Value
case class Obj(value: mutable.Map[String, Value]) extends Value

object Obj{
  implicit def from(items: TraversableOnce[(String, Value)]): Obj = {
    Obj(mutable.LinkedHashMap(items.toSeq:_*))
  }
  // Weird telescoped version of `apply(items: (String, Value)*)`, to avoid
  // type inference issues due to overloading the existing `apply` method
  // generated by the case class itself
  // https://github.com/lihaoyi/upickle/issues/230
  def apply[V <% Value](item: (String, V),
                        items: (String, Value)*): Obj = {
    val map = new mutable.LinkedHashMap[String, Value]()
    map.put(item._1, item._2)
    for (i <- items) map.put(i._1, i._2)
    Obj(map)
  }

  def apply(): Obj = Obj(new mutable.LinkedHashMap[String, Value]())
}
case class Arr(value: ArrayBuffer[Value]) extends Value

object Arr{
  implicit def from[T <% Value](items: TraversableOnce[T]): Arr =
    Arr(items.map(x => x: Value).to(mutable.ArrayBuffer))

  def apply(items: Value*): Arr = new Arr(items.to(mutable.ArrayBuffer))
}
case class Num(value: Double) extends Value
sealed abstract class Bool extends Value{
  def value: Boolean
}
object Bool{
  def apply(value: Boolean): Bool = if (value) True else False
  def unapply(bool: Bool): Option[Boolean] = Some(bool.value)
}
case object False extends Bool{
  def value = false
}
case object True extends Bool{
  def value = true
}
case object Null extends Value{
  def value = null
}
