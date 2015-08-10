package model.impl

import java.time.Instant

import model.{Entity, EntityId, Version}

case class BlogId(value: Long) extends EntityId(value)

/**
 * Our application supports multiple blogs. Each blog can have many [[BlogPost]].
 */
case class Blog(id: Option[BlogId], version: Option[Version], created: Option[Instant], modified: Option[Instant],
                name: String, description: String) extends Entity[BlogId] {

  def this(name: String, description: String) = this(None, None, None, None, name, description)
}
