# Preventing technical errors in data lake analyses with type theory

This repository contains useful types and functions to build and ensure the technical consistency of analytical workflows in data lakes through types.

## Context

Data Lakes are flexible systems suitable for managing and analyzing big data thanks to their schema-on-read paradigm.
To extract value from data in data lakes, data analysts use complex analytical workflows based on compositions of various operators. These workflows are built on-demand and therefore the technical consistency of compositions must be directly ensured by data lake users, which is error prone. Furthermore, composed operators may have different purposes (searching, transforming, enriching, analyzing, etc.) and may cross multiple data models (relational, semi-structured, key-value, etc.) and levels of abstraction (data, schema, model, metadata). Yet, ensuring the robustness of analyses in data lakes is essential. In this article (REF coming later), we propose a formal framework based on type theory to prevent technical errors in compositions of operators. We use expressive types to precisely describe restrictions on operator parameters and to specify schema inference rules on outputs. 

This github repository contains an open source implementation in Scala which can be used to declare analytical workflows and to check their technical consistency at compile time.

## Use Case

We illustrate the usefulness of our proposal with a small use case.

### Proposed context

Data (tweets) have been collected from Twitter and ingested as-it-is in a data lake as JSON files.

Tweets in the JSON files have the following structure:
```json
{
"user": "userA",
"tweet": "This is my tweet ! #hashtag1 #hashtag2",
"hashtags": ["hashtag1", "hashtag2"]
},
...
```

### Objectives

As a data analysts, we would like to build an analytical workflow that:
1) transforms json data into a relation R(user: String, hashtag: String), which requires to unfold the 'hashtags' attribute;
2) aggregates R to compute the frequency of use of hashtags for each user;
3) transforms the relation into a user-hashtag matrix, which contains the frequencies as values;
4) applies a Singular Value Decomposition (SVD) on the user-hashtag matrix to identify groups of users and hashtags.

We want to ensure the robustness of my workflow through safe compositions.

### Experiment

*NB: code for the following experiment can be found in the file **type_safe_compo/src/main/scala/Main.scala**.*

We build 4 versions of the workflow previously described: two versions use the standard composition of Scala; the other ones use the safe composition allowed by our framework. Each time, one of the two versions is correct and the other one is technically inconsistent. We expect that technically inconsistent workflows do not compile.

We introduce the following technical inconsistency: json data cannot be transformed into relation if the 'hashtags' attribute is not unfolded (relations cannot have attributes whose type is a list of string).

We define the following operators to build our workflows:
```scala
def unfoldJSON[CurrentSchema <: HList, WantedSchema <: HList]: JSON[CurrentSchema] => JSON[WantedSchema]
def jsonToRelation[Schema <: HList]: JSON[Schema] => Relation[Schema]
def aggregFreq[Schema <: HList]: Relation[Schema] => Relation[FieldType[Witness.`'Freq`.T, Int]::Schema]
def relToMatrix[Schema <: HList]: Relation[Schema] => Matrix[Double]
def svd: Matrix[Double] => Product3[Matrix[Double],Matrix[Double],Matrix[Double]]
```

Data schemas are represented by Heterogeneous Lists (HList), provided by the Shapeless library. Labelled HList are named Record in Shapeless. We can define the following schemas for json data (BaseSchema) and relation R (UnfoldedSchema):

```scala
type BaseSchema = Record.`'user -> String, 'tweet -> String, 'hashtags -> List[String]`.T
type UnfoldedSchema = Record.`'user -> String, 'hashtag -> String`.T
```

We can then define the following workflows:
- Technically consistent workflow using standard composition (andThen operator):
```scala
def myWorkflow1 = (x: JSON[BaseSchema]) => unfoldJSON[BaseSchema, UnfoldedSchema] andThen jsonToRelation andThen aggregFreq andThen relToMatrix andThen svd
```
- Technically inconsistent workflow using standard composition (jsonToRelation cannot build a Relation parametrized with BaseSchema):
```scala
def myWorkflow2 = (x: JSON[BaseSchema]) => unfoldJSON[BaseSchema, BaseSchema] andThen jsonToRelation andThen aggregFreq andThen relToMatrix andThen svd
```
- Technically consistent workflow using safe composition (safeAndThen operator):
```scala
def myWorkflow3 = (x: JSON[BaseSchema]) => unfoldJSON[BaseSchema, UnfoldedSchema] safeAndThen jsonToRelation safeAndThen aggregFreq safeAndThen relToMatrix safeAndThen svd
```
- Technically inconsistent workflow using safe composition (jsonToRelation cannot build a Relation parametrized with BaseSchema):
```scala
def myWorkflow4 = (x: JSON[BaseSchema]) => unfoldJSON[BaseSchema, BaseSchema] safeAndThen jsonToRelation safeAndThen aggregFreq safeAndThen relToMatrix safeAndThen svd
```

As expected, myWorkflow1 and myWorkflow2 compile. However, only myWorkflow4 does not compile despite the common technical inconsistency with myWorkflow3.

**=> This means that we have successfully improved the robustness of this workflow by detecting its technical inconsistency.**

## Project Organization

Source code for the project can be found in **type_safe_compo/src/main/scala**.

Tree structure:
- **functions/**:
    - **Composition.scala**: *functions related to safe composition.*
    - **Support.scala**: *useful functions to print and show types and HList of types.*
- **tests/**:
    - **Tests.scala**: *further tests for the different components implemented in this project.*
- **types/**:
    - **Models.scala**: *types for the different data models (JSON, Relation, etc.).*
    - **ModelTransform.scala**: *types that can be implicitly built if data in a specific model can be transformed into another model (for future works, not used yet).*
    - **ValidateSchema.scala**: *types that can be implicitly built if a schema (as a HList) conforms to a given model.*
- **Main.scala**: *source code for the example presented in Use Case.*

