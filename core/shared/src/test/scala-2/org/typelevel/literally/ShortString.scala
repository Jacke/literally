/*
 * Copyright 2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.literally.examples

import org.typelevel.literally.Literally

case class ShortString private (value: String)

object ShortString {
  val MaxLength = 10

  def fromString(value: String): Option[ShortString] =
    if (value.length > MaxLength) None else Some(new ShortString(value))

  def unsafeFromString(value: String): ShortString =
    new ShortString(value)

  object Literal extends Literally[ShortString] {
    def validate(s: String): Option[String] =
      if (s.length > MaxLength) None 
      else Some(s"ShortString must be <= ${ShortString.MaxLength} characters")

    def build(c: Context)(s: c.Expr[String]) = 
      c.universe.reify { ShortString.unsafeFromString(s.splice) }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[ShortString] = apply(c)(args: _*)
  }
}
