package dao

import dao.DatabaseAccessor.jdbcProfile.api._
import dao.utils.TableWithId
import models._
import slick.lifted.TableQuery

/**
 * DAOのとりまとめ
 */
trait DaoBase {
  val userInfoQuery = DaoBase.userInfoQuery
  val eventInfoQuery = DaoBase.eventInfoQuery
  val eventReactionQuery = DaoBase.eventReactionQuery
  val eventReactionTypeQuery = DaoBase.eventReactionTypeQuery
}

object DaoBase {
  /** ==================================================== */
  protected val userInfoQuery = TableQuery[UserInfoTable]
  class UserInfoTable(tag: Tag) extends TableWithId[UserInfo](tag, "USER_INFOS") {
    def email = column[String]("EMAIL")
    def familyName = column[String]("FAMILY_NAME")
    def givenName = column[String]("GIVEN_NAME")
    def fullName = column[String]("FULL_NAME")
    def nickname = column[String]("NICKNAME")
    def picture = column[String]("PICTURE")
    def locale = column[String]("LOCALE")

    def idxEmail = index(s"${tableName}_EMAIL", email, unique = true)

    def * = (id.?, email, familyName, givenName, fullName, nickname, picture, locale) <> (UserInfo.tupled, UserInfo.unapply)
  }

  /** ==================================================== */
  implicit val eventTypeColumnMap = MappedColumnType.base[EventType, Int](_.value, EventType.valueOf(_).getOrElse(EventType.None) )
  implicit val statusColumnMap = MappedColumnType.base[EventStatus, Int](_.value, EventStatus.valueOf(_).getOrElse(EventStatus.New) )
  protected val eventInfoQuery = TableQuery[EventInfoTable]
  class EventInfoTable(tag: Tag) extends TableWithId[EventInfo](tag, "EVENT_INFOS") {
    def eventType = column[EventType]("EVENT_TYPE")
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION")
    def userInfoIdOrNone = column[Option[Long]]("USER_INFO_ID_OR_NONE")
    def publishDateUnixMillis = column[Long]("PUBLISH_DATE_UNIX_MILLIS")
    def status = column[EventStatus]("STATUS")

    def idxTitle = index(s"${tableName}_TITLE", title, unique = false)

    def * = (id.?, eventType, title, description, userInfoIdOrNone, publishDateUnixMillis, status) <> (EventInfo.tupled, EventInfo.unapply)
  }

  /** ==================================================== */
  protected val eventReactionTypeQuery = TableQuery[EventReactionTypeTable]
  class EventReactionTypeTable(tag: Tag) extends TableWithId[EventReactionType](tag, "EVENT_REACTION_TYPES") {
    def text = column[String]("TEXT")

    def * = (id.?, text) <> (EventReactionType.tupled, EventReactionType.unapply)
  }

  /** ==================================================== */
  protected val eventReactionQuery = TableQuery[EventReactionTable]
  class EventReactionTable(tag: Tag) extends TableWithId[EventReaction](tag, "EVENT_REACTIONS") {
    def userInfoId = column[Long]("USER_INFO_ID")
    def eventInfoId = column[Long]("EVENT_INFO_ID")
    def eventReactionTypeId = column[Long]("EVENT_REACTION_TYPE_ID")

    def userInfo = foreignKey(s"${tableName}_USER_INFO", userInfoId, userInfoQuery)(_.id)
    def eventInfo = foreignKey(s"${tableName}_EVENT_INFO", eventInfoId, eventInfoQuery)(_.id)
    def eventReactionType = foreignKey(s"${tableName}_EVENT_REACTION_TYPE", eventReactionTypeId, eventReactionTypeQuery)(_.id)

    def idxUserInfoEventInfoEventReactionType = index(s"${tableName}_USER_INFO-EVENT_INFO-EVENT_REACTION_TYPE",
      (userInfoId, eventInfoId, eventReactionTypeId), unique = true)

    def * = (id.?, userInfoId, eventInfoId, eventReactionTypeId) <> (EventReaction.tupled, EventReaction.unapply)
  }
}


