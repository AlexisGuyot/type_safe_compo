package functions

import shapeless._
import shapeless.labelled.FieldType
import scala.reflect.runtime.universe._

object support_functions {
    // Returns the label of type T as a string
    def show[T](val_name: String, value: T)(implicit tag: TypeTag[T]): String = 
        val_name + ": " + tag.toString()
        .replace("Main.", "")
        .replace("TypeTag[", "")
        .replace("types.", "")
        .replace("shapeless.labelled.", "")
        .replace("shapeless.tag.", "")
        .dropRight(1) + "\n"

    def show[T : TypeTag](value: T): String = show("", value)

    // Implicit recipe to print the type of a record (labelled HList)
    implicit def fieldTypeTypeable[K,V](
        implicit 
        labelOfField: Witness.Aux[K], 
        typeOfField: Typeable[V]
    ): Typeable[FieldType[K,V]] = new Typeable[FieldType[K,V]] {
        def describe = s"(${labelOfField.value}: ${typeOfField.describe})"
        def cast(t: Any): Option[FieldType[K,V]] = None
    }

    // Prints the output of the show function
    def printType[T: TypeTag](val_name: String, value: T): Unit = println(show(val_name, value))

    def printType[T: TypeTag](value: T): Unit = printType("Type", value)

    // Prints a record (labelled HList)
    def printHList[T](implicit t: Typeable[T]) = println(t.describe)
}