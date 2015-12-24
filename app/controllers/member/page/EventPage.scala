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
      "status" -> CustomConstraints.ofEventStatus,
      "tags" -> list(longNumber),
      "registerMe" -> boolean
    )(EventInfoForForm.apply)(EventInfoForForm.unapply)
  }

  def create() = Action { implicit request =>
    val newEvent = EventInfoForForm(
      None,
      EventType.None,
      "New Event",
      "",
      None,
      0,
      EventStatus.New,
      List.empty,
      registerMe = true
    )
    val userInfo = UserInfoDao.findById(1).get
    val tags = Iterable.empty
    Ok(views.html.event.edit(request.flash, userInfo, None, tags, eventForm.fill(newEvent)))
  }

  def edit(eventId: Long) = Action { implicit request =>
    implicit val userInfo = UserInfoDao.findById(1).get
    EventInfoDao.findById(eventId) match {
      case Some(eventInfo) =>
        val event = EventInfoForForm(eventInfo)
        val tags = EventTagRelationDao.findTagsByEventInfoId(eventId)
        Ok(views.html.event.edit(request.flash, userInfo, Some(eventInfo), tags, eventForm.fill(event)))
      case _ => NotFound("")
    }
  }

  def postData() = Action { implicit request =>
    val userInfo = UserInfoDao.findById(1).get
    eventForm.bindFromRequest.fold(
      formWithErrors => {
        val maybeEventInfo = formWithErrors("id").value.flatMap(idStr => EventInfoDao.findById(idStr.toLong))
        val tags = maybeEventInfo match {
          case Some(eventInfo) => EventTagRelationDao.findTagsByEventInfoId(eventInfo.id.get)
          case _ => Iterable.empty
        }
        val errForm = formWithErrors.withGlobalError("event.edit.failed")
        BadRequest(views.html.event.edit(request.flash, userInfo, maybeEventInfo, tags, errForm))
      },
      formEventInfo => {
        formEventInfo.authorIdOrNone
        val newEventInfo = if (formEventInfo.registerMe) {
          formEventInfo.copy(authorIdOrNone = userInfo.id).toEventInfo
        } else {
          formEventInfo.toEventInfo
        }
        val successMessage = newEventInfo.id match {
          case Some(eventInfoId) =>
            EventInfoDao.update(newEventInfo)
            EventTagRelationDao.deleteByFilter(_.eventInfoId === eventInfoId)
            formEventInfo.tags.foreach(tagId => {
              EventTagRelationDao.create(EventTagRelation(eventInfoId, tagId))
            })
            Messages("event.edit.success")
          case _ =>
            EventInfoDao.create(newEventInfo) match {
              case Some(eventInfoId) =>
                formEventInfo.tags.foreach(tagId => {
                  EventTagRelationDao.create(EventTagRelation(eventInfoId, tagId))
                })
                Messages("event.create.success")
              case _ =>
                Messages("event.create.failed")
            }
        }

        Redirect(controllers.member.page.routes.EventPage.listAll()).flashing("success" -> successMessage)
      }
    )
  }

}
