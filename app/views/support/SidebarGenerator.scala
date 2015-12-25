package views.support

import dao.EventTagRelationDao
import models.UserInfo
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Messages
import play.twirl.api.Html
import slick.driver.JdbcProfile
import views.html.sidebars._

object SidebarGenerator {
  private implicit val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  val emptySidebarContents = Iterable.empty[Html]

  def generateBasicSidebarContents(loginUserOrNone: Option[UserInfo])(implicit m: Messages): Iterable[Html] = {
    val eventTagGroupsList = EventTagRelationDao.findTagsCountAsGroupByTag()
    Iterable(sidebar_searchbox(), sidebar_tagcloud(eventTagGroupsList))
  }

}
