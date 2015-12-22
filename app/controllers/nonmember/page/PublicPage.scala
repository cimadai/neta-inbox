package controllers.nonmember.page

import controllers.BaseController
import jsmessages.JsMessagesFactory
import play.api.mvc.Action
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object PublicPage extends BaseController {

  private val jsMessagesFactory = new JsMessagesFactory(applicationMessagesApi)
  private val allJsMessages = jsMessagesFactory.all

  def messages = Action { implicit request =>
    Ok(allJsMessages(Some("window.i18n")))
  }


}
