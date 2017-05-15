#!/bin/bash

MD Install Directory : $1
Test JARs Directory : $2
Test Class Name : $3

#run command from MD install dir
cd $1

MDKJARS=$(find $1/plugins/gov.nasa.jpl.cae.magicdraw.mdk -name "*.jar")
MDKJARS=${MDKJARS//[$';\ ']/}
MDKJARS=${MDKJARS//[$'\r\t\n ']/:}

MDKTESTJARS=$(find $2 -name "*.jar")
MDKTESTJARS=${MDKTESTJARS//[$';\ ']/}
MDKTESTJARS=${MDKTESTJARS//[$'\r\t\n ']/:}

#all additional needed JARs must be added individually to the -Dmd.additional.class.path property like the ones below
java -Xmx4096M -Xss1024M -DLOCALCONFIG=true -DWINCONFIG=true \
       -Djsse.enableSNIExtension=false \
       -Djava.net.preferIPv4Stack=true \
       -cp lib/com.nomagic.osgi.launcher-17.0.5-SNAPSHOT.jar:\
`         `lib/bundles/org.eclipse.osgi_3.10.1.v20140909-1633.jar:\
`         `lib/bundles/com.nomagic.magicdraw.osgi.fragment_1.0.0.201607250742.jar:\
`         `lib/md_api.jar:lib/md_common_api.jar:lib/md.jar:lib/md_common.jar:lib/jna.jar \
       -Dmd.class.path=file:bin/magicdraw.properties#CLASSPATH \
       -Dmd.additional.class.path=${MDKTESTJARS}:${MDKJARS} \
       -Dcom.sun.media.imageio.disableCodecLib=true \
       -noverify \
       -Dcom.nomagic.osgi.config.dir=configuration \
       -Desi.system.config=data/application.conf \
       -Dlogback.configurationFile=data/logback.xml \
       -Dsun.locale.formatasdefault=true \
       -Dcom.nomagic.magicdraw.launcher=com.nomagic.osgi.launcher.junit_support.JUnitCoreWrapper \
       com.nomagic.osgi.launcher.ProductionFrameworkLauncher gov.nasa.jpl.mbee.mdk.test.tests.$3
