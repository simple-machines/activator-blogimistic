package data.impl

import data.TestDatabaseSupport
import model.Version
import model.impl.{Blog, User, UserId}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global

class BlogComponentSpec extends FunSpec with ShouldMatchers with BeforeAndAfter with BeforeAndAfterAll
    with ScalaFutures with TestDatabaseSupport with BlogComponent with BlogRoleComponent with UserComponent with TokenComponent {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  import profile.api._

  val time = new DateTime(2014, 2, 26, 9, 30, DateTimeZone.UTC)
  val user = User(Some(UserId(1)), Some(Version(0)), Some(time), Some(time), "facebookId", "Test User", None, None)

  val schema = blogs.schema ++ blogRoles.schema ++ users.schema ++ tokens.schema

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
      val result = db.run(Blogs.create(blog, user)).futureValue
      result.id should be ('defined)

      db.run(BlogRoles.isAdmin(user.id.get, result.id.get)).futureValue should be (true)
    }
  }
}
