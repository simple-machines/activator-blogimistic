package data

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
 * In-memory database profile for testing.
 */
trait TestDatabaseSupport extends DatabaseSupport {

  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("database.test")

  val profile = dbConfig.driver

  val db = dbConfig.db

}