package functions

import types._

object safe_composition {
    // Defines a safe composition between two functions on models 
    implicit class SafeComposition[M1 <: Model[_], M2 <: Model[_]](private val f: M1 => M2) extends Function1[M1, M2] {
        def apply(v1: M1): M2 = f(v1)

        // A composition is safe if:
        // - the schema returned by the first function and taken as input by the second is conforms to its model;
        def safeAndThen[M4 <: Model[_]](g: M2 => M4)(
            implicit 
            check: ValidateSchema[M2]
        ): M1 => M4 = { x => g(f(x)) }
    }
}