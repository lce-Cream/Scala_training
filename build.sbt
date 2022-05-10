ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
      name := "test_4",
        libraryDependencies += "org.apache.spark"             %% "spark-sql"             % "3.1.3"  ,

        libraryDependencies += "com.ibm.stocator"             %  "stocator"              % "1.1.4"   ,
        libraryDependencies += "com.ibm.db2"                  %  "jcc"                   % "11.5.7.0",

        libraryDependencies += "com.amazonaws"                %  "aws-java-sdk"          % "1.12.187",
        libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.13.2"  ,

        libraryDependencies += "org.scalatest"                %% "scalatest"             % "3.2.12"    % "test",

      //   libraryDependencies += "mysql"                        %  "mysql-connector-java" % "8.0.27",
      //   libraryDependencies += "io.spray"                     %% "spray-json"           % "1.3.6",
      //   libraryDependencies += "com.lihaoyi"                  %% "upickle"              % "1.6.0",
      //   libraryDependencies += "com.lihaoyi"                  %% "os-lib"               % "0.8.1",
      //    libraryDependencies += "org.apache.spark"            %% "spark-core"           % "3.1.3",
      //    libraryDependencies += "com.ibm.cos"                 % "ibm-cos-java-sdk"      % "2.8.0",
      //    libraryDependencies += "org.yaml"                    % "snakeyaml"             % "1.30",
      //    libraryDependencies += "com.lihaoyi"                 %% "os-lib"               % "0.8.1",
  )

//assembly / mainClass := Some("Main")
//assembly / assemblyMergeStrategy := {
//    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//    case x => MergeStrategy.first
//}
//retrieveManaged := true
