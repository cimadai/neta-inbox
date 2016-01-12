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
    MTable.getTables("USER_INFOS").runAndAwait.isEmpty
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
    val event = EventInfo(None, EventType.Require, title, description, userOrNone.flatMap(_.id), 0, EventStatus.New)
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
    val user0 = createUser("dice.k1984@gmail.com", "嶋田", "大輔", "daisuke-shimada", "https://lh5.googleusercontent.com/-t_SSgdNb9LM/AAAAAAAAAAI/AAAAAAAAAAA/5sUlkQ-eH6g/photo.jpg")

    val tagScala = createTag("Scala")
    val tagJavaScript = createTag("JavaScript")
    val tagPlayFramework = createTag("PlayFramework")
    val tagTypeScript = createTag("TypeScript")
    val tagScss = createTag("SCSS")
    val tagIntellij = createTag("Intellij IDEA")
    val tagUnity = createTag("Unity")
    val tagCsharp = createTag("C#")
    val tagGo = createTag("Go")
    val tagPhp = createTag("PHP")
    val tagLaravel = createTag("Laravel")
    val tagHasunoha = createTag("Hasunoha")

    val ev0 = createEvent("ネタ募集箱を支える技術", "このネタ募集箱に使われている技術をご紹介します。", Some(user0), Iterable(tagScala, tagJavaScript, tagPlayFramework, tagTypeScript, tagScss, tagIntellij))
    val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "聞きたい！")).get

    createEventReaction(user0, ev0, reactionTypeId)
  }
}
