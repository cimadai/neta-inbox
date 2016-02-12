package controllers.member.page

import _root_.utils.Global
import controllers.utils.AuthenticateUtil
import controllers.validator.CustomConstraints
import dao.utils.DaoBase.EventInfoTable
import dao.utils.DatabaseAccessor
import DatabaseAccessor.jdbcProfile.api._
import dao._
import helpers.{SlackConfig, Auth0Config, ChatworkConfig}
import models._
import play.api.Play
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{AnyContent, Request, Result}

object EventPage extends AuthenticateUtil {

  object ListType extends Enumeration { val All, Assigned, NotAssigned = Value }
  object EventPublishType extends Enumeration { val All, InPast, InFuture = Value }

  val eventForm = Form {
    mapping(
      "id" -> optional(longNumber),
      "eventType" -> CustomConstraints.ofEventType,
      "title" -> text(maxLength = 50),
      "description" -> text,
      "authorIdOrNone" -> optional(longNumber),
      "publishDateUnixMillis" -> default(longNumber, 0L),
      "status" -> CustomConstraints.ofEventStatus,
      "duration" -> default(longNumber, 0L),
      "tags" -> list(longNumber),
      "registerMe" -> boolean
    )(EventInfoForForm.apply)(EventInfoForForm.unapply)
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
  private def genPublishDateFilter(ev: (EventInfoTable, Rep[Int]), publishType: EventPublishType.Value): Rep[Boolean] = {
    genPublishDateFilter(ev._1, publishType)
  }
  private def getList(page: Int, size: Int, listType: ListType.Value, publishType: EventPublishType.Value, searchTagOrNone: Option[EventTag] = None, queryOrNone: Option[String] = None)(implicit request: Request[AnyContent]): Result = {
    val (events, numPages) = listType match {
      case ListType.All =>
        searchTagOrNone match {
          case Some(searchTag) =>
            val eventIds = EventTagRelationDao.findByFilter(_.eventTagId === searchTag.id.get).map(_.eventInfoId)
            EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(ev => {
              genPublishDateFilter(ev, publishType) && (ev._1.id inSet eventIds)
            })(page, size)
          case _ =>
            queryOrNone match {
              case Some(query) =>
                EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(ev => {
                  genPublishDateFilter(ev, publishType) && (ev._1.title.toUpperCase like s"%${query.toUpperCase}%")
                })(page, size)
              case _ =>
                EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(ev => {
                  genPublishDateFilter(ev, publishType)
                })(page, size)
            }
        }
      case ListType.Assigned =>
        EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(ev => {
          genPublishDateFilter(ev, publishType) && ev._1.userInfoIdOrNone.isDefined
        })(page, size)
      case ListType.NotAssigned =>
        EventInfoDao.getPaginationAndNumPagesWithReactionNumByFilter(ev => {
          genPublishDateFilter(ev, publishType) && ev._1.userInfoIdOrNone.isEmpty
        })(page, size)
    }

    val eventWithReactions = events.map({eventWithNumReactions =>
      val ev = eventWithNumReactions._1
      val authorOrNone = ev.authorIdOrNone.flatMap(UserInfoDao.findById)
      val reactions = EventReactionDao.findByEventInfoId(ev.id.get)
      val tags = EventTagRelationDao.findTagsByEventInfoId(ev.id.get)
      EventInfoWithReaction(ev, authorOrNone, reactions, tags)
    })

    val assignedNum = EventInfoDao.countByFilter(ev => { genPublishDateFilter(ev, EventPublishType.InFuture) && ev.userInfoIdOrNone.isDefined })
    val notAssignedNum = EventInfoDao.countByFilter(ev => { genPublishDateFilter(ev, EventPublishType.InFuture) && ev.userInfoIdOrNone.isEmpty })
    val pastEventNum = EventInfoDao.countByFilter(ev => { genPublishDateFilter(ev, EventPublishType.InPast) })

    Ok(views.html.event.list(request.flash, getUserInfoOrNone, eventWithReactions, assignedNum, notAssignedNum, pastEventNum, searchTagOrNone, queryOrNone)(page, size, numPages))
  }

  def listAll(page: Int, size: Int) = AuthenticatedAction { implicit request =>
    getList(page, size, ListType.All, EventPublishType.InFuture)
  }

  def listAssigned(page: Int, size: Int) = AuthenticatedAction { implicit request =>
    getList(page, size, ListType.Assigned, EventPublishType.InFuture)
  }

  def listNotAssigned(page: Int, size: Int) = AuthenticatedAction { implicit request =>
    getList(page, size, ListType.NotAssigned, EventPublishType.InFuture)
  }

  def listAllPast(page: Int, size: Int) = AuthenticatedAction { implicit request =>
    getList(page, size, ListType.All, EventPublishType.InPast)
  }

  def listSearch(tag: String, query: String, page: Int, size: Int) = AuthenticatedAction { implicit request =>
    if (!tag.isEmpty) {
      EventTagDao.findByTagName(tag) match {
        case Some(eventTag) =>
          getList(page, size, ListType.All, EventPublishType.All, Some(eventTag))
        case _ =>
          Ok(views.html.event.list(request.flash, getUserInfoOrNone, Iterable.empty, 0, 0, 0, None, Some(query))(page, size, 0))
      }
    } else {
      if (!query.isEmpty) {
        getList(page, size, ListType.All, EventPublishType.All, None, Some(query))
      } else {
        Ok(views.html.event.list(request.flash, getUserInfoOrNone, Iterable.empty, 0, 0, 0, None, Some(query))(page, size, 0))
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
      case _ =>
        Ok(views.html.event.notfound(request.flash, Some(userInfo)))
    }
  }

  def create() = AuthenticatedAction { implicit request =>
    val newEvent = EventInfoForForm(
      None,
      EventType.None,
      "",
      "",
      None,
      0,
      EventStatus.New.value,
      0,
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
          formEventInfo.id match {
            case Some(eventInfoId) =>
              EventInfoDao.findById(eventInfoId) match {
                case Some(event) => if (event.authorIdOrNone == userInfo.id) {
                  // 元が自分なら解除できる。
                  formEventInfo.copy(authorIdOrNone = None).toEventInfo
                } else {
                  formEventInfo.toEventInfo
                }
                case _ =>
                  formEventInfo.toEventInfo
              }
            case _ =>
              formEventInfo.toEventInfo
          }
        }
        val successMessage = newEventInfo.id match {
          case Some(eventInfoId) =>
            EventInfoDao.update(newEventInfo)
            EventTagRelationDao.deleteByFilter(_.eventInfoId === eventInfoId)
            formEventInfo.tags.foreach(tagId => {
              EventTagRelationDao.create(EventTagRelation(eventInfoId, tagId))
            })
            postChatWork("message.event.updated.chatwork", eventInfoId, newEventInfo)
            postSlack("message.event.updated.slack", eventInfoId, newEventInfo)
            Messages("event.edit.success")
          case _ =>
            EventInfoDao.create(newEventInfo) match {
              case Some(eventInfoId) =>
                formEventInfo.tags.foreach(tagId => {
                  EventTagRelationDao.create(EventTagRelation(eventInfoId, tagId))
                })

                postChatWork("message.new.event.created.chatwork", eventInfoId, newEventInfo)
                postSlack("message.new.event.created.slack", eventInfoId, newEventInfo)
                Messages("event.create.success")
              case _ =>
                Messages("event.create.failed")
            }
        }

        Redirect(controllers.member.page.routes.EventPage.listAll()).flashing("success" -> successMessage)
      }
    )
  }

  /**
   * 投稿用データの作成
   */
  private def createPostData(eventId: Long, event: EventInfo)(implicit request: Request[AnyContent]): (String, String, String, String, String, String, String) = {
    val author = event.authorIdOrNone.flatMap(UserInfoDao.findById).map(_.fullName).getOrElse(Messages("event.no.user"))
    val title = event.title
    val desc = event.description
    val date = views.support.ViewSupport.formatDateTimeOrDefault(event.publishDateUnixMillis, Messages("event.date.undefined"))
    val duration = if (event.duration > 0) {
      Messages("event.duration.as", event.duration)
    } else {
      Messages("event.duration.undefined")
    }
    val tags = EventTagRelationDao.findTagsByEventInfoId(eventId).map(_.text).mkString(",")
    val url =
      if (Play.isProd) {
        s"${Auth0Config.get().baseURL}${controllers.member.page.routes.EventPage.view(eventId).url}"
      } else {
        controllers.member.page.routes.EventPage.view(eventId).absoluteURL()
      }
    (author, title, desc, date, duration, tags, url)
  }

  /**
   * Chatworkへの投稿
   */
  private def postChatWork(messageKey: String, eventId: Long, event: EventInfo)(implicit request: Request[AnyContent]): Unit = {
    val (author, title, desc, date, duration, tags, url) = createPostData(eventId, event)
    Global.chatworkClient.foreach(cw => {
      val config = ChatworkConfig.get()
      cw.postRoomMessage(config.roomId, Messages(messageKey, author, title, desc, date, duration, tags, url))
    })
  }

  /**
   * Slackへの投稿
   */
  private def postSlack(messageKey: String, eventId: Long, event: EventInfo)(implicit request: Request[AnyContent]): Unit = {
    val (author, title, desc, date, duration, tags, url) = createPostData(eventId, event)
    Global.slackClient.foreach(slack => {
      val config = SlackConfig.get()
      slack.chat.postMessage(config.channelName, Messages(messageKey, author, title, desc, date, duration, tags, url))
    })
  }

}
