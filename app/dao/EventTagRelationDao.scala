package dao

import dao.utils.DaoBase.EventTagRelationTable
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import dao.utils.QueryExtensions._
import dao.utils.{SchemaAccessible, _}
import models.{EventTag, EventTagGroups, EventTagRelation}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

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

  def findTagsCountAsGroupByTag()(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[EventTagGroups] = {
    baseQuery.groupBy(_.eventTagId).map {
      case (eventTagId, group) =>
        (eventTagId, group.length)
    }.sortBy(_._2.desc).result.runAndAwait.get.map {
      case (eventTagId, relationCount) =>
        EventTagGroups(EventTagDao.findById(eventTagId).get, relationCount)
    }
  }
}

