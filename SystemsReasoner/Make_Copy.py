from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mddependencies import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions import *
from com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities import *
from com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities import *
from com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import *
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import *
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import * 

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)
import Specialize
reload(Specialize)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

def createGeneralizationInstance(parent, child):
    g = ef.createGeneralizationInstance()
    g.setOwner(child)
    ModelHelper.setClientElement(g, child)
    ModelHelper.setSupplierElement(g, parent)
    
def makeGeneralizationRecursive(parent, child):
    createGeneralizationInstance(parent, child)
    for attr in child.getOwnedAttribute():
        if StereotypesHelper.hasStereotype(attr, SRUtils.partPropS) and attr.hasRedefinedProperty() and attr.getType() is not None:
            ctype = attr.getType()
            ptype = attr.getRedefinedProperty()[0].getType()
            makeGeneralizationRecursive(ptype, ctype)

            
def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Classifier):
        gl.log("You must select a classifier!!!")
        return

    try:
        SessionManager.getInstance().createSession("generalize")
        new = None
        package = MDUtils.getUserSelections([Package], project.getModel(), False, 'Select a package to put generated stuff in')
        if isinstance(selected, Activity):
            new = ef.createActivityInstance()
        elif isinstance(selected, Class):
            new = ef.createClassInstance()
        new.setName(selected.getName())
        new.setOwner(package)
        MDUtils.createGeneralizationInstance(selected, new)
        MDUtils.copyStereotypes(selected, new)
        Specialize.generalize(new)
        #makeGeneralizationRecursive(selected, new)
    
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)