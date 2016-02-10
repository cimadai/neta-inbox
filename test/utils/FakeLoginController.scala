package utils

import models.UserInfo
import play.api.Application
import play.api.cache.Cache
import play.api.mvc.{AnyContentAsEmpty, Action, Controller}
import play.api.test.FakeRequest
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

object FakeLoginController extends Controller {

  private val DUMMY_TOKEN = "dummy-token"

  def login(implicit app: Application, dbConfig: DatabaseConfig[JdbcProfile]) = Action {
    Cache.set(DUMMY_TOKEN + "profile", SpecsCommon.getFirstUser)
    Ok("ok")
  }
  def loginAs(user: UserInfo)(implicit app: Application, dbConfig: DatabaseConfig[JdbcProfile]) = Action {
    Cache.set(DUMMY_TOKEN + "profile", user)
    Ok("ok")
  }

  def logout(implicit app: Application) = Action {
    Cache.remove(DUMMY_TOKEN + "profile")
    Ok("ok")
  }

  implicit class FakeRequestExtension(request: FakeRequest[AnyContentAsEmpty.type]) {
    def withDummyToken() = {
      request.withSession("idToken" -> DUMMY_TOKEN)
    }
    def withXHR() = {
      request.withHeaders("X-Requested-With" -> "XMLHttpRequest")
    }
  }

}

