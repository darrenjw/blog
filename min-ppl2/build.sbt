name := "min-ppl2"

version := "0.1-SNAPSHOT"

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature"
)

libraryDependencies  ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.0-SNAP13" % "test",
  "org.scalactic" %% "scalactic" % "3.0.8" % "test",
  "org.typelevel" %% "cats-core" % "2.0.0-RC1",
  "org.scalanlp" %% "breeze" % "1.0-RC4",
  //"org.scalanlp" %% "breeze-viz" % "1.0-RC4",
  "org.scalanlp" %% "breeze-natives" % "1.0-RC4"
)

resolvers ++= Seq(
  "Sonatype Snapshots" at
    "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at
    "https://oss.sonatype.org/content/repositories/releases/"
)

enablePlugins(TutPlugin)

scalaVersion := "2.13.0"

