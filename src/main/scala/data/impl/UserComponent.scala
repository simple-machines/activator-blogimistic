package data.impl

import data.{EntityComponent, Profile}
import model.Version
import model.impl.{User, UserId}
import org.joda.time.{DateTimeZone, DateTime}

import scala.concurrent.ExecutionContext

trait UserComponent extends EntityComponent { this: Profile with TokenComponent =>

  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "users") with EntityTable[UserId, User] {
    def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[DateTime]("created")
    def modified = column[DateTime]("modified")
    def facebookId = column[String]("facebook_id")
    def name = column[String]("name")
    def email = column[Option[String]]("email")
    def picture = column[Option[String]]("picture")

    def emailUniqueIdx = index("email_unq", email, unique = true)

    def * = (id.?, version.?, created.?, modified.?, facebookId, name, email, picture) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  object Users extends EntityQueries(users) {

    def copyEntityFields(entity: User, id: Option[UserId], version: Option[Version], created: Option[DateTime], modified: Option[DateTime]): User =
      entity.copy(id = id, version = version, created = created, modified = modified)

    def findByEmail(email: String) =
      (for (u <- users if u.email === email) yield u).result.headOption

    def findByFacebookId(facebookId: String) =
      (for (u <- users if u.facebookId === facebookId) yield u).result.headOption

    def findByToken(token: String) =
      (for {
        t <- tokens if t.token === token if t.expires > DateTime.now(DateTimeZone.UTC)
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