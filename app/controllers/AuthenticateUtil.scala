package controllers

import models.UserInfo
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc._


trait AuthenticateUtil extends BaseController {

  def AuthenticatedAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action { request =>
      (request.session.get("idToken").flatMap { idToken =>
        Cache.getAs[UserInfo](idToken + "profile")
      } map { profile =>
        f(request)
      }).orElse {
        Some(Redirect(routes.Application.index()))
      }.get
    }
  }

  protected def getUserInfo(implicit request: Request[AnyContent]): UserInfo = {
    val idToken = request.session.get("idToken").get
    Cache.getAs[UserInfo](idToken + "profile").get
  }

}
