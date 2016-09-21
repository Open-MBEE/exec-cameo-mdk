'''
Created on Oct 13, 2011

@author: efosse
'''
from sets import Set
from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.mdprofiles import Stereotype
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mddependencies import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions import *
from com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities import *
from com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities import *
from com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import *
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import *
from com.nomagic.uml2.ext.magicdraw.classes.mdpowertypes import GeneralizationSet
from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Interaction 
from SystemsReasoner import Specialize
reload(Specialize)
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
from SystemsReasoner import SRUtils
reload(SRUtils)
from FrameworkValidation import ProfileValidation as VP
reload(VP)

import PackageClone
reload(PackageClone)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
classmeta = StereotypesHelper.getProfileForStereotype
datapack=project.getModel()
mapping={}

if len(scriptInput['target'])>0:
    pack = scriptInput['target'][0]
    if len(scriptInput['target'])>0:
        mapping=PackageClone.packageClone(pack)
        options={"checkOnly":False, "mapping": mapping, 'fixForOpsrev':True}
#        for m in mapping:
#            if not isinstance(m, Package):
#                newElement=mapping[m]
#                Specialize.generalize(newElement,options)
    #Summary(package,csItem)
else:
    gl.log("**ERROR** No Target Provided!")



scriptOutput = ["***Validate Package Complete***"]


 
 
#def run(mode):
#    if mode == 'b':
#        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
#    try:
#        SessionManager.getInstance().createSession("syncProfile")
#        packageClone(selected)
#        options={"checkOnly":False, "mapping": mapping, 'fixForOpsrev':True}
#        for m in mapping:
#            if not isinstance(m, Package):
#                newElement=mapping[m]
#                Specialize.generalize(newElement,options)
#        SessionManager.getInstance().closeSession()
#    except:
#        if SessionManager.getInstance().isSessionCreated():
#            SessionManager.getInstance().cancelSession()
#        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
#        gl.log("*** EXCEPTION:")
#        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
#        for message in messages:
#            gl.log(message)