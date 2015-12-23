package controllers.validator

import models.{EventStatus, EventType}
import play.api.data.validation.{Constraint, Valid}
import utils.TextUtil
import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.format.Formatter



/**
 * フォームの制約
 */
object CustomConstraints {
  /**
   * 説明を付ける用。
   */
  def addNote[T](messageKey: String): Constraint[T] = Constraint[T](messageKey) { values => Valid }

  /**
   * 一致してはいけない複数のフォーム要素のチェック
   * @param errorMessage エラーメッセージ
   * @param targetField チェックするもう一つの要素フィールド
   */
  def notMatchTargetFor[A](errorMessage: String, targetField: String, map: (String, Map[String, String]) => A, unmap: A => String): Formatter[A] = {
    new Formatter[A] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], A] = {
        // この要素自身のフィールドの値
        val first = data.getOrElse(key, "")
        // targetFiledで指定されたフィールドの値
        val second = data.getOrElse(targetField, "")

        if (first == "" || first.equals(second)) {
          // 一致してたらエラー
          Left(List(FormError(key, errorMessage)))
        } else {
          // 一致してなければOK
          Right(map(key, data))
        }
      }

      override def unbind(key: String, value: A): Map[String, String] = Map(key -> unmap(value))
    }
  }

  /**
   * 　文字列型のフォーム要素に対してかける制約。他の復数のフォーム要素を参照する制約を記述することが可能である。
   *
   * 使用例：
   * val myConstraint = TextConstraint { case (key, data) => /* 制約の既述 */ }
   * val myForm = Form {
   * mapping(
   * "mystringfield" -> of(myConstraint)
   * }
   * }(FormParams.apply)(FormParams.unapply))
   *
   * この場合、keyには"mystringfield"が、dataには全フォームフィールドのマップが渡される。
   *
   * @param validation 制約対象のキーと全てのフォームデータを受け取り、制約を満たさない時はFormErrorの列、制約を満たすときは空列を返す関数。
   */
  class TextConstraint(validation: (String, Map[String, String]) => Seq[FormError]) extends Formatter[String] {

    override final def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      // このクラスを利用した制約の既述の中で例外が起こることでサーバが停止することを回避するため
      try {
        val errors: Seq[FormError] = validation(key, data)
        if (errors.isEmpty) Right(data.getOrElse(key, "")) else Left(errors)
      } catch {
        case e: Exception =>
          Left(Seq(FormError(key, "error.exception_on_validation", Seq(e))))
      }
    }

    override final def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  object TextConstraint {
    def apply(validation: (String, Map[String, String]) => Seq[FormError]) = new TextConstraint(validation)
  }

  implicit class StringFormatterExtension(formatter: Formatter[String]) {
    def cascading(nextFormatter: Formatter[String]) = {
      new Formatter[String] {
        override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
          formatter.bind(key, data).right.flatMap { formattedValue =>
            nextFormatter.bind(key, data + (key -> formattedValue))
          }
        }

        override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
      }
    }
  }

  object TextTrimmer extends Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      Right(TextUtil.trim(data.getOrElse(key, "")))
    }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  implicit def eventTypeFormat = new Formatter[EventType] {
    override val format: Option[(String, Seq[Any])] = Some("format.event.type", Nil)

    /**
     * リクエストデータを EventType 型に変換します。
     * @param key リクエストデータを取り出す際に使用するキー値
     * @param data リクエストデータ
     * @return EventType 型への変換が失敗した場合フォームエラー。成功した場合変換した値
     */
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], EventType] = {
      intFormat.bind(key, data).right.flatMap { i =>
        scala.util.control.Exception.allCatch[EventType]
					.either(EventType.valueOf(i).get) // 文字列から EventType に変換する処理
					.left.map(e => Seq(FormError(key, "error.event.type", Nil))) // それが失敗した場合
      }
    }

    def unbind(key: String, value: EventType): Map[String, String] = Map(key -> value.value.toString)
  }
  val ofEventType = of[EventType]

  implicit def eventStatusFormat = new Formatter[EventStatus] {
    override val format: Option[(String, Seq[Any])] = Some("format.event.type", Nil)

    /**
     * リクエストデータを EventStatus 型に変換します。
     * @param key リクエストデータを取り出す際に使用するキー値
     * @param data リクエストデータ
     * @return EventStatus 型への変換が失敗した場合フォームエラー。成功した場合変換した値
     */
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], EventStatus] = {
      intFormat.bind(key, data).right.flatMap { i =>
        scala.util.control.Exception.allCatch[EventStatus]
					.either(EventStatus.valueOf(i).get) // 文字列から EventStatus に変換する処理
					.left.map(e => Seq(FormError(key, "error.event.status", Nil))) // それが失敗した場合
      }
    }

    def unbind(key: String, value: EventStatus): Map[String, String] = Map(key -> value.value.toString)
  }
  val ofEventStatus = of[EventStatus]

}