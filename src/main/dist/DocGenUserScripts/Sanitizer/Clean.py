##################################################################################################################
#Script is designed to clean a model, takes names and descriptions and makes them generic
#Creator: Louise Anderson 

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
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.openapi.uml import PresentationElementsManager
from com.nomagic.magicdraw.uml.symbols.shapes import ShapeElement
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
project = Application.getInstance().getProjectsManager().getActiveProject()
pem=PresentationElementsManager.getInstance()#presentation elements manager
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
        nameType=nameType.replace("Impl","")
        nameType=nameType.strip()
        if nameType in TypesMap:
            TypesMap[nameType]+=1
        else:
            TypesMap[nameType]=1
        
        nameElem=nameType+" "+str(TypesMap[nameType])
        #gl.log("RENAMING ELEMENT=========>"+El.getQualifiedName())
        El.setName(nameElem)
        ModelHelper.setComment(El,"Documentation for Element =>"+nameElem)
        
        if isinstance(El,Diagram):
            #gl.log("I want to do some special manipulation for diagram element notes")
            #get the element for this diagram
            diagR=project.getDiagram(El) #this is a presentation element
            presElems=diagR.getPresentationElements() #children presentation elements
            for p in presElems:
                checkEl=p.getElement() #checking to see if thing on diagram is element, if not an element, then we don't need it
                #gl.log(str(blah))
#                if isinstance(p,ShapeElement):  #throw error, because of ShapeElement, not being a note,textbox or separator.....
#                    pem.setText(p,"Comment")
                #lets not delete, lets just change text
                if checkEl is None:  #assuming if your not an element you are something I don't want and thus deleting you
                    pem.deletePresentationElement(p)
        i+=1
        
    package.setName("Top Level Package")
    ModelHelper.setComment(package,"Documentation for top level package")

if len(scriptInput['target'])>0:
    package = scriptInput['target'][0]
    gl.log("===========SANITIZING MODEL==============")
    cleanse(package)
else:
    gl.log("**ERROR** No Target Provided!")



scriptOutput = ["--------------------THIS SCRIPT IS COMPLETE----------------------"]


#need to add removal of documentation field
