name := "play2-template"

version := "0.1.0.0"

val PROJECT_SCALA_VERSION = "2.11.7"

scalaVersion := PROJECT_SCALA_VERSION

lazy val librairies = Seq(
  "org.scala-lang" % "scala-library" % PROJECT_SCALA_VERSION,
  "org.scala-lang" % "scala-reflect" % PROJECT_SCALA_VERSION,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "com.google.guava" % "guava" % "16.0.1",
  "junit" % "junit" % "4.12" % "test"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    scalaVersion := PROJECT_SCALA_VERSION,
    libraryDependencies ++= librairies
  )

