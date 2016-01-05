package helpers

import java.util.concurrent.atomic.AtomicReference

import play.api.Play

case class ChatworkConfig(apiKey: String, roomId: String)
object ChatworkConfig {
  private val chatworkConfigRef = new AtomicReference[ChatworkConfig](ChatworkConfig(
    Play.current.configuration.getString("chatwork.api.key").getOrElse(""),
    Play.current.configuration.getString("chatwork.push.room.id").getOrElse("")
  ))

  def get() = chatworkConfigRef.get()
  def set(config: ChatworkConfig) = chatworkConfigRef.set(config)
}
