name := "area51-akka"
organization := "org.dmonix.akka"
version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

val akkaVersion = "2.4.0"

scalacOptions <++= scalaVersion map { (v: String) => 
  if (v.trim.startsWith("2.1"))
    Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions", "-language:higherKinds", "-target:jvm-1.7")
  else
    Seq("-deprecation", "-unchecked")
}

scalacOptions in (Compile, doc) ++= Seq("-doc-title", "Akka Persistence Mock API")
scalacOptions in (Compile, doc) ++= Seq("-doc-root-content", baseDirectory.value+"/src/main/scaladoc/root-doc.txt")
scalacOptions in (Compile, doc) ++= Seq("-doc-footer", "Copyright (c) 2015 Peter Nerg, Apache License v2.0.")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test", 
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-persistence-tck" % akkaVersion % "test"
)
