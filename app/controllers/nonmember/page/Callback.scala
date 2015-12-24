package controllers.nonmember.page

import controllers.BaseController
import dao.UserInfoDao
import helpers.Auth0Config
import play.api.Play
import play.api.Play.current
import play.api.cache.Cache
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WS
import play.api.mvc.Action

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Callback extends BaseController {

  // callback route
  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async {
    (for {
      code <- codeOpt
      state <- stateOpt
    } yield {
        // Get the token
        getToken(code).flatMap { case (idToken, accessToken) =>
          // Get the user
          getUser(accessToken).map { user =>
            UserInfoDao.loadUserInfoOrNone(user) match {
              case Some(userInfo) =>
                // Cache the user and tokens into cache and session respectively
                Cache.set(idToken+ "profile", userInfo)
                Redirect(controllers.member.page.routes.EventPage.listAll())
                  .withSession(
                    "idToken" -> idToken,
                    "accessToken" -> accessToken
                  )
              case None =>
                // 取得できなかったらダメ
                Redirect(controllers.nonmember.page.routes.PublicPage.logout())
            }
          }

        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }
      }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  def getToken(code: String): Future[(String, String)] = {
    val config = Auth0Config.get()
    val tokenResponse = WS.url(String.format("https://%s/oauth/token", config.domain))(Play.current).
      withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).
      post(
        Json.obj(
          "client_id" -> config.clientId,
          "client_secret" -> config.secret,
          "redirect_uri" -> config.callbackURL,
          "code" -> code,
          "grant_type"-> "authorization_code"
        )
      )

    tokenResponse.flatMap { response =>
      (for {
        idToken <- (response.json \ "id_token").asOpt[String]
        accessToken <- (response.json \ "access_token").asOpt[String]
      } yield {
          Future.successful((idToken, accessToken))
        }).getOrElse(Future.failed[(String, String)](new IllegalStateException("Tokens not sent")))
    }

  }

  def getUser(accessToken: String): Future[JsValue] = {
    val userResponse = WS.url(String.format("https://%s/userinfo", "cimadai.au.auth0.com"))(Play.current)
      .withQueryString("access_token" -> accessToken)
      .get()

    userResponse.flatMap(response => Future.successful(response.json))
  }
}
