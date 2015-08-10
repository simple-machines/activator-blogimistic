package model.impl

import java.time.Instant

import model.impl.Role.Role
import model.{Entity, EntityId, Version}

case class BlogRoleId(value: Long) extends EntityId(value)

/**
 * Keeps track of user permissions in relation to a blog.
 * A user can be an administrator or a contributor in order to post to a blog.
 */
case class BlogRole(id: Option[BlogRoleId], version: Option[Version], created: Option[Instant], modified: Option[Instant],
                    blogId: BlogId, userId: UserId, role: Role) extends Entity[BlogRoleId] {

  def this(blogId: BlogId, userId: UserId, role: Role) = this(None, None, None, None, blogId, userId, role)
}
