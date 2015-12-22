package dao

import dao.DaoBase.EventReactionTable
import dao.DatabaseAccessor.jdbcProfile.api._
import dao.utils.SchemaAccessible
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
}

