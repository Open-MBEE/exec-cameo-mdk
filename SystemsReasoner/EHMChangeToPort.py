from java.lang import *
from java.awt import Point

from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.magicdraw.openapi.uml import PresentationElementsManager
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
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures import *

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()
pem = PresentationElementsManager.getInstance()

      
def run(mode):
    selected = None
    dpel = Application.getInstance().getProject().getActiveDiagram()

    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = dpel.getSelected().get(0).getElement()
    if not isinstance(selected, Connector):
        gl.log("You must select a connector!!!")
        return

    try:
        SessionManager.getInstance().createSession("changetoport")
        end1 = selected.getEnd().get(0)
        end2 = selected.getEnd().get(1)
        role1 = end1.getRole()
        role2 = end2.getRole()
        newconn = ef.createConnectorInstance()
        newconn.setOwner(selected.getOwner())
        newend1 = newconn.getEnd().get(0)
        newend2 = newconn.getEnd().get(1)
        paths = StereotypesHelper.getStereotypePropertyValue(end1, SRUtils.nestedEndS, "propertyPath")
        # lets get the original points, so the ports can be mapped to them later (won't keep the routes the same) 
        # SPEL is a PathElement
        spel    = dpel.findPresentationElement(selected, None)
        point1  = spel.getClientPoint()
        point2  = spel.getSupplierPoint()
        bpoints = spel.getAllBreakPoints()
        lineWidth       = spel.getLineWidth()
#        lineLinkStyle   = spel.getLinkLineStyle()
        lineColor       = spel.getLineColor()
        lineName        = spel.getName()
        
        for path in paths:
            StereotypesHelper.setStereotypePropertyValue(newend1, SRUtils.nestedEndS, "propertyPath", path, True)
        paths = StereotypesHelper.getStereotypePropertyValue(end2, SRUtils.nestedEndS, "propertyPath")
        for path in paths:
            StereotypesHelper.setStereotypePropertyValue(newend2, SRUtils.nestedEndS, "propertyPath", path, True)
        if isinstance(role1, Property) and not isinstance(role1, Port):
            block1 = role1.getType()
            port1 = ef.createPortInstance()
            port1.setOwner(block1)
            newend1.setRole(port1)
            newend1.setPartWithPort(role1)
            StereotypesHelper.setStereotypePropertyValue(newend1, SRUtils.nestedEndS, "propertyPath", role1, True)
            role1pel = dpel.findPresentationElement(role1, None)
            port1sel = pem.createShapeElement(port1, role1pel, False)
            dim = port1sel.getPreferredSize()
            newPoint = Point()  # need to use a new point otherwise undo doesn't work properly
            newPoint.setLocation(point1.getX() - dim.getWidth()/2, point1.getY() - dim.getHeight()/2)
            pem.movePresentationElement(port1sel, newPoint)
        if isinstance(role2, Property) and not isinstance(role2, Port):
            block2 = role2.getType()
            port2 = ef.createPortInstance()
            port2.setOwner(block2)
            newend2.setRole(port2)
            newend2.setPartWithPort(role2)
            StereotypesHelper.setStereotypePropertyValue(newend2, SRUtils.nestedEndS, "propertyPath", role2, True)
            role2pel = dpel.findPresentationElement(role2, None)
            port2sel = pem.createShapeElement(port2, role2pel, False)
            dim = port2sel.getPreferredSize()
            newPoint = Point() 
            newPoint.setLocation(point2.getX() - dim.getWidth()/2, point2.getY() - dim.getHeight()/2)
            pem.movePresentationElement(port2sel, newPoint)
        newconn.setName(lineName)
        path = pem.createPathElement(newconn, port1sel, port2sel)
        path.setLineWidth(lineWidth)
#        path.setLinkLineStyle(lineLinkStyle)
        path.setLineColor(lineColor)
        pem.changePathBreakPoints(path, bpoints)

        mem.removeElement(selected)
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)