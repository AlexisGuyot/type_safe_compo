package types

import shapeless._
import shapeless.labelled.FieldType

// A model is parametrized with a (possibly empty) schema in the form of a HList (empty schema = HNil)
sealed trait Model[S <: HList]

// A schema S (HList) must conforms to the model (i.e. an instance of ValidateSchemaModel should be able to be implicitly built with S)
sealed class JSON[S <: HList : SchemaJSON] extends Model[S]
sealed class Relation[S <: HList : SchemaRelation] extends Model[S]
sealed trait KeyValue[F <: FieldType[_,_]] extends Model[F::HNil]