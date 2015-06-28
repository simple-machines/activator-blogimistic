package model.impl

import org.joda.time.DateTime

/**
 * An access token to authorize a user on the API.
 */
case class Token(token: String, userId: UserId, expires: DateTime)