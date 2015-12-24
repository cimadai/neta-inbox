package views.support

import java.util.Date

import org.joda.time.{DateTime, Duration}
import play.api.data.Field
import play.api.i18n.Messages
import play.twirl.api.Html
import utils.TextUtil

/**
  */
object ViewSupport {
	def attributes(attrs: Seq[(Symbol, String)]): Html =
		Html(
			attrs.map { case (attrName, attrValue) =>
				s"""${attrName.name}="$attrValue""""
			}.mkString(" ")
		)

	def urlWithParams(url: String, params: Map[String, Any]): String = {
		val queryString = params.map { case (key, value) =>
			s"$key=$value"
		}.mkString("&")
		val prefix = if (url.contains("?")) "&" else "?"
		s"$url$prefix$queryString"
	}

	def inputAttributesOfConstraints(field: Field): Html = {
		Html(field.constraints.map {
			case ("constraint.maxLength", values) =>
				val value = values.head.toString
				s"maxlength=$value"
			case ("constraint.required", _) =>
				"required"
			case ("constraint.min", values) =>
				val value = values.head.toString
				s"min=$value"
			case ("constraint.max", values) =>
				val value = values.head.toString
				s"max=$value"
			case _ =>
				""
		}.mkString(" "))
	}

	def toSiPrefixNotationText(n: Long): String = {
		val (s, prefix) = TextUtil.toSiPrefixNotation(n)
		s"${s.toDouble.floor.toLong} $prefix"
	}

	def formatDateTimeOrDefault(millis: Long, default: String): String = {
		if (millis > 0) {
			"%tY/%<tm/%<td %<tR" format new Date(millis)
		} else {
			default
		}
	}
}
