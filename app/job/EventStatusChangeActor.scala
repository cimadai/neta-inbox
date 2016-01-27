package job

import akka.actor.Actor
import dao.EventInfoDao
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import models.EventStatus
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

class EventStatusChangeActor extends Actor {

  override def receive: Receive = {
    case msg: String => {
      EventStatusChangeActor.changeStatusToDoneByPastPublishDate()
    }
  }

}

object EventStatusChangeActor {
  private implicit lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  private implicit lazy val db = dbConfig.db

  def changeStatusToDoneByPastPublishDate(): Unit = {
    val now = System.currentTimeMillis()
    EventInfoDao.findByFilter(ev => {(ev.publishDateUnixMillis > 0L) && (ev.publishDateUnixMillis < now)}).foreach(event => {
      EventInfoDao.update(event.copy(status = EventStatus.Done.value))
    })
  }

}
