name := "jsbml"

version := "0.1"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
		"org.sbml.jsbml" % "jsbml" % "1.2",
		"org.apache.logging.log4j" % "log4j-1.2-api" % "2.3",
		"org.apache.logging.log4j" % "log4j-api" % "2.3",
		"org.apache.logging.log4j" % "log4j-core" % "2.3"
			)

scalaVersion := "2.11.7"


