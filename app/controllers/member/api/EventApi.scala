package controllers.member.api

import controllers.utils.{AuthenticateUtil, JsonResponsible}
import dao.{EventReactionDao, UserInfoDao}
import models.JsonModelWriterImplicits._
import play.api.libs.json.Json
import play.api.mvc.Action

object EventApi extends AuthenticateUtil with JsonResponsible {

  // TODO: member only
  def toggleReaction(eventId: Long, reactionTypeId: Long) = Action { implicit request =>
    onAjax {
      val dummy = UserInfoDao.findById(1).get
      EventReactionDao.toggleReaction(dummy.id.get, eventId, reactionTypeId)
      val reactions = EventReactionDao.findByEventInfoId(eventId)
      renderJsonOk(Json.obj(
        "reactions" -> reactions.map(Json.toJson(_))
      ))
    }
  }

}
