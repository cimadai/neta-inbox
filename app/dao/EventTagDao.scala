package dao

import dao.utils.DaoBase.EventTagTable
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import dao.utils.{DaoCRUDWithId, SchemaAccessible, _}
import models.EventTag
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * イベントタグ情報
 */
object EventTagDao extends DaoCRUDWithId[EventTag, EventTagTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventTagQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create

  def findByLikeTagName(key: String)(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[EventTag] = {
    findByFilter(_.text like s"%$key%")
  }

  def findByTagName(key: String)(implicit acc: DatabaseConfig[JdbcProfile]): Option[EventTag] = {
    findFirstByFilter(_.text === key)
  }
}

