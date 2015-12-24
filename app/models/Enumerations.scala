package models

sealed abstract class EventType(value: Int) extends ValuedObject(value)
object EventType extends AbstractValuedObject[Int, EventType] {
  case object None extends EventType(1)
  case object Presentation extends EventType(2)
  case object Require extends EventType(3)
  override val elements = Iterable(Presentation, Require)
}

sealed abstract class EventStatus(value: Int) extends ValuedObject(value)
object EventStatus extends AbstractValuedObject[Int, EventStatus] {
  case object New extends EventStatus(1)
  case object Done extends EventStatus(2)
  override val elements = Iterable(New, Done)
}

