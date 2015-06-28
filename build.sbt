name := """activator-blogimistic"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "com.typesafe.slick" %% "slick"          % "3.0.0"
libraryDependencies += "com.typesafe"       %  "config"         % "1.2.1"
libraryDependencies += "com.h2database"     %  "h2"             % "1.3.175"
libraryDependencies += "org.postgresql"     %  "postgresql"     % "9.4-1201-jdbc41"
libraryDependencies += "com.zaxxer"         %  "HikariCP"       % "2.3.8"
libraryDependencies += "io.spray"           %%  "spray-can"     % "1.3.3"
libraryDependencies += "io.spray"           %%  "spray-routing" % "1.3.3"
libraryDependencies += "io.spray"           %%  "spray-testkit" % "1.3.3"  % "test"
libraryDependencies += "io.spray"           %%  "spray-json"    % "1.3.1"
libraryDependencies += "io.spray"           %%  "spray-client"  % "1.3.3"
libraryDependencies += "com.typesafe.akka"  %%  "akka-actor"    % "2.3.9"
libraryDependencies += "com.typesafe.akka"  %%  "akka-testkit"  % "2.3.9"   % "test"
libraryDependencies += "ch.qos.logback"     % "logback-classic" % "1.1.3"
libraryDependencies += "joda-time"          %   "joda-time"     % "2.8"
// joda-convert is required to use joda-time in scala
libraryDependencies += "org.joda"           %   "joda-convert"  % "1.7"

fork in run := true