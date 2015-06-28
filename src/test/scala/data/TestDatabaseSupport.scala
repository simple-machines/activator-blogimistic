package data

import slick.driver.H2Driver
import slick.jdbc.JdbcBackend._

/**
 * In-memory database profile for testing.
 */
trait TestDatabaseSupport extends DatabaseSupport {

  val db = Database.forURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")

  val profile = H2Driver
}