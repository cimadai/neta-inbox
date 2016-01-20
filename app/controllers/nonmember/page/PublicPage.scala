package controllers.nonmember.page

import controllers.utils.AuthenticateUtil
import helpers.Auth0Config
import jsmessages.JsMessagesFactory
import play.api.Play
import play.api.mvc.Action
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.cache.Cache
import utils.Global

object PublicPage extends AuthenticateUtil {

  private val jsMessagesFactory = new JsMessagesFactory(applicationMessagesApi)
  private val allJsMessages = jsMessagesFactory.all

  def messages = Action { implicit request =>
    Ok(allJsMessages(Some("window.i18n")))
  }

  def index = Action { implicit request =>
    getUserInfoOrNone match {
      case Some(userInfo) =>
        Redirect(controllers.member.page.routes.EventPage.listAll())
      case _ =>

        val authConfig = if (Play.isProd) {
          Auth0Config.get()
        } else {
          val callbackUrl = if (request.secure) { s"https://${request.host}/callback" } else { s"http://${request.host}/callback" }
          val newConfig = Auth0Config.get().copy(callbackURL = callbackUrl)
          Auth0Config.set(newConfig)
          newConfig
        }

        Ok(views.html.Application.index(request.flash, authConfig, Global.loginPermittedDomain.getOrElse("")))
    }
  }

  def logout = Action { implicit request =>
    request.session.get("idToken").foreach { idToken =>
      Cache.remove(idToken+ "profile")
    }

    val rootUrl = controllers.nonmember.page.routes.PublicPage.index().absoluteURL(request.secure)
    Redirect(s"https://cimadai.au.auth0.com/v2/logout?returnTo=$rootUrl")
  }

}
