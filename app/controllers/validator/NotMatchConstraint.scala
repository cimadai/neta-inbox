package controllers.validator

import play.api.data.FormError
import play.api.data.format.Formatter

/**
 * 一致してはいけない制限
 */
class NotMatchConstraint[A](val errorMessage: String, val targetField:String, val map:(String, Map[String, String]) => A, val unmap:A => String) extends Formatter[A] {
	override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] = {
		val first = data.getOrElse(key, "")
		val second = data.getOrElse(targetField, "")
		if (first == "" || first.equals(second)) {
			Left(List(FormError(key, errorMessage)))
		}
		else {
			Right(map(key, data))
		}
	}

	override def unbind(key: String, value: A): Map[String, String] = Map(key -> unmap(value))
}
