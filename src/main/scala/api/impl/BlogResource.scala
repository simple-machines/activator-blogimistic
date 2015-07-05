package api.impl

import api.VersionDirectives
import api.impl.authentication.token.BearerTokenAuthentication
import api.impl.authorization.BlogAuthorization
import data.DatabaseSupport
import data.impl.DAL
import model.impl._
import spray.http.HttpHeaders.Location
import spray.http.MediaTypes.`application/json`
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport
import spray.routing.Directives

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class BlogResource(val dataAccess: DatabaseSupport with DAL)(implicit val dbDispatcher: ExecutionContext)
  extends Directives with VersionDirectives with BearerTokenAuthentication with BlogAuthorization {

  import JsonProtocol._
  import SprayJsonSupport._
  import dataAccess._

  val routes = {
    path("blogs") {
      get {
        respondWithMediaType(`application/json`) {
          onComplete(db.run(blogs.list())) {
            case Success(blogs) => complete(blogs)
            case Failure(ex) => failWith(ex)
          }
        }
      } ~
      post {
        authenticate(bearerToken) { user =>
          entity(as[Blog]) { blog =>
            requestUri { uri =>
              onComplete(db.run(blogs.create(blog, user))) {
                case Success(blog) => {
                  respondWithHeader(Location(s"$uri/${blog.id.get}")) {
                    complete(Created)
                  }
                }
                case Failure(ex) => failWith(ex)
              }
            }
          }
        }
      }
    } ~
    pathPrefix("blogs" / IntNumber) { blogId =>
      pathEnd {
        get {
          respondWithMediaType(`application/json`) {
            onComplete(db.run(blogs.get(BlogId(blogId)))) {
              case Success(blog) => {
                respondWithEntityVersion(blog) {
                  complete(blog)
                }
              }
              case Failure(ex) => failWith(ex)
            }
          }
        } ~
        put {
          authenticate(bearerToken) { user =>
            entity(as[Blog]) { blog =>
              entityVersion { version =>
                checkBlogAccess(user.id.get, BlogId(blogId)) {
                  respondWithMediaType(`application/json`) {
                    onComplete(db.run(blogs.update(BlogId(blogId), version, blog))) {
                      case Success(blog) => {
                        respondWithEntityVersion(blog) {
                          complete(blog)
                        }
                      }
                      case Failure(ex) => failWith(ex)
                    }
                  }
                }
              }
            }
          }
        }
      } ~
      path("posts") {
        get {
          parameters('offset.as[Int], 'limit.as[Int]) { (offset, limit) =>
            respondWithMediaType(`application/json`) {
              onComplete(db.run(blogPosts.paginateByBlog(BlogId(blogId), offset, limit))) {
                case Success(blogPostPage) => complete(blogPostPage)
                case Failure(ex) => failWith(ex)
              }
            }
          }
        } ~
        post {
          authenticate(bearerToken) { user =>
            checkBlogAccess(user.id.get, BlogId(blogId)) {
              entity(as[BlogPost]) { blogPost =>
                requestUri { uri =>
                  onComplete(db.run(blogPosts.insert(blogPost))) {
                    case Success(blogPost) => {
                      respondWithHeader(Location(s"$uri/${blogPost.id.get}")) {
                        complete(Created)
                      }
                    }
                    case Failure(ex) => failWith(ex)
                  }
                }
              }
            }
          }
        }
      } ~
      pathPrefix("posts" / IntNumber) { blogPostId =>
        get {
          respondWithMediaType(`application/json`) {
            onComplete(db.run(blogPosts.get(BlogPostId(blogPostId)))) {
              case Success(blogPost) => {
                respondWithEntityVersion(blogPost) {
                  complete(blogPost)
                }
              }
              case Failure(ex) => failWith(ex)
            }
          }
        } ~
        put {
          authenticate(bearerToken) { user =>
            checkBlogAccess(user.id.get, BlogId(blogId)) {
              entity(as[BlogPost]) { blogPost =>
                entityVersion { version =>
                  respondWithMediaType(`application/json`) {
                    onComplete(db.run(blogPosts.update(BlogPostId(blogPostId), version, blogPost))) {
                      case Success(blogPost) => {
                        respondWithEntityVersion(blogPost) {
                          complete(blogPost)
                        }
                      }
                      case Failure(ex) => failWith(ex)
                    }
                  }
                }
              }
            }
          }
        }
      } ~
      path("roles") {
        post {
          authenticate(bearerToken) { user =>
            checkBlogAdmin(user.id.get, BlogId(blogId)) {
              entity(as[BlogRole]) { blogRole =>
                onComplete(db.run(blogRoles.insert(blogRole))) {
                  case Success(blogRole) => complete(Created)
                  case Failure(ex) => failWith(ex)
                }
              }
            }
          }
        }
      }
    }
  }
}
