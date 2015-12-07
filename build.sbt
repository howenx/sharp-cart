import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayJava
name := """style-shopping"""
version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters
)

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4" withSources() withJavadoc()

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1205-jdbc42" withSources() withJavadoc()

libraryDependencies += "com.h2database" % "h2" % "1.4.188" withSources() withJavadoc()

libraryDependencies += "org.mybatis" % "mybatis" % "3.3.0" withSources() withJavadoc()

libraryDependencies += "org.mybatis" % "mybatis-guice" % "3.6" withSources() withJavadoc()

libraryDependencies += "com.google.inject.extensions" % "guice-multibindings" % "4.0" withSources() withJavadoc()

libraryDependencies += "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0" withSources() withJavadoc()

libraryDependencies += "com.aliyun.oss" % "aliyun-sdk-oss" % "2.0.1" withSources() withJavadoc()

libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.0" withSources() withJavadoc()

resolvers ++= Seq(
  "Apache" at "https://repo1.maven.org/maven2/"
)

javacOptions += "-Xlint:deprecation"

routesGenerator := InjectedRoutesGenerator
