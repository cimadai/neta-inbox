package models

abstract class ValuedObject[T](val value: T)

abstract class AbstractValuedObject[T, U <: ValuedObject[T]] {
  def elements: Iterable[U]
  def valueOf(value: T): Option[U] = elements.find(_.value == value)
  def valueOfOrDefault(value: T, default: U): U = valueOf(value).getOrElse(default)
  def containsValue(value: T): Boolean = elements.map(_.value).toSeq.contains(value)
}
