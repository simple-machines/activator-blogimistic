package data.impl

import java.time.{LocalDateTime, ZoneOffset}

import data.TestDatabaseSupport
import model.Version
import model.impl.{Blog, User, UserId}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global

class BlogRepositorySpec extends FunSpec with ShouldMatchers with BeforeAndAfter
    with ScalaFutures with TestDatabaseSupport with BlogRepository with BlogRoleRepository with UserRepository with TokenRepository {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  import profile.api._

  val time = LocalDateTime.of(2014, 2, 26, 9, 30).toInstant(ZoneOffset.UTC)
  val user = User(Some(UserId(1)), Some(Version(0)), Some(time), Some(time), "facebookId", "Test User", None, None)

  val schema = blogs.schema ++ users.schema ++ tokens.schema ++ blogRoles.schema

  before {
    db.run(DBIO.seq(
      schema.create,
      users += user
    )).futureValue
  }

  after {
    db.run(schema.drop).futureValue
  }

  describe("create") {
    it("should create a blog and set the creator as the administrator") {
      val blog = new Blog("My blog", "Description")
      val result = db.run(blogs.create(blog, user)).futureValue
      result.id should be ('defined)

      db.run(blogRoles.isAdmin(user.id.get, result.id.get)).futureValue should be (true)
    }
  }
}
