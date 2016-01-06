package controllers.member.page

import _root_.utils.Global
import controllers.utils.AuthenticateUtil
import controllers.validator.CustomConstraints
import dao.utils.DatabaseAccessor
import DatabaseAccessor.jdbcProfile.api._
import dao._
import helpers.{Auth0Config, ChatworkConfig}
import models._
import play.api.Play
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}

object EventPage extends AuthenticateUtil {

  object ListType extends Enumeration { val All, Assigned, NotAssigned = Value }

  private def getList(page: Int, size: Int, listType: ListType.Value, searchTagOrNone: Option[EventTag] = None, queryOrNone: Option[String] = None)(implicit request: Request[AnyContent]): Result = {
    val (events, numPages) = listType match {
      case ListType.All =>
        searchTagOrNone match {
          case Some(searchTag) =>
            val eventIds = EventTagRelationDao.findByFilter(_.eventTagId === searchTag.id.get).map(_.eventInfoId)
            EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(_._1.id inSet eventIds)(page, size)
          case _ =>
            queryOrNone match {
              case Some(query) =>
                EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(_._1.title.toUpperCase like s"%${query.toUpperCase}%")(page, size)
              case _ =>
                EventInfoDao.getPaginationAndNumPagesWithReactionNum(page, size)
            }
        }
      case ListType.Assigned =>
        EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(_._1.userInfoIdOrNone.isDefined)(page, size)
      case ListType.NotAssigned =>
        EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(_._1.userInfoIdOrNone.isEmpty)(page, size)
    }

    val eventWithReactions = events.map({eventWithNumReactions =>
      val ev = eventWithNumReactions._1
      val authorOrNone = ev.authorIdOrNone.flatMap(UserInfoDao.findById)
      val reactions = EventReactionDao.findByEventInfoId(ev.id.get)
      val tags = EventTagRelationDao.findTagsByEventInfoId(ev.id.get)
      EventInfoWithReaction(ev, authorOrNone, reactions, tags)
    })

    val assignedNum = EventInfoDao.getSizeOfUserAssigned()
    val notAssignedNum = EventInfoDao.getSizeOfUserNotAssigned()

    Ok(views.html.event.list(request.flash, getUserInfoOrNone, eventWithReactions, assignedNum, notAssignedNum, searchTagOrNone, queryOrNone)(page, size, numPages))
  }

  def listAll(page: Int, size: Int) = Action { implicit request =>
    getList(page, size, ListType.All)
  }

  def listAssigned(page: Int, size: Int) = Action { implicit request =>
    getList(page, size, ListType.Assigned)
  }

  def listNotAssigned(page: Int, size: Int) = Action { implicit request =>
    getList(page, size, ListType.NotAssigned)
  }

  def listSearch(tag: String, query: String, page: Int, size: Int) = Action { implicit request =>
    if (!tag.isEmpty) {
      EventTagDao.findByTagName(tag) match {
        case Some(eventTag) =>
          getList(page, size, ListType.All, Some(eventTag))
        case _ =>
          Ok(views.html.event.list(request.flash, getUserInfoOrNone, Iterable.empty, 0, 0, None, Some(query))(page, size, 0))
      }
    } else {
      if (!query.isEmpty) {
        getList(page, size, ListType.All, None, Some(query))
      } else {
        Ok(views.html.event.list(request.flash, getUserInfoOrNone, Iterable.empty, 0, 0, None, Some(query))(page, size, 0))
      }
    }
  }

  def view(eventId: Long) = AuthenticatedAction { implicit request =>
    implicit val userInfo = getUserInfoOrNone.get
    EventInfoDao.findById(eventId) match {
      case Some(eventInfo) =>
        val event = EventInfoForForm(eventInfo)
        val authorOrNone = eventInfo.authorIdOrNone.flatMap(UserInfoDao.findById)
        val tags = EventTagRelationDao.findTagsByEventInfoId(eventId)
        val reactions = EventReactionDao.findByEventInfoId(event.id.get)
        Ok(views.html.event.view(request.flash, Some(userInfo), eventInfo, reactions, authorOrNone, tags, eventForm.fill(event)))
      case _ => NotFound("")
    }
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

  def create() = AuthenticatedAction { implicit request =>
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
    val tags = Iterable.empty
    Ok(views.html.event.edit(request.flash, getUserInfoOrNone, None, None, tags, eventForm.fill(newEvent)))
  }

  def edit(eventId: Long) = AuthenticatedAction { implicit request =>
    implicit val userInfo = getUserInfoOrNone.get
    EventInfoDao.findById(eventId) match {
      case Some(eventInfo) =>
        val event = EventInfoForForm(eventInfo)
        val authorOrNone = eventInfo.authorIdOrNone.flatMap(UserInfoDao.findById)
        val tags = EventTagRelationDao.findTagsByEventInfoId(eventId)
        Ok(views.html.event.edit(request.flash, Some(userInfo), Some(eventInfo), authorOrNone, tags, eventForm.fill(event)))
      case _ => NotFound("")
    }
  }

  def postData() = AuthenticatedAction { implicit request =>
    val userInfo = getUserInfoOrNone.get
    eventForm.bindFromRequest.fold(
      formWithErrors => {
        val maybeEventInfo = formWithErrors("id").value.flatMap(idStr => EventInfoDao.findById(idStr.toLong))
        val authorOrNone = maybeEventInfo.flatMap(_.authorIdOrNone.flatMap(UserInfoDao.findById))
        val tags = maybeEventInfo match {
          case Some(eventInfo) => EventTagRelationDao.findTagsByEventInfoId(eventInfo.id.get)
          case _ => Iterable.empty
        }
        val errForm = formWithErrors.withGlobalError("event.edit.failed")
        BadRequest(views.html.event.edit(request.flash, Some(userInfo), maybeEventInfo, authorOrNone, tags, errForm))
      },
      formEventInfo => {
        val newEventInfo = if (formEventInfo.registerMe) {
          formEventInfo.copy(authorIdOrNone = userInfo.id).toEventInfo
        } else {
          formEventInfo.copy(authorIdOrNone = None).toEventInfo
        }
        val successMessage = newEventInfo.id match {
          case Some(eventInfoId) =>
            EventInfoDao.update(newEventInfo)
            EventTagRelationDao.deleteByFilter(_.eventInfoId === eventInfoId)
            formEventInfo.tags.foreach(tagId => {
              EventTagRelationDao.create(EventTagRelation(eventInfoId, tagId))
            })
            postChatWork("message.event.updated", eventInfoId, newEventInfo)
            Messages("event.edit.success")
          case _ =>
            EventInfoDao.create(newEventInfo) match {
              case Some(eventInfoId) =>
                formEventInfo.tags.foreach(tagId => {
                  EventTagRelationDao.create(EventTagRelation(eventInfoId, tagId))
                })

                postChatWork("message.new.event.created", eventInfoId, newEventInfo)
                Messages("event.create.success")
              case _ =>
                Messages("event.create.failed")
            }
        }

        Redirect(controllers.member.page.routes.EventPage.listAll()).flashing("success" -> successMessage)
      }
    )
  }

  private def postChatWork(messageKey: String, eventId: Long, event: EventInfo)(implicit request: Request[AnyContent]): Unit = {
    val author = event.authorIdOrNone.flatMap(UserInfoDao.findById).map(_.fullName).getOrElse(Messages("event.no.user"))
    val title = event.title
    val desc = event.description
    val tags = EventTagRelationDao.findTagsByEventInfoId(eventId).map(_.text).mkString(",")
    val url =
      if (Play.isProd) {
        Auth0Config.get().callbackURL.replace("/callback", controllers.member.page.routes.EventPage.view(eventId).url)
      } else {
        controllers.member.page.routes.EventPage.view(eventId).absoluteURL()
      }
    val config = ChatworkConfig.get()
    Global.chatworkClient.foreach(cw => {
      cw.postRoomMessage(config.roomId, Messages(messageKey, author, title, desc, tags, url))
    })
  }

}
