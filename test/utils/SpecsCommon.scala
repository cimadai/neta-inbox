package utils

import dao._
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import dao.utils.QueryExtensions._
import models._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, Suite}
import play.api.Application
import play.api.test.FakeRequest
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable

trait SpecsCommon extends BeforeAndAfter with BeforeAndAfterEach {
  self: Suite =>

  implicit val app: Application
  implicit val dbConfig: DatabaseConfig[JdbcProfile]
  implicit val db: Database

  before {
    SpecsCommon.createTablesIfNeeded()
    SpecsCommon.truncateDatabases()
    SpecsCommon.setupData()
  }

  override def beforeEach(): Unit = {
    FakeLoginController.logout.apply(FakeRequest())
  }

}

object SpecsCommon {
  private def withForeignKeyDisabled(f: => Unit)(implicit db: Database): Unit = {
    try {
      sqlu"SET FOREIGN_KEY_CHECKS=0".runAndAwait
      f
    } finally {
      sqlu"SET FOREIGN_KEY_CHECKS=1".runAndAwait
    }
  }

  def truncateDatabases()(implicit db: Database): Unit = {
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

  def createTablesIfNeeded()(implicit db: Database): Unit = {
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

  private def isFirstLaunch(implicit db: Database): Boolean = {
    val existOrNone = MTable.getTables(UserInfoDao.baseQuery.baseTableRow.tableName).runAndAwait
    existOrNone.isEmpty || existOrNone.get.isEmpty
  }

  private def createUser(email: String, familyName: String, givenName: String, nickName: String, picture: String)(implicit dbConfig: DatabaseConfig[JdbcProfile]): UserInfo = {
    val user = UserInfo(None, email, familyName, givenName, familyName + givenName, nickName, picture, "ja")
    val userId = UserInfoDao.create(user).get
    user.copy(id = Some(userId))
  }
  private def createTag(tagName: String)(implicit dbConfig: DatabaseConfig[JdbcProfile]): EventTag = {
    val tag = EventTag(None, tagName)
    val tagId = EventTagDao.create(tag).get
    tag.copy(id = Some(tagId))
  }
  private def createEvent(title: String, description: String, userOrNone: Option[UserInfo], tags: Iterable[EventTag])(implicit dbConfig: DatabaseConfig[JdbcProfile]): EventInfo = {
    val event = EventInfo(None, EventType.Require, title, description, userOrNone.flatMap(_.id), publishDateUnixMillis = 0, EventStatus.New.value, duration = 10)
    val eventId = EventInfoDao.create(event).get
    createEventTagRelation(eventId, tags)
    event.copy(id = Some(eventId))
  }

  private def createEventTagRelation(eventId: Long, tags: Iterable[EventTag])(implicit dbConfig: DatabaseConfig[JdbcProfile]): Iterable[EventTagRelation] = {
    tags.map(tag => {
      val eventRel = EventTagRelation(eventId, tag.id.get)
      EventTagRelationDao.create(eventRel)
      eventRel
    })
  }

  private def createEventReaction(user: UserInfo, ev: EventInfo, reactionTypeId: Long)(implicit dbConfig: DatabaseConfig[JdbcProfile]): EventReaction = {
    val reaction = EventReaction(None, user.id.get, ev.id.get, reactionTypeId)
    val reactionId = EventReactionDao.create(reaction)
    reaction.copy(id = reactionId)
  }

  def setupData()(implicit dbConfig: DatabaseConfig[JdbcProfile]): Unit = {
    val user0 = createUser("test0@example.com", "test0", "test0", "test0", "")
    val user1 = createUser("test1@example.com", "test1", "test1", "test1", "")

    val tagScala = createTag("Scala")
    val tagJavaScript = createTag("JavaScript")

    val ev0 = createEvent("test", "test", Some(user0), Iterable(tagScala, tagJavaScript))
    val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "test")).get

    createEventReaction(user0, ev0, reactionTypeId)
  }

  def getFirstUser(implicit dbConfig: DatabaseConfig[JdbcProfile]) = UserInfoDao.findFirstByFilter(_.id > 0L).get
  def getUserByNickName(name: String)(implicit dbConfig: DatabaseConfig[JdbcProfile]) = UserInfoDao.findFirstByFilter(_.nickname === name).get

  def getFirstEvent(implicit dbConfig: DatabaseConfig[JdbcProfile]) = EventInfoDao.findFirstByFilter(_.id > 0L).get

  def getFirstReactionType(implicit dbConfig: DatabaseConfig[JdbcProfile]) = EventReactionTypeDao.findFirstByFilter(_.id > 0L).get
}
