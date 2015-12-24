package models

/**
 * サーバーにおけるエラーを表すクラスです。
 */
object JsonErrors extends ErrorDefinition(domain=1) {

  val ERROR_TAG_CREATE_FAILED = define("error.tag.create.failed")

}

