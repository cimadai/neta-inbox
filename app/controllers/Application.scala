package controllers

import play.api.mvc._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.Application.index(request.flash))
  }

}
