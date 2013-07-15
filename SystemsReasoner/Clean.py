##################################################################################################################
#Script is designed to search project for certain streotypes IEEE 1471 and JEO Architecture, finds usages in project and replaces those stereotype with the new IMCE Plugin Stereotypes. Also script looks for the common dependency relationships among the elements and if that dependency exists stereotype it with the correct stereotype relation.
from gov.nasa.jpl.mbee.lib import Utils
from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.core import Project
from com.nomagic.magicdraw.core.project import *
from com.nomagic.magicdraw.openapi.uml import *
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Package
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Diagram
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Class
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import NamedElement
from com.nomagic.magicdraw.ui.browser import *
from com.nomagic.uml2.impl import ElementsFactory
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from javax.swing import *
from java.lang import Object
from java.lang import String
import re
import os
import sys
import traceback
import locale
import MDUtils._MDUtils as MDUtils
reload(MDUtils)

gl = Application.getInstance().getGUILog()
#project = Application.getInstance().getProjectsManager().getActiveProject()
#package = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
#csProject=Application.getProject("Control Service")

TypesMap={}

##need to use userscript thing specifically

def cleanse(package):
    i=0
    ElAll=Utils.collectOwnedElements(package,10)
    for ElA in ElAll:
        ModelHelper.setComment(ElA,"")
    ElNamed=filter(lambda element: isinstance(element,NamedElement),ElAll)
    for El in ElNamed:
        #gl.log("What Element are we on====>"+El.getName())
        nameType=El.getClass().getSimpleName()
        if nameType in TypesMap:
            TypesMap[nameType]+=1
        else:
            TypesMap[nameType]=1
        
        nameElem=nameType+" "+str(TypesMap[nameType])
        #gl.log("RENAMING ELEMENT=========>"+El.getQualifiedName())
        El.setName(nameElem)
        ModelHelper.setComment(El,"Documentation for Element =>"+nameElem)
        i+=1
        

if len(scriptInput['target'])>0:
    package = scriptInput['target'][0]
    gl.log("===========SANITIZING MODEL==============")
    cleanse(package)
else:
    gl.log("**ERROR** No Target Provided!")



scriptOutput = ["--------------------THIS SCRIPT IS COMPLETE----------------------"]


#need to add removal of documentation field
