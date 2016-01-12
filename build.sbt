import scala.collection.mutable.ArrayBuffer

name := "neta-inbox"

version := "0.1.2.1"

val PROJECT_SCALA_VERSION = "2.11.7"

scalaVersion := PROJECT_SCALA_VERSION

lazy val librairies = Seq(
  "org.scala-lang" % "scala-library" % PROJECT_SCALA_VERSION,
  "org.scala-lang" % "scala-reflect" % PROJECT_SCALA_VERSION,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "junit" % "junit" % "4.12" % "test",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "foundation" % "5.3.0",
  cache,
  ws,
  "com.h2database" % "h2" % "1.4.177",
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "org.julienrf" %% "play-jsmessages" % "2.0.0",
  "net.cimadai" %% "chatwork-scala" % "1.0.1",
  "com.typesafe.play" %% "play-json" % "2.4.4",
  "com.flyberrycapital" %% "scala-slack" % "0.3.0-SNAPSHOT"
)

val preOrder = Iterable("plain/jquery-1.11.3.min.js", "plain/jquery-migrate-1.2.1.min.js", "plain/moment.min.js")
val postOrder = Iterable("application-all.js")
val outputFile = "application-all.min.js"

def doPartition(files: Seq[(File, String)], key: String): (Seq[(File, String)], Seq[(File, String)]) = {
  files.partition(_._2.endsWith(key))
}
def splitFileList(files: Seq[(File, String)], keys: Iterable[String]): (Seq[(File, String)], Seq[(File, String)]) = {
  val array = new ArrayBuffer[(File, String)]()
  var target = files
  keys.foreach(key => {
    val (matched, rest) = doPartition(target, key)
    target = rest
    array ++= matched
  })
  (array.toSeq, target)
}

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtWeb)
  .settings(
    scalaVersion := PROJECT_SCALA_VERSION,
    libraryDependencies ++= librairies,
    sassOptions in Assets ++= Seq("--compass", "-r", "compass"),
    TypescriptKeys.sourceRoot := "app/assets/js/",
    TypescriptKeys.outFile := "app/assets/js/application-all.js",
    includeFilter in TypescriptKeys.typescript := "application.ts",

    pipelineStages := Seq(uglify),
    UglifyKeys.buildDir := file("app/assets/js/build"),
    UglifyKeys.compressOptions := Seq("warnings=false"),
    UglifyKeys.uglifyOps := { js => {
      val target = js.filterNot(_._2.contains("js/build")).filterNot(_._2.endsWith(outputFile)).filter(_._1.isFile)
      val (pre, rest) = splitFileList(target, preOrder)
      val (post, middle) = splitFileList(rest, postOrder)
      Seq((pre ++ middle ++ post, "/../" + outputFile))
    }}
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "helpers"
  )

