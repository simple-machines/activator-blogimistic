package data.impl

import java.security.SecureRandom
import java.util.Base64

import data.Profile
import model.impl.{UserId, Token}
import org.joda.time.{DateTimeZone, DateTime}

trait TokenRepository { this: Profile with UserRepository =>

  import profile.api._

  class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {
    def token = column[String]("token")
    def userId = column[UserId]("user_id_fk")
    def expires = column[DateTime]("expires")

    def tokenUniqueIdx = index("token_unq", token, unique = true)

    def userFk = foreignKey("tokens_user_fk", userId, users)(_.id)

    def * = (token, userId, expires) <> (Token.tupled, Token.unapply)
  }

  object tokens extends TableQuery(new Tokens(_)) {

    val TTL_MINUTES = 120

    /**
     * Generate a random string and associate it with the given user in the database.
     * @return [[Token]] with token string and expires date populated.
     */
    def generateToken(userId: UserId): DBIO[Token] = {
      val expires = DateTime.now(DateTimeZone.UTC).plusMinutes(TTL_MINUTES)
      val token = Token(randomString(), userId, expires)
      (tokens returning tokens.map(_.token) into { case(t, tok) => t }
        ) += token
    }

    /**
     * Delete tokens that have passed their expires date.
     * @return number of tokens deleted.
     */
    def deleteExpiredTokens(): DBIO[Int] = {
      val now = DateTime.now(DateTimeZone.UTC)
      (for (t <- tokens if t.expires < now) yield t).delete
    }

    private val secRandom = SecureRandom.getInstance("SHA1PRNG")
    private val encoder = Base64.getUrlEncoder

    private def randomString() = {
      val bytes = new Array[Byte](64)
      secRandom.nextBytes(bytes)
      val token = encoder.encode(bytes)
      new String(token).replaceAll("=", "")
    }
  }
}
