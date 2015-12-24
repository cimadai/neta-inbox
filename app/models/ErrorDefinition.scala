package models

/**
 * JsonError定義のための基底クラス。
 * [規約]domainはエラーのカテゴリごとに固有の値を指定しなければならない。
 */
class ErrorDefinition(domain: Int)  {
  private var code = 0
  private def publishNewCode(): Int = {
    code += 1
    code
  }

  protected def define(caption: String) = JsonError(domain, publishNewCode(), caption)
}

