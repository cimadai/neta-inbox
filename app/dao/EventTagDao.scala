package dao

import dao.utils.DaoBase.{EventTagTable, EventInfoTable}
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import dao.utils.{DaoCRUDWithId, DatabaseAccessor, SchemaAccessible, _}
import models.{EventTag, EventInfo}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * イベントタグ情報
 */
object EventTagDao extends DaoCRUDWithId[EventTag, EventTagTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventTagQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create
}

