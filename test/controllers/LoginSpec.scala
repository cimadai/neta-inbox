package controllers

import _root_.utils.SpecsCommon
import dao._
import dao.utils.DatabaseAccessor.jdbcProfile.api._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.cache.Cache
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import slick.driver.JdbcProfile

class LoginSpec extends PlaySpec with BeforeAndAfter with OneServerPerSuite with SpecsCommon {

  implicit override lazy val app = FakeApplication(additionalConfiguration = inMemoryDatabase("play"))
  implicit override lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
  implicit override lazy val db = dbConfig.db

  private val DUMMY_TOKEN = "dummy-token"

  private def getFirstUser = UserInfoDao.findFirstByFilter(_.id > 0L).get

  private def getFirstEvent = EventInfoDao.findFirstByFilter(_.id > 0L).get

  private def getFirstReactionType = EventReactionTypeDao.findFirstByFilter(_.id > 0L).get

  object TestController extends Controller {
    def login() = Action {
      Cache.set(DUMMY_TOKEN + "profile", getFirstUser)
      Ok("ok")
    }
  }

  before {
    createTablesIfNeeded()
    truncateDatabases()
    setupData()
  }

  implicit class FakeRequestExtension(request: FakeRequest[AnyContentAsEmpty.type]) {
    def withDummyToken() = {
      request.withSession("idToken" -> DUMMY_TOKEN)
    }
    def withXHR() = {
      request.withHeaders("X-Requested-With" -> "XMLHttpRequest")
    }
  }

  def getLoginRequiredMethods = {
    val eventId = getFirstEvent.id.get
    Iterable(
      controllers.member.page.User.index(),
      controllers.member.page.EventPage.listAll(1, 10),
      controllers.member.page.EventPage.listAssigned(1, 10),
      controllers.member.page.EventPage.listNotAssigned(1, 10),
      controllers.member.page.EventPage.listSearch("", "", 1, 10),
      controllers.member.page.EventPage.edit(eventId),
      controllers.member.page.EventPage.view(eventId),
      controllers.member.page.EventPage.create()
    )
  }

  def getLoginAndAjaxRequiredMethods = {
    val eventId = getFirstEvent.id.get
    val reactionId = getFirstReactionType.id.get
    Iterable(
      controllers.member.api.EventTagApi.addTag(""),
      controllers.member.api.EventTagApi.getAllTags,
      controllers.member.api.EventApi.toggleReaction(eventId, reactionId)
    )
  }

  "Non-authenticate user" should {
    "must redirect to top page without login" in {
      getLoginRequiredMethods.foreach(action => {
        val result = action.apply(FakeRequest().withDummyToken())
        status(result) mustBe SEE_OTHER
      })
    }

    "must return 200 with login" in {
      TestController.login().apply(FakeRequest())
      getLoginRequiredMethods.foreach(action => {
        val result = action.apply(FakeRequest().withDummyToken())
        status(result) mustBe OK
      })
    }

    "required login from ajax" in {
      getLoginAndAjaxRequiredMethods.foreach(action => {
        val result = action.apply(FakeRequest().withXHR())
        header("X-AJAX-REDIRECT", result).isDefined mustBe true
      })
    }

    "required login (ajax method)" in {
      TestController.login().apply(FakeRequest())
      getLoginAndAjaxRequiredMethods.foreach(action => {
        val result = action.apply(FakeRequest().withXHR().withDummyToken())
        header("X-AJAX-REDIRECT", result).isEmpty mustBe true
      })
    }
  }
}
