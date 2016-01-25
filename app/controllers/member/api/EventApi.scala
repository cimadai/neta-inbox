package controllers.member.api

import controllers.utils.AuthenticateUtil
import dao.{EventInfoDao, EventReactionDao}
import play.api.libs.json.Json

object EventApi extends AuthenticateUtil {

  import models.JsonModelWriterImplicits._

  val MAX_REACTION_DISPLAY = 6

  def toggleReaction(eventId: Long, reactionTypeId: Long) = AuthenticatedAction { implicit request =>
    onAjax {
      withUserInfo { userInfo =>
        EventReactionDao.toggleReaction(userInfo.id.get, eventId, reactionTypeId)
        val reactions = EventReactionDao.findByEventInfoId(eventId)
        renderJsonOk(Json.obj(
          "maxNum" -> MAX_REACTION_DISPLAY,
          "reactions" -> reactions.map(reaction => Json.toJson(reaction))
        ))
      }
    }
  }

  def deleteEvent(eventId: Long) = AuthenticatedAction { implicit request =>
    onAjax {
      withUserInfo { userInfo =>
        EventInfoDao.deleteById(eventId)
        renderJsonOk().withHeaders(
          "X-AJAX-REDIRECT" -> controllers.member.page.routes.EventPage.listAll().url
        )
      }
    }
  }

}
