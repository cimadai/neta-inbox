package utils

import java.util.concurrent.atomic.AtomicReference

import _root_.slick.driver.JdbcProfile
import dao._
import helpers.ChatworkConfig
import models._
import net.cimadai.chatwork.ChatworkClient
import org.slf4j.LoggerFactory
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Application, GlobalSettings}
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Global extends GlobalSettings {
  private val logger = LoggerFactory.getLogger(getClass)
  private val chatworkClientRef = new AtomicReference[Option[ChatworkClient]](None)
  def chatworkClient = chatworkClientRef.get()

  override def onStart(app: Application): Unit = {
    logger.debug("Play application is starting.")
    implicit val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
    if (isFirstLaunch) {
      createTables
      setupData
    }

    val config = ChatworkConfig.get()
    if (config.apiKey.nonEmpty) {
      chatworkClientRef.set(Some(new ChatworkClient(config.apiKey)))
    }
  }

  override def onStop(app: Application): Unit = {
    logger.debug("Play application is stopping.")
  }

  private def createTables(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    val f0 = acc.db.run(DBIO.seq(
      UserInfoDao.createDDL,
      EventInfoDao.createDDL,
      EventReactionTypeDao.createDDL,
      EventReactionDao.createDDL,
      EventTagDao.createDDL,
      EventTagRelationDao.createDDL
    ))
    Await.result(f0, Duration.Inf)
  }

  private def createUser(email: String, familyName: String, givenName: String, nickName: String, picture: String)(implicit acc: DatabaseConfig[JdbcProfile]): UserInfo = {
    val user = UserInfo(None, email, familyName, givenName, familyName + givenName, nickName, picture, "ja")
    val userId = UserInfoDao.create(user).get
    user.copy(id = Some(userId))
  }
  private def createTag(tagName: String)(implicit acc: DatabaseConfig[JdbcProfile]): EventTag = {
    val tag = EventTag(None, tagName)
    val tagId = EventTagDao.create(tag).get
    tag.copy(id = Some(tagId))
  }
  private def createEvent(title: String, description: String, userOrNone: Option[UserInfo], tags: Iterable[EventTag])(implicit acc: DatabaseConfig[JdbcProfile]): EventInfo = {
    val event = EventInfo(None, EventType.Require, title, description, userOrNone.flatMap(_.id), 0, EventStatus.New)
    val eventId = EventInfoDao.create(event).get

    tags.foreach(tag => {
      EventTagRelationDao.create(EventTagRelation(eventId, tag.id.get))
    })
    event.copy(id = Some(eventId))
  }

  private def createEventReaction(user: UserInfo, ev: EventInfo, reactionTypeId: Long)(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    EventReactionDao.create(EventReaction(None, user.id.get, ev.id.get, reactionTypeId))
  }

  private def isFirstLaunch(implicit acc: DatabaseConfig[JdbcProfile]): Boolean = {
    val action = MTable.getTables("USER_INFOS")
    val f = acc.db.run(action)
    Await.result(f, Duration.Inf).isEmpty
  }

  private def setupData(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    val user1 = createUser("dice.k1984@gmail.com", "嶋田", "大輔", "daisuke-shimada", "https://lh5.googleusercontent.com/-t_SSgdNb9LM/AAAAAAAAAAI/AAAAAAAAAAA/5sUlkQ-eH6g/photo.jpg")

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

    val ev3 = createEvent("ネタ募集箱を支える技術", "このネタ募集箱に使われている技術をご紹介します。", Some(user1), Iterable(tagScala, tagJavaScript, tagPlayFramework, tagTypeScript, tagScss, tagIntellij))
    val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "聞きたい！")).get
  }
}
