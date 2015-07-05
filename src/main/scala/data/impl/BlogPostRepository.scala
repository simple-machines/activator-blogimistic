package data.impl

import data.{EntityRepository, Profile}
import model._
import model.impl._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

trait BlogPostRepository extends EntityRepository { this: Profile with BlogRepository with UserRepository =>

  import profile.api._

  class BlogPosts(tag: Tag) extends Table[BlogPost](tag, "blog_posts") with EntityTable[BlogPostId, BlogPost] {
    def id = column[BlogPostId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[DateTime]("created")
    def modified = column[DateTime]("modified")
    def blogId = column[BlogId]("blog_id_fk")
    def authorId = column[UserId]("user_id_fk")
    def title = column[String]("title")
    def content = column[String]("content")

    def blogFk = foreignKey("blogpost_blog_fk", blogId, blogs)(_.id)
    def userFk = foreignKey("blogpost_user_fk", authorId, users)(_.id)

    def * = (id.?, version.?, created.?, modified.?, blogId, authorId, title, content) <> (BlogPost.tupled, BlogPost.unapply)
  }

  object blogPosts extends EntityQueries[BlogPostId, BlogPost, BlogPosts](new BlogPosts(_)) {

    def copyEntityFields(entity: BlogPost, id: Option[BlogPostId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime]): BlogPost =
      entity.copy(id = id, version = version, created = created, modified = modified)

    def paginateByBlog(blogId: BlogId, offset: Int, limit: Int)(implicit ec: ExecutionContext): DBIO[BlogPostPage] = {
      val baseQuery = for (p <- blogPosts if p.blogId === blogId) yield p
      (for {
        blogPosts <- baseQuery.sortBy(_.id.desc).drop(offset).take(limit).result
        total <- baseQuery.length.result
      } yield BlogPostPage(blogPosts, total)).transactionally
    }
  }
}
