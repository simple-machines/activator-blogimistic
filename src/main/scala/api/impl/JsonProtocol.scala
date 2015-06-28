package api.impl

import model.Version
import model.impl.Role.Role
import model.impl._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

object JsonProtocol extends DefaultJsonProtocol {

  implicit object VersionFormat extends JsonFormat[Version] {
    def read(json: JsValue): Version = Version(json.convertTo[Int])

    def write(obj: Version): JsValue = JsNumber(obj.value)
  }

  implicit object UserIdFormat extends JsonFormat[UserId] {
    def read(json: JsValue): UserId = UserId(json.convertTo[Int])

    def write(obj: UserId): JsValue = JsNumber(obj.value)
  }

  implicit object BlogIdFormat extends JsonFormat[BlogId] {
    def read(json: JsValue): BlogId = BlogId(json.convertTo[Int])

    def write(obj: BlogId): JsValue = JsNumber(obj.value)
  }

  implicit object BlogPostIdFormat extends JsonFormat[BlogPostId] {
    def read(json: JsValue): BlogPostId = BlogPostId(json.convertTo[Int])

    def write(obj: BlogPostId): JsValue = JsNumber(obj.value)
  }

  implicit object BlogRoleIdFormat extends JsonFormat[BlogRoleId] {
    def read(json: JsValue): BlogRoleId = BlogRoleId(json.convertTo[Int])

    def write(obj: BlogRoleId): JsValue = JsNumber(obj.value)
  }

  implicit object RoleFormat extends JsonFormat[Role] {
    def read(json: JsValue): Role = Role.withName(json.convertTo[String])

    def write(obj: Role): JsValue = JsString(obj.toString)
  }

  implicit object DateTimeFormat extends JsonFormat[DateTime] {
    val dateTimeParser = ISODateTimeFormat.dateTimeParser().withZoneUTC()
    val dateTimePrinter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()

    def read(json: JsValue): DateTime = dateTimeParser.parseDateTime(json.convertTo[String])

    def write(obj: DateTime): JsValue = JsString(dateTimePrinter.print(obj))
  }

  implicit val tokenFormat = jsonFormat3(Token)
  implicit val blogFormat = jsonFormat6(Blog)
  implicit val blogPostFormat = jsonFormat8(BlogPost)
  implicit val blogPostPageFormat = jsonFormat2(BlogPostPage)
  implicit val userFormat = jsonFormat8(User)
  implicit val blogRoleFormat = jsonFormat7(BlogRole)

  implicit val fbUserPictureFormat = jsonFormat1(FbUserPictureData)
  implicit val fbUserPictureDataFormat = jsonFormat1(FbUserPicture)
  implicit val fbAuthResponseFormat = jsonFormat4(FbAuthResponse)
  implicit val fbUserFormat = jsonFormat5(FbUser)
}
