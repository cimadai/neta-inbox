package controllers

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Controller
import slick.driver.JdbcProfile

trait BaseController extends Controller {

  implicit val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

}
