package data.impl

import data.TestDatabaseSupport
import model.Version
import model.impl.{Token, User, UserId}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositorySpec extends FunSpec with ShouldMatchers with BeforeAndAfter
    with ScalaFutures with TestDatabaseSupport with UserRepository with TokenRepository {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  import profile.api._

  val time = new DateTime(2014, 2, 26, 9, 30, DateTimeZone.UTC)
  val schema = users.schema ++ tokens.schema

  before {
    db.run(DBIO.seq(
      schema.create,
      users ++= Seq(
        User(Some(UserId(1)), Some(Version(0)), Some(time), Some(time), "fb-id-1", "User1", Some("user1@example.com"), None),
        User(Some(UserId(2)), Some(Version(0)), Some(time), Some(time), "fb-id-2", "User2", None, None),
        User(Some(UserId(3)), Some(Version(0)), Some(time), Some(time), "fb-id-3", "User3", Some("user3@example.com"), None)
      ),
      tokens ++= Seq(
        Token("token1", UserId(1), DateTime.now().minusDays(1)),
        Token("token3", UserId(3), DateTime.now().plusDays(1))
      )
    )).futureValue
  }

  after {
    db.run(schema.drop).futureValue
  }

  describe("findByEmail") {
    it("should return None if no matching email") {
      db.run(users.findByEmail("user2@example.com")).futureValue should equal (None)
    }

    it("should return some user with matching email") {
      val result = db.run(users.findByEmail("user3@example.com")).futureValue
      result should be ('defined)
      result.get.id should equal (Some(UserId(3)))
    }
  }

  describe("findByFacebookId") {
    it("should return None if no matching facebook ID") {
      db.run(users.findByFacebookId("123")).futureValue should equal (None)
    }

    it("should return some user with matching facebook ID") {
      val result = db.run(users.findByFacebookId("fb-id-3")).futureValue
      result should be ('defined)
      result.get.id should equal (Some(UserId(3)))
    }
  }

  describe("findByToken") {
    it("should not return user with expired token") {
      db.run(users.findByToken("token1")).futureValue should equal (None)
    }

    it("should return the user with the given token") {
      val result = db.run(users.findByToken("token3")).futureValue
      result should be ('defined)
      result.get.id should equal (Some(UserId(3)))
    }
  }

  describe("upsertByFacebookId") {
    it("should insert a new record if the given facebook ID is not found") {
      val user = new User("fb-id-4", "User4", Some("user4@example.com"), Some("http://www.example.com/user4.png"))
      val result = db.run(users.upsertByFacebookId(user)).futureValue
      result.id should equal (Some(UserId(4)))
    }

    it("should update the existing record if the given facebook ID exists") {
      val user = new User("fb-id-2", "User2", Some("user2@example.com"), Some("http://www.example.com/user2.png"))
      db.run(users.upsertByFacebookId(user)).futureValue

      // re-fetch the value from the database to make sure it is updated
      val result = db.run(users.get(UserId(2))).futureValue
      result.email should equal (Some("user2@example.com"))
      result.picture should equal (Some("http://www.example.com/user2.png"))
    }
  }
}
