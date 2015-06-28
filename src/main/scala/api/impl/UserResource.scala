package api.impl

import akka.actor.ActorRefFactory
import api.impl.authentication.token.BearerTokenAuthentication
import data.DatabaseSupport
import data.impl.DAL
import model.impl.{FbUserPicture, FbAuthResponse, FbUser, User}
import spray.client.pipelining._
import spray.http.HttpHeaders.Accept
import spray.http.MediaTypes._
import spray.http.{HttpRequest, MediaTypes}
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UserResource(val dataAccess: DAL with DatabaseSupport)(implicit val dbDispatcher: ExecutionContext, val actorRefFactory: ActorRefFactory)
  extends HttpService with BearerTokenAuthentication {

  import JsonProtocol._
  import SprayJsonSupport._
  import dataAccess._

  private val fbUserPipeline: HttpRequest => Future[FbUser] = sendReceive ~> unmarshal[FbUser]
  private val fbUserPicturePipeline: HttpRequest => Future[FbUserPicture] = sendReceive ~> unmarshal[FbUserPicture]

  val routes = {
    pathPrefix("users") {
      path("fbauth") {
        post {
          entity(as[FbAuthResponse]) { authResponse =>
            ctx => {
              val futureToken = for {
                fbUser <- fbUserPipeline(Get(s"https://graph.facebook.com/v2.3/me?access_token=${authResponse.accessToken}").
                  withHeaders(Accept(MediaTypes.`application/json`)))
                fbUserPicture <- fbUserPicturePipeline(Get(s"https://graph.facebook.com/v2.3/me/picture?redirect=false&access_token=${authResponse.accessToken}").
                  withHeaders(Accept(MediaTypes.`application/json`)))
                user <- db.run(Users.upsertByFacebookId(
                  new User(facebookId = fbUser.id, name = fbUser.first_name, email = fbUser.email, picture = fbUserPicture.data.flatMap(_.url))))
                token <- db.run(Tokens.generateToken(user.id.get))
              } yield token
              futureToken onComplete {
                case Success(token) => ctx.complete(token)
                case Failure(ex) => ctx.failWith(ex)
              }
            }
          }
        }
      } ~
      path("me") {
        get {
          authenticate(bearerToken) { user =>
            respondWithMediaType(`application/json`) {
              complete {
                user
              }
            }
          }
        }
      }
    }
  }
}
