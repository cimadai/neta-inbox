package controllers.member.page

import controllers.utils.AuthenticateUtil
import controllers.validator.CustomConstraints
import dao.utils.DatabaseAccessor
import DatabaseAccessor.jdbcProfile.api._
import dao.{EventTagRelationDao, EventInfoDao, EventReactionDao, UserInfoDao}
import models._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, Request, Result}

object EventPage extends AuthenticateUtil {

  object ListType extends Enumeration { val All, Assigned, NotAssigned = Value }

  private def getList(page: Int, size: Int, listType: ListType.Value)(implicit request: Request[_]): Result = {
    println(listType)
    val (events, numPages) = listType match {
      case ListType.All =>
        val pages = EventInfoDao.getPagination(page, size)
        val numPages = EventInfoDao.numPages(size)
        (pages, numPages)
      case ListType.Assigned =>
        val pages = EventInfoDao.getPaginationByFilter(_.userInfoIdOrNone.isDefined)(page, size)
        val numPages = EventInfoDao.numPagesByFilter(_.userInfoIdOrNone.isDefined)(size)
        (pages, numPages)
      case ListType.NotAssigned =>
        val pages = EventInfoDao.getPaginationByFilter(_.userInfoIdOrNone.isEmpty)(page, size)
        val numPages = EventInfoDao.numPagesByFilter(_.userInfoIdOrNone.isDefined)(size)
        (pages, numPages)
    }

    val eventWithReactions = events._1.map({ev =>
      val authorOrNone = ev.authorIdOrNone.flatMap(UserInfoDao.findById)
      val reactions = EventReactionDao.findByEventInfoId(ev.id.get)
      val tags = EventTagRelationDao.findTagsByEventInfoId(ev.id.get)
      EventInfoWithReaction(ev, authorOrNone, reactions, tags)
    })

    val assignedNum = EventInfoDao.getSizeOfUserAssigned()
    val notAssignedNum = EventInfoDao.getSizeOfUserNotAssigned()

    val userInfo = UserInfoDao.findById(1).get
    Ok(views.html.event.list(request.flash, userInfo, eventWithReactions, assignedNum, notAssignedNum)(page, size, numPages))
  }

  //def list(page: Int, size: Int) = AuthenticatedAction { implicit request =>
  def listAll(page: Int, size: Int) = Action { implicit request =>
    // Ok(views.html.Inbox.list(request.flash, getUserInfo, eventWithReactions))
    getList(page, size, ListType.All)
  }

  def listAssigned(page: Int, size: Int) = Action { implicit request =>
    getList(page, size, ListType.Assigned)
  }

  def listNotAssigned(page: Int, size: Int) = Action { implicit request =>
    getList(page, size, ListType.NotAssigned)
  }

  val eventForm = Form {
    mapping(
      "id" -> optional(longNumber),
      "eventType" -> CustomConstraints.ofEventType,
      "title" -> text(maxLength = 50),
      "description" -> text,
      "authorIdOrNone" -> optional(longNumber),
      "publishDateUnixMillis" -> longNumber,
      "status" -> CustomConstraints.ofEventStatus
    )(EventInfo.apply)(EventInfo.unapply)
  }

  def create() = Action { implicit request =>
    val newEvent = EventInfo(
      None,
      EventType.None,
      "New Event",
      "",
      None,
      0,
      EventStatus.New
    )
    val userInfo = UserInfoDao.findById(1).get
    Ok(views.html.event.edit(request.flash, userInfo, None, eventForm.fill(newEvent)))
  }

  def edit(eventId: Long) = Action { implicit request =>
    val userInfo = UserInfoDao.findById(1).get
    EventInfoDao.findById(eventId) match {
      case Some(eventInfo) =>
        Ok(views.html.event.edit(request.flash, userInfo, Some(eventInfo), eventForm.fill(eventInfo)))
      case _ => NotFound("")
    }
  }

  def postData() = Action { implicit request =>
    val userInfo = UserInfoDao.findById(1).get
    eventForm.bindFromRequest.fold(
      formWithErrors => {
        val maybeEventInfo = formWithErrors("id").value.flatMap(idStr => EventInfoDao.findById(idStr.toLong))
        val errForm = formWithErrors.withGlobalError("event.edit.failed")
        BadRequest(views.html.event.edit(request.flash, userInfo, maybeEventInfo, errForm))
      },
      formEventInfo => {
        val successMessage = formEventInfo.id match {
          case Some(syncJobId) =>
            Messages("event.edit.success")
          case _ =>
            Messages("event.create.success")
        }

        EventInfoDao.update(formEventInfo)
        Redirect(controllers.member.page.routes.EventPage.listAll()).flashing("success" -> successMessage)
      }
    )
  }

}
