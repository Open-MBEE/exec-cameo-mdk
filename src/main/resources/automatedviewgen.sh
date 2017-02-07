#!/bin/bash

#echo TeamworkProject $1
#echo DocumentList $2
#echo CredentialsLocation $3
#echo RunRoot $4

#run command from MD install dir
MDKJARS=$(find plugins/gov.nasa.jpl.cae.magicdraw.mdk -name "*.jar" | tr -s '\r\n' ':')
MDKJARS=${MDKJARS/%?/}

#cd $4

#all additional needed JARs must be added individually to the -Dmd.additional.class.path property like the ones below
java -Xmx4096M -Xss1024M -DLOCALCONFIG=true -DWINCONFIG=true \
       -Djsse.enableSNIExtension=false \
       -Djava.net.preferIPv4Stack=true \
       -cp lib/com.nomagic.osgi.launcher-17.0.5-SNAPSHOT.jar:\
`         `lib/bundles/org.eclipse.osgi_3.10.1.v20140909-1633.jar:\
`         `lib/bundles/com.nomagic.magicdraw.osgi.fragment_1.0.0.201701310902.jar:\
`         `lib/md_common_api.jar:lib/md_common.jar:lib/md_api.jar:lib/md.jar:lib/jna.jar \
       -Dmd.class.path=file:bin/magicdraw.properties#CLASSPATH \
       -Dmd.additional.class.path=${MDKJARS} \
       -Dcom.sun.media.imageio.disableCodecLib=true \
       -noverify \
       -Dcom.nomagic.osgi.config.dir=configuration \
       -Desi.system.config=data/application.conf \
       -Dlogback.configurationFile=data/logback.xml \
       -Dsun.locale.formatasdefault=true \
       -Dcom.nomagic.magicdraw.launcher=gov.nasa.jpl.mbee.pma.analyses.AutomatedViewGeneration \
       com.nomagic.osgi.launcher.ProductionFrameworkLauncher -Debug -TeamworkProject $1 -DocumentList $2 -CredentialsLocation $3
#       com.nomagic.osgi.launcher.ProductionFrameworkLauncher -RunRoot $4 -TeamworkProject $1 -DocumentList $2 -CredentialsLocation $3

#NOTE a silent fail that returns error code 2 without ever launching the execute block of the task is a licensing issue (can be verified in magicdraw.log)
ERROR=$?
echo
echo Error code: $ERROR
exit $ERROR
