package functions

import scala.reflect.runtime.universe._
import shapeless._
import shapeless.labelled.FieldType

object support_functions {
    // Returns the label of type T as a string
    def show[T](value: T)(implicit tag: TypeTag[T]) = tag.toString().replace("Main.", "") + "\n"

    // Implicit recipe to print the type of a record (labelled HList)
    implicit def fieldTypeTypeable[K,V](
        implicit 
        labelOfField: Witness.Aux[K], 
        typeOfField: Typeable[V]
    ): Typeable[FieldType[K,V]] = new Typeable[FieldType[K,V]] {
        def describe = s"(${labelOfField.value}: ${typeOfField.describe})"
        def cast(t: Any): Option[FieldType[K,V]] = None
    }

    // Prints a record (labelled HList)
    def printHList[T](implicit t: Typeable[T]) = println(t.describe)
}