#!/bin/bash

# sage: $MAGICDRAW_HOME/bin/cli/automatedviewgenerator.sh [-h] [-debug]
#       [-mmsHost <arg>] [-mmsPort <arg>] [-mmsUsername <arg>] [-mmsPassword <arg>]
#       [-twcHost <arg>] [-twcPort <arg>] [-twcUsername <arg>] [-twcPassword <arg>]
#       [-pmaHost <arg>] [-pmaPort <arg>] [-pmaJobId <arg>]
#       [-projectId <arg>] [-refId <arg>] [-targetViewId <arg>] [-generateRecursively]

# This script manages the settings and configuration options
# necessary to launch a MagicDraw CommandLine program in the OSGI framework.
# This tool must be launched with the associated shell script or a similar
# manual configuration; it will not run directly.

if [ -z "$MAGICDRAW_HOME" ]; then
    echo "ERROR: MAGICDRAW_HOME environment variable not set, script cannot proceed."
    echo "Please create the environment variable and set it to the MagicDraw installation folder."
    exit 1
fi

CP_DELIM=":"
MD_HOME_URL_LEAD=$(echo "$MAGICDRAW_HOME" | sed -e 's/ /%20/g')
MD_HOME_URL_BASE=$(echo "$MAGICDRAW_HOME" | sed -e 's/ /%20/g')

MD_CP_URL=file:$MD_HOME_URL_LEAD/bin/magicdraw.properties?base=$MD_HOME_URL_BASE#CLASSPATH

OSGI_LAUNCHER=$(echo "$MAGICDRAW_HOME"/lib/com.nomagic.osgi.launcher-*.jar)
OSGI_FRAMEWORK=$(echo "$MAGICDRAW_HOME"/lib/bundles/org.eclipse.osgi_*.jar)
MD_OSGI_FRAGMENT=$(echo "$MAGICDRAW_HOME"/lib/bundles/com.nomagic.magicdraw.osgi.fragment_*.jar)

CP="${OSGI_LAUNCHER}${CP_DELIM}${OSGI_FRAMEWORK}${CP_DELIM}${MD_OSGI_FRAGMENT}${CP_DELIM}\
`  `$MAGICDRAW_HOME/lib/md_api.jar${CP_DELIM}$MAGICDRAW_HOME/lib/md_common_api.jar${CP_DELIM}\
`  `$MAGICDRAW_HOME/lib/md.jar${CP_DELIM}$MAGICDRAW_HOME/lib/md_common.jar${CP_DELIM}\
`  `$MAGICDRAW_HOME/lib/jna.jar"

java -Xmx8192M -Xss1024M -DLOCALCONFIG=true -DWINCONFIG=true \
       -cp "$CP" \
       -Dmd.class.path=$MD_CP_URL \
       -Dcom.nomagic.osgi.config.dir="$MAGICDRAW_HOME/configuration" \
       -Desi.system.config="$MAGICDRAW_HOME/data/application.conf" \
       -Dlogback.configurationFile="$MAGICDRAW_HOME/data/logback.xml" \
       -noverify \
       -Djsse.enableSNIExtension=false \
       -Djava.net.preferIPv4Stack=true \
       -Dcom.sun.media.imageio.disableCodecLib=true \
       -Dsun.locale.formatasdefault=true \
       -Dcom.nomagic.magicdraw.launcher=com.nomagic.magicdraw.commandline.CommandLineActionLauncher \
       -Dcom.nomagic.magicdraw.commandline.action=gov.nasa.jpl.mbee.pma.cli.AutomatedViewGenerator \
       com.nomagic.osgi.launcher.ProductionFrameworkLauncher -verbose "$@"

exit $?
