package api

import data.StaleStateException
import spray.http.StatusCodes._
import spray.routing.{Directives, ExceptionHandler, RejectionHandler}

trait CustomErrorHandling extends CORSSupport { this: Directives =>

  /**
   * Add CORS headers to error responses.
   * To disable CORS, simply remove the cors directive.
   */
  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case exception =>
      cors {
        exception match {
          case e: java.util.NoSuchElementException => ctx =>
            ctx.complete(NotFound)
          case e: IllegalArgumentException => ctx =>
            ctx.complete(BadRequest, e.getMessage.replaceFirst("requirement failed: ", ""))
          case e: StaleStateException => ctx =>
            ctx.complete(PreconditionFailed)
          case e: Exception => ctx =>
            ctx.complete(InternalServerError, e.getMessage)
        }
      }
  }

  /**
   * Add CORS headers to rejected requests.
   * Since we don't have any custom rejection handling, you can delete this altogether if disabling CORS.
   */
  val rejectionHandler = RejectionHandler {
    case rejection =>
      cors {
        RejectionHandler.Default(rejection)
      }
  }

}
