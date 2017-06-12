#!/bin/bash

#usage: $MAGICDRAW_HOME/bin/cli/automatedviewgenerator.sh [-debug]
#       [-generateRecursively] [-h] [-mmsHost <arg>] [-mmsPassword <arg>]
#       [-mmsPort <arg>] [-mmsUsername <arg>] [-pmaHost <arg>]
#       [-pmaJobId <arg>] [-pmaPort <arg>] [-projectId <arg>]
#       [-refId <arg>] [-targetViewId <arg>]
#       [-twcHost <arg>] [-twcPassword <arg>] [-twcPort <arg>]
#       [-twcUsername <arg>] [-verbose]

#This script manages the settings and configuration options
#necessary to launch a MagicDraw CommandLine program in the OSGI framework.
#This tool must be launched with the associated shell script or a similar
#manual configuration; it will not run directly.

if [ -z "$MAGICDRAW_HOME" ]; then
    echo "MAGICDRAW_HOME environment variable not set, please set it to the MagicDraw installation folder"
    exit 1
fi

cp_delim=":"
md_home_url_leader=$(echo "$MAGICDRAW_HOME" | sed -e 's/ /%20/g')
md_home_url_base=$(echo "$MAGICDRAW_HOME" | sed -e 's/ /%20/g')
md_cp_url=file:$md_home_url_leader/bin/magicdraw.properties?base=$md_home_url_base#CLASSPATH

OSGI_LAUNCHER=$(echo "$MAGICDRAW_HOME"/lib/com.nomagic.osgi.launcher-*.jar)
OSGI_FRAMEWORK=$(echo "$MAGICDRAW_HOME"/lib/bundles/org.eclipse.osgi_*.jar)
MD_OSGI_FRAGMENT=$(echo "$MAGICDRAW_HOME"/lib/bundles/com.nomagic.magicdraw.osgi.fragment_*.jar)

CP="${OSGI_LAUNCHER}${cp_delim}${OSGI_FRAMEWORK}${cp_delim}${MD_OSGI_FRAGMENT}${cp_delim}\
`  `$MAGICDRAW_HOME/lib/md_api.jar${cp_delim}$MAGICDRAW_HOME/lib/md_common_api.jar${cp_delim}\
`  `$MAGICDRAW_HOME/lib/md.jar${cp_delim}$MAGICDRAW_HOME/lib/md_common.jar${cp_delim}\
`  `$MAGICDRAW_HOME/lib/jna.jar"

MDKJARS=$(find $MAGICDRAW_HOME/plugins/gov.nasa.jpl.cae.magicdraw.mdk -name "*.jar")
MDKJARS=${MDKJARS//[$';\ ']/}
MDKJARS=${MDKJARS//[$'\r\t\n ']/${cp_delim}}

#all additional needed JARs must be added individually to the -Dmd.additional.class.path property like the ones below
java -Xmx4096M -Xss1024M -DLOCALCONFIG=true -DWINCONFIG=true \
       -Djsse.enableSNIExtension=false \
       -Djava.net.preferIPv4Stack=true \
       -cp "$CP" \
       -Dmd.class.path=$md_cp_url \
       -Dmd.additional.class.path="$MDKJARS${cp_delim}$MAGICDRAW_HOME/collaboration/lib/commons-cli-1.2.jar" \
       -Dcom.sun.media.imageio.disableCodecLib=true \
       -noverify \
       -Dcom.nomagic.osgi.config.dir="$MAGICDRAW_HOME/configuration" \
       -Desi.system.config="$MAGICDRAW_HOME/data/application.conf" \
       -Dlogback.configurationFile="$MAGICDRAW_HOME/data/logback.xml" \
       -Dsun.locale.formatasdefault=true \
       -Dcom.nomagic.magicdraw.launcher=gov.nasa.jpl.mbee.pma.cli.AutomatedViewGenerator \
       com.nomagic.osgi.launcher.ProductionFrameworkLauncher "$@"

exit $?
