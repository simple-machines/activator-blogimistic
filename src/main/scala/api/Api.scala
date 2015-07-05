package api

import api.impl.{BlogResource, UserResource}
import core.Core
import data.DatabaseSupport
import data.impl.DAL
import spray.routing.Directives

import scala.concurrent.ExecutionContext

trait Api extends Directives with CORSSupport { this: DatabaseSupport with DAL with Core =>

  // provide an dedicated dispatcher to make database calls so the blocking is isolated to that execution context
  implicit val dbDispatcher: ExecutionContext = system.dispatchers.lookup("contexts.db-operations")

  val userResource = new UserResource(this)
  val blogResource = new BlogResource(this)

  val routes =
    cors {
      userResource.routes ~ blogResource.routes
    }


}
