/* -------------------- Imports -------------------- */

// External Dependencies
import shapeless._
import shapeless.record._
import shapeless.labelled.FieldType
import org.scalatest.Assertions._

// Internal
import types._
import functions._
import test._ 

sealed trait Matrix[T]

object Main extends App {
  import support_functions._
  import safe_composition._

  /* == Use Case ============================= */

  /* 
  Context: data (tweets) have been collected from Twitter and ingested as-it-is in a data lake as JSON files.

  Tweets in the JSON files have the following structure:
  {
    "user": "userA",
    "tweet": "This is my tweet ! #hashtag1 #hashtag2",
    "hashtags": ["hashtag1", "hashtag2"]
  },
  ...

  As a data analysts, we would like to build an analytical workflow that:
  1) transforms json data into a relation R(user: String, hashtag: String), which requires to unfold the 'hashtags' attribute;
  2) aggregates R to compute the frequency of use of hashtags for each user;
  3) transforms the relation into a user-hashtag matrix, which contains the frequencies as values;
  4) applies a Singular Value Decomposition (SVD) on the user-hashtag matrix to identify groups of users and hashtags.

  We want to ensure the robustness of my workflow thanks to safe compositions.
  */



  /* == Analytical Workflow Operators ============================= */

  // NB: For now we do not focus on the implementations of operators, only on the technical consistency of their composition at type level.

  def unfoldJSON[CurrentSchema <: HList, WantedSchema <: HList]: JSON[CurrentSchema] => JSON[WantedSchema] = ???
  def jsonToRelation[Schema <: HList]: JSON[Schema] => Relation[Schema] = ???
  def aggregFreq[Schema <: HList]: Relation[Schema] => Relation[FieldType[Witness.`'Freq`.T, Int]::Schema] = ???
  def relToMatrix[Schema <: HList]: Relation[Schema] => KeyValue[FieldType[_, Matrix[Double]]] = ???
  def svd: KeyValue[FieldType[_, Matrix[Double]]] => KeyValue[FieldType[_, Product3[Matrix[Double],Matrix[Double],Matrix[Double]]]] = ???



  /* == Analytical Workflow Definition ============================= */

  // BaseSchema shows the structure of tweets ingested in the data lake as a record (i.e. a labelled HList)
  type BaseSchema = Record.`'user -> String, 'tweet -> String, 'hashtags -> List[String]`.T

  // UnfoldedSchema is the structure I want to be able to transform my json into a relation
  type UnfoldedSchema = Record.`'user -> String, 'hashtag -> String`.T

  // -------- Not Type-Safe Workflow Definition (u_)

  // ---- Technically Consistent Workflow (uc_)

  def uc_firstPartWorkflow = (x: JSON[BaseSchema]) =>
    unfoldJSON[BaseSchema, UnfoldedSchema] andThen jsonToRelation andThen aggregFreq

  def uc_myWorkflow = (x: JSON[BaseSchema]) =>
    unfoldJSON[BaseSchema, UnfoldedSchema] andThen jsonToRelation andThen aggregFreq andThen relToMatrix andThen svd

  // Will Compile
  printType("firstPartWorkflow", uc_firstPartWorkflow)
  printType("myWorkflow", uc_myWorkflow)

  // ---- Not Technically Consistent Workflow (unc_)

  def unc_firstPartWorkflow = (x: JSON[BaseSchema]) =>
    unfoldJSON[BaseSchema, BaseSchema] andThen jsonToRelation andThen aggregFreq

  def unc_myWorkflow = (x: JSON[BaseSchema]) =>
    unfoldJSON[BaseSchema, BaseSchema] andThen jsonToRelation andThen aggregFreq andThen relToMatrix andThen svd

  // Unfortunately, will compile even if BaseSchema does not conform to the relational model.
  printType("firstPartWorkflow", unc_firstPartWorkflow)
  printType("myWorkflow", unc_myWorkflow)


  // -------- Type-Safe Workflow Definition (s_)

  // ---- Technically Consistent Workflow (sc_)

  def sc_firstPartWorkflow = (x: JSON[BaseSchema]) =>
    unfoldJSON[BaseSchema, UnfoldedSchema] safeAndThen jsonToRelation safeAndThen aggregFreq

  def sc_myWorkflow = (x: JSON[BaseSchema]) =>
    unfoldJSON[BaseSchema, UnfoldedSchema] safeAndThen jsonToRelation safeAndThen aggregFreq safeAndThen relToMatrix safeAndThen svd

  // Will Compile
  printType("firstPartWorkflow", sc_firstPartWorkflow)
  printType("myWorkflow", sc_myWorkflow)

  // ---- Not Technically Consistent Workflow (snc_)

  assertTypeError("""
    def snc_firstPartWorkflow = (x: JSON[BaseSchema]) =>
      unfoldJSON[BaseSchema, BaseSchema] safeAndThen jsonToRelation safeAndThen aggregFreq
  """)

  assertTypeError("""
    def snc_myWorkflow = (x: JSON[BaseSchema]) =>
      unfoldJSON[BaseSchema, BaseSchema] safeAndThen jsonToRelation safeAndThen aggregFreq safeAndThen relToMatrix safeAndThen svd
  """)

  // Do not compile because there is no way to build an instance of ValidateSchema for a relation parametrized with BaseSchema.

  

  /* == Further Tests ============================= */

  // Individual testing of the different components.
  tests.further_tests()
}