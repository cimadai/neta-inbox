package dao

import dao.utils._
import DaoBase.EventInfoTable
import dao.utils.{DatabaseAccessor, DaoCRUDWithId, SchemaAccessible}
import DatabaseAccessor.jdbcProfile.api._
import models.EventInfo
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import dao.utils.QueryExtensions._

/**
 * イベント情報
 */
object EventInfoDao extends DaoCRUDWithId[EventInfo, EventInfoTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventInfoQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create

  def getSizeOfUserAssigned(filter: ((EventInfoTable, Rep[Int])) => Rep[Boolean])(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(_.userInfoIdOrNone.isDefined)
  }
  def getSizeOfUserNotAssigned(filter: ((EventInfoTable, Rep[Int])) => Rep[Boolean])(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(_.userInfoIdOrNone.isEmpty)
  }
  def getSizeOfAll(filter: (EventInfoTable) => Rep[Boolean])(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(filter)
  }

  private def generateQueryWithReactionNum(): Query[(EventInfoTable, Rep[Int]), (EventInfo, Int), Seq] = {
    val groupedEventReaction = eventReactionQuery.groupBy(_.eventInfoId).map {
      case (eventInfoId, group) => (eventInfoId, group.length)
    }
    (eventInfoQuery joinLeft groupedEventReaction on (_.id === _._1)).map {
      case (eventInfo, reactionsNumOrNone) => (eventInfo, reactionsNumOrNone.flatMap(_._2.?).getOrElse(0))
    }
  }

  private def getPaginationAndNumPages(query: Query[(EventInfoTable, Rep[Int]), (EventInfo, Int), Seq], pageNum: Int, sizeNum: Int)
      (implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[(EventInfo, Int)], Int) = {
    val page = query
      .sortBy(row => (row._2.desc, row._1.publishDateUnixMillis.desc, row._1.id.asc))
      .page(pageNum, sizeNum)
      .result.runAndAwait
      .getOrElse(Iterable.empty[(EventInfo, Int)])
    val numPages = query.numPages(sizeNum).runAndAwait.get
    (page, numPages)
  }

  def getPaginationAndNumPagesWithReactionNum(pageNum: Int, sizeNum: Int)
      (implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[(EventInfo, Int)], Int) = {
    getPaginationAndNumPages(generateQueryWithReactionNum(), pageNum, sizeNum)
  }

  def getPaginationAndNumPagesWithReactionNumByFilter(filter: ((EventInfoTable, Rep[Int])) => Rep[Boolean])(pageNum: Int, sizeNum: Int)
      (implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[(EventInfo, Int)], Int) = {
    getPaginationAndNumPages(generateQueryWithReactionNum().filter(filter), pageNum, sizeNum)
  }

  override def deleteById(id: Long)(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    DBIO.seq(
      eventReactionQuery.filter(_.eventInfoId === id).delete,
      eventTagRelationQuery.filter(_.eventInfoId === id).delete,
      eventInfoQuery.filter(_.id === id).delete
    ).runAndAwait
  }

}

