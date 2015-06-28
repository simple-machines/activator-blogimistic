package model.impl

import model.{EntityId, Entity, Version}
import org.joda.time.DateTime

case class UserId(value: Long) extends EntityId(value)

case class User(id: Option[UserId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime],
                facebookId: String, name: String, email: Option[String], picture: Option[String]) extends Entity[UserId] {

  def this(facebookId: String, name: String, email: Option[String], picture: Option[String]) =
    this(None, None, None, None, facebookId, name, email, picture)
}
