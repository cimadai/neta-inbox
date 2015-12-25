package controllers.member.page

import controllers.utils.AuthenticateUtil
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object User extends AuthenticateUtil {

  def index = AuthenticatedAction { implicit request =>
    Ok(views.html.Application.user(request.flash, getUserInfoOrNone))
  }

}