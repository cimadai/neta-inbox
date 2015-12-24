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

    def setupData(): Unit = {
      val user1 = UserInfo(None, "dice.k1984@gmail.com", "嶋田", "大輔", "嶋田大輔", "daisuke-shimada", "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg", "ja")
      val user1Id = UserInfoDao.create(user1).get

      val user2 = UserInfo(None, "test@gmail.com", "te", "st", "test", "test", "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg", "ja")
      val user2Id = UserInfoDao.create(user2).get

      val event1 = EventInfo(None, EventType.Require, "10分でわかるUnityゴリゴリ3Dプログラミング", "Unityを使った3Dグリングリンなゲームの作り方を10分で解説します。", Some(user2Id), 0, EventStatus.New)
      val event1Id = EventInfoDao.create(event1).get

      val event2 = EventInfo(None, EventType.Require, "エンジニアが本気でやる3分クッキング", "てすと。", None, 0, EventStatus.New)
      val event2Id = EventInfoDao.create(event2).get

      val event3 = EventInfo(None, EventType.Require, "ネタ募集箱を支える技術", "このネタ募集箱に使われている技術をご紹介します。", Some(user1Id), 0, EventStatus.New)
      val event3Id = EventInfoDao.create(event3).get

      val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "聞きたい！")).get

      val tagScala = EventTagDao.create(EventTag(None, "Scala")).get
      val tagJavaScript = EventTagDao.create(EventTag(None, "JavaScript")).get
      val tagPlayFramework = EventTagDao.create(EventTag(None, "PlayFramework")).get
      val tagTypeScript = EventTagDao.create(EventTag(None, "TypeScript")).get
      val tagScss = EventTagDao.create(EventTag(None, "SCSS")).get
      val tagIntellij = EventTagDao.create(EventTag(None, "Intellij IDEA")).get

      EventReactionDao.create(EventReaction(None, user1Id, event1Id, reactionTypeId))
      EventReactionDao.create(EventReaction(None, user2Id, event1Id, reactionTypeId))

      EventReactionDao.create(EventReaction(None, user1Id, event3Id, reactionTypeId))
      EventReactionDao.create(EventReaction(None, user2Id, event3Id, reactionTypeId))

      EventTagRelationDao.create(EventTagRelation(event1Id, tagScala))
      EventTagRelationDao.create(EventTagRelation(event1Id, tagJavaScript))
      EventTagRelationDao.create(EventTagRelation(event1Id, tagPlayFramework))

      EventTagRelationDao.create(EventTagRelation(event3Id, tagScala))
      EventTagRelationDao.create(EventTagRelation(event3Id, tagJavaScript))
      EventTagRelationDao.create(EventTagRelation(event3Id, tagPlayFramework))
      EventTagRelationDao.create(EventTagRelation(event3Id, tagTypeScript))
      EventTagRelationDao.create(EventTagRelation(event3Id, tagScss))
      EventTagRelationDao.create(EventTagRelation(event3Id, tagIntellij))

    }
    createTables()
    setupData()
  }

  override def onStop(app: Application): Unit = {
    logger.debug("Play application is stopping.")
  }
}
