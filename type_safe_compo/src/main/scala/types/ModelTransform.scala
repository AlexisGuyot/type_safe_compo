package types

import shapeless._ 

// Checks if a transformation between two models is possible (i.e. schema of ModelIn also conforms to ModelOut)
// Inner type Out usually matches ModelOut, but can be used to describe changes on the schema.
sealed trait ModelTransform[ModelIn <: Model[_], +ModelOut <: Model[_]] { type Out ; def apply(d: ModelIn): Out }

object ModelTransform {
    def apply[ModelIn <: Model[_], ModelOut <: Model[_]](implicit ok: ModelTransform[ModelIn, ModelOut]): Aux[ModelIn, ModelOut,ok.Out] = ok

    // Auxiliary type to extract the Out inner type
    type Aux[ModelIn <: Model[_], ModelOut <: Model[_], Out0] = ModelTransform[ModelIn, ModelOut] { type Out = Out0 }

    // Simple transformations (schema of model1 conforms model2)
    implicit def toJSON[Schema <: HList : SchemaJSON, M1 <: Model[Schema]]: Aux[M1, JSON[Schema], JSON[Schema]] = new ModelTransform[M1, JSON[Schema]] { type Out = JSON[Schema];  def apply(d: M1) = new JSON[Schema] }
    implicit def toRelation[Schema <: HList : SchemaRelation, M1 <: Model[Schema]]: Aux[M1, Relation[Schema], Relation[Schema]] = new ModelTransform[M1, Relation[Schema]] { type Out = Relation[Schema]; def apply(d: M1) = new Relation[Schema] }

    // More complex transformations

    // If no schema is given for model2 (value Nothing), a transformation is still correct if the schema of model1 conforms to model2
    implicit def inference[Schema <: HList, M1 <: Model[Schema], M2[_] <: Model[_]](
        implicit
        mt: ModelTransform[M1, M2[Schema]]
    ): Aux[M1, M2[Nothing], mt.Out] = new ModelTransform[M1, M2[Nothing]] {
        type Out = mt.Out
        def apply(d: M1) = mt(d)
    }
}