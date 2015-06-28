package model.impl

import model.{EntityId, Entity, Version}
import org.joda.time.DateTime

case class BlogPostId(value: Long) extends EntityId(value)

/**
 * An entry that is posted under a blog.
 */
case class BlogPost(id: Option[BlogPostId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime],
                    blogId: BlogId, authorId: UserId, title: String, content: String) extends Entity[BlogPostId] {

  def this(blogId: BlogId, authorId: UserId, title: String, content: String) =
    this(None, None, None, None, blogId, authorId, title, content)
}

/**
 * For pagination.
 */
case class BlogPostPage(blogPosts: Seq[BlogPost], total: Int)