import actors.CoreActors
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import api.{Api, ApiServiceActor}
import core.BootedCore
import data.ConfigDatabaseSupport
import data.impl.DAL
import org.slf4j.LoggerFactory
import spray.can.Http

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Boot extends App with BootedCore with ConfigDatabaseSupport with DAL with CoreActors with Api {

  private val log = LoggerFactory.getLogger(getClass)

  db.run(create()) onComplete {
    case Success(_) => log.info("Database schema successfully created")
    case Failure(ex) => log.error("Database create failed - {}", ex.getMessage)
  }

  val apiServiceActor = system.actorOf(ApiServiceActor.props(routes))

  implicit val timeout = Timeout(5.seconds)

  val port = Option(System.getProperty("http.port")).map(_.toInt).getOrElse(8080)

  IO(Http) ? Http.Bind(apiServiceActor, interface = "0.0.0.0", port = port)

}
