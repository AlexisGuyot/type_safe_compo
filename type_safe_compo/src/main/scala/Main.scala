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

  As a data analyst, I would like to build an analytical workflow that:
  1) transforms json data into a relation R(user: String, hashtag: String), which requires to unfold the 'hashtags' attribute;
  2) aggregates R to compute the frequency of use of hashtags for each user;
  3) transforms the relation into a user-hashtag matrix, which contains the frequencies as values;
  4) applies a Singular Value Decomposition (SVD) on the user-hashtag matrix to identify groups of users and hashtags.

  I want to ensure the robustness of my workflow thanks to safe compositions.
  */



  /* == Analytical Workflow Operators ============================= */

  // NB: For now we do not focus on the implementations of operators, only on the technical consistency of their composition at type level.

  def transformJSON[CurrentSchema <: HList, WantedSchema <: HList]: JSON[CurrentSchema] => JSON[WantedSchema] = ???
  def jsonToRelation[Schema <: HList]: JSON[Schema] => Relation[Schema] = ???
  def computeFreq[Schema <: HList]: Relation[Schema] => Relation[FieldType[Witness.`'Freq`.T, Int]::Schema] = ???
  def relationToMatrix[Schema <: HList]: Relation[Schema] => Matrix[Double] = ???
  def svd: Matrix[Double] => Product3[Matrix[Double],Matrix[Double],Matrix[Double]] = ???



  /* == Analytical Workflow Definition ============================= */

  // CurrentSchema shows the structure of tweets ingested in the data lake as a record (i.e. a labelled HList)
  type CurrentSchema = Record.`'user -> String, 'tweet -> String, 'hashtags -> List[String]`.T

  // WantedSchema is the structure I want to be able to transform my json into a relation
  type WantedSchema = Record.`'user -> String, 'hashtag -> String`.T

  // -------- Not Type-Safe Workflow Definition (u_)

  // ---- Technically Consistent Workflow (uc_)

  def uc_firstPartWorkflow = (x: JSON[CurrentSchema]) =>
    transformJSON[CurrentSchema, WantedSchema] andThen jsonToRelation andThen computeFreq

  def uc_myWorkflow = (x: JSON[CurrentSchema]) =>
    transformJSON[CurrentSchema, WantedSchema] andThen jsonToRelation andThen computeFreq andThen relationToMatrix andThen svd

  // Will Compile
  printType("firstPartWorkflow", uc_firstPartWorkflow)
  printType("myWorkflow", uc_myWorkflow)

  // ---- Not Technically Consistent Workflow (unc_)

  def unc_firstPartWorkflow = (x: JSON[CurrentSchema]) =>
    transformJSON[CurrentSchema, CurrentSchema] andThen jsonToRelation andThen computeFreq

  def unc_myWorkflow = (x: JSON[CurrentSchema]) =>
    transformJSON[CurrentSchema, CurrentSchema] andThen jsonToRelation andThen computeFreq andThen relationToMatrix andThen svd

  // Unfortunately, will compile even if CurrentSchema does not conform to the relational model.
  printType("firstPartWorkflow", unc_firstPartWorkflow)
  printType("myWorkflow", unc_myWorkflow)


  // -------- Type-Safe Workflow Definition (s_)

  // ---- Technically Consistent Workflow (sc_)

  def sc_firstPartWorkflow = (x: JSON[CurrentSchema]) =>
    transformJSON[CurrentSchema, WantedSchema] safeAndThen jsonToRelation safeAndThen computeFreq

  def sc_myWorkflow = (x: JSON[CurrentSchema]) =>
    transformJSON[CurrentSchema, WantedSchema] safeAndThen jsonToRelation safeAndThen computeFreq safeAndThen relationToMatrix andThen svd
  // NB: last composition uses andThen rather than safeAndThen because Product3[_,_,_] is not a Model (should be fixed later on).

  // Will Compile
  printType("firstPartWorkflow", sc_firstPartWorkflow)
  printType("myWorkflow", sc_myWorkflow)

  // ---- Not Technically Consistent Workflow (snc_)

  assertTypeError("""
    def snc_firstPartWorkflow = (x: JSON[CurrentSchema]) =>
      transformJSON[CurrentSchema, CurrentSchema] safeAndThen jsonToRelation safeAndThen computeFreq
  """)

  assertTypeError("""
    def snc_myWorkflow = (x: JSON[CurrentSchema]) =>
      transformJSON[CurrentSchema, CurrentSchema] safeAndThen jsonToRelation safeAndThen computeFreq safeAndThen relationToMatrix andThen svd
  """)

  // Do not compile because there is no way to build an instance of ValidateSchema for a relation parametrized with CurrentSchema.

  

  /* == Further Tests ============================= */

  // Individual testing of the different components.
  tests.further_tests()
  tgd.tests_tgds()
}