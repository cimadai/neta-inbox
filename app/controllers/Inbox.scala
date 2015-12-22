package controllers

import dao.{UserInfoDao, EventReactionDao, EventInfoDao}
import models.EventInfoWithReaction
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Action

object Inbox extends AuthenticateUtil {
  EventInfoDao.createDDL

  //def list(page: Int, size: Int) = AuthenticatedAction { implicit request =>
  def list(page: Int, size: Int) = Action { implicit request =>
    val events = EventInfoDao.getPagination(page, size)
    val eventWithReactions = events._1.map({ev =>
      val authorOrNone = ev.authorIdOrNone.flatMap(UserInfoDao.findById)
      EventInfoWithReaction(ev, authorOrNone, EventReactionDao.findByEventInfoId(ev.id.get))
    })
    val dummy = UserInfoDao.findById(1).get
    // Ok(views.html.Inbox.list(request.flash, getUserInfo, eventWithReactions))
    Ok(views.html.Inbox.list(request.flash, dummy, eventWithReactions))
  }

  // def postInbox(inboxItem: InboxItem)

}
