package utils

import _root_.slick.driver.JdbcProfile
import dao._
import models._
import org.slf4j.LoggerFactory
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Application, GlobalSettings}
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Global extends GlobalSettings {
  private val logger = LoggerFactory.getLogger(getClass)

  override def onStart(app: Application): Unit = {
    logger.debug("Play application is starting.")
    implicit val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)

    def createTables(): Unit = {
      val f0 = dbConfig.db.run(DBIO.seq(
        UserInfoDao.createDDL,
        EventInfoDao.createDDL,
        EventReactionTypeDao.createDDL,
        EventReactionDao.createDDL,
        EventTagDao.createDDL,
        EventTagRelationDao.createDDL
      ))
      Await.result(f0, Duration.Inf)
    }

    def createUser(email: String, familyName: String, givenName: String, nickName: String, picture: String): UserInfo = {
      val user = UserInfo(None, email, familyName, givenName, familyName + givenName, nickName, picture, "ja")
      val userId = UserInfoDao.create(user).get
      user.copy(id = Some(userId))
    }
    def createTag(tagName: String): EventTag = {
      val tag = EventTag(None, tagName)
      val tagId = EventTagDao.create(tag).get
      tag.copy(id = Some(tagId))
    }
    def createEvent(title: String, description: String, userOrNone: Option[UserInfo], tags: Iterable[EventTag]): EventInfo = {
      val event = EventInfo(None, EventType.Require, title, description, userOrNone.flatMap(_.id), 0, EventStatus.New)
      val eventId = EventInfoDao.create(event).get

      tags.foreach(tag => {
        EventTagRelationDao.create(EventTagRelation(eventId, tag.id.get))
      })
      event.copy(id = Some(eventId))
    }

    def createEventReaction(user: UserInfo, ev: EventInfo, reactionTypeId: Long): Unit = {
      EventReactionDao.create(EventReaction(None, user.id.get, ev.id.get, reactionTypeId))
    }

    def setupData(): Unit = {
      val user1 = createUser("dice.k1984@gmail.com", "嶋田", "大輔", "daisuke-shimada", "https://lh5.googleusercontent.com/-t_SSgdNb9LM/AAAAAAAAAAI/AAAAAAAAAAA/5sUlkQ-eH6g/photo.jpg")
      val user2 = createUser("test@gmail.com", "te", "st", "test", "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg")

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

      val ev1 = createEvent("10分でわかるUnityゴリゴリ3Dプログラミング", "Unityを使った3Dグリングリンなゲームの作り方を10分で解説します。", Some(user2), Iterable(tagUnity, tagScala, tagJavaScript, tagPlayFramework))
      val ev2 = createEvent("エンジニアが本気でやる3分クッキング", "てすと。", None, Iterable(tagScala))
      val ev3 = createEvent("ネタ募集箱を支える技術", "このネタ募集箱に使われている技術をご紹介します。", Some(user1), Iterable(tagScala, tagJavaScript, tagPlayFramework, tagTypeScript, tagScss, tagIntellij))
      val ev4 = createEvent("UnityとVuforiaではじめるAR", "", Some(user2), Iterable(tagUnity, tagCsharp))
      val ev5 = createEvent("Unityもくもく会", "", None, Iterable(tagUnity, tagCsharp))
      val ev6 = createEvent("Go conference in Altplus", "", None, Iterable(tagGo))
      val ev7 = createEvent("Unity for expert", "", Some(user2), Iterable(tagUnity, tagCsharp))
      val ev8 = createEvent("Unity for beginner", "", Some(user2), Iterable(tagUnity))
      val ev9 = createEvent("はじめてのLaravel", "", Some(user2), Iterable(tagPhp, tagLaravel))
      val ev10 = createEvent("実践 Laravel", "", Some(user2), Iterable(tagPhp, tagLaravel))
      val ev11 = createEvent("はすのは・やってみよう", "", Some(user2), Iterable(tagPhp, tagHasunoha))

      val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "聞きたい！")).get

      createEventReaction(user1, ev1, reactionTypeId)
      createEventReaction(user2, ev1, reactionTypeId)

      createEventReaction(user1, ev3, reactionTypeId)
      createEventReaction(user2, ev3, reactionTypeId)

      createEventReaction(user1, ev5, reactionTypeId)
    }
    createTables()
    setupData()
  }

  override def onStop(app: Application): Unit = {
    logger.debug("Play application is stopping.")
  }
}
