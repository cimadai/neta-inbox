package controllers.member.api

import controllers.utils.AuthenticateUtil
import dao.EventTagDao
import models.JsonModelWriterImplicits._
import models.{EventTag, JsonError, JsonErrors}
import play.api.libs.json.Json

object EventTagApi extends AuthenticateUtil {

  def getAllTags = AuthenticatedAction { implicit request =>
    onAjax {
      renderJsonOk(Json.obj(
        "tags" -> EventTagDao.list().map(ev => Json.toJson(ev))
      ))
    }
  }

  def addTag(key: String) = AuthenticatedAction { implicit request =>
    onAjax {
      createTagOrError(key) match {
        case Right(tag) => renderJsonOk(Json.obj("tag" -> Json.toJson(tag)))
        case Left(err) => renderJsonError(err)
      }
    }
  }

  def createTagOrError(key: String): Either[JsonError, EventTag] = {
    EventTagDao.findByTagName(key) match {
      case Some(tag) =>
        // すでに存在していたらそのIDを返す
        Right(tag)
      case _ =>
        // 存在していなければ新しく作成してIDを返す
        val newEventTag = EventTag(None, key)
        EventTagDao.create(newEventTag) match {
          case Some(tagId) =>
            Right(newEventTag.copy(id = Some(tagId)))
          case _ =>
            // 作成できなかったらエラー
            Left(JsonErrors.ERROR_TAG_CREATE_FAILED)
        }
    }
  }

}
