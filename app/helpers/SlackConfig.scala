package helpers

import java.util.concurrent.atomic.AtomicReference

import play.api.Play

case class SlackConfig(apiToken: String, channelName: String)
object SlackConfig {
  private val slackConfigRef = new AtomicReference[SlackConfig](SlackConfig(
    Play.current.configuration.getString("slack.api.token").getOrElse(""),
    Play.current.configuration.getString("slack.channel.name").getOrElse("")
  ))

  def get() = slackConfigRef.get()
  def set(config: SlackConfig) = slackConfigRef.set(config)
}
