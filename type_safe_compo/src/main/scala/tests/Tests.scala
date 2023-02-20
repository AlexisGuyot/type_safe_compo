package tests 

import scala.reflect.runtime.universe._
import org.scalatest.Assertions._
import shapeless.record._

import types._ 
import functions._ 

object further_tests {
    import support_functions._ 
    import safe_composition._ 

    type SchemaDepart = Record.`'user -> String, 'tweet -> String, 'hashtags -> List[String]`.T
    type SchemaSouhaite = Record.`'user -> String, 'hashtag -> String`.T

    // Tests on schema validation for models
    val testsModels1 = new JSON[SchemaDepart]
    val testsModels2 = new JSON[SchemaSouhaite]
    val testsModels3 = new Relation[SchemaSouhaite]
    //val testsModels4 = new Relation[SchemaDepart]

    // Tests on model transformations
    val testTransform1 = ModelTransform[Relation[SchemaSouhaite], JSON[SchemaSouhaite]]
    val testTransform2 = ModelTransform[Relation[SchemaSouhaite], Relation[SchemaSouhaite]]
    val testTransform3 = ModelTransform[JSON[SchemaSouhaite], Relation[SchemaSouhaite]]
    val testTransform4 = ModelTransform[JSON[SchemaSouhaite], JSON[SchemaSouhaite]]
    val testTransform5 = ModelTransform[JSON[SchemaDepart], JSON[SchemaDepart]]

    def apply() = {
        // Not Compiling Assertions
        assertDoesNotCompile("val testsModels4 = new Relation[SchemaDepart]")
        assertDoesNotCompile("val testTransform6 = ModelTransform[JSON[SchemaDepart], Relation[SchemaDepart]]")

        // Tests on support functions
        printHList[SchemaDepart]
        printHList[SchemaSouhaite]
    }
}