package model

import org.joda.time.DateTime
import slick.lifted.MappedTo

/**
 * Typesafe IDs are enforced in our application.
 */
abstract class EntityId(value: Long) extends MappedTo[Long] {
  override def toString = value.toString
}

case class Version(value: Long) extends MappedTo[Long] {
  def increment() = copy(value + 1)
}

/**
 * Trait for entities: all model classes that require an auto-incremental ID and optimistic locking.
 */
trait Entity[I <: EntityId] {

  def id: Option[I]
  def version: Option[Version]
  def created: Option[DateTime]
  def modified: Option[DateTime]

  def isPersisted = id.isDefined
}
