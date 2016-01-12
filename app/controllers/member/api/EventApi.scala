package controllers.member.api

import controllers.utils.AuthenticateUtil
import dao.EventReactionDao
import play.api.libs.json.Json

object EventApi extends AuthenticateUtil {

  import models.JsonModelWriterImplicits._

  def toggleReaction(eventId: Long, reactionTypeId: Long) = AuthenticatedAction { implicit request =>
    onAjax {
      getUserInfoOrNone match {
        case Some(userInfo) =>
          EventReactionDao.toggleReaction(userInfo.id.get, eventId, reactionTypeId)
          val reactions = EventReactionDao.findByEventInfoId(eventId)
          renderJsonOk(Json.obj(
            "reactions" -> reactions.map(reaction => Json.toJson(reaction))
          ))
        case _ =>
          renderJsonError().withHeaders(
            "X-AJAX-REDIRECT" -> controllers.nonmember.page.routes.PublicPage.index().url
          )
      }
    }
  }

}
