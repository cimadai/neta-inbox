package dao

import dao.utils._
import DaoBase.EventReactionTable
import dao.utils.{DatabaseAccessor, DaoCRUDWithId, SchemaAccessible}
import DatabaseAccessor.jdbcProfile.api._
import models.{EventReaction, EventReactionUserAndReactionType}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * イベントに対するリアクション
 */
object EventReactionDao extends DaoCRUDWithId[EventReaction, EventReactionTable] with DaoBase with SchemaAccessible {
  override val baseQuery = eventReactionQuery

  override implicit def database(implicit acc: DatabaseConfig[JdbcProfile]) = acc.db

  override def createDDL = baseQuery.schema.create

  def findByEventInfoId(eventInfoId: Long)(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[EventReactionUserAndReactionType] = {
    findByFilter(_.eventInfoId === eventInfoId).map(reaction => {
      val userInfo = UserInfoDao.findFirstByFilter(_.id === reaction.userInfoId)
      val eventReactionType = EventReactionTypeDao.findFirstByFilter(_.id === reaction.eventReactionTypeId)
      EventReactionUserAndReactionType(userInfo, eventReactionType)
    })
  }

  def toggleReaction(userInfoId: Long, eventInfoId: Long, reactionTypeId: Long)(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    findFirstByFilter(record => {
      record.userInfoId === userInfoId && record.eventInfoId === eventInfoId && record.eventReactionTypeId === reactionTypeId
    }) match {
      case Some(reaction) => delete(reaction)
      case _ => create(EventReaction(None, userInfoId, eventInfoId, reactionTypeId))
    }
  }
}

