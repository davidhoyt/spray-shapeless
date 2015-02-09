name := "spray-shapeless"

version := "1.0.0"

scalaVersion := "2.11.5"

scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:jvm-1.6",
  "-language:_",
  "-Xlog-reflective-calls"
)

val akkaVersion = "2.3.9"
val json4sVersion = "3.2.11"
val sprayVersion = "1.3.2"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-routing-shapeless2" % sprayVersion
    exclude("com.chuusai", "shapeless"),
  "com.chuusai" %% "shapeless" % "2.1.0-RC2",
  //
  "com.typesafe.play" %% "play-json" % "2.3.7" % "provided",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion % "provided",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % "provided",
  //
  "io.spray" %%  "spray-json" % "1.3.1" % "test",
  "org.json4s" %% "json4s-jackson" % json4sVersion % "test",
  "org.json4s" %% "json4s-native" % json4sVersion % "test",
  "org.json4s" %% "json4s-ext" % json4sVersion % "test",
  //
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

