package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.indexes
import io.github.zeal18.zio.mongodb.driver.indexes.CreateIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.DropIndexOptions
import io.github.zeal18.zio.mongodb.driver.indexes.Index
import io.github.zeal18.zio.mongodb.driver.indexes.IndexOptions
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoCollectionTest
import zio.Chunk
import zio.duration.*
import zio.test.*

object MongoCollectionSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[Environment, Failure] = suite("MongoCollectionSpec")(
    suite("indexes")(
      suite("createIndex")(
        testM("createIndex") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.createIndex(
                indexes.compound(indexes.asc("a"), indexes.desc("b")),
              )
            } yield assertTrue(result == "a_1_b_-1")
          }
        },
        testM("createIndex with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.createIndex(
                indexes.asc("a"),
                options = IndexOptions(name = Some("index-name")),
              )
            } yield assertTrue(result == "index-name")
          }
        },
        testM("createIndex in session") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.startSession().use { session =>
                collection.createIndex(session, indexes.geo2d("a"))
              }
            } yield assertTrue(result == "a_2d")
          }
        },
        testM("createIndex in session with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.startSession().use { session =>
                collection.createIndex(
                  session,
                  indexes.text("a"),
                  IndexOptions(name = Some("text-index")),
                )
              }
            } yield assertTrue(result == "text-index")
          }
        },
      ),
      suite("createIndexes")(
        testM("createIndexes") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.createIndexes(
                Index(indexes.asc("a")),
                Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
              )
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
        testM("createIndexes with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.createIndexes(
                Seq(
                  Index(indexes.asc("a")),
                  Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
                ),
                CreateIndexOptions(maxTime = 10.seconds),
              )
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
        testM("createIndexes in session") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.startSession().use { session =>
                collection.createIndexes(
                  session,
                  Index(indexes.asc("a")),
                  Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
                )
              }
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
        testM("createIndexes in session with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              result <- collection.startSession().use { session =>
                collection.createIndexes(
                  session,
                  Seq(
                    Index(indexes.asc("a")),
                    Index(indexes.desc("b"), IndexOptions(name = Some("index-name"))),
                  ),
                  CreateIndexOptions(maxTime = 10.seconds),
                )
              }
            } yield assertTrue(result == Chunk("a_1", "index-name"))
          }
        },
      ),
      suite("dropIndex")(
        testM("dropIndex by name") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              indexName <- collection.createIndex(indexes.asc("a"))
              _         <- collection.dropIndex(indexName)

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by name with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              indexName <- collection.createIndex(indexes.asc("a"))
              _         <- collection.dropIndex(indexName, DropIndexOptions(10.seconds))

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val indexKey = indexes.asc("a")

            for {
              _ <- collection.createIndex(indexKey)
              _ <- collection.dropIndex(indexKey)

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val indexKey = indexes.asc("a")

            for {
              _ <- collection.createIndex(indexKey)
              _ <- collection.dropIndex(indexKey, DropIndexOptions(10.seconds))

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by name in session") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              indexName <- collection.createIndex(indexes.asc("a"))
              _ <- collection.startSession().use { session =>
                collection.dropIndex(session, indexName)
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by name in session with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              indexName <- collection.createIndex(indexes.asc("a"))
              _ <- collection.startSession().use { session =>
                collection.dropIndex(session, indexName, DropIndexOptions(10.seconds))
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key in session") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val indexKey = indexes.asc("a")

            for {
              _ <- collection.createIndex(indexKey)
              _ <- collection.startSession().use { session =>
                collection.dropIndex(session, indexKey)
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndex by key in session with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val indexKey = indexes.asc("a")

            for {
              _ <- collection.createIndex(indexKey)
              _ <- collection.startSession().use { session =>
                collection.dropIndex(session, indexKey, DropIndexOptions(10.seconds))
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
      ),
      suite("dropIndexes")(
        testM("dropIndexes") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- collection.dropIndexes()

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndexes with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- collection.dropIndexes(DropIndexOptions(10.seconds))

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndexes in session") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- collection.startSession().use { session =>
                collection.dropIndexes(session)
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
        testM("dropIndexes in session with options") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            for {
              _ <- collection.createIndex(indexes.asc("a"))
              _ <- collection.createIndex(indexes.desc("b"))

              _ <- collection.startSession().use { session =>
                collection.dropIndexes(session, DropIndexOptions(10.seconds))
              }

              indexes <- collection.listIndexes().execute.runCollect
            } yield assertTrue(indexes.size == 1) // only the _id index remains
          }
        },
      ),
    ),
  ).provideCustomLayerShared(MongoClientTest.live().orDie)
}