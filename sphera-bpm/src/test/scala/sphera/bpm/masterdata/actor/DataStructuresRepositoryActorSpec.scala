package sphera.bpm.masterdata.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.core.test.PersistenceSpec

class DataStructuresRepositoryActorSpec extends TestKit(ActorSystem("DataStructuresRepositorySpec"))
  with PersistenceSpec
  with NewDataStructuresRepository {
  import DataStructuresRepositoryActor._

  "A DataStructuresRepositoryActor" when receive {
    "CreateCmd" must {
      "create new defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateDataStructure()
          d2 <- generateUpdateDataStructure()
          a <- newDataStructureRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, GetAll).mapTo[DataStructureMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z should have size 2
          z should contain key d1.id
          z should contain key d2.id
          z(d1.id).name shouldBe d1.name
          z(d1.id).description shouldBe d1.description
          z(d1.id).elements shouldBe d1.elements
          z(d1.id).modifyAttr.createdBy shouldBe d1.userId
          z(d1.id).modifyAttr.updatedBy shouldBe None
        }
      }
    }
    "UpdateCmd" must {
      "update defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateDataStructure()
          d2 <- generateUpdateDataStructure(d1.id)
          a <- newDataStructureRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, UpdateCmd(d2))
          z <- ask(a, GetById(d1.id)).mapTo[DataStructureOpt].map(_.x.get)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z.id shouldBe d1.id
          z.name shouldBe d2.name
          z.description shouldBe d2.description
          z.elements shouldBe d2.elements
          z.modifyAttr.createdBy shouldBe d1.userId
          z.modifyAttr.updatedBy shouldBe Option(d2.userId)
        }
      }
      "not update when defStorageActor structure not exist" in {
        for {
          d1 <- generateUpdateDataStructure()
          a <- newDataStructureRepositoryActor()
          z <- ask(a, UpdateCmd(d1))
        } yield z shouldBe NotFound
      }
    }
    "DeleteCmd" must {
      "delete defStorageActor structure in repository" in {
        for {
          d1 <- generateUpdateDataStructure()
          d2 <- generateUpdateDataStructure()
          a <- newDataStructureRepositoryActor()
          x <- ask(a, CreateCmd(d1))
          y <- ask(a, CreateCmd(d2))
          z <- ask(a, DeleteCmd(d2.id))
          b <- ask(a, GetAll).mapTo[DataStructureMap].map(_.x)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z shouldBe Done
          b should have size 1
          b should contain key d1.id
        }
      }
      "not delete when defStorageActor structure not exist" in {
        for {
          d1 <- generateUpdateDataStructure()
          a <- newDataStructureRepositoryActor()
          z <- ask(a, DeleteCmd(d1.id))
        } yield z shouldBe NotFound
      }
    }
  }
}