package models

import dao.model.DatabaseObjectWithId

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
  status: EventStatus
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

// for view
case class EventReactionUserAndReactionType(userInfoOrNone: Option[UserInfo], eventReactionTypeOrNone: Option[EventReactionType])
case class EventInfoWithReaction(eventInfo: EventInfo, authorOrNoe: Option[UserInfo], reactions: Iterable[EventReactionUserAndReactionType])

