package data.impl

import java.time.{LocalDateTime, ZoneOffset}

import data.TestDatabaseSupport
import model.Version
import model.impl._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global

class BlogPostRepositorySpec extends FunSpec with ShouldMatchers with BeforeAndAfter
    with ScalaFutures with TestDatabaseSupport with BlogPostRepository with UserRepository with BlogRepository
    with BlogRoleRepository with TokenRepository {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  import profile.api._

  val time = LocalDateTime.of(2014, 2, 26, 9, 30).toInstant(ZoneOffset.UTC)
  val user = User(Some(UserId(1)), Some(Version(0)), Some(time), Some(time), "facebookId", "Test User", None, None)

  val schema = users.schema ++ tokens.schema ++ blogs.schema ++ blogRoles.schema ++ blogPosts.schema

  before {
    db.run(DBIO.seq(
      schema.create,
      users += user,
      blogs += new Blog(Some(BlogId(1)), Some(Version(0)), Some(time), Some(time), "Test Blog", "Description"),
      blogPosts ++= Seq(
        new BlogPost(Some(BlogPostId(1)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), "Title 1", "Content 1"),
        new BlogPost(Some(BlogPostId(2)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), "Title 2", "Content 2"),
        new BlogPost(Some(BlogPostId(3)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), "Title 3", "Content 3"),
        new BlogPost(Some(BlogPostId(4)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), "Title 4", "Content 4"),
        new BlogPost(Some(BlogPostId(5)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), "Title 5", "Content 5")
      )
    )).futureValue
  }

  after {
    db.run(schema.drop).futureValue
  }

  describe("paginateByBlog") {
    it("should handle blogs with no posts") {
      val result = db.run(blogPosts.paginateByBlog(BlogId(2), 1, 1)).futureValue
      result.total should equal (0)
      result.blogPosts should equal (Seq.empty)
    }

    it("should return page with the given offset/limit in descending order") {
      val result = db.run(blogPosts.paginateByBlog(BlogId(1), 2, 2)).futureValue
      result.total should equal (5)
      result.blogPosts.size should equal (2)
      result.blogPosts.head.id should equal (Some(BlogPostId(3)))
      result.blogPosts(1).id should equal (Some(BlogPostId(2)))
    }

    it("should return remaining results on the last page") {
      val result = db.run(blogPosts.paginateByBlog(BlogId(1), 4, 2)).futureValue
      result.total should equal (5)
      result.blogPosts.size should equal (1)
      result.blogPosts.head.id should equal (Some(BlogPostId(1)))
    }
  }
}
