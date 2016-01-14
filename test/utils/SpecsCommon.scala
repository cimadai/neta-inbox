package utils

import dao._
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import dao.utils.QueryExtensions._
import models._
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable

trait SpecsCommon {

  implicit val dbConfig: DatabaseConfig[JdbcProfile]
  implicit val db: Database

  private def withForeignKeyDisabled(f: => Unit): Unit = {
    try {
      sqlu"SET FOREIGN_KEY_CHECKS=0".runAndAwait
      f
    } finally {
      sqlu"SET FOREIGN_KEY_CHECKS=1".runAndAwait
    }
  }

  protected def truncateDatabases(): Unit = {
    withForeignKeyDisabled {
      DBIO.seq(
        EventReactionDao.truncateDDL(),
        EventTagRelationDao.truncateDDL(),
        UserInfoDao.truncateDDL(),
        EventInfoDao.truncateDDL(),
        EventReactionTypeDao.truncateDDL(),
        EventTagDao.truncateDDL()
      ).runAndAwait
    }
  }

  protected def createTablesIfNeeded(): Unit = {
    if (isFirstLaunch) {
      println("Table creating...")
      DBIO.seq(
        UserInfoDao.createDDL,
        EventInfoDao.createDDL,
        EventReactionTypeDao.createDDL,
        EventReactionDao.createDDL,
        EventTagDao.createDDL,
        EventTagRelationDao.createDDL
      ).runAndAwait
    }
  }

  private def isFirstLaunch: Boolean = {
    val existOrNone = MTable.getTables(UserInfoDao.baseQuery.baseTableRow.tableName).runAndAwait
    existOrNone.isEmpty || existOrNone.get.isEmpty
  }

  private def createUser(email: String, familyName: String, givenName: String, nickName: String, picture: String): UserInfo = {
    val user = UserInfo(None, email, familyName, givenName, familyName + givenName, nickName, picture, "ja")
    val userId = UserInfoDao.create(user).get
    user.copy(id = Some(userId))
  }
  private def createTag(tagName: String): EventTag = {
    val tag = EventTag(None, tagName)
    val tagId = EventTagDao.create(tag).get
    tag.copy(id = Some(tagId))
  }
  private def createEvent(title: String, description: String, userOrNone: Option[UserInfo], tags: Iterable[EventTag]): EventInfo = {
    val event = EventInfo(None, EventType.Require, title, description, userOrNone.flatMap(_.id), publishDateUnixMillis = 0, EventStatus.New, duration = 10)
    val eventId = EventInfoDao.create(event).get
    createEventTagRelation(eventId, tags)
    event.copy(id = Some(eventId))
  }

  private def createEventTagRelation(eventId: Long, tags: Iterable[EventTag]): Iterable[EventTagRelation] = {
    tags.map(tag => {
      val eventRel = EventTagRelation(eventId, tag.id.get)
      EventTagRelationDao.create(eventRel)
      eventRel
    })
  }

  private def createEventReaction(user: UserInfo, ev: EventInfo, reactionTypeId: Long): EventReaction = {
    val reaction = EventReaction(None, user.id.get, ev.id.get, reactionTypeId)
    val reactionId = EventReactionDao.create(reaction)
    reaction.copy(id = reactionId)
  }

  protected def setupData(): Unit = {
    val user0 = createUser("test@example.com", "test", "test", "test", "")

    val tagScala = createTag("Scala")
    val tagJavaScript = createTag("JavaScript")

    val ev0 = createEvent("test", "test", Some(user0), Iterable(tagScala, tagJavaScript))
    val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "test")).get

    createEventReaction(user0, ev0, reactionTypeId)
  }
}
