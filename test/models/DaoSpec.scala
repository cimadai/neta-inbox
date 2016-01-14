package models

import _root_.utils.SpecsCommon
import dao._
import dao.utils.DatabaseAccessor
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.FakeApplication
import play.api.test.Helpers._
import slick.driver.JdbcProfile
import DatabaseAccessor.jdbcProfile.api._

class DaoSpec extends PlaySpec with BeforeAndAfter with OneServerPerSuite with SpecsCommon {

  implicit override lazy val app = FakeApplication(additionalConfiguration = inMemoryDatabase("test"))
  implicit override lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
  implicit override lazy val db = dbConfig.db

  before {
    createTablesIfNeeded()
    truncateDatabases()
    setupData()
  }

  "DAOs" should {
    "run right" in {
      EventInfoDao.count() > 0 mustBe true
      EventReactionDao.count() > 0 mustBe true
      EventReactionTypeDao.count() > 0 mustBe true
      EventTagDao.count() > 0 mustBe true
      EventTagRelationDao.count() > 0 mustBe true
      UserInfoDao.count() > 0 mustBe true
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
  }
}
