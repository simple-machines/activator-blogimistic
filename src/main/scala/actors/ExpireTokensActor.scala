package actors

import akka.actor.{ActorLogging, Props, Actor}
import data.DatabaseSupport
import data.impl.DAL
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import scala.language.postfixOps

/**
 * Actor that periodically deletes expired tokens from the database.
 */
object ExpireTokensActor {
  def props(dbAccess: DAL with DatabaseSupport) = Props.create(classOf[ExpireTokensActor], dbAccess)
}

class ExpireTokensActor(dbAccess: DAL with DatabaseSupport) extends Actor with ActorLogging {

  import dbAccess._

  implicit def ec: ExecutionContext = context.dispatcher

  val tick = context.system.scheduler.schedule(30 seconds, 5 minutes, self, "tick")

  def receive = {
    case "tick" => {
      db.run(tokens.deleteExpiredTokens()) onComplete {
        case Success(deleted) => log.debug("Deleted {} expired tokens", deleted)
        case Failure(ex) => log.error("Unable to delete expired tokens", ex)
      }
    }
  }
}
