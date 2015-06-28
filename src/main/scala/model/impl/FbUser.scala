package model.impl

case class FbUser(id: String, email: Option[String], first_name: String, name: String, gender: Option[String])

case class FbUserPictureData(url: Option[String])
case class FbUserPicture(data: Option[FbUserPictureData])
