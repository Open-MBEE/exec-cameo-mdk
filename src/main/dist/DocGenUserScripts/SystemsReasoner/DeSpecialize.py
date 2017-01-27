from java.lang import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Class
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Property
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import AggregationKindEnum
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Enumeration
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port


from javax.swing import JOptionPane



import sys
import traceback
import os

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

def undo(selected):
    toremove = []
    for attr in selected.getOwnedAttribute():
        if attr.hasRedefinedProperty():
            if attr.getType() is not None and attr.getType().getOwner() == selected:
                toremove.append(attr.getType())
            toremove.append(attr)
    for i in range(0, len(toremove)):
        mem.removeElement(toremove[i])


def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Class):
        gl.log("You must select a block!!!")
        return

    try:
        SessionManager.getInstance().createSession("generalize")
        choice = JOptionPane.showConfirmDialog(None, "If you undo specializations you may lose default values on blocks since they'll be deleted, continue?", "Continue?", JOptionPane.YES_NO_OPTION);
        if choice == JOptionPane.YES_OPTION:    
            undo(selected)
        
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
