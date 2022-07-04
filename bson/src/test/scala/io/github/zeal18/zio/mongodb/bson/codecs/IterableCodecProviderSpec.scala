/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zeal18.zio.mongodb.bson.codecs

import io.github.zeal18.zio.mongodb.bson.BaseSpec
import org.bson.codecs.configuration.CodecRegistries.fromProviders

class IterableCodecProviderSpec extends BaseSpec {

  "IterableCodecProvider" should "get the correct codec" in {

    val provider = IterableCodecProvider()
    val registry = fromProviders(provider)

    provider.get[Iterable[Any]](classOf[Iterable[Any]], registry) shouldBe a[IterableCodec]
    provider.get[List[String]](classOf[List[String]], registry) shouldBe a[IterableCodec]
    provider.get[Seq[Integer]](classOf[Seq[Integer]], registry) shouldBe a[IterableCodec]
    provider
      .get[Map[String, Integer]](classOf[Map[String, Integer]], registry) shouldBe a[IterableCodec]
    Option(provider.get[String](classOf[String], registry)) shouldBe None
  }

}
