package api.impl.authorization

import data.DatabaseSupport
import data.impl.DAL
import model.impl.{BlogId, UserId}
import spray.routing._

import scala.concurrent.ExecutionContext

trait BlogAuthorization extends Directives {

  implicit def dbDispatcher: ExecutionContext
  def dataAccess: DAL with DatabaseSupport

  /**
   * Checks if a user is allowed to update a blog (ie. is an admin or contributor).
   */
  def checkBlogAccess(userId: UserId, blogId: BlogId): Directive0 =
    onSuccess(dataAccess.db.run(dataAccess.blogRoles.hasAccess(userId, blogId))).flatMap(authorize(_))

  /**
   * Checks if a user is an admin of the given blog.
   */
  def checkBlogAdmin(userId: UserId, blogId: BlogId): Directive0 =
    onSuccess(dataAccess.db.run(dataAccess.blogRoles.isAdmin(userId, blogId))).flatMap(authorize(_))

}
