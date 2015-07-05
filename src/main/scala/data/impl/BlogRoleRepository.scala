package data.impl

import data.{EntityRepository, Profile}
import model.Version
import model.impl.Role.Role
import model.impl.{Role, _}
import org.joda.time.DateTime

trait BlogRoleRepository extends EntityRepository { this: Profile with BlogRepository with UserRepository =>

  import profile.api._

  implicit val roleMapper = MappedColumnType.base[Role, String] (
    role => role.toString,
    s => Role.withName(s)
  )

  class BlogRoles(tag: Tag) extends Table[BlogRole](tag, "blog_roles") with EntityTable[BlogRoleId, BlogRole] {
    def id = column[BlogRoleId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[DateTime]("created")
    def modified = column[DateTime]("modified")
    def blogId = column[BlogId]("blog_id_fk")
    def userId = column[UserId]("user_id_fk")
    def role = column[Role]("role")

    def blogUserUniqueIdx = index("blogrole_unq", (blogId, userId), unique = true)

    def blogFk = foreignKey("blogrole_blog_fk", blogId, blogs)(_.id)
    def userFk = foreignKey("blogrole_user_fk", userId, users)(_.id)

    def * = (id.?, version.?, created.?, modified.?, blogId, userId, role) <> (BlogRole.tupled, BlogRole.unapply)
  }

  object blogRoles extends EntityQueries[BlogRoleId, BlogRole, BlogRoles](new BlogRoles(_)) {
    def copyEntityFields(entity: BlogRole, id: Option[BlogRoleId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime]): BlogRole =
      entity.copy(id = id, version = version, created = created, modified = modified)

    def hasAccess(userId: UserId, blogId: BlogId): DBIO[Boolean] =
      (for (r <- blogRoles if r.userId === userId && r.blogId === blogId) yield r).exists.result

    def isAdmin(userId: UserId, blogId: BlogId): DBIO[Boolean] =
      (for (r <- blogRoles if r.userId === userId && r.blogId === blogId && r.role === Role.ADMIN) yield r).exists.result
  }
}
