package api.impl.authentication

import scala.concurrent.Future

package object token {
  type TokenAuthenticator[T] = Option[String] => Future[Option[T]]
}
