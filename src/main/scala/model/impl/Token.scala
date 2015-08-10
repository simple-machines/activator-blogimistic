package model.impl

import java.time.Instant

/**
 * An access token to authorize a user on the API.
 */
case class Token(token: String, userId: UserId, expires: Instant)