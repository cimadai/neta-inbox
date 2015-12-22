package models

import play.api.libs.json.{JsValue, Json, Writes}

// JSONでのエラークラス
case class JsonError(
  domain: Int,
  code: Int,
  caption: String,
  args: List[String] = List.empty) {
  def withCaption(caption: String) = this.copy(domain, code, caption, args)

  /* domainとcodeの組で等価性を判定する。captionの違いは無視する。 */
  def canEqual(other: Any): Boolean = other match {
    case _: JsonError => true
    case _ => false
  }

  override def equals(other: Any): Boolean = other match {
    case that: JsonError =>
      (that canEqual this) &&
        domain == that.domain &&
        code == that.code
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(domain, code)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object JsonModelWriterImplicits {
  implicit val jsonErrorWrites = new Writes[JsonError] {
    def writes(e: JsonError): JsValue = {
      Json.obj(
        "domain" -> e.domain,
        "code" -> e.code.toString,
        "caption" -> e.caption
      )
    }
  }
}

