package types

import shapeless._
import shapeless.labelled.FieldType
import scala.annotation.implicitNotFound

// Checks if a given model type is conform
sealed trait ValidateSchema[+M <: Model[_]]
object ValidateSchema {
    def apply[M <: Model[_]](implicit ok: ValidateSchema[M]): ValidateSchema[M] = ok

    implicit def validate_json[S <: HList : ValidateSchemaJSON]: ValidateSchema[JSON[S]] = new ValidateSchema[JSON[S]] {}
    implicit def validate_relation[S <: HList : ValidateSchemaRelation]: ValidateSchema[Relation[S]] = new ValidateSchema[Relation[S]] {}
}

// Checks if a given schema (as HList) conforms to a model: authorized types for values.
sealed trait ValidateSchemaModel[Schema]

object Null

/* -- JSON Model -- */

sealed trait ValidateSchemaJSON[Schema] extends ValidateSchemaModel[Schema]

object ValidateSchemaJSON {
    def apply[S <: HList](implicit ok: ValidateSchemaJSON[S]): ValidateSchemaJSON[S] = ok

    // Scalar types
    implicit val json_string: ValidateSchemaJSON[String] = new ValidateSchemaJSON[String] {}
    implicit val json_int: ValidateSchemaJSON[Int] = new ValidateSchemaJSON[Int] {}
    implicit val json_double: ValidateSchemaJSON[Double] = new ValidateSchemaJSON[Double] {}
    implicit val json_boolean: ValidateSchemaJSON[Boolean] = new ValidateSchemaJSON[Boolean] {}
    implicit val json_null: ValidateSchemaJSON[Null] = new ValidateSchemaJSON[Null] {}

    // Homogeneous collection
    implicit def json_hom_array[T](implicit ok: ValidateSchemaJSON[T]): ValidateSchemaJSON[List[T]] = new ValidateSchemaJSON[List[T]] {}

    // HList traversal + Heterogeneous collection and recursive structure
    implicit val json_hnil: ValidateSchemaJSON[HNil] = new ValidateSchemaJSON[HNil] {}
    implicit def json_hlist[K, H, T <: HList](
        implicit 
        head_ok: ValidateSchemaJSON[H], 
        tail_ok: ValidateSchemaJSON[T]
    ): ValidateSchemaJSON[FieldType[K,H]::T] = new ValidateSchemaJSON[FieldType[K,H]::T] {}
}

/* -- Relational Model -- */

sealed trait ValidateSchemaRelation[Schema] extends ValidateSchemaModel[Schema]

sealed trait IsRelationalScalar[T]
object IsRelationalScalar {
    def apply[T](implicit ok: IsRelationalScalar[T]): IsRelationalScalar[T] = ok

    // Scalar types
    implicit val rel_string: IsRelationalScalar[String] = new IsRelationalScalar[String] {}
    implicit val rel_int: IsRelationalScalar[Int] = new IsRelationalScalar[Int] {}
    implicit val rel_double: IsRelationalScalar[Double] = new IsRelationalScalar[Double] {}
    implicit val rel_boolean: IsRelationalScalar[Boolean] = new IsRelationalScalar[Boolean] {}
    implicit val rel_null: IsRelationalScalar[Null] = new IsRelationalScalar[Null] {}
}

object ValidateSchemaRelation {
    def apply[S <: HList](implicit ok: ValidateSchemaRelation[S]): ValidateSchemaRelation[S] = ok

    // HList traversal
    implicit val rel_hnil: ValidateSchemaRelation[HNil] = new ValidateSchemaRelation[HNil] {}
    implicit def rel_hlist[K, H, T <: HList](
        implicit 
        head_ok: IsRelationalScalar[H], 
        tail_ok: ValidateSchemaRelation[T]
    ): ValidateSchemaRelation[FieldType[K,H]::T] = new ValidateSchemaRelation[FieldType[K,H]::T] {}
}