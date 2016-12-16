#!/bin/bash

#echo Test JARs Directory : $1
#echo MD Install Directory : $2
#echo Test Class Name : $3

#run command from MD install dir
cd $2

export MDKTESTJARS=$(find $1 -name "*.jar" | tr -s '\r\n' ':')
export MDKTESTJARS=${MDKTESTJARS/%?/}
export MDKJARS=$(find $2/plugins/gov.nasa.jpl.mbee.mdk -name "*.jar" | tr -s '\r\n' ':')
export MDKJARS=${MDKJARS/%?/}

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
