package models

import _root_.utils.SpecsCommon
import controllers.member.page.EventPage.EventPublishType
import dao._
import dao.utils.DaoBase.EventInfoTable
import dao.utils.DatabaseAccessor
import job.EventStatusChangeActor
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.FakeApplication
import play.api.test.Helpers._
import slick.driver.JdbcProfile
import DatabaseAccessor.jdbcProfile.api._

import scala.language.postfixOps
import scala.concurrent.duration._

class DaoSpec extends PlaySpec with OneServerPerSuite with SpecsCommon {

  implicit override lazy val app = FakeApplication(additionalConfiguration = inMemoryDatabase("play"))
  implicit override lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
  implicit override lazy val db = dbConfig.db

  "DAOs" should {
    "run right" in {
      val evnetInfoNum = EventInfoDao.count()
      val eventReactionNum = EventReactionDao.count()
      val eventReactionTypeNum = EventReactionTypeDao.count()
      val eventTagDaoNum = EventTagDao.count()
      val eventTagRelationNum = EventTagRelationDao.count()
      val userInfoNum = UserInfoDao.count()

      evnetInfoNum > 0 mustBe true
      eventReactionNum > 0 mustBe true
      eventReactionTypeNum > 0 mustBe true
      eventTagDaoNum > 0 mustBe true
      eventTagRelationNum > 0 mustBe true
      userInfoNum > 0 mustBe true
    }
  }

  "EventInfo" should {
    "update run right" in {
      val event = EventInfoDao.findFirstByFilter(_.id > 0L).get
      EventInfoDao.update(event.copy(duration = 0)) mustBe true
      EventInfoDao.findById(event.id.get).get.duration mustBe 0
      EventInfoDao.update(event.copy(duration = 100)) mustBe true
      EventInfoDao.findById(event.id.get).get.duration mustBe 100
    }

    "listing run right" in {
      // 全削除
      EventInfoDao.deleteById(SpecsCommon.getFirstEvent.id.get)
      EventInfoDao.count() mustBe 0

      val user = SpecsCommon.getFirstUser
      val now = System.currentTimeMillis()
      val evZeroAssigned = EventInfo(None, EventType.Presentation, "zero assigned", "", user.id, 0, EventStatus.New.value, 0L)
      val evZeroNotAssigned = EventInfo(None, EventType.Presentation, "zero not assigned", "", None, 0, EventStatus.New.value, 0L)
      val evOldAssigned = EventInfo(None, EventType.Presentation, "old assigned", "", user.id, now - 1, EventStatus.New.value, 0L)
      val evOldNotAssigned = EventInfo(None, EventType.Presentation, "old not assigned", "", None, now - 1, EventStatus.New.value, 0L)
      val evFutureAssigned = EventInfo(None, EventType.Presentation, "future assigned", "", user.id, now + (1 hour).toMillis, EventStatus.New.value, 0L)
      val evFutureNotAssigned = EventInfo(None, EventType.Presentation, "future not assigned", "", None, now + (1 hour).toMillis, EventStatus.New.value, 0L)

      EventInfoDao.create(evZeroAssigned)
      EventInfoDao.create(evZeroNotAssigned)
      EventInfoDao.create(evOldAssigned)
      EventInfoDao.create(evOldNotAssigned)
      EventInfoDao.create(evFutureAssigned)
      EventInfoDao.create(evFutureNotAssigned)

      EventStatusChangeActor.changeStatusToDoneByPastPublishDate()

      val assignedNum = EventInfoDao.countByFilter(ev => { genPublishDateFilter(ev, EventPublishType.InFuture) && ev.userInfoIdOrNone.isDefined })
      val notAssignedNum = EventInfoDao.countByFilter(ev => { genPublishDateFilter(ev, EventPublishType.InFuture) && ev.userInfoIdOrNone.isEmpty })
      val pastEventNum = EventInfoDao.countByFilter(ev => { genPublishDateFilter(ev, EventPublishType.InPast) })

      assignedNum mustBe 2 // future and assiged
      notAssignedNum mustBe 2 // future and not assigned
      pastEventNum mustBe 2 // all past
    }
  }

  private def genPublishDateFilter(ev: (EventInfoTable), publishType: EventPublishType.Value): Rep[Boolean] = {
    publishType match {
      case EventPublishType.All =>
        true
      case EventPublishType.InPast =>
        ev.status === EventStatus.Done.value
      case EventPublishType.InFuture =>
        ev.status === EventStatus.New.value
    }
  }
}
