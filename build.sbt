import java.io.File
import java.util.Calendar
import java.text.SimpleDateFormat
import sbt.Keys._
import sbt._

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")
enablePlugins(GitVersioning)

val cae_artifactory_releases =
  Resolver.url(
    "Artifactory Realm",
    url("https://cae-artifactory.jpl.nasa.gov/artifactory/ext-release-local")
  )(Resolver.mavenStylePatterns)

val cae_artifactory_plugin_releases = 
  Resolver.url(
    "Artifactory Realm",
    url("https://cae-artifactory.jpl.nasa.gov/artifactory/plugins-release-local")
  )(Resolver.mavenStylePatterns)
  
val cae_artifactory_plugin_snapshots = 
  Resolver.url(
    "Artifactory Realm",
    url("https://cae-artifactory.jpl.nasa.gov/artifactory/plugins-snapshot-local;build.timestamp=" + new java.util.Date().getTime())
  )(Resolver.mavenStylePatterns)
  
ivyLoggingLevel := UpdateLogging.Full

logLevel in Compile := Level.Debug

persistLogLevel := Level.Debug

val commonSettings: Seq[Setting[_]] = Seq(
  publishMavenStyle := true,
  publishTo := Some(cae_artifactory_plugin_releases),
  fullResolvers ++= Seq(new MavenRepository("cae ext-release-local", "https://cae-artifactory.jpl.nasa.gov/artifactory/ext-release-local"),
                        new MavenRepository("cae plugins-release-local", "https://cae-artifactory.jpl.nasa.gov/artifactory/plugins-release-local")
                    ),
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
  autoScalaLibrary := false,
  // disable using the Scala version in output paths and artifacts
  crossPaths := false,
  //disable publishing other artifacts as a workaround for weird snapshot behavior
  publishArtifact in (Compile, packageBin) := true,
  publishArtifact in (Compile, packageDoc) := false,
  publishArtifact in (Compile, packageSrc) := true,
  publishArtifact in Test := false
)

def moduleSettings(moduleID: ModuleID): Seq[Setting[_]] =
  Seq(
    name := moduleID.name,
    organization := moduleID.organization,
    version := moduleID.revision
  )

val artifactPluginZipFile = TaskKey[File]("Location of mdk plugin zip file")
lazy val extractArchives = TaskKey[File]("extract-archives", "Extracts base md zip")
lazy val getMdClasspath = TaskKey[Seq[Attributed[File]]]("get md jar classpath")
lazy val buildMdk = TaskKey[File]("build-mdk", "construct md plugin folder structure after compiling")
lazy val genResourceDescriptor = TaskKey[File]("gen-resource-descriptor", "generate resource descriptor")
lazy val zipMdk = TaskKey[File]("zip-mdk", "zip up mdk plugin")

// lib_patches package
val lib_patches_packageID = "gov.nasa.jpl.cae.magicdraw.packages" % "cae_md18_0_sp6_lib_patches" % "1.1"
val lib_patches_packageA = Artifact(lib_patches_packageID.name, "zip", "zip")
val lib_patches_package_zipID = lib_patches_packageID.artifacts(lib_patches_packageA)

val mdk_pluginID = "gov.nasa.jpl.cae.magicdraw.plugins" % "mdk" % "2.5.4"
val mdk_pluginA = Artifact(mdk_pluginID.name, "zip", "zip")
val mdk_plugin_zipID = mdk_pluginID.artifacts(mdk_pluginA)

lazy val plugin = (project in file("."))
  .settings(commonSettings)
  .settings(artifactPluginZipFile := { baseDirectory.value / "target" / "package" / "CAE.MDK.Plugin.zip" })
  .settings(addArtifact( mdk_pluginA, artifactPluginZipFile).settings: _*)
  .settings(moduleSettings(mdk_pluginID): _*)
  .settings(
    homepage := Some(url("https://github.jpl.nasa.gov/mbee-dev/mdk")),
    organizationHomepage := Some(url("http://cae.jpl.nasa.gov"))
  )
  .settings(
    unmanagedJars in Compile <++= getMdClasspath,
    libraryDependencies += lib_patches_package_zipID,
    publish <<= publish dependsOn zipMdk,

    extractArchives <<= (baseDirectory, update, streams) map { (base, up, s) =>
      val extractFolderString = sys.props.getOrElse("MD_BASE_DIR", "")
      var extractFolder = base / "target" / "expand"
      if (extractFolderString == null || extractFolderString == "") {
        val filter = artifactFilter(`type`="zip", extension = "zip")
        val zips: Seq[File] = up.matching(filter)
        s.log.info(s"*** Got: ${zips.size} zips")
        zips.foreach { zip =>
            s.log.info(s"\n\nzip: $zip")
            val files = IO.unzip(zip, extractFolder)
            s.log.info(s"=> extracted ${files.size} files!")
        }
      } else
        extractFolder = file(extractFolderString)
      extractFolder
    },
    
    getMdClasspath := {
        val mdbase = extractArchives.value
        val mdlibjars = (mdbase / "lib") ** "*.jar"
        val mdpluginjars = (mdbase / "plugins") ** "*.jar"
        val mdjars = mdlibjars +++ mdpluginjars
        mdjars.classpath
    },
    
    buildMdk := { 
      val githash = git.gitHeadCommit.value getOrElse ""
      val mdkjar = (packageBin in Compile).value
      val zipfolder = baseDirectory.value / "target" / "zip"
      IO.copyFile(baseDirectory.value / "profiles" / "MDK" / "SysML Extensions.mdxml", zipfolder / "profiles" / "MDK" / "SysML Extensions.mdxml", true)
      IO.copyDirectory(baseDirectory.value / "data" / "diagrams", zipfolder / "data" / "defaults" / "data" / "diagrams", true)
      IO.copyDirectory(baseDirectory.value / "DocGenUserScripts", zipfolder / "DocGenUserScripts", true)
      IO.copyFile(mdkjar, zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "DocGen-plugin.jar", true)
      IO.copyDirectory(baseDirectory.value / "lib", zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "lib", true)
      
      val pluginxml = IO.read(baseDirectory.value / "src" / "main" / "resources" / "plugin.xml")
      val towrite = pluginxml.replaceAllLiterally("@release.version.internal@", sys.props.getOrElse("BUILD_NUMBER", "1")).replaceAllLiterally("@release.version.human@", "2.5.4-" + githash)
      IO.write(zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "plugin.xml", towrite, append=false)
      //IO.copyFile(baseDirectory.value / "src" / "main" / "resources" / "plugin.xml", zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "plugin.xml", true)
      //get env var BUILD_NUMBER, GIT_COMMIT, JOB_NAME, BUILD_ID (date)
      zipfolder
    },
    
    genResourceDescriptor := {
        val zipfolder = buildMdk.value
        val template = IO.read(baseDirectory.value / "data" / "resourcemanager" / "MDR_Plugin_Docgen_91110_descriptor_template.xml")
        val subpaths = Path.selectSubpaths(zipfolder, "*.*")
        val content = ("" /: subpaths) { 
            case (result, (file, subpath)) =>
                result + "<file from=\"" + subpath + "\" to=\"" + subpath + "\"/>\n"
        }
        //streams.value.log.info(content)
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
        val currentDate = dateFormat.format(Calendar.getInstance().getTime())
        val towrite = template.replaceAllLiterally("@installation@", content)
                              .replaceAllLiterally("@release.version.internal@", sys.props.getOrElse("BUILD_NUMBER", "1"))
                              .replaceAllLiterally("@release.date@", currentDate)
                              .replaceAllLiterally("@release.version.human@", "2.5.4")
        IO.write(zipfolder / "data" / "resourcemanager" / "MDR_Plugin_Docgen_91110_descriptor.xml", towrite, append=false)
        zipfolder
    },
    
    zipMdk := {
        val zipfolder = genResourceDescriptor.value;
        IO.zip(allSubpaths(zipfolder), artifactPluginZipFile.value)
        artifactPluginZipFile.value
    }
  )