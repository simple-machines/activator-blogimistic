package api

import model.{Entity, Version}
import spray.http.HttpHeaders.ETag
import spray.routing.{Directive0, Directive1, Directives}

trait VersionDirectives extends Directives {

  /**
   * Add an ETag to the response containing the entity version.
   */
  def respondWithEntityVersion(e: Entity[_]): Directive0 =
    respondWithHeader(ETag(e.version.get.value.toString))

  /**
   * Extracts the entity version from the If-Match tag.
   */
  def entityVersion: Directive1[Version] =
    headerValueByName("If-Match").flatMap { v =>
      // etags are surrounding by quotes
      provide(Version(v.replaceAll("^\"|\"$", "").toLong))
    }

}
