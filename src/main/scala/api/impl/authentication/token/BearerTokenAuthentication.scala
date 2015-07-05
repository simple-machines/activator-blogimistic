package api.impl.authentication.token

import data.DatabaseSupport
import data.impl.DAL
import model.impl.User

import scala.concurrent.{ExecutionContext, Future}

trait BearerTokenAuthentication {

  implicit def dbDispatcher: ExecutionContext
  def dataAccess: DAL with DatabaseSupport

  def bearerToken = BearerToken(
    TokenAuthenticator[User] {
      case None => Future(None)
      case Some(t) => dataAccess.db.run(dataAccess.users.findByToken(t))
    }
  )
}
