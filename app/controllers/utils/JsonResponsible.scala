package controllers.utils

import models.JsonError
import play.api.libs.json._
import play.api.mvc.{Result, Results}


/**
 * Json返却用トレイトクラス
 */
trait JsonResponsible extends Results {
  import models.JsonModelWriterImplicits._
  protected def renderJsonOk(): Result = {
    Ok(Json.obj("result" -> 0))
  }
  protected def renderJsonOk(jsObject: JsObject): Result = {
    Ok(jsObject + ("result" -> JsNumber(0)))
  }

  protected def renderJsonError(): Result = {
    Ok(Json.obj("result" -> 1))
  }
  protected def renderJsonError(obj: JsonError): Result = {
    renderJsonError(Json.toJson(obj).as[JsObject])
  }
  protected def renderJsonError(obj: JsonError, jsObject: JsObject): Result = {
    renderJsonError(jsObject ++ Json.toJson(obj).as[JsObject])
  }
  protected def renderJsonError(jsObject: JsObject): Result = {
    Ok(jsObject + ("result" -> JsNumber(1)))
  }

}
