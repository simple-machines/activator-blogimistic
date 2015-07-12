package api

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.Logging._
import spray.http.{HttpRequest, HttpResponse}
import spray.routing._
import spray.routing.directives.LogEntry

object ApiServiceActor {

  def props(routes: Route) = Props.create(classOf[ApiServiceActor], routes)
}

class ApiServiceActor(routes: Route) extends HttpService with Actor with ActorLogging with CORSSupport with CustomErrorHandling {

  implicit def eh = exceptionHandler
  implicit def rh = rejectionHandler

  def actorRefFactory = context

  def receive: Receive = runRoute(
    logRequestResponse(showRequestResponses _)(routes)
  )

  /**
   * Log each request and response.
   */
  def showRequestResponses(request: HttpRequest): Any => Option[LogEntry] = {
    case HttpResponse(status, _, _, _) => Some(LogEntry(s"${request.method} ${request.uri} ($status)", InfoLevel))
    case response => Some(LogEntry(s"${request.method} ${request.uri} $response", WarningLevel))
  }
}
