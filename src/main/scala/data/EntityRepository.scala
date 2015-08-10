package data

import java.sql.Timestamp
import java.time.Instant

import model.{Entity, EntityId, Version}

import scala.concurrent.ExecutionContext

/**
 * Provides common database queries and operations for entities.
 */
trait EntityRepository { this: Profile =>

  import profile.api._

  /**
   * Instant to Timestamp mapper.
   */
  implicit val instantMapper = MappedColumnType.base[Instant, Timestamp] (
    inst => if (inst == null) null else Timestamp.from(inst),
    ts => if (ts == null) null else ts.toInstant
  )

  /**
   * All entity tables must store the columns defined here.
   * The values will be managed by [[EntityQueries]] - values set by the client will be ignored.
   * @tparam I ID type.
   * @tparam E Entity type.
   */
  trait EntityTable[I <: EntityId, E <: Entity[I]] extends Table[E] {
    def id: Rep[I]
    def version: Rep[Version]
    def created: Rep[Instant]
    def modified: Rep[Instant]
  }

  /**
   * Subclass this for each entity table to get these common operations for free.
   */
  abstract class EntityQueries[I <: EntityId: BaseColumnType, E <: Entity[I], T <: EntityTable[I, E]](cons: Tag => T)
    extends TableQuery(cons) {

    def tableQuery = this

    /**
     * Subclasses simply need to copy the given ID, version, created and modified values into the entity.
     * This is because we can't access the case-class' copy method here.
     */
    def copyEntityFields(entity: E, id: Option[I], version: Option[Version], created: Option[Instant], modified: Option[Instant]): E

    def list(): DBIO[Seq[E]] = (for (e <- tableQuery) yield e).result

    def get(id: I): DBIO[E] = (for (e <- tableQuery if e.id === id) yield e).result.head

    def find(id: I): DBIO[Option[E]] = (for (e <- tableQuery if e.id === id) yield e).result.headOption

    def count(): DBIO[Int] = (for (e <- tableQuery) yield e).length.result

    /**
     * Insert the given entity and return a copy of that entity with ID, version, created and modified fields populated.
     */
    def insert(e: E): DBIO[E] = {
      require(!e.isPersisted)

      val now = Instant.now()
      val newCopy = copyEntityFields(e, None, Some(Version(0)), Some(now), Some(now))
      (tableQuery returning tableQuery.map(_.id) into {
        case(t, id) => copyEntityFields(t, Some(id), t.version, t.created, t.modified)
      }) += newCopy
    }

    /**
     * Update with optimistic locking. Only updates if ID and version match up.
     * @throws StaleStateException if no matching row with ID/version.
     */
    def update(e: E)(implicit ec: ExecutionContext): DBIO[E] = {
      require(e.isPersisted)
      require(e.version.isDefined)

      val now = Instant.now()
      val q = for (r <- tableQuery if r.id === e.id && r.version === e.version) yield r

      (for {
        // clients aren't required to pass in created/modified dates, so need to query for it
        existing <- get(e.id.get)
        newCopy = copyEntityFields(e, existing.id, e.version.map(_.increment()), existing.created, Some(now))
        result <- q.update(newCopy)
      } yield
        if (result == 1) newCopy else throw new StaleStateException("Updated " + result + " rows, expecting 1.")
      ).transactionally
    }

    /**
     * Convenience method that allows ID and version to be expressed explicitly.
     */
    def update(id: I, version: Version, e: E)(implicit ec: ExecutionContext): DBIO[E] =
      update(copyEntityFields(e, Some(id), Some(version), e.created, e.modified))

    def delete(id: I): DBIO[Int] = (for (e <- tableQuery if e.id === id) yield e).delete

  }
}
