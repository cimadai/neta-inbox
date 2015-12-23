package dao

import dao.utils._
import DaoBase.EventReactionTypeTable
import dao.utils.{DatabaseAccessor, DaoCRUDWithId, SchemaAccessible}
import DatabaseAccessor.jdbcProfile.api._
import models.EventReactionType
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * イベントに対するリアクションのマスタ
 */
object EventReactionTypeDao extends DaoCRUDWithId[EventReactionType, EventReactionTypeTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventReactionTypeQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create
}

