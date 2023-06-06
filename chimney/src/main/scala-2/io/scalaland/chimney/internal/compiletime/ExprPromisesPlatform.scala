package io.scalaland.chimney.internal.compiletime

import scala.annotation.nowarn

@nowarn("msg=The outer reference in this type test cannot be checked at run time.")
private[compiletime] trait ExprPromisesPlatform extends ExprPromises { this: DefinitionsPlatform =>

  import c.universe.{internal as _, Transformer as _, *}
  import TypeImplicits.*

  final override protected type ExprPromiseName = TermName

  protected object ExprPromise extends ExprPromiseModule {

    // made public for ChimneyExprsPlatform: Transformer.lift and PartialTransformer.lift
    def provideFreshName[From: Type](nameGenerationStrategy: NameGenerationStrategy): ExprPromiseName =
      nameGenerationStrategy match {
        case NameGenerationStrategy.FromPrefix(src) => freshTermName(src)
        case NameGenerationStrategy.FromType        => freshTermName(Type[From])
        case NameGenerationStrategy.FromExpr(expr)  => freshTermName(expr)
      }

    protected def createRefToName[From: Type](name: ExprPromiseName): Expr[From] =
      Expr.platformSpecific.asExpr[From](q"$name")

    def createAndUseLambda[From: Type, To: Type, B](
        fromName: ExprPromiseName,
        to: Expr[To],
        use: Expr[From => To] => B
    ): B =
      use(Expr.platformSpecific.asExpr[From => To](q"($fromName: ${Type[From]}) => $to"))

    def createAndUseLambda2[From: Type, From2: Type, To: Type, B](
        fromName: ExprPromiseName,
        from2Name: ExprPromiseName,
        to: Expr[To],
        use: Expr[(From, From2) => To] => B
    ): B =
      use(
        Expr.platformSpecific.asExpr[(From, From2) => To](
          q"($fromName: ${Type[From]}, $from2Name: ${Type[From2]}) => $to"
        )
      )

    private def freshTermName(prefix: String): ExprPromiseName =
      c.internal.reificationSupport.freshTermName(prefix.toLowerCase + "$")
    private def freshTermName(tpe: c.Type): ExprPromiseName =
      freshTermName(tpe.typeSymbol.name.decodedName.toString.toLowerCase)
    private def freshTermName(srcPrefixTree: Expr[?]): ExprPromiseName =
      freshTermName(toFieldName(srcPrefixTree))

    // TODO: document why it that a thing
    // undo the encoding of freshTermName
    private def toFieldName[A](srcPrefixTree: Expr[A]): String =
      srcPrefixTree.tree.toString.replaceAll("\\$\\d+", "").replace("$u002E", ".")
  }

  protected object PrependValsTo extends PrependValsToModule {

    def initializeVals[To: Type](vals: Vector[(ExprPromiseName, ExistentialExpr)], expr: Expr[To]): Expr[To] = {
      val statements = vals.map { case (name, initialValue) =>
        ExistentialExpr.use(initialValue) { tpe => expr => q"val $name: $tpe = $expr" }
      }.toList
      Expr.platformSpecific.asExpr[To](q"..$statements; $expr")
    }
  }
}
