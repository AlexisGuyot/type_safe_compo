# Controlling composition of operators in data lake analytical workflows with type theory

This repository contains useful types and functions to build and ensure the technical consistency of analytical workflows in data lakes through types.

## Context

Data Lakes are flexible systems suitable for managing and analyzing big data thanks to their schema-on-read paradigm. To extract value from data in data lakes, data analysts use complex analytical workflows based on compositions of various operators. These workflows are built on-the-fly and therefore the technical consistency of the compositions must be directly ensured by data lake users, which is error prone. Furthermore, composed operators may have different purposes (searching, transforming, enriching, analyzing, etc.) and may span multiple data models (relational, semi-structured, graphs, key-value, etc.) and levels of abstraction (data, schemas, metadata). Yet, ensuring the robustness of analyses in data lakes is essential. In this article (REF coming later), we propose a formal framework based on type theory to control the compositions of operators in data lakes. We use expressive types to precisely describe restrictions on inputs of operators and to specify schema inference rules on outputs. 

This github repository contains an open source implementation in Scala of our framework.

## Use Case

We illustrate the usefulness of our proposal with a use case.

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

As a data analyst, I would like to build an analytical workflow that:
1) transforms json data into a relation R(user: String, hashtag: String), which requires to unfold the 'hashtags' attribute;
2) aggregates R to compute the frequency of use of hashtags for each user;
3) transforms the relation into a user-hashtag matrix, which contains the frequencies as values;
4) applies a Singular Value Decomposition (SVD) on the user-hashtag matrix to identify groups of users and hashtags.

I want to ensure the robustness of my workflow thanks to safe compositions.

### Experiment

*NB: code for the following experiment can be found in the file **type_safe_compo/src/main/scala/Main.scala**.*

We build 4 versions of the workflow previously described: two versions use the standard composition of Scala; the other ones use the safe composition allowed by our framework. Each time, one of the two versions is correct and the other one is technically inconsistent. We expect that technically inconsistent workflows do not compile.

We introduce the following technical inconsistency: json data cannot be transformed into relation if the 'hashtags' attribute is not unfolded (relations cannot have attributes whose type is list of string).

We define the following operators to build our workflows:
```scala
def transformJSON[CurrentSchema <: HList, WantedSchema <: HList]: JSON[CurrentSchema] => JSON[WantedSchema]
def jsonToRelation[Schema <: HList]: JSON[Schema] => Relation[Schema]
def computeFreq[Schema <: HList]: Relation[Schema] => Relation[FieldType[Witness.`'Freq`.T, Int]::Schema]
def relationToMatrix[Schema <: HList]: Relation[Schema] => Matrix[Double]
def svd: Matrix[Double] => Product3[Matrix[Double],Matrix[Double],Matrix[Double]]
```

Data schemas are represented by Heterogeneous Lists (HList), provided by the Shapeless library. Labelled HList are named Record in Shapeless. We can define the following schemas for json data (CurrentSchema) and relation R (WantedSchema):

```scala
type CurrentSchema = Record.`'user -> String, 'tweet -> String, 'hashtags -> List[String]`.T
type WantedSchema = Record.`'user -> String, 'hashtag -> String`.T
```

We can then define the following workflows:
- Technically consistent workflow using standard composition (andThen operator):
```scala
def myWorkflow1 = (x: JSON[CurrentSchema]) => transformJSON[CurrentSchema, WantedSchema] andThen jsonToRelation andThen computeFreq andThen relationToMatrix andThen svd
```
- Technically inconsistent workflow using standard composition (jsonToRelation cannot build a Relation parametrized with CurrentSchema):
```scala
def myWorkflow2 = (x: JSON[CurrentSchema]) => transformJSON[CurrentSchema, CurrentSchema] andThen jsonToRelation andThen computeFreq andThen relationToMatrix andThen svd
```
- Technically consistent workflow using safe composition (safeAndThen operator):
```scala
def myWorkflow3 = (x: JSON[CurrentSchema]) => transformJSON[CurrentSchema, WantedSchema] safeAndThen jsonToRelation safeAndThen computeFreq safeAndThen relationToMatrix andThen svd
```
- Technically inconsistent workflow using safe composition (jsonToRelation cannot build a Relation parametrized with CurrentSchema):
```scala
def myWorkflow4 = (x: JSON[CurrentSchema]) => transformJSON[CurrentSchema, CurrentSchema] safeAndThen jsonToRelation safeAndThen computeFreq safeAndThen relationToMatrix andThen svd
```

As expected, myWorkflow1 and myWorkflow2 compile. However, only myWorkflow4 does not compile despite the common technical inconsistency with myWorkflow3.

**=> This means that we have successfully improved the robustness of this workflow by detecting its technical inconsistency through our formal framework implemented as a type system.**

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
    - **ModelTransform.scala**: *types that can be implicitly built if data in a specific model can be transformed into another specific model.*
    - **ValidateSchema.scala**: *types that can be implicitly built if a schema (as a HList) conforms to a given model.*
- **Main.scala**: *source code for the example presented in Use Case.*

