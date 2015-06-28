package api.impl.authentication.token

import spray.http.HttpHeaders.`WWW-Authenticate`
import spray.http._
import spray.routing.RequestContext
import spray.routing.authentication.HttpAuthenticator

import scala.concurrent.{Future, ExecutionContext}

class BearerTokenAuthenticator[T](tokenAuthenticator: TokenAuthenticator[T])(implicit val executionContext: ExecutionContext) extends HttpAuthenticator[T] {
  def authenticate(credentials: Option[HttpCredentials], ctx: RequestContext): Future[Option[T]] = {
    tokenAuthenticator {
      credentials.flatMap {
        case OAuth2BearerToken(token) => Some(token)
        case _ => None
      }
    }
  }

  def getChallengeHeaders(httpRequest: HttpRequest): List[HttpHeader] =
    `WWW-Authenticate`(HttpChallenge(scheme = "Bearer", realm = "", params = Map.empty)) :: Nil
}

object BearerToken {
  def apply[T](tokenStore: TokenAuthenticator[T])(implicit ec: ExecutionContext) = new BearerTokenAuthenticator(tokenStore)
}
