package data

import java.net.URI

import org.slf4j.LoggerFactory
import slick.backend.DatabaseConfig
import slick.driver._
import slick.jdbc.JdbcBackend._

trait DatabaseSupport extends Profile {

  val db: JdbcProfile#Backend#Database
}

/**
 * DatabaseSupport configured either by DATABASE_URL environment variable or application.conf
 */
trait ConfigDatabaseSupport extends DatabaseSupport {

  private val log = LoggerFactory.getLogger(getClass)

  private val dbConfig = Option(System.getenv("DATABASE_URL")) match {
    case Some(dbUrl) => {
      log.info("Heroku DATABASE_URL detected: {}", dbUrl)
      DbConfig.forHerokuPsql(dbUrl)
    }
    case None => {
      log.info("Initialising database from application.conf")
      DbConfig.forConfig("database.default")
    }
  }

  val db = dbConfig.db
  val profile = dbConfig.driver

  private case class DbConfig(db: JdbcProfile#Backend#Database, driver: JdbcProfile)

  private object DbConfig {

    def forConfig(path: String) = {
      val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig(path)
      DbConfig(dbConfig.db, dbConfig.driver)
    }

    def forHerokuPsql(url: String) = {
      val dbUri = new URI(url)

      require(dbUri.getScheme == "postgres")

      val username = dbUri.getUserInfo.split(":")(0)
      val password = dbUri.getUserInfo.split(":")(1)
      val dbUrl = "jdbc:postgresql://" + dbUri.getHost + dbUri.getPath

      DbConfig(Database.forURL(dbUrl, username, password), PostgresDriver)
    }
  }
}
