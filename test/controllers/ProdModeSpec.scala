package controllers

import _root_.utils.{FakeLoginController, SpecsCommon}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import slick.driver.JdbcProfile
import FakeLoginController.FakeRequestExtension

class ProdModeSpec extends PlaySpec with OneServerPerSuite with SpecsCommon {

  private val configurations = inMemoryDatabase("play")
  implicit override lazy val app = new FakeApplication(additionalConfiguration = configurations) {
    override val mode = play.api.Mode.Prod
  }
  implicit override lazy val dbConfig = DatabaseConfigProvider.get[JdbcProfile](app)
  implicit override lazy val db = dbConfig.db

  "Non-authenticate user" should {
    "show default page without login" in {
      val result = controllers.nonmember.page.PublicPage.index.apply(FakeRequest())
      status(result) mustBe OK
    }
  }

  "Authenticated user" should {
    "can logout" in {
      FakeLoginController.login.apply(FakeRequest())
      val result = controllers.nonmember.page.PublicPage.logout.apply(FakeRequest().withDummyToken())
      status(result) mustBe SEE_OTHER
    }
  }
}
