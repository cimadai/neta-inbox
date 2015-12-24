package controllers.nonmember.page

import controllers.BaseController
import helpers.Auth0Config
import jsmessages.JsMessagesFactory
import play.api.mvc.Action
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.cache.Cache

object PublicPage extends BaseController {

  private val jsMessagesFactory = new JsMessagesFactory(applicationMessagesApi)
  private val allJsMessages = jsMessagesFactory.all

  def messages = Action { implicit request =>
    Ok(allJsMessages(Some("window.i18n")))
  }

  def index = Action { implicit request =>
    val callbackUrl = if (request.secure) { s"https://${request.host}/callback" } else { s"http://${request.host}/callback" }
    val newConfig = Auth0Config.get().copy(callbackURL = callbackUrl)
    Auth0Config.set(newConfig)
    Ok(views.html.Application.index(request.flash, newConfig))
  }

  def logout = Action { implicit request =>
    request.session.get("idToken").foreach { idToken =>
      Cache.remove(idToken+ "profile")
    }

    val rootUrl = controllers.nonmember.page.routes.PublicPage.index().absoluteURL(request.secure)
    Redirect(s"https://cimadai.au.auth0.com/v2/logout?returnTo=$rootUrl")
  }

}
