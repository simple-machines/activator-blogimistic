package core

import akka.actor.ActorSystem

trait Core {

  implicit def system: ActorSystem

}

trait BootedCore extends Core {

  implicit lazy val system = ActorSystem("blogimistic")

  sys.addShutdownHook(system.shutdown())
}