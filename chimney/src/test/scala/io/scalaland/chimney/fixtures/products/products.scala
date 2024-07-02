package io.scalaland.chimney.fixtures.products

import io.scalaland.chimney.*

object Domain1 {

  case class UserName(value: String)

  val userNameToStringTransformer: Transformer[UserName, String] =
    (userName: UserName) => userName.value + "T"
  val userNameToStringPartialTransformer: PartialTransformer[UserName, String] =
    (userName: UserName, _) => partial.Result.fromValue(userName.value + "T")

  case class UserDTO(id: String, name: String)
  case class User(id: String, name: UserName)
}

object Poly {

  case class MonoSource(poly: String, other: String)
  case class PolySource[A](poly: A, other: String)
  case class MonoTarget(poly: String, other: String)
  case class PolyTarget[A](poly: A, other: String)

  val monoSource = MonoSource("test", "test")
  val polySource = PolySource("test", "test")
  val monoTarget = MonoTarget("test", "test")
  val polyTarget = PolyTarget("test", "test")
}

object NonCaseDomain {

  class ClassSource(val id: String, val name: String)

  trait TraitSource {
    val id: String
    val name: String
  }

  class TraitSourceImpl(val id: String, val name: String) extends TraitSource
}

case class Foo(x: Int, y: String, z: (Double, Double))
case class Bar(x: Int, z: (Double, Double))
case class BarParams[A, B](x: A, z: (B, Double))
case class HaveY(y: String)

object Renames {

  case class User(id: Int, name: String, age: Option[Int])
  case class UserStrict(id: Int, name: String, age: Int)
  case class UserPL(id: Int, imie: String, wiek: Either[Unit, Int])
  case class UserPLStd(id: Int, imie: String, wiek: Option[Int])
  case class User2ID(id: Int, name: String, age: Option[Int], extraID: Int)
  case class UserPLStrict(id: Int, imie: String, wiek: Int)

  def ageToWiekTransformer: Transformer[Option[Int], Either[Unit, Int]] =
    new Transformer[Option[Int], Either[Unit, Int]] {
      def transform(obj: Option[Int]): Either[Unit, Int] =
        obj.fold[Either[Unit, Int]](Left(()))(Right.apply)
    }
}

object Defaults {

  case class Source(xx: Int, yy: String, z: Double)
  case class Target(x: Int = 10, y: String = "y", z: Double)
  case class Target2(xx: Long = 10L, yy: String = "y", z: Double)
  class Target3(val xx: Long = 10L, val yy: String = "y", val z: Double) {
    override def toString: String = s"Target3($xx, $yy, $z)"
    override def equals(obj: Any): Boolean = obj match {
      case another: Target3 => xx == another.xx && yy == another.yy && z == another.z
      case _                => false
    }
  }

  case class Nested[A](value: A)
}

object Inherited {

  trait SourceParent {
    val value: String = "value"
  }
  class Source extends SourceParent

  case class Target(value: String)
}

object Accessors {

  case class Source(x: Int) {
    val y: String = x.toString
    def z: Double = x.toDouble
  }
  case class Target(x: Int, y: String)
  case class Target2(x: Int, z: Double)

  case class Nested[A](value: A)
}
