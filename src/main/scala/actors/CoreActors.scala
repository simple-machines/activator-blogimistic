package actors

import core.Core
import data.DatabaseSupport
import data.impl.DAL

trait CoreActors { this: Core with DAL with DatabaseSupport =>

  val expireTokensActor = system.actorOf(ExpireTokensActor.props(this))
}
