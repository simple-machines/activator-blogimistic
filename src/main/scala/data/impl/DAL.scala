package data.impl

import data.Profile

/**
 * A cake of all our data repositories that allows us to build a schema of our entire database.
 * Useful for the application as it exposes all available queries, not so much for testing of individual components.
 */
trait DAL extends UserRepository
    with TokenRepository
    with BlogRepository
    with BlogRoleRepository
    with BlogPostRepository { this: Profile =>

  import profile.api._

  val schema = users.schema ++ tokens.schema ++ blogs.schema ++ blogRoles.schema ++ blogPosts.schema

  def create() = schema.create

  def drop() = schema.drop
}
