package controllers.utils

import controllers.BaseController
import models.UserInfo
import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc._


trait AuthenticateUtil extends BaseController with JsonResponsible {
  val accessLogger = Logger("access")

  def AuthenticatedAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action { implicit request =>
      (request.session.get("idToken").flatMap { idToken =>
        Cache.getAs[UserInfo](idToken + "profile")
      } map { profile =>
        val accessToken = request.session.get("accessToken").getOrElse("unknown")
        accessLogger.info(s"<AuthenticateUtil> Logged in access.\taccessToken:$accessToken\trequestUri:${request.uri}")
        f(request)
      }).orElse {
        accessLogger.info(s"<AuthenticateUtil> Not logged in.\trequestUri:${request.uri}")
        if (isAjax) {
          // Apiで認証エラーが発生した場合はトップページに戻す
          Some(renderJsonError().withHeaders(
            "X-AJAX-REDIRECT" -> controllers.nonmember.page.routes.PublicPage.index().url
          ))
        } else {
          // 通常のページならば次回ログイン後にそこに遷移させてあげる
          Some(Redirect(controllers.nonmember.page.routes.PublicPage.index())
            .withCookies(Cookie(AuthenticateUtil.LAST_URI_COOKIE, request.uri, Some(3600 * 24 * 7)))
          )
        }
      }.get
    }
  }

  // AuthenticatedActionなメソッドからは取得可能
  // ただのActionから呼ぶとNoneが返る
  protected def getUserInfoOrNone(implicit request: Request[AnyContent]): Option[UserInfo] = {
    request.session.get("idToken").flatMap(idToken => Cache.getAs[UserInfo](idToken + "profile"))
  }

  protected def withUserInfo(f: UserInfo => Result)(implicit request: Request[AnyContent]): Result = {
    getUserInfoOrNone match {
      case Some(userInfo) =>
        f(userInfo)
      case _ =>
        renderJsonError().withHeaders(
          "X-AJAX-REDIRECT" -> controllers.nonmember.page.routes.PublicPage.index().url
        )
    }
  }

}

object AuthenticateUtil {
  val LAST_URI_COOKIE = "last.url.cookie"
}
