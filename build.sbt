ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
      name := "test_4",
        libraryDependencies += "org.apache.spark"             %% "spark-sql"             % "3.1.3"   ,
        libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.13.2"  ,
        libraryDependencies += "org.scalatest"                %% "scalatest"             % "3.2.12"    % "test",
        libraryDependencies += "com.github.mrpowers"          %% "spark-fast-tests"      % "0.23.0"    % "test",

        libraryDependencies += "com.ibm.db2"                  %  "jcc"                   % "11.5.7.0",
        libraryDependencies += "com.ibm.stocator"             %  "stocator"              % "1.1.4"   ,
        libraryDependencies += "com.amazonaws"                %  "aws-java-sdk-s3"       % "1.12.187",

  )
