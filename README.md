# Cameo Model Development Kit (MDK)

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/Open-MBEE/mdk?label=download)](https://github.com/Open-MBEE/mdk/releases/latest) [![Jira](https://img.shields.io/badge/issues-jira-blue)](https://openmbee.atlassian.net/browse/MDK) [![CircleCI](https://circleci.com/gh/Open-MBEE/mdk.svg?style=shield)](https://circleci.com/gh/Open-MBEE/mdk) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/Open-MBEE/mdk.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Open-MBEE/mdk/context:java)

Cameo MDK is a plugin for [Cameo Systems Modeler](https://www.nomagic.com/products/cameo-systems-modeler) and other No Magic environment bundles that’s primary purposes are to sync models with the [MMS](https://github.com/Open-MBEE/mms-alfresco) and implement the [DocGen](src/main/dist/manual) language, which allows modelers to dynamically generate documents in a model-based approach using the view and viewpoint concept.

# Quickstart

## Prerequisites

* [Cameo Systems Modeler (CSM)](https://www.nomagic.com/products/cameo-systems-modeler) or another No Magic environment bundle that includes the [SysML plugin](https://www.nomagic.com/product-addons/magicdraw-addons/sysml-plugin)
    * The latest Cameo MDK is tested with and supports **19.0 SP4**. Compatibility for previous versions of Cameo MDK can be found in the [compatibility matrices](https://github.com/Open-MBEE/open-mbee.github.io/wiki/Compatibilities).
#### Versions prior to 5.x:
* [Model Management System (MMS) 3](https://github.com/Open-MBEE/mms-alfresco)
#### Version 5.0+
* [Model Management System (MMS) 4](https://github.com/Open-MBEE/mms)

## Installation

1. [Download](https://github.com/Open-MBEE/mdk/releases/latest) the Cameo MDK plugin, e.g. `mdk-*-plugin.zip`.

    *Note: Make sure you are downloading the correct version for your installation of MMS (for mms-alfresco (mms3/donbot) use
    version 4.5.x, for mms (mms4/execubots) use versions 5.0+)*

2. Run CSM. From the main menu, select "Help" > "Resource/Plugin Manager".
   ![Resource/Plugin Manager](doc/images/resource-plugin-manager.png)
   
3. Click "Import", navigate to the downloaded Cameo MDK plugin, and click "Open".

4. CSM should confirm installation with the following prompt.

   ![MDK Installation Confirmation](doc/images/mdk-installation-confirmation.png)
   
5. Restart CSM. To check installation, select from the main menu "Help" > "Resource/Plugin Manager" > "Plugins (no cost)" and ensure a row exists for "Model Development Kit" and its status is "Installed" with the correct version.
  ![MDK Installed](doc/images/mdk-installed.png)
  
> Cameo MDK can also be installed headlessly by simply unzipping the plugin archive into the directory that CSM is installed in.
  
## Usage

Documentation can be accessed [online](src/main/dist/manual) or offline in CSM from the main menu "Help" > "Other Documentation" > "MDK [...] UserGuide".

# Contributing

To learn how you can get involved in a variety of ways, please see [Contibuting to OpenMBEE](https://www.openmbee.org/contribute).

# Local Development

Cameo MDK is a Java project that uses the [Gradle](https://gradle.org/) build tool. It can be imported as a Gradle project in IDEs like [IntelliJ](https://www.jetbrains.com/idea/) and [Eclipse](https://www.eclipse.org/ide/).

## Common Tasks
*Note starting in version 5.0 you will need to define a build profile (in `./buildProfiles`) which will house the classpath
for your particular version of cameo. In order to retrieve this classpath, copy paste the CLASSPATH entry from your 
`${md.install.dir}/bin/(magicraw/csm/cea).properties` file. Copy the entire line into a new file and find replace the `\:`
with `,` for more information refer to the existing `buildProfiles/opensource.properties`. Also, note that that profile should
work for most 19.0sp3 installations.*

In order to use your custom profile run any of the below commands with `-PbuildAccess=<yourProfileName>` otherwise it will
default to using `opensource`.

* `./gradlew dependencies` will download all necessary dependencies.
* `./gradlew assemble` will compile Cameo MDK from source.
* `./gradlew distZip` will package the installable plugin.
* `./gradlew installDist` will install the plugin in a staging environment.
* `./gradlew runJava` will interactively run CSM with Cameo MDK from the staging environment.

Task dependencies are declared such that all necessary prerequisites are executed/cached, e.g. `runJava` will first `dependencies`, `assemble`, `distZip`, etc.
