package data.impl

import data.{EntityComponent, Profile}
import model._
import model.impl._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

trait BlogComponent extends EntityComponent { this: Profile with BlogRoleComponent =>

  import profile.api._

  class Blogs(tag: Tag) extends Table[Blog](tag, "blogs") with EntityTable[BlogId, Blog] {
    def id = column[BlogId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[DateTime]("created")
    def modified = column[DateTime]("modified")
    def name = column[String]("name")
    def description = column[String]("description")

    def nameUniqueIdx = index("name_unq", name, unique = true)

    def * = (id.?, version.?, created.?, modified.?, name, description) <> (Blog.tupled, Blog.unapply)
  }

  val blogs = TableQuery[Blogs]

  object Blogs extends EntityQueries(blogs) {

    def copyEntityFields(entity: Blog, id: Option[BlogId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime]): Blog =
      entity.copy(id = id, version = version, created = created, modified = modified)

    /**
     * Create a blog and set the creator as its administrator in a transaction.
     */
    def create(blog: Blog, creator: User)(implicit ec: ExecutionContext): DBIO[Blog] = {
      (for {
        b <- insert(blog)
        br <- BlogRoles.insert(new BlogRole(b.id.get, creator.id.get, Role.ADMIN))
      } yield b).transactionally
    }
  }
}