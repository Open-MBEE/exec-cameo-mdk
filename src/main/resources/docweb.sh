#!/bin/bash

#echo Teamwork Project: $1
#echo Document List: $2
#echo Credentials Location: $3

#complete classpath for PMA, MDK, and MD
export CLASSPATH=$MD_HOME/automations/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/automations/lib/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/gov.nasa.jpl.mbee.docgen/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/gov.nasa.jpl.mbee.docgen/lib/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/gov.nasa.jpl.mbee.docgen/lib/test/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/lib/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/lib/graphics/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/lib/webservice/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/engines/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/engines/beanshell/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/engines/groovy-2.0.1/embeddable/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/engines/groovy-2.0.1/indy/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/engines/groovy-2.0.1/lib/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/engines/js-rhino/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/help/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/lib/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.automaton/resources/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.codeengineering/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.codeengineering/lib/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.contentdiagram/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.dependencymatrix/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.dependencymatrix/resources/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.diagramtable/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.jpython/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.jpython/jython/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.jpython/jython/Lib/test/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.jpython/jython/Lib/test/bug1126/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.merge/*
export CLASSPATH=${CLASSPATH}:$MD_HOME/plugins/com.nomagic.magicdraw.merge/resources/*

#export display because MD requires GUI
export DISPLAY=:1

#perform docgen
echo java -Xmx4096M -XX:PermSize=64M -XX:MaxPermSize=512M -Dinstall.root=$MD_HOME gov.nasa.jpl.mbee.pma.analyses.AutomatedViewGeneration -tstrt ${WORKSPACE}/ -twprj $1 --doclist $2 -crdlc $3
java -Xmx4096M -XX:PermSize=64M -XX:MaxPermSize=512M -Dinstall.root=$MD_HOME gov.nasa.jpl.mbee.pma.analyses.AutomatedViewGeneration -tstrt ${WORKSPACE}/ -twprj $1 --doclist $2 -crdlc $3

ERROR=$?
echo Error code: $ERROR
exit $ERROR

