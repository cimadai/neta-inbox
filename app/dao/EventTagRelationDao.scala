package dao

import dao.utils.DaoBase.{EventTagRelationTable, EventTagTable}
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import dao.utils.{DaoCRUDWithId, SchemaAccessible, _}
import models.{EventTagRelation, EventTag}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import dao.utils.QueryExtensions._

/**
 * イベントタグ関連情報
 */
object EventTagRelationDao extends DaoCRUD[EventTagRelation, EventTagRelationTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventTagRelationQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create

  /**
   * 作成
   */
  def create(obj: EventTagRelation)(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    baseQuery.insertOrUpdate(obj).runAndAwait
  }

  def findTagsByEventInfoId(eventInfoId: Long)(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[EventTag] = {
    baseQuery.filter(_.eventInfoId === eventInfoId).flatMap(_.eventTag).result.runAndAwait.getOrElse(Iterable.empty)
  }
}

