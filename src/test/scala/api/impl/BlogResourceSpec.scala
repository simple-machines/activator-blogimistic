package api.impl

import api.{CustomErrorHandling, Api}
import core.Core
import data.TestDatabaseSupport
import data.impl.DAL
import model.Version
import model.impl._
import org.joda.time.{DateTimeZone, DateTime}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSpec, ShouldMatchers}
import spray.http.{EntityTag, HttpHeaders, OAuth2BearerToken, StatusCodes}
import spray.httpx.SprayJsonSupport
import spray.routing.HttpService
import spray.testkit.ScalatestRouteTest

class BlogResourceSpec extends FunSpec with ShouldMatchers with ScalaFutures with ScalatestRouteTest with BeforeAndAfter
    with TestDatabaseSupport with DAL with Api with Core with HttpService with CustomErrorHandling {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  implicit def eh = exceptionHandler
  implicit def rh = rejectionHandler

  def actorRefFactory = system

  import JsonProtocol._
  import SprayJsonSupport._

  val time = new DateTime(2014, 2, 26, 9, 30, DateTimeZone.UTC)
  val expires = DateTime.now().plusDays(30)

  val user1 = User(Some(UserId(1)), Some(Version(0)), Some(time), Some(time), "fb-id-1", "Test User1", None, None)
  val user2 = User(Some(UserId(2)), Some(Version(0)), Some(time), Some(time), "fb-id-2", "Test User2", None, None)

  val user1Credentials = OAuth2BearerToken("token1")
  val user2Credentials = OAuth2BearerToken("token2")

  val blog1 = Blog(Some(BlogId(1)), Some(Version(0)), Some(time), Some(time), "Blog1", "Description")
  val blogPost1 = BlogPost(Some(BlogPostId(1)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), "BlogPost1", "Contents")

  before {
    import profile.api._

    db.run(DBIO.seq(
      create(),
      users ++= Seq(user1, user2),
      tokens ++= Seq(
        Token("token1", UserId(1), expires),
        Token("token2", UserId(2), expires)),
      blogs += blog1,
      blogPosts += blogPost1,
      blogRoles += BlogRole(Some(BlogRoleId(1)), Some(Version(0)), Some(time), Some(time), BlogId(1), UserId(1), Role.ADMIN)
    )).futureValue
  }

  after {
    db.run(drop()).futureValue
  }

  describe("GET /blogs") {
    it("should list all blogs") {
      Get("/blogs") ~> routes ~> check {
        status should equal (StatusCodes.OK)
        responseAs[List[Blog]] should equal (List(blog1))
      }
    }
  }

  describe("GET /blogs/{id}") {
    it("should return 200 and the blog with the given ID, and version number in an ETag") {
      Get("/blogs/1") ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.OK)
        responseAs[Blog] should equal (blog1)
        header("Etag").map(_.value) should equal (Some("\"0\""))
      }
    }

    it("should return 404 if the given ID does not exist") {
      Get("/blogs/2") ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.NotFound)
      }
    }
  }

  describe("POST /blogs") {
    it("should add a new blog and return its location in a header") {
      val blog = new Blog("My New Blog", "New blog")
      Post("/blogs", blog) ~> addCredentials(user1Credentials) ~> routes ~> check {
        status should equal (StatusCodes.Created)
        header("Location").map(_.value) should equal (Some("http://example.com/blogs/2"))
      }
    }
  }

  describe("PUT /blogs/{id}") {
    it("should update the given blog if If-Match header matches the current version of the blog") {
      val blog = new Blog("Updated blog", "Updated blog")
      Put("/blogs/1", blog) ~> addCredentials(user1Credentials) ~> addHeader(HttpHeaders.`If-Match`(EntityTag("0"))) ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.OK)
      }
    }

    it("should return 412 if the version does not match") {
      val blog = new Blog("Updated blog", "Updated blog")
      Put("/blogs/1", blog) ~> addCredentials(user1Credentials) ~> addHeader(HttpHeaders.`If-Match`(EntityTag("999"))) ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.PreconditionFailed)
      }
    }

    it("should return 401 (unauthorized) if credentials not supplied") {
      val blog = new Blog("Updated blog", "Updated blog")
      Put("/blogs/1", blog) ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.Unauthorized)
      }
    }

    it("should return 403 (forbidden) if user has no role on the given blog") {
      val blog = new Blog("Updated blog", "Updated blog")
      Put("/blogs/1", blog) ~> addCredentials(user2Credentials) ~> addHeader(HttpHeaders.`If-Match`(EntityTag("0")))  ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.Forbidden)
      }
    }

    it("should return 400 (bad request) if If-Match header is missing") {
      val blog = new Blog("Updated blog", "Updated blog")
      Put("/blogs/1", blog) ~> addCredentials(user1Credentials) ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.BadRequest)
      }
    }
  }

  describe("GET /blogs/{id}/posts") {
    it("should paginate posts on the given blog") {
      Get("/blogs/1/posts?limit=2&offset=0") ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.OK)
        responseAs[BlogPostPage] should equal (BlogPostPage(Seq(blogPost1), 1))
      }
    }
  }

  describe("POST /blogs/{id}/posts") {
    it("should create a new blog post in the given blog") {
      val blogPost = new BlogPost(BlogId(1), UserId(1), "New blog post", "New blog post")
      Post("/blogs/1/posts", blogPost) ~> addCredentials(user1Credentials) ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.Created)
        header("Location").map(_.value) should equal (Some("http://example.com/blogs/1/posts/2"))
      }
    }
  }

  describe("POST /blogs/{id}/roles") {
    it("should let an admin assign another user as a contributor to a blog") {
      val blogRole = new BlogRole(BlogId(1), UserId(2), Role.CONTRIBUTOR)
      Post("/blogs/1/roles", blogRole) ~> addCredentials(user1Credentials) ~> sealRoute(routes) ~> check {
        status should equal (StatusCodes.Created)
      }
    }
  }
}
