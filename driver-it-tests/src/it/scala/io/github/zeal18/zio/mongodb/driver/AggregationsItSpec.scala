package io.github.zeal18.zio.mongodb.driver

import io.github.zeal18.zio.mongodb.bson.collection.immutable.Document
import io.github.zeal18.zio.mongodb.driver.aggregates.accumulators
import io.github.zeal18.zio.mongodb.driver.aggregates.expressions
import io.github.zeal18.zio.mongodb.testkit.MongoClientTest
import io.github.zeal18.zio.mongodb.testkit.MongoCollectionTest
import org.bson.BsonDocument
import zio.Chunk
import zio.test.*

object AggregationsItSpec extends ZIOSpecDefault {
  override def spec =
    suite("AggregationsItSpec")(
      suite("match")(
        test("query") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
              """{"_id": ObjectId("512bc962e835e68f199c8687"), "author": "dave", "score": 85, "views": 521 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b259"), "author": "annT", "score": 60, "views": 50 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25b"), "author": "ty", "score": 95, "views": 1000 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
              """{"_id": ObjectId("512bc962e835e68f199c8687"), "author": "dave", "score": 85, "views": 521 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result1 <- collection
                .aggregate(aggregates.`match`(filters.eq("author", "dave")))
                .runToChunk
            } yield assertTrue(result1 == expected)
          }
        },
        test("expression") {
          MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
            val documents = Chunk(
              """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
              """{"_id": ObjectId("512bc962e835e68f199c8687"), "author": "dave", "score": 85, "views": 521 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
              """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b259"), "author": "annT", "score": 60, "views": 50 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25b"), "author": "ty", "score": 95, "views": 1000 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            val expected = Chunk(
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
              """{"_id": ObjectId("55f5a1d3d4bede9ac365b25b"), "author": "ty", "score": 95, "views": 1000 }""",
            ).map(d => Document(BsonDocument.parse(d)))

            for {
              _ <- collection.insertMany(documents)

              result1 <- collection
                .aggregate(
                  aggregates.`match`(
                    expressions.gte(expressions.fieldPath("$score"), expressions.const(90)),
                  ),
                )
                .runToChunk
            } yield assertTrue(result1 == expected)
          }
        },
      ),
      test("limit") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
            """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected = Chunk(
            """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
            """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
          ).map(d => Document(BsonDocument.parse(d)))

          for {
            _ <- collection.insertMany(documents)

            result1 <- collection.aggregate(aggregates.limit(2)).runToChunk
          } yield assertTrue(result1 == expected)
        }
      },
      test("count") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{"_id": ObjectId("512bc95fe835e68f199c8686"), "author": "dave", "score": 80, "views": 100 }""",
            """{"_id": ObjectId("55f5a1d3d4bede9ac365b25a"), "author": "li", "score": 94, "views": 999 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b257"), "author": "ahn", "score": 60, "views": 1000 }""",
            """{"_id": ObjectId("55f5a192d4bede9ac365b258"), "author": "li", "score": 55, "views": 5000 }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected1 = Chunk(Document(BsonDocument.parse("""{"count": 4}""")))
          val expected2 = Chunk(Document(BsonDocument.parse("""{"authors_count": 4}""")))

          for {
            _ <- collection.insertMany(documents)

            result1 <- collection.aggregate(aggregates.count()).runToChunk
            result2 <- collection.aggregate(aggregates.count("authors_count")).runToChunk
          } yield assertTrue(result1 == expected1 && result2 == expected2)
        }
      },
      test("bucket") {
        MongoCollectionTest.withRandomName[Document, TestResult] { collection =>
          val documents = Chunk(
            """{ "_id" : 1, "last_name" : "Bernard", "first_name" : "Emil", "year_born" : 1868, "year_died" : 1941, "nationality" : "France" }""",
            """{ "_id" : 2, "last_name" : "Rippl-Ronai", "first_name" : "Joszef", "year_born" : 1861, "year_died" : 1927, "nationality" : "Hungary" }""",
            """{ "_id" : 3, "last_name" : "Ostroumova", "first_name" : "Anna", "year_born" : 1871, "year_died" : 1955, "nationality" : "Russia" }""",
            """{ "_id" : 4, "last_name" : "Van Gogh", "first_name" : "Vincent", "year_born" : 1853, "year_died" : 1890, "nationality" : "Holland" }""",
            """{ "_id" : 5, "last_name" : "Maurer", "first_name" : "Alfred", "year_born" : 1868, "year_died" : 1932, "nationality" : "USA" }""",
            """{ "_id" : 6, "last_name" : "Munch", "first_name" : "Edvard", "year_born" : 1863, "year_died" : 1944, "nationality" : "Norway" }""",
            """{ "_id" : 7, "last_name" : "Redon", "first_name" : "Odilon", "year_born" : 1840, "year_died" : 1916, "nationality" : "France" }""",
            """{ "_id" : 8, "last_name" : "Diriks", "first_name" : "Edvard", "year_born" : 1855, "year_died" : 1930, "nationality" : "Norway" }""",
          ).map(d => Document(BsonDocument.parse(d)))

          val expected =
            Chunk(
              Document(
                BsonDocument.parse(
                  """|{ "_id" : 1860, "count" : 4, "artists" : [
                     |  { "name" : "Emil Bernard", "year_born" : 1868 },
                     |  { "name" : "Joszef Rippl-Ronai", "year_born" : 1861 },
                     |  { "name" : "Alfred Maurer", "year_born" : 1868 },
                     |  { "name" : "Edvard Munch", "year_born" : 1863 }
                     |]}""".stripMargin,
                ),
              ),
            )

          for {
            _ <- collection.insertMany(documents)

            result <- collection
              .aggregate(
                aggregates.bucket(
                  groupBy = expressions.fieldPath("$year_born"),
                  boundaries = Seq(1840, 1850, 1860, 1870, 1880),
                  default = "Other",
                  output = Map(
                    "count" -> accumulators.sum(expressions.const(1)),
                    "artists" -> accumulators.push(
                      expressions.obj(
                        "name" -> expressions.concat(
                          expressions.fieldPath("$first_name"),
                          expressions.const(" "),
                          expressions.fieldPath("$last_name"),
                        ),
                        "year_born" -> expressions.fieldPath("$year_born"),
                      ),
                    ),
                  ),
                ),
                aggregates.`match`(filters.gt("count", 3)),
              )
              .runToChunk
          } yield assertTrue(result == expected)
        }
      },
    ).provideLayerShared(MongoClientTest.live().orDie)
}
