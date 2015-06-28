package model.impl

import model.{EntityId, Entity, Version}
import org.joda.time.DateTime

case class BlogId(value: Long) extends EntityId(value)

/**
 * Our application supports multiple blogs. Each blog can have many [[BlogPost]].
 */
case class Blog(id: Option[BlogId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime],
                name: String, description: String) extends Entity[BlogId] {

  def this(name: String, description: String) = this(None, None, None, None, name, description)
}
