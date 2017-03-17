organization in ThisBuild := "org.thrx.challenger"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val cOneLagomJavaProject = (project in file("."))
  .aggregate(cOneLagomJavaUserApi
  			, cOneLagomJavaUserImpl
  			)
  .settings(eclipseSettingsParent: _*)

lazy val cOneLagomJavaUserApi = (project in file("c-one-user-api"))
  .settings(common: _*)
  .settings(eclipseSettingsJava: _*)
  .settings(eclipseSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  ).dependsOn(cOneLagomJavaSecurity)
  

lazy val cOneLagomJavaUserImpl = (project in file("c-one-user-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(eclipseSettingsJava: _*)
  .settings(eclipseSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0",
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(cOneLagomJavaUserApi)
  .dependsOn(cOneLagomJavaCommon)

lazy val cOneLagomJavaSecurity = (project in file("c-one-security"))
  .settings(common: _*)
  .settings(eclipseSettingsJava: _*)
  .settings(eclipseSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslServer % Optional
    )
  )

lazy val cOneLagomJavaCommon = (project in file("c-one-common"))
  .settings(common: _*)
  .settings(eclipseSettingsJava: _*)
  .settings(eclipseSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

val lombok = "org.projectlombok" % "lombok" % "1.16.10"


def common = Seq(
//  javacOptions in compile += "-parameters"
  javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters")
)


//Configuration of sbteclipse

EclipseKeys.useProjectId := true

//Needed for importing the project into Eclipse
EclipseKeys.skipParents in ThisBuild := false

// Eclipse for parent project
lazy val eclipseSettingsParent = Seq(
  // avoid source directories
  unmanagedSourceDirectories in Compile := Seq(),
  unmanagedSourceDirectories in Test := Seq(),
  // avoid resource directories
  unmanagedResourceDirectories in Compile := Seq(),
  unmanagedResourceDirectories in Test := Seq()
)

// Eclipse for java projects
lazy val eclipseSettingsJava = Seq(
  EclipseKeys.projectFlavor := EclipseProjectFlavor.Java,
  EclipseKeys.withBundledScalaContainers := false,
  // will automatically download and attach javadoc if available
  EclipseKeys.withJavadoc := true,
  // avoid some scala specific source directories
  unmanagedSourceDirectories in Compile := Seq((javaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((javaSource in Test).value)
)

// Eclipse general settings
lazy val eclipseSettings = Seq(
  EclipseKeys.eclipseOutput := Some(".target"),
  // will automatically download and attach sources if available
  EclipseKeys.withSource := true
)

//lazy val eclipseSettingsPlayJava = Seq(
//	// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
//	EclipseKeys.preTasks := Seq(compile in (server, Compile))
//)

