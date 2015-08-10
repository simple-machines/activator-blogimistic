package data

import java.time.Instant

import model.{Entity, EntityId, Version}

case class TestEntityId(value: Long) extends EntityId(value)

case class TestEntity(id: Option[TestEntityId], version: Option[Version], created: Option[Instant], modified: Option[Instant],
                      name: String) extends Entity[TestEntityId]

trait TestEntityRepository extends EntityRepository { this: Profile =>

  import profile.api._

  class TestEntities(tag: Tag) extends Table[TestEntity](tag, "test_entities") with EntityTable[TestEntityId, TestEntity] {
    def id = column[TestEntityId]("id", O.PrimaryKey, O.AutoInc)
    def version = column[Version]("version")
    def created = column[Instant]("created")
    def modified = column[Instant]("modified")
    def name = column[String]("name")

    def * = (id.?, version.?, created.?, modified.?, name) <> (TestEntity.tupled, TestEntity.unapply)
  }

  object testEntities extends EntityQueries[TestEntityId, TestEntity, TestEntities](new TestEntities(_)) {

    def copyEntityFields(entity: TestEntity, id: Option[TestEntityId], version: Option[Version], created: Option[Instant], modified: Option[Instant]): TestEntity =
      entity.copy(id = id, version = version, created = created, modified = modified)

  }
}
