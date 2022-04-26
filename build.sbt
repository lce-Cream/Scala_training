ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

assembly / mainClass := Some("Main")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "test_4",
      libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.3" % "provided",
      libraryDependencies += "com.ibm.stocator" % "stocator" % "1.1.4" % "provided",
      libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.12.187" % "provided",
      libraryDependencies += "com.ibm.cos" % "ibm-cos-java-sdk" % "2.8.0" % "provided",
      libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.8.1" % "provided",

//    libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.2",
//    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
//    libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.27",
//    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
//    libraryDependencies += "org.yaml" % "snakeyaml" % "1.30",
//    // https://mvnrepository.com/artifact/com.lihaoyi/os-lib
//    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.8.1",
//    libraryDependencies += "com.lihaoyi" %% "upickle" % "0.9.5",
  )
