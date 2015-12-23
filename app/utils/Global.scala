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
      val user1 = UserInfo(None, "dice.k1984@gmail.com", "嶋田", "大輔", "嶋田大輔", "daisuke-shimada",
        "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg", "ja")
      val user1Id = UserInfoDao.create(user1)
      val user2 = UserInfo(None, "test@gmail.com", "te", "st", "test", "test",
        "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg", "ja")
      val user2Id = UserInfoDao.create(user2)
      val reactionTypeId = EventReactionTypeDao.create(EventReactionType(None, "聞きたい！"))
      val event1 = EventInfo(None, EventType.Require, "10分でわかるUnityゴリゴリ3Dプログラミング",
        "Unityを使った3Dグリングリンなゲームの作り方を10分で解説します。", Some(user1Id), 0, EventStatus.New)
      val event1Id = EventInfoDao.create(event1)
      val event2 = EventInfo(None, EventType.Require, "エンジニアが本気で3分クッキング",
        "てすと。", None, 0, EventStatus.New)
      EventInfoDao.create(event2)
      EventReactionDao.create(EventReaction(None, user1Id, reactionTypeId, event1Id))
      EventReactionDao.create(EventReaction(None, user2Id, reactionTypeId, event1Id))

      val tagScala = EventTagDao.create(EventTag(None, "Scala"))
      val tagJavaScript = EventTagDao.create(EventTag(None, "JavaScript"))
      val tagPlayFramework = EventTagDao.create(EventTag(None, "PlayFramework"))

      EventTagRelationDao.create(EventTagRelation(event1Id, tagScala))
      EventTagRelationDao.create(EventTagRelation(event1Id, tagJavaScript))
      EventTagRelationDao.create(EventTagRelation(event1Id, tagPlayFramework))
    }
    createTables()
    setupData()
  }

  override def onStop(app: Application): Unit = {
    logger.debug("Play application is stopping.")
  }
}
