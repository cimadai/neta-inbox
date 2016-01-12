package utils

import java.util.concurrent.atomic.AtomicReference

import slick.driver.JdbcProfile
import com.flyberrycapital.slack.SlackClient
import dao._
import dao.utils.QueryExtensions._
import helpers.{ChatworkConfig, SlackConfig}
import net.cimadai.chatwork.ChatworkClient
import org.slf4j.LoggerFactory
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Application, GlobalSettings}
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.meta.MTable

object Global extends GlobalSettings {
  private val logger = LoggerFactory.getLogger(getClass)
  private val chatworkClientRef = new AtomicReference[Option[ChatworkClient]](None)
  def chatworkClient = chatworkClientRef.get()
  private val slackClientRef = new AtomicReference[Option[SlackClient]](None)
  def slackClient = slackClientRef.get()

  override def onStart(app: Application): Unit = {
    logger.debug("Play application is starting.")
    implicit val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
    if (isFirstLaunch) {
      createTables
    }

    val config = ChatworkConfig.get()
    if (config.apiKey.nonEmpty) {
      chatworkClientRef.set(Some(new ChatworkClient(config.apiKey)))
    }

    val slackConfig = SlackConfig.get()
    if (slackConfig.apiToken.nonEmpty) {
      val slack = new SlackClient(slackConfig.apiToken)
      slack.connTimeout(5000)
      slack.readTimeout(5000)
      slackClientRef.set(Some(slack))
    }
  }

  override def onStop(app: Application): Unit = {
    logger.debug("Play application is stopping.")
  }

  private def createTables(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    implicit val database = acc.db
    DBIO.seq(
      UserInfoDao.createDDL,
      EventInfoDao.createDDL,
      EventReactionTypeDao.createDDL,
      EventReactionDao.createDDL,
      EventTagDao.createDDL,
      EventTagRelationDao.createDDL
    ).runAndAwait
  }

  private def isFirstLaunch(implicit acc: DatabaseConfig[JdbcProfile]): Boolean = {
    implicit val database = acc.db
    val existOrNone = MTable.getTables(UserInfoDao.baseQuery.baseTableRow.tableName).runAndAwait
    existOrNone.isEmpty || existOrNone.get.isEmpty
  }
}
