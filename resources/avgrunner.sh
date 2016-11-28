#!/bin/bash

#echo Credentials Location: $1
#echo Document List: $2
#echo Teamwork Project: $3
#echo Teamwork Branch: $4

#example command
#bash avgrunner.sh "../../resources/mms.properties" "_18_0_2_bec02f9_1444944948911_779834_74342" "ID_10_20_16_12_01_59_AM__58f6714a_157e0e07e65__7ffd_LMC_055004_192_168_1_27" "Branch2"

#run command from MD install dir
cd /Users/ablack/git/mdk/build/install

#all additional needed JARs must be added individually to the -Dmd.additional.class.path property like the ones below
java -Xmx4096M -Xss1024M -DLOCALCONFIG=true -DWINCONFIG=true \
       -Djsse.enableSNIExtension=false \
       -Djava.net.preferIPv4Stack=true \
       -cp lib/com.nomagic.osgi.launcher-17.0.5-SNAPSHOT.jar:\
`         `lib/bundles/org.eclipse.osgi_3.10.1.v20140909-1633.jar:\
`         `lib/bundles/com.nomagic.magicdraw.osgi.fragment_1.0.0.201607250742.jar:\
`         `lib/md_api.jar:lib/md_common_api.jar:lib/md.jar:lib/md_common.jar:lib/jna.jar \
       -Dmd.class.path=file:bin/magicdraw.properties#CLASSPATH \
       -Dmd.additional.class.path=plugins/gov.nasa.jpl.mbee.mdk/mdk-3.0-SNAPSHOT.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/jackson-databind-2.8.3.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/jackson-annotations-2.8.0.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/jackson-core-2.8.3.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/jackson-coreutils-1.6.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/commons-codec-1.9.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/commons-io-1.3.2.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/commons-logging-1.2.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/httpclient-4.5.2.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/httpcore-4.4.4.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/httpmime-4.5.2.jar:\
`         `plugins/gov.nasa.jpl.mbee.mdk/* \
       -Dcom.sun.media.imageio.disableCodecLib=true \
       -noverify \
       -Dcom.nomagic.osgi.config.dir=configuration \
       -Desi.system.config=data/application.conf \
       -Dlogback.configurationFile=data/logback.xml \
       -Dsun.locale.formatasdefault=true \
       -Dcom.nomagic.magicdraw.launcher=gov.nasa.jpl.mbee.pma.analyses.AutomatedViewGeneration \
       com.nomagic.osgi.launcher.ProductionFrameworkLauncher -crdlc $1 -doclist $2 -twprj $3 -twbrn ${4:-master}
