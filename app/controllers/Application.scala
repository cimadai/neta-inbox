package controllers

import helpers.Auth0Config
import play.api.Play.current
import play.api.cache.Cache
import play.api.i18n.Messages.Implicits._
import play.api.mvc._

object Application extends BaseController {

  def index = Action { implicit request =>
    Ok(views.html.Application.index(request.flash, Auth0Config.get()))
  }

  def logout = Action { implicit request =>
    request.session.get("idToken").foreach { idToken =>
      Cache.remove(idToken+ "profile")
    }
    Redirect("https://cimadai.au.auth0.com/v2/logout?returnTo=http://localhost:9090/")
  }

}
