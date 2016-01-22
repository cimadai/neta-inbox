package helpers

import java.util.concurrent.atomic.AtomicReference

import play.api.Play

case class Auth0Config(secret: String, clientId: String, callbackURL: String, domain: String) {
  def baseURL = callbackURL.replace("/callback", "")
}
object Auth0Config {
  private val auth0configRef = new AtomicReference[Auth0Config](new Auth0Config(
          Play.current.configuration.getString("auth0.clientSecret").get,
          Play.current.configuration.getString("auth0.clientId").get,
          Play.current.configuration.getString("auth0.callbackURL").get,
          Play.current.configuration.getString("auth0.domain").get
    ))

  def get() = auth0configRef.get()
  def set(config: Auth0Config) = auth0configRef.set(config)
}
