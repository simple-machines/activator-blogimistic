package data.impl

import java.time.Instant

import data.{EntityRepository, Profile}
import model.Version
import model.impl.{User, UserId}

import scala.concurrent.ExecutionContext

trait UserRepository extends EntityRepository { this: Profile with TokenRepository =>

  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "users") with EntityTable[UserId, User] {
    def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[Instant]("created")
    def modified = column[Instant]("modified")
    def facebookId = column[String]("facebook_id")
    def name = column[String]("name")
    def email = column[Option[String]]("email")
    def picture = column[Option[String]]("picture")

    def emailUniqueIdx = index("email_unq", email, unique = true)

    def * = (id.?, version.?, created.?, modified.?, facebookId, name, email, picture) <> (User.tupled, User.unapply)
  }

  object users extends EntityQueries[UserId, User, Users](new Users(_)) {

    def copyEntityFields(entity: User, id: Option[UserId], version: Option[Version], created: Option[Instant], modified: Option[Instant]): User =
      entity.copy(id = id, version = version, created = created, modified = modified)

    def findByEmail(email: String) =
      (for (u <- users if u.email === email) yield u).result.headOption

    def findByFacebookId(facebookId: String) =
      (for (u <- users if u.facebookId === facebookId) yield u).result.headOption

    def findByToken(token: String) =
      (for {
        t <- tokens if t.token === token if t.expires > Instant.now()
        u <- users if t.userId === u.id
      } yield u).result.headOption

    def upsertByFacebookId(user: User)(implicit ec: ExecutionContext): DBIO[User] = {
      findByFacebookId(user.facebookId) flatMap {
        case Some(existing) => update(existing.copy(name = user.name, email = user.email, picture = user.picture))
        case None => insert(user)
      }
    }
  }
}