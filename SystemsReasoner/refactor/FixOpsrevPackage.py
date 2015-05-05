from java.lang import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Class
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import Interface

from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Property
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Package
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import AggregationKindEnum
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Enumeration
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Constraint
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port
from com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses import AssociationClass
from com.nomagic.magicdraw.copypaste import CopyPasteManager
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import Activity

from javax.swing import JOptionPane


from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)
import Validate_Structure
reload(Validate_Structure)
import Specialize
reload(Specialize)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()


def findRightType(p):
    redefs = p.getRedefinedProperty()
    if len(redefs) > 0:
        redef = redefs[0]
        type = redef.getType()
        if type is not None:
            for g in type.get_generalizationOfGeneral():
                potential = g.getSpecific()
                if potential.getName().endswith("Fix Me!"):
                    return potential
    return None

def shouldHaveInstance(t):
    if t is None:
        return False
    owner = t.getOwner()
    while owner is not None:
        if owner.getName() == "Control Service":
            return True
        owner = owner.getOwner()
    return False

def validateBlock(b):
    for a in b.getOwnedAttribute():
        if a.getType() is not None and a.getType().getOwner() is b or shouldHaveInstance(a.getType()):
            gl.log("Wrong type found! " + a.getQualifiedName() + " has type " + a.getType().getQualifiedName())
            right = findRightType(a)
            
            if right is None:
                gl.log("Cannot find the right type! Do nothing....")
            else:
                gl.log("Right type is " + right.getQualifiedName() + ", fixing type...")
                wrongType = a.getType()
                a.setType(right)
                if wrongType.getOwner() is b:
                    mem.removeElement(wrongType)
                      
def validatePackage(p):
    for e in p.getOwnedElement():
        if isinstance(e, Class):
            validateBlock(e)
        if isinstance(e, Package):
            validatePackage(e)

def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Package):
        gl.log("You must select a package!!")
        return
    if not Specialize.editable(selected):
        gl.log("Selected is not editable all the way!!!")
        return
    try:
        SessionManager.getInstance().createSession("specialize")
        validatePackage(selected)
        
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
