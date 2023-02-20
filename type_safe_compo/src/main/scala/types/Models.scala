package types

import shapeless._

// A model is parametrized with a (possibly empty) schema in the form of a HList (empty schema = HNil)
sealed trait Model[S <: HList]

// A schema S (HList) must conforms to the model (i.e. an instance of ValidateSchemaModel should be able to be implicitly built with S)
sealed class JSON[S <: HList : ValidateSchemaJSON] extends Model[S]
sealed class Relation[S <: HList : ValidateSchemaRelation] extends Model[S]
sealed trait Matrix[T] extends Model[HNil]