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
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import Activity

from javax.swing import JOptionPane


from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

class PartsCycleDetector:
    
    def __init__(self, element):
        self.root = element
        self.cycles = []
        
    def analyze(self, cur, seen, path):
        for part in cur.getPart():
            if part.getType() is not None and isinstance(part.getType(), Class):
                if part.getType() in seen:
                    cyclepath = []
                    cyclepath.extend(path)
                    cyclepath.append(part)
                    self.cycles.append(cyclepath)
                    continue
                else:
                    newpath = []
                    newpath.extend(path)
                    newpath.append(part)
                    newseen = []
                    newseen.extend(seen)
                    newseen.append(part.getType())
                    self.analyze(part.getType(), newseen, newpath)
    
    def printCycles(self):
        if len(self.cycles) == 0:
            gl.log("No cycles detected")
        for cycle in self.cycles:
            gl.log("Cycle:")
            for partpath in cycle:
                gl.log("\t" + partpath.getQualifiedName())

def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Class):
        gl.log("You must select a block/class!!!")
        return
    try:
        SessionManager.getInstance().createSession("specialize")
        pcd = PartsCycleDetector(selected)
        pcd.analyze(selected,[selected],[])
        pcd.printCycles()
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
