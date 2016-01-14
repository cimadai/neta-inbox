package utils

import java.util.concurrent.atomic.AtomicReference

import com.flyberrycapital.slack.SlackClient
import dao._
import helpers.{ChatworkConfig, SlackConfig}
import net.cimadai.chatwork.ChatworkClient
import play.api.{Application, GlobalSettings, Logger}

object Global extends GlobalSettings {
  private val chatworkClientRef = new AtomicReference[Option[ChatworkClient]](None)
  def chatworkClient = chatworkClientRef.get()
  private val slackClientRef = new AtomicReference[Option[SlackClient]](None)
  def slackClient = slackClientRef.get()

  override def onStart(app: Application): Unit = {
    Logger.info("Play application is starting.")

    UserInfoDao.createDDL.statements.foreach(println(_))
    EventInfoDao.createDDL.statements.foreach(println(_))
    EventReactionTypeDao.createDDL.statements.foreach(println(_))
    EventReactionDao.createDDL.statements.foreach(println(_))
    EventTagDao.createDDL.statements.foreach(println(_))
    EventTagRelationDao.createDDL.statements.foreach(println(_))
    EventTagRelationDao.createDDL.statements.foreach(println(_))

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
    Logger.info("Play application is stopping.")
  }

}
