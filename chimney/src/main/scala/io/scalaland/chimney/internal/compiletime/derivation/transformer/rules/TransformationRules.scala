package io.scalaland.chimney.internal.compiletime.derivation.transformer.rules

import io.scalaland.chimney.internal.compiletime.DerivationResult
import io.scalaland.chimney.internal.compiletime.derivation.transformer.Derivation
import io.scalaland.chimney.partial

import scala.annotation.nowarn

@nowarn("msg=The outer reference in this type test cannot be checked at run time.")
private[compiletime] trait TransformationRules { this: Derivation =>

  import ChimneyTypeImplicits.*

  abstract protected class Rule(val name: String) {

    def expand[From, To](implicit ctx: TransformationContext[From, To]): DerivationResult[Rule.ExpansionResult[To]]
  }

  protected object Rule {

    sealed trait ExpansionResult[+A]

    object ExpansionResult {
      // successfully expanded TransformationExpr
      case class Expanded[A](transformationExpr: TransformationExpr[A]) extends ExpansionResult[A]

      // continue expansion with another rule on the list
      case object AttemptNextRule extends ExpansionResult[Nothing]
    }

    def expandRules[From, To](
        rules: List[Rule]
    )(implicit ctx: TransformationContext[From, To]): DerivationResult[TransformationExpr[To]] = rules match {
      case Nil =>
        DerivationResult.notSupportedTransformerDerivation
      case rule :: nextRules =>
        DerivationResult
          .namedScope(s"Attempting expansion of rule ${rule.name}")(
            rule.expand[From, To].logFailure(errors => errors.prettyPrint)
          )
          .flatMap {
            case ExpansionResult.Expanded(transformationExpr) =>
              DerivationResult
                .log(s"Rule ${rule.name} expanded successfully")
                .as(transformationExpr.asInstanceOf[TransformationExpr[To]])
            case ExpansionResult.AttemptNextRule =>
              DerivationResult.log(s"Rule ${rule.name} decided to continue expansion") >>
                expandRules[From, To](nextRules)
          }
    }
  }

  sealed protected trait TransformationExpr[A] extends Product with Serializable {

    import TransformationExpr.{PartialExpr, TotalExpr}

    implicit private lazy val A: Type[A] = this match {
      case TotalExpr(expr) => Expr.typeOf(expr)
      case PartialExpr(expr) =>
        val ChimneyType.PartialResult(a) = Expr.typeOf(expr): @unchecked
        a.Underlying.asInstanceOf[Type[A]]
    }

    final def map[B: Type](f: Expr[A] => Expr[B]): TransformationExpr[B] = this match {
      case TotalExpr(expr)   => TotalExpr(f(expr))
      case PartialExpr(expr) => PartialExpr(ChimneyExpr.PartialResult.map(expr)(Expr.Function1.instance(f)))
    }

    final def flatMap[B: Type](f: Expr[A] => TransformationExpr[B]): TransformationExpr[B] = this match {
      case TotalExpr(expr) => f(expr)
      case PartialExpr(expr) =>
        ExprPromise
          .promise[A](ExprPromise.NameGenerationStrategy.FromType)
          .map(f(_).toEither)
          .foldEither { (totalE: ExprPromise[A, Expr[B]]) =>
            // '{ ${ expr }.map { a: $A => ${ b } } }
            PartialExpr(
              totalE.fulfilAsLambda[B, Expr[partial.Result[B]]](ChimneyExpr.PartialResult.map(expr)(_))
            )
          } { (partialE: ExprPromise[A, Expr[partial.Result[B]]]) =>
            // '{ ${ expr }.flatMap { a: $A => ${ resultB } } }
            PartialExpr(
              partialE
                .fulfilAsLambda[partial.Result[B], Expr[partial.Result[B]]](ChimneyExpr.PartialResult.flatMap(expr)(_))
            )
          }
    }

    final def fold[B](onTotal: Expr[A] => B)(onPartial: Expr[partial.Result[A]] => B): B = this match {
      case TotalExpr(expr)   => onTotal(expr)
      case PartialExpr(expr) => onPartial(expr)
    }

    final def toEither: Either[Expr[A], Expr[partial.Result[A]]] =
      fold[Either[Expr[A], Expr[partial.Result[A]]]](e => Left(e))(e => Right(e))

    final def ensureTotal: Expr[A] = fold(identity) { _ =>
      assertionFailed("Derived partial.Result expression where total Transformer expects direct value")
    }

    final def ensurePartial: Expr[partial.Result[A]] = fold { expr =>
      implicit val A: Type[A] = Expr.typeOf(expr)
      ChimneyExpr.PartialResult.Value(expr).upcastExpr[partial.Result[A]]
    }(identity)
  }

  protected object TransformationExpr {
    def fromTotal[A](expr: Expr[A]): TransformationExpr[A] = TotalExpr(expr)
    def fromPartial[A](expr: Expr[partial.Result[A]]): TransformationExpr[A] = PartialExpr(expr)

    final case class TotalExpr[A](expr: Expr[A]) extends TransformationExpr[A]
    final case class PartialExpr[A](expr: Expr[partial.Result[A]]) extends TransformationExpr[A]
  }

  protected val rulesAvailableForPlatform: List[Rule]
}
