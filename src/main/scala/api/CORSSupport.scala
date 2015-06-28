package api

import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import spray.http.{AllOrigins, HttpMethod, HttpMethods, HttpResponse}
import spray.routing._

/**
 * Credits to Jose Raya (https://gist.github.com/joseraya/176821d856b43b1cfe19)
 */
// see also https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
trait CORSSupport {
  this: Directives =>

  val exposeHeaders = `Access-Control-Expose-Headers`("Location, ETag")
  val allowOriginHeader = `Access-Control-Allow-Origin`(AllOrigins)
  val optionsCorsHeaders = List(
    `Access-Control-Allow-Headers`("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent, Authorization", "If-Match"),
    `Access-Control-Max-Age`(1728000))

  def cors[T]: Directive0 = mapRequestContext { ctx =>
    ctx.withRouteResponseHandling {
      //It is an option request for a resource that responds to some other method
      case Rejected(x) if ctx.request.method.equals(HttpMethods.OPTIONS) && x.exists(_.isInstanceOf[MethodRejection]) => {
        val allowedMethods: List[HttpMethod] = x.filter(_.isInstanceOf[MethodRejection]).map { rejection =>
          rejection.asInstanceOf[MethodRejection].supported
        }
        ctx.complete(HttpResponse().withHeaders(
          `Access-Control-Allow-Methods`(OPTIONS, allowedMethods :_*) :: allowOriginHeader :: exposeHeaders ::
            optionsCorsHeaders))
      }
    }.withHttpResponseHeadersMapped { headers =>
        exposeHeaders :: allowOriginHeader :: headers
    }
  }
}