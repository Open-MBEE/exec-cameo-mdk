import java.io.File

import sbt.Keys._
import sbt._

val cae_artifactory_releases =
  Resolver.url(
    "Artifactory Realm",
    url("https://cae-artrepo.jpl.nasa.gov/artifactory/ext-release-local")
  )(Resolver.mavenStylePatterns)

val cae_artifactory_snapshots = 
  Resolver.url(
    "Artifactory Realm",
    url("https://cae-artrepo.jpl.nasa.gov/artifactory/libs-snapshot-local")
  )(Resolver.mavenStylePatterns)
  
ivyLoggingLevel := UpdateLogging.Full

logLevel in Compile := Level.Debug

persistLogLevel := Level.Debug

val noSourcesSettings: Seq[Setting[_]] = Seq(

  // Map artifact ModuleID to a Maven-style path for publishing/lookup on the repo
  publishMavenStyle := true,

  // where to publish artifacts
  publishTo := Some(cae_artifactory_releases),

  // where to look for resolving library dependencies
  fullResolvers += new MavenRepository("cae ext-release-local", "https://cae-artrepo.jpl.nasa.gov/artifactory/ext-release-local"),

  // disable automatic dependency on the Scala library
  autoScalaLibrary := false,

  // disable using the Scala version in output paths and artifacts
  crossPaths := false,

  // disable publishing the main jar produced by `package`
  publishArtifact in (Compile, packageBin) := false,

  // disable publishing the main API jar
  publishArtifact in (Compile, packageDoc) := false,

  // disable publishing the main sources jar
  publishArtifact in (Compile, packageSrc) := false

)


def moduleSettings(moduleID: ModuleID): Seq[Setting[_]] =
  Seq(
    name := moduleID.name,
    organization := moduleID.organization,
    version := moduleID.revision
  )

val artifactPackageZipFile = TaskKey[File]("Location of the full md zip artifact file")
val artifactPluginZipFile = TaskKey[File]("Location of mdk plugin zip file")

lazy val extractArchives = TaskKey[File]("extract-archives", "Extracts base md zip")
lazy val getMdClasspath = TaskKey[Seq[Attributed[File]]]("get md jar classpath")
lazy val buildMdk = TaskKey[File]("build-mdk", "construct md plugin folder structure after compiling")
lazy val genResourceDescriptor = TaskKey[File]("gen-resource-descriptor", "generate resource descriptor")
lazy val zipMdk = TaskKey[File]("zip-mdk", "zip up mdk plugin")
lazy val zipMdkFull = TaskKey[File]("zip-mdk-full", "make prepackaged md with mdk") 

// lib_patches package
val lib_patches_packageID = "gov.nasa.jpl.cae.magicdraw.packages" % "cae_md18_0_sp4_lib_patches" % "1.0"
val lib_patches_packageA = Artifact(lib_patches_packageID.name, "zip", "zip")
val lib_patches_package_zipID = lib_patches_packageID.artifacts(lib_patches_packageA)

val mdk_packageID = "gov.nasa.jpl.cae.magicdraw.packages" % "cae_md18_0_sp4_mdk" % "1.0"
val mdk_packageA = Artifact(mdk_packageID.name, "zip", "zip")

val mdk_pluginID = "gov.nasa.jpl.cae.magicdraw.plugins" % "mdk" % "1.0"
val mdk_pluginA = Artifact(mdk_pluginID.name, "zip", "zip")

unmanagedJars in Compile <++= getMdClasspath

lazy val core = Project("cae_magicdraw_packages_mdk", file("."))
  .settings(noSourcesSettings)
  .settings(artifactPackageZipFile := { baseDirectory.value / "package" / "CAE.MDK.Package.zip" })
  .settings(artifactPluginZipFile := { baseDirectory.value / "package" / "CAE.MDK.Plugin.zip" })
  .settings(addArtifact( mdk_packageA, artifactPackageZipFile ).settings: _*) //TODO can a project publish 2 artifacts?
  .settings(addArtifact( mdk_pluginA, artifactPluginZipFile).settings: _*)
  .settings(moduleSettings(mdk_packageID): _*)
  .settings(moduleSettings(mdk_pluginID): _*) //TODO this is probably wrong
  .settings(
    homepage := Some(url("https://github.jpl.nasa.gov/mbee-dev/mdk")),
    organizationHomepage := Some(url("http://cae.jpl.nasa.gov"))
  )
  .settings(
    resourceDirectory := baseDirectory.value / "package",
    libraryDependencies += lib_patches_package_zipID,

    extractArchives <<= (baseDirectory, update, streams) map { (base, up, s) =>
      val extractFolder = base / "target" / "expand"
      val filter = artifactFilter(`type`="zip", extension = "zip")
      val zips: Seq[File] = up.matching(filter)
      s.log.info(s"*** Got: ${zips.size} zips")
      zips.foreach { zip =>
        s.log.info(s"\n\nzip: $zip")
        val files = IO.unzip(zip, extractFolder)
        s.log.info(s"=> extracted ${files.size} files!")
      }
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
      val mdkjar = (packageBin in Compile).value
      val zipfolder = baseDirectory.value / "target" / "zip"
      IO.copyFile(baseDirectory.value / "profiles" / "MDK" / "SysML Extensions.mdxml", zipfolder / "profiles" / "MDK" / "SysML Extensions.mdxml", true)
      IO.copyDirectory(baseDirectory.value / "data" / "diagrams", zipfolder / "data" / "defaults" / "data" / "diagrams", true)
      IO.copyDirectory(baseDirectory.value / "DocGenUserScripts", zipfolder / "DocGenUserScripts", true)
      IO.copyFile(mdkjar, zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "DocGen-plugin.jar", true)
      IO.copyDirectory(baseDirectory.value / "lib", zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "lib", true)
      IO.copyFile(baseDirectory.value / "src" / "main" / "resources" / "plugin.xml", zipfolder / "plugins" / "gov.nasa.jpl.mbee.docgen" / "plugin.xml", true)
      //TODO put in release info in plugin.xml?
      zipfolder
    },
    
    genResourceDescriptor := {
        val zipfolder = buildMdk.value
        val template = IO.read(baseDirectory.value / "data" / "resourcemanager" / "MDR_Plugin_Docgen_91110_descriptor_template.xml")
        //val filesInZip = zipfolder ** "*.*"
        val subpaths = Path.selectSubpaths(zipfolder, "*.*")
        val content = ("" /: subpaths) { 
            case (result, (file, subpath)) =>
                result + "<file from=\"" + subpath + "\" to=\"" + subpath + "\"/>\n"
        }
        streams.value.log.info(content)
        //TODO need release version info in descriptor
        val towrite = template.replaceAllLiterally("@installation@", content)
        IO.write(zipfolder / "data" / "resourcemanager" / "MDR_Plugin_Docgen_91110_descriptor.xml", towrite, append=false)
        zipfolder
    },
    
    zipMdk := {
        val zipfolder = genResourceDescriptor.value;
        IO.zip(allSubpaths(zipfolder), artifactPluginZipFile.value)
        artifactPluginZipFile.value
    },
    
    zipMdkFull := {
        val zipfile = zipMdk.value
        val extractFolder = extractArchives.value
        IO.unzip(zipfile, extractFolder)
        IO.zip(allSubpaths(extractFolder), artifactPackageZipFile.value)
        artifactPackageZipFile.value
    }
  )