package dao

import dao.utils._
import DaoBase.EventInfoTable
import dao.utils.{DatabaseAccessor, DaoCRUDWithId, SchemaAccessible}
import DatabaseAccessor.jdbcProfile.api._
import models.EventInfo
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * イベント情報
 */
object EventInfoDao extends DaoCRUDWithId[EventInfo, EventInfoTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventInfoQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create

  def getSizeOfUserAssigned()(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(_.userInfoIdOrNone.isDefined)
  }
  def getSizeOfUserNotAssigned()(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(_.userInfoIdOrNone.isEmpty)
  }
}

