package api.impl.authentication.token

object TokenAuthenticator {

  def apply[T](f: TokenAuthenticator[T]) = f
}
