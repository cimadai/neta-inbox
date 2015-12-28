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

  def getSizeOfUserAssigned()(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(_.userInfoIdOrNone.isDefined)
  }
  def getSizeOfUserNotAssigned()(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    countByFilter(_.userInfoIdOrNone.isEmpty)
  }

  private def generateQueryWithReactionNum(): Query[(EventInfoTable, Rep[Int]), (EventInfo, Int), Seq] = {
    val groupedEventReaction = eventReactionQuery.groupBy(_.eventInfoId).map {
      case (eventInfoId, group) => (eventInfoId, group.length)
    }
    (eventInfoQuery joinLeft groupedEventReaction on (_.id === _._1)).map {
      case (eventInfo, reactionsNumOrNone) => (eventInfo, reactionsNumOrNone.flatMap(_._2).getOrElse(0))
    }
  }

  private def getPaginationAndNumPages(query: Query[(EventInfoTable, Rep[Int]), (EventInfo, Int), Seq], pageNum: Int, sizeNum: Int)
      (implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[(EventInfo, Int)], Int) = {
    val page = query
      .sortBy(_._2.desc)
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

  def getPaginationAndNumPagesWithReactionNumByFilter[V <: Rep[Boolean]](filter: ((EventInfoTable, Rep[Int])) => V)(pageNum: Int, sizeNum: Int)
      (implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[(EventInfo, Int)], Int) = {
    getPaginationAndNumPages(generateQueryWithReactionNum().filter(filter), pageNum, sizeNum)
  }

}

