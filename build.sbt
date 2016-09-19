import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayJava

name := """style-shopping"""
version := "0.5.6"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

evictionWarningOptions in evicted := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)


libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  filters
)


libraryDependencies += "commons-io" % "commons-io" % "2.4"
libraryDependencies += "com.squareup.okhttp" % "okhttp" % "2.7.2"
libraryDependencies += "commons-beanutils" % "commons-beanutils" % "1.9.2"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4" withSources() withJavadoc()

libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1205-jdbc42" withSources() withJavadoc()

libraryDependencies += "com.h2database" % "h2" % "1.4.191"

libraryDependencies += "org.mybatis" % "mybatis" % "3.3.0" withSources() withJavadoc()

libraryDependencies += "org.mybatis" % "mybatis-guice" % "3.6" withSources() withJavadoc()

libraryDependencies += "com.google.inject.extensions" % "guice-multibindings" % "4.0" withSources() withJavadoc()

libraryDependencies += "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0" withSources() withJavadoc()

libraryDependencies += "com.aliyun.oss" % "aliyun-sdk-oss" % "2.0.1" withSources() withJavadoc()

libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.0" withSources() withJavadoc()

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.1" withSources() withJavadoc()

libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.11" % "2.4.1" withSources() withJavadoc()

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.1" withSources() withJavadoc()

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.1" withSources() withJavadoc()

libraryDependencies += "com.aliyun.mns" % "aliyun-sdk-mns" % "1.1.3"

libraryDependencies += "ch.qos.logback" % "logback-access" % "1.1.3"

libraryDependencies += "net.glxn.qrgen" % "javase" % "2.0" withSources() withJavadoc()
libraryDependencies += "redis.clients" % "jedis" % "2.8.1"

libraryDependencies += "com.typesafe.play" %% "play-mailer" % "4.0.0" withSources() withJavadoc()


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % "2.4.1",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
)

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "Sonatype OSS Snasphots" at "http://oss.sonatype.org/content/repositories/snapshots"
)

javacOptions += "-Xlint:deprecation"

routesGenerator := InjectedRoutesGenerator
