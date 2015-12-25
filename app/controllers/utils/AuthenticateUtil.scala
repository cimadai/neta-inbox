package controllers.utils

import controllers.BaseController
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
        Some(Redirect(controllers.nonmember.page.routes.PublicPage.index()))
      }.get
    }
  }

  // AuthenticatedActionなメソッドからは取得可能
  protected def getUserInfoOrNone(implicit request: Request[AnyContent]): Option[UserInfo] = {
    val idToken = request.session.get("idToken").get
    Cache.getAs[UserInfo](idToken + "profile")
  }

}
