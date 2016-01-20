package models

case class UserInfo(
  override val id: Option[Long],
  email: String,
  familyName: String,
  givenName: String,
  fullName: String,
  nickname: String,
  picture: String,
  locale: String
) extends DatabaseObjectWithId(id)

case class EventInfo(
  override val id: Option[Long],
  eventType: EventType,
  title: String,
  description: String,
  authorIdOrNone: Option[Long],
  publishDateUnixMillis: Long,
  status: EventStatus,
  duration: Long
) extends DatabaseObjectWithId(id)

case class EventReactionType(
  override val id: Option[Long],
  text: String
) extends DatabaseObjectWithId(id)

case class EventReaction(
  override val id: Option[Long],
  userInfoId: Long,
  eventInfoId: Long,
  eventReactionTypeId: Long
) extends DatabaseObjectWithId(id)

case class EventTag(
  override val id: Option[Long],
  text: String
) extends DatabaseObjectWithId(id)

case class EventTagRelation(
  eventInfoId: Long,
  eventTagId: Long
)

// for view
case class EventReactionUserAndReactionType(userInfoOrNone: Option[UserInfo], eventReactionTypeOrNone: Option[EventReactionType])
case class EventInfoWithReaction(eventInfo: EventInfo, authorOrNone: Option[UserInfo], reactions: Iterable[EventReactionUserAndReactionType], tags: Iterable[EventTag])
case class EventInfoForForm(
  id: Option[Long],
  eventType: EventType,
  title: String,
  description: String,
  authorIdOrNone: Option[Long],
  publishDateUnixMillis: Long,
  status: EventStatus,
  duration: Long,
  tags: List[Long],
  registerMe: Boolean
) {
  def toEventInfo: EventInfo = {
    EventInfo(this.id, this.eventType, this.title, this.description, this.authorIdOrNone, this.publishDateUnixMillis, this.status, this.duration)
  }
}
object EventInfoForForm {
  def apply(ev: EventInfo)(implicit userInfo: UserInfo): EventInfoForForm = {
    val registerMe = ev.authorIdOrNone match {
      case Some(authorId) =>
        userInfo.id match {
          case Some(userId) => authorId == userId
          case _ => false
        }
      case _ => false
    }
    EventInfoForForm(ev.id, ev.eventType, ev.title, ev.description, ev.authorIdOrNone, ev.publishDateUnixMillis, ev.status, ev.duration, List.empty, registerMe)
  }
}
case class EventTagGroups(tag: EventTag, eventCount: Int)
