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
        Some(Redirect(controllers.nonmember.page.routes.PublicPage.index())
          .withCookies(Cookie(AuthenticateUtil.LAST_URI_COOKIE, request.uri, Some(3600 * 24 * 7)))
        )
      }.get
    }
  }

  // AuthenticatedActionなメソッドからは取得可能
  protected def getUserInfoOrNone(implicit request: Request[AnyContent]): Option[UserInfo] = {
    request.session.get("idToken").flatMap(idToken => Cache.getAs[UserInfo](idToken + "profile"))
  }

}

object AuthenticateUtil {
  val LAST_URI_COOKIE = "last.url.cookie"
}
