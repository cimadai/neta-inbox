package dao

import dao.DaoBase.EventReactionTypeTable
import dao.DatabaseAccessor.jdbcProfile.api._
import dao.utils.SchemaAccessible
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

