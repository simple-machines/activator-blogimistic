package model.impl

/**
 * Case class to unmarshall a Facebook response object.
 * See: https://developers.facebook.com/docs/reference/javascript/FB.getLoginStatus
 */
case class FbAuthResponse(accessToken: String, expiresIn: Int, signedRequest: String, userID: String)
