package dao

import dao.DaoBase.UserInfoTable
import dao.DatabaseAccessor.jdbcProfile.api._
import dao.utils.SchemaAccessible
import models.UserInfo
import play.api.libs.json.JsValue
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * イベント情報
 */
object UserInfoDao extends DaoCRUDWithId[UserInfo, UserInfoTable] with DaoBase with SchemaAccessible {
  override val baseQuery = userInfoQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create

  private def toUserInfo(jsonValue: JsValue): UserInfo = {
    UserInfo(
      None,
      (jsonValue \ "email").as[String],
      (jsonValue \ "family_name").as[String],
      (jsonValue \ "given_name").as[String],
      (jsonValue \ "name").as[String],
      (jsonValue \ "nickname").as[String],
      (jsonValue \ "picture").as[String],
      (jsonValue \ "locale").as[String]
    )
  }

  def loadUserInfoOrNone(jsonValue: JsValue)(implicit acc: DatabaseConfig[JdbcProfile]): Option[UserInfo] = {
    val email = (jsonValue \ "email").as[String]
    if (email.endsWith("gmail.com")) {
      this.findFirstByFilter(_.email === email).fold( {
        val userInfo = toUserInfo(jsonValue)
        val userInfoId = this.create(userInfo)
        if (userInfoId > 0) {
          Some(userInfo.copy(id = Some(userInfoId)))
        } else {
          None
        }
      } ) { userInfo =>
        // 必要であれば更新処理
        this.update(toUserInfo(jsonValue).copy(id = userInfo.id))
        Some(userInfo)
      }
    } else {
      // gmail.com以外はダメ。
      None
    }
  }
}

