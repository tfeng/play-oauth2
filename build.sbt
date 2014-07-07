name := "play-oauth2"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  "org.springframework" % "spring-aop" % "4.0.3.RELEASE",
  "org.springframework" % "spring-expression" % "4.0.3.RELEASE",
  "org.springframework" % "spring-test" % "4.0.3.RELEASE",
  "org.springframework.security" % "spring-security-config" % "3.2.3.RELEASE",
  "org.springframework.security" % "spring-security-core" % "3.2.3.RELEASE",
  "org.springframework.security.oauth" % "spring-security-oauth2" % "2.0.2.RELEASE"
)

EclipseKeys.withSource := true
