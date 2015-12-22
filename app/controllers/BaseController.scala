package controllers

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.{Result, RequestHeader, Controller}
import slick.driver.JdbcProfile

trait BaseController extends Controller {

  implicit val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  def isAjax(implicit request : RequestHeader) = request.headers.get("X-Requested-With").contains("XMLHttpRequest")

  def onAjax(f: => Result)(implicit request : RequestHeader) = {
    if (isAjax) {
      f
    } else {
      BadRequest("This api is only for ajax.")
    }
  }

}
