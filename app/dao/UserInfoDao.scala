package dao

import dao.utils._
import DaoBase.UserInfoTable
import dao.utils.{DatabaseAccessor, DaoCRUDWithId, SchemaAccessible}
import DatabaseAccessor.jdbcProfile.api._
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
        this.create(userInfo).map(userInfoId => userInfo.copy(id = Some(userInfoId)))
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

