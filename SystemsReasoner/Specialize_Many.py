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
from com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses import AssociationClass
from com.nomagic.magicdraw.copypaste import CopyPasteManager
from com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines import StateMachine

from java.lang import Object
from javax.swing import JOptionPane
from javax.swing import JCheckBox
from jarray import array

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
import Validate_Structure
reload(Validate_Structure)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

def run(mode):
    selecteds = MDUtils.getUserSelections([Class,StateMachine], project.getModel(), True, "choose the blocks to specialize")
    
    try:
        SessionManager.getInstance().createSession("specialize many")
        options = {'checkOnly':False}
        if selecteds is not None:
            check = JCheckBox("Ignore further parent errors in this session")
            for selected in selecteds:
                if not Specialize.editable(selected):
                    gl.log(selected.getQualifiedName() + " is not editable all the way! Skipping!")
                    JOptionPane.showMessageDialog(None, selected.getQualifiedName() + " is not editable all the way! Skipping!", "Not Editable!", JOptionPane.ERROR_MESSAGE)
                    continue
                if Specialize.checkParentTree(selected):   
                    Specialize.generalize(selected, options)
                else:
                    choice = None
                    if not check.isSelected():
                        choice = JOptionPane.showConfirmDialog(None, array(["There are errors in the generalization tree above for " + selected.getQualifiedName() + ", continue with the update?", check], Object), "Continue?", JOptionPane.YES_NO_OPTION);
                    if choice == JOptionPane.YES_OPTION or check.isSelected():
                        Specialize.generalize(selected, options)
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
