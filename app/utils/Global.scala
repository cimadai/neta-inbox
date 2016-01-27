package utils

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Props, ActorSystem}
import com.flyberrycapital.slack.SlackClient
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import helpers.{ChatworkConfig, SlackConfig}
import job.EventStatusChangeActor
import net.cimadai.chatwork.ChatworkClient
import play.api.{Play, Application, GlobalSettings, Logger}

object Global extends GlobalSettings {

  val system = ActorSystem("NetaInboxSystem")
  val actor = system.actorOf(Props(classOf[EventStatusChangeActor]))

  private val chatworkClientRef = new AtomicReference[Option[ChatworkClient]](None)
  def chatworkClient = chatworkClientRef.get()
  private val slackClientRef = new AtomicReference[Option[SlackClient]](None)
  def slackClient = slackClientRef.get()
  private val loginPermittedDomainRef = new AtomicReference[Option[String]](None)
  def loginPermittedDomain = loginPermittedDomainRef.get()

  override def onStart(app: Application): Unit = {
    Logger.info("Play application is starting.")

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

    loginPermittedDomainRef.set(Play.current.configuration.getString("login.permitted.domain"))

    if (!Play.isTest(app)) {
      // どのスケジュールを実行するか、どのアクターにメッセージを送信するか、どういうメッセージを送信するかを指定する
      QuartzSchedulerExtension(system).schedule("EventStatusChangeActor", actor, "")
    }
  }

  override def onStop(app: Application): Unit = {
    if (!Play.isTest(app)) {
      system.shutdown()
    }
    Logger.info("Play application is stopping.")
  }

}
