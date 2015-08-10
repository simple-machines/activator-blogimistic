package model.impl

import java.time.Instant

import model.{Entity, EntityId, Version}

case class UserId(value: Long) extends EntityId(value)

case class User(id: Option[UserId], version: Option[Version], created: Option[Instant], modified: Option[Instant],
                facebookId: String, name: String, email: Option[String], picture: Option[String]) extends Entity[UserId] {

  def this(facebookId: String, name: String, email: Option[String], picture: Option[String]) =
    this(None, None, None, None, facebookId, name, email, picture)
}
