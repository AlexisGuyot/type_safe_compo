package types

import shapeless._
import shapeless.labelled.FieldType


/* -- Relational Model -- */

sealed trait SchemaRelation[Schema]

sealed trait RelationalScalar[T]
object RelationalScalar {
    def apply[T](implicit ok: RelationalScalar[T]): RelationalScalar[T] = ok

    // Scalar types
    implicit val rel_string: RelationalScalar[String] = new RelationalScalar[String] {}
    implicit val rel_int: RelationalScalar[Int] = new RelationalScalar[Int] {}
    implicit val rel_double: RelationalScalar[Double] = new RelationalScalar[Double] {}
    implicit val rel_boolean: RelationalScalar[Boolean] = new RelationalScalar[Boolean] {}
}

object SchemaRelation {
    def apply[S <: HList](implicit ok: SchemaRelation[S]): SchemaRelation[S] = ok

    // HList traversal
    implicit val rel_hnil: SchemaRelation[HNil] = new SchemaRelation[HNil] {}
    implicit def rel_hlist[N, T, S <: HList](
        implicit 
        head_ok: RelationalScalar[T], 
        tail_ok: SchemaRelation[S]
    ): SchemaRelation[FieldType[N,T]::S] = new SchemaRelation[FieldType[N,T]::S] {}
}


/* -- JSON Model -- */

sealed trait SchemaJSON[Schema]

sealed trait JSONType[T]
object JSONType {
    def apply[T](implicit ok: JSONType[T]): JSONType[T] = ok 

    // Scalar types
    implicit val json_string: JSONType[String] = new JSONType[String] {}
    implicit val json_int: JSONType[Int] = new JSONType[Int] {}
    implicit val json_double: JSONType[Double] = new JSONType[Double] {}
    implicit val json_boolean: JSONType[Boolean] = new JSONType[Boolean] {}

    // Homogeneous collection
    implicit def json_hom_array[T](implicit ok: JSONType[T]): JSONType[List[T]] = new JSONType[List[T]] {}

    // Other JSON Object (+ heterogeneous collections)
    implicit def json_object[S <: HList](implicit ok: SchemaJSON[S]): JSONType[S] = new JSONType[S] {}
}

object SchemaJSON {
    def apply[S <: HList](implicit ok: SchemaJSON[S]): SchemaJSON[S] = ok

    // HList traversal
    implicit val json_hnil: SchemaJSON[HNil] = new SchemaJSON[HNil] {}
    implicit def json_hlist[N, T, S <: HList](
        implicit 
        head_ok: JSONType[T], 
        tail_ok: SchemaJSON[S]
    ): SchemaJSON[FieldType[N,T]::S] = new SchemaJSON[FieldType[N,T]::S] {}
}


/* -- Checks if a given model type is conform -- */ 


sealed trait ValidateSchema[+M <: Model[_]]
object ValidateSchema {
    def apply[M <: Model[_]](implicit ok: ValidateSchema[M]): ValidateSchema[M] = ok

    implicit def validate_json[S <: HList : SchemaJSON]: ValidateSchema[JSON[S]] = new ValidateSchema[JSON[S]] {}
    implicit def validate_relation[S <: HList : SchemaRelation]: ValidateSchema[Relation[S]] = new ValidateSchema[Relation[S]] {}
    implicit def validate_keyvalue[F <: FieldType[_,_]]: ValidateSchema[KeyValue[F]] = new ValidateSchema[KeyValue[F]] {}
}