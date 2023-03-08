
// ======== Configuration

scalaVersion := "2.13.8"
name := "type_safe_compo"
organization := "fr.u-bourgogne.lib.guyot"
version := "1.0"

// ======== Dependencies

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"

// Shapeless
resolvers ++= Resolver.sonatypeOssRepos("releases")
resolvers ++= Resolver.sonatypeOssRepos("snapshots")
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3"

// Scala Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14"


// Scala Reflect
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

// Scalaz
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.7"