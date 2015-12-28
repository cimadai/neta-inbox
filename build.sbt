name := "neta-inbox"

version := "0.1.0.0"

val PROJECT_SCALA_VERSION = "2.11.7"

scalaVersion := PROJECT_SCALA_VERSION

lazy val librairies = Seq(
  "org.scala-lang" % "scala-library" % PROJECT_SCALA_VERSION,
  "org.scala-lang" % "scala-reflect" % PROJECT_SCALA_VERSION,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "com.google.guava" % "guava" % "16.0.1",
  "junit" % "junit" % "4.12" % "test",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "foundation" % "5.3.0",
  cache,
  ws,
  "com.h2database" % "h2" % "1.4.177",
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "org.julienrf" %% "play-jsmessages" % "2.0.0"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtWeb)
  .settings(
    scalaVersion := PROJECT_SCALA_VERSION,
    libraryDependencies ++= librairies,
    sassOptions in Assets ++= Seq("--compass", "-r", "compass"),
    TypescriptKeys.sourceRoot := "app/assets/js/",
    TypescriptKeys.outFile := "app/assets/js/application-all.js",
    includeFilter in TypescriptKeys.typescript := "app/assets/js/application.ts"
  )
