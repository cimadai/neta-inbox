package controllers

import _root_.utils.{Global, FakeLoginController, SpecsCommon}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import slick.driver.JdbcProfile
import FakeLoginController.FakeRequestExtension

class DevModeSpec extends PlaySpec with OneServerPerSuite with SpecsCommon {

  private val configurations = inMemoryDatabase("play")
  implicit override lazy val app = FakeApplication(additionalConfiguration = configurations)
  implicit override lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
  implicit override lazy val db = dbConfig.db

  def getLoginRequiredMethods = {
    val eventId = SpecsCommon.getFirstEvent.id.get
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
    val eventId = SpecsCommon.getFirstEvent.id.get
    val reactionId = SpecsCommon.getFirstReactionType.id.get
    Iterable(
      controllers.member.api.EventTagApi.addTag(""),
      controllers.member.api.EventTagApi.getAllTags,
      controllers.member.api.EventApi.toggleReaction(eventId, reactionId)
    )
  }

  "Non-authenticate user" should {
    "show default page without login" in {
      val result = controllers.nonmember.page.PublicPage.index.apply(FakeRequest())
      status(result) mustBe OK
    }

    "show default page with login" in {
      FakeLoginController.login.apply(FakeRequest())
      val result = controllers.nonmember.page.PublicPage.index.apply(FakeRequest().withDummyToken())
      status(result) mustBe SEE_OTHER
      val url = controllers.member.page.routes.EventPage.listAll(1, 10).url
      header("location", result) mustBe Some(url)
    }

    "must redirect to top page without login" in {
      getLoginRequiredMethods.foreach(action => {
        val result = action.apply(FakeRequest().withDummyToken())
        status(result) mustBe SEE_OTHER
      })
    }

    "must return 200 with login" in {
      FakeLoginController.login.apply(FakeRequest())
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
      FakeLoginController.login.apply(FakeRequest())
      getLoginAndAjaxRequiredMethods.foreach(action => {
        val result = action.apply(FakeRequest().withXHR().withDummyToken())
        header("X-AJAX-REDIRECT", result).isEmpty mustBe true
      })
    }
  }

  "EventPage" should {
    "remove author by self" in {
      val user0 = SpecsCommon.getUserByNickName("test0")
      FakeLoginController.loginAs(user0).apply(FakeRequest())
      val action = controllers.member.page.EventPage.postData()
      val event = SpecsCommon.getFirstEvent
      val result = action.apply(FakeRequest().withDummyToken().withFormUrlEncodedBody(
        "id" -> event.id.get.toString,
        "eventType" -> "1",
        "title" -> "test",
        "description" -> "test",
        "authorIdOrNone" -> user0.id.get.toString,
        "publishDateUnixMillis" -> "0",
        "status" -> "1",
        "duration" -> "0",
        "tags" -> "[]",
        "registerMe" -> "false"))
      val eventAfter = SpecsCommon.getFirstEvent
      eventAfter.authorIdOrNone mustBe None
    }

    "remove author by other" in {
      val user0 = SpecsCommon.getUserByNickName("test0")
      val user1 = SpecsCommon.getUserByNickName("test1")
      FakeLoginController.loginAs(user1).apply(FakeRequest())
      val action = controllers.member.page.EventPage.postData()
      val event = SpecsCommon.getFirstEvent
      val result = action.apply(FakeRequest().withDummyToken().withFormUrlEncodedBody(
        "id" -> event.id.get.toString,
        "eventType" -> "1",
        "title" -> "test",
        "description" -> "test",
        "authorIdOrNone" -> user0.id.get.toString,
        "publishDateUnixMillis" -> "0",
        "status" -> "1",
        "duration" -> "0",
        "tags" -> "[]",
        "registerMe" -> "false"))
      val eventAfter = SpecsCommon.getFirstEvent
      eventAfter.authorIdOrNone mustBe user0.id
    }
  }

  "Slack api" should {
    "run right" in {
      Global.slackClient.foreach(slack => {
        val resp = slack.channels.list()
        resp.ok mustBe true
      })
    }
  }
}
