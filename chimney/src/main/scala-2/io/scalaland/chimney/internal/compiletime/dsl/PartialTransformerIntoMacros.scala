package io.scalaland.chimney.internal.compiletime.dsl

import io.scalaland.chimney.dsl.PartialTransformerInto
import io.scalaland.chimney.internal.runtime.{ArgumentLists, Path, TransformerCfg, TransformerFlags}
import io.scalaland.chimney.internal.runtime.TransformerCfg.*

import scala.annotation.unused
import scala.reflect.macros.whitebox

class PartialTransformerIntoMacros(val c: whitebox.Context) extends utils.DslMacroUtils {

  import c.universe.{Select as _, *}

  def withFieldConstImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](selector: Tree, value: Tree)(@unused ev: Tree): Tree = c.prefix.tree
    .addOverride(value)
    .asInstanceOfExpr(
      new ApplyFieldNameType {
        def apply[FromField <: Path: WeakTypeTag]: c.WeakTypeTag[?] =
          weakTypeTag[PartialTransformerInto[From, To, FieldConst[FromField, Cfg], Flags]]
      }.applyFromSelector(selector)
    )

  def withFieldConstPartialImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](selector: Tree, value: Tree)(@unused ev: Tree): Tree = c.prefix.tree
    .addOverride(value)
    .asInstanceOfExpr(
      new ApplyFieldNameType {
        def apply[FromField <: Path: WeakTypeTag]: c.WeakTypeTag[?] =
          weakTypeTag[PartialTransformerInto[From, To, FieldConstPartial[FromField, Cfg], Flags]]
      }.applyFromSelector(selector)
    )

  def withFieldComputedImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](selector: Tree, f: Tree)(@unused ev: Tree): Tree = c.prefix.tree
    .addOverride(f)
    .asInstanceOfExpr(
      new ApplyFieldNameType {
        def apply[FromField <: Path: WeakTypeTag]: c.WeakTypeTag[?] =
          weakTypeTag[PartialTransformerInto[From, To, FieldComputed[FromField, Cfg], Flags]]
      }.applyFromSelector(selector)
    )

  def withFieldComputedPartialImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](selector: Tree, f: Tree)(@unused ev: Tree): Tree = c.prefix.tree
    .addOverride(f)
    .asInstanceOfExpr(
      new ApplyFieldNameType {
        def apply[FromField <: Path: WeakTypeTag]: c.WeakTypeTag[?] =
          weakTypeTag[PartialTransformerInto[From, To, FieldComputedPartial[FromField, Cfg], Flags]]
      }.applyFromSelector(selector)
    )

  def withFieldRenamedImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](selectorFrom: Tree, selectorTo: Tree): Tree = c.prefix.tree
    .asInstanceOfExpr(
      new ApplyFieldNameTypes {
        def apply[FromField <: Path: WeakTypeTag, ToField <: Path: WeakTypeTag]: c.WeakTypeTag[?] =
          weakTypeTag[PartialTransformerInto[From, To, FieldRelabelled[FromField, ToField, Cfg], Flags]]
      }.applyFromSelectors(selectorFrom, selectorTo)
    )

  def withCoproductInstanceImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag,
      Inst: WeakTypeTag
  ](f: Tree): Tree = new ApplyFixedCoproductType {
    def apply[FixedInstance: WeakTypeTag]: Tree = c.prefix.tree
      .addOverride(f)
      .asInstanceOfExpr[PartialTransformerInto[From, To, CoproductInstance[FixedInstance, To, Cfg], Flags]]
  }.applyJavaEnumFixFromClosureSignature[Inst](f)

  def withCoproductInstancePartialImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag,
      Inst: WeakTypeTag
  ](f: Tree): Tree = new ApplyFixedCoproductType {
    def apply[FixedInstance: WeakTypeTag]: Tree = c.prefix.tree
      .addOverride(f)
      .asInstanceOfExpr[PartialTransformerInto[From, To, CoproductInstancePartial[FixedInstance, To, Cfg], Flags]]
  }.applyJavaEnumFixFromClosureSignature[Inst](f)

  def withConstructorImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](f: Tree)(@unused ev: Tree): Tree = new ApplyConstructorType {
    def apply[Ctor <: ArgumentLists: WeakTypeTag]: Tree = c.prefix.tree
      .addOverride(f)
      .asInstanceOfExpr[PartialTransformerInto[From, To, Constructor[Ctor, To, Cfg], Flags]]
  }.applyFromBody(f)

  def withConstructorPartialImpl[
      From: WeakTypeTag,
      To: WeakTypeTag,
      Cfg <: TransformerCfg: WeakTypeTag,
      Flags <: TransformerFlags: WeakTypeTag
  ](f: Tree)(@unused ev: Tree): Tree = new ApplyConstructorType {
    def apply[Ctor <: ArgumentLists: WeakTypeTag]: Tree = c.prefix.tree
      .addOverride(f)
      .asInstanceOfExpr[PartialTransformerInto[From, To, ConstructorPartial[Ctor, To, Cfg], Flags]]
  }.applyFromBody(f)
}
