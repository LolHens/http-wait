name := "http-wait"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.3"

val http4sVersion = "0.21.6"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.monix" %% "monix" % "3.2.2",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-jdk-http-client" % "0.3.0",
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

Compile / doc / sources := Seq.empty

version := {
  val tagPrefix = "refs/tags/"
  sys.env.get("CI_VERSION").filter(_.startsWith(tagPrefix)).map(_.drop(tagPrefix.length)).getOrElse(version.value)
}

assembly / assemblyJarName := s"${name.value}-${version.value}.sh.bat"

assembly / assemblyOption := (assembly / assemblyOption).value
  .copy(prependShellScript = Some(AssemblyPlugin.defaultUniversalScript(shebang = false)))

assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") =>
    MergeStrategy.discard

  case PathList("META-INF", "jpms.args") =>
    MergeStrategy.discard

  case PathList("META-INF", "io.netty.versions.properties") =>
    MergeStrategy.first

  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

enablePlugins(
  GraalVMNativeImagePlugin
)

GraalVMNativeImage / name := (GraalVMNativeImage / name).value + "-" + (GraalVMNativeImage / version).value
graalVMNativeImageOptions ++= Seq(
  "--no-server",
  "--no-fallback",
  "--initialize-at-build-time",
  "--install-exit-handlers",
  "--enable-url-protocols=http,https",
  "--allow-incomplete-classpath" /*logback-classic*/
)
