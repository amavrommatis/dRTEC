import com.typesafe.sbt.SbtNativePackager.packageArchetype

/** Project */
name := "dRTEC"

version := "0.1"

organization := "gr.demokritos.iit"

scalaVersion := "2.11.7"

autoScalaLibrary := true

managedScalaInstance := true

packageArchetype.java_application

// Append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.7", "-target", "1.7",
  "-Xlint:unchecked", "-Xlint:deprecation")

// add JVM options to use when forking a JVM for 'run'
javaOptions ++= Seq(
  "-XX:+DoEscapeAnalysis",
  "-XX:+UseFastAccessorMethods",
  "-XX:+OptimizeStringConcat")

// Append scalac options
scalacOptions ++= Seq(
  "-Yclosure-elim",
  "-deprecation",
  "-Yinline-warnings",
  "-Yinline",
  "-feature",
  "-target:jvm-1.7",
  "-language:implicitConversions",
  "-optimize" //use the new optimisation level
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// fork a new JVM for 'test:run', but not 'run'
fork in Test := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx4G"

//snapshot version
isSnapshot := true

/** Dependencies */
resolvers ++= Seq(
  "MQTT Repository" at "https://repo.eclipse.org/content/repositories/paho-releases/")

// Scala-lang
libraryDependencies += "org.scala-lang" % "scala-library" % scalaVersion.value

// Apache Spark
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.2" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.5.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "1.5.2" % "provided"
)


// HEALPix library
libraryDependencies += "healpix" % "healpix" % "3.20" from "file://"+baseDirectory.value.absolutePath+"/lib/healpix.jar"

// Adding auxlib library requires local publishing (for details see https://github.com/anskarl/auxlib)
libraryDependencies += "com.github.anskarl" %% "auxlib" % "0.1"

// MQTT dependency
libraryDependencies += "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"