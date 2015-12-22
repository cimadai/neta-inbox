package dao

import dao.DaoBase.EventInfoTable
import dao.DatabaseAccessor.jdbcProfile.api._
import dao.utils.SchemaAccessible
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

}

