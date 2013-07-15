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
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures import *

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os
from javax.swing import JOptionPane
import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()


def findOppositeEnd(role1, end1, connector):
    for end in connector.getEnd():
        if end.getRole() is not role1:
            return end
    for end in connector.getEnd():
        if end is not end1:
            return end
    return None

def findPeerParts(part):
    owner = part.getOwner()
    res = []
    for p in owner.getOwnedAttribute():
        if p is not part:
            res.append(p)
    return res

def pathUnderPart(end, part):
    paths = StereotypesHelper.getStereotypePropertyValue(end, SRUtils.nestedEndS, "propertyPath")
    if paths is not None and len(paths) > 0:
        if paths.get(0) is part:
            return True
    return False

def findPartsTypedBy(type):
    parts = type.get_typedElementOfType()
    res = []
    res.extend(parts)
    return res

def copyPropertyPaths(fromEnd, toEnd):
    paths = StereotypesHelper.getStereotypePropertyValue(fromEnd, SRUtils.nestedEndS, "propertyPath")
    if paths is not None:
        for path in paths:
            StereotypesHelper.setStereotypePropertyValue(toEnd, SRUtils.nestedEndS, "propertyPath", path, True)

def copyMultiplicity(prop, newprop):
    newprop.setLowerValue(SRUtils.cloneValueSpec(prop.getLowerValue()))
    newprop.setUpperValue(SRUtils.cloneValueSpec(prop.getUpperValue()))
    newprop.setUnique(prop.isUnique())
    newprop.setOrdered(prop.isOrdered())
                          
def movePartDown(part, newOwner, ownerPart):
    ends = part.getEnd()
    asso = part.getAssociation()
    
    newasso = ef.createAssociationInstance()
    newasso.setOwner(newOwner.getOwner())
    newProp = newasso.getMemberEnd().get(0)
    newProp2 = newasso.getMemberEnd().get(1)
    newProp.setType(part.getType())
    newProp2.setType(newOwner)
    newProp.setOwner(newOwner)
    newProp2.setOwner(newasso)
    newProp.setAggregation(part.getAggregation())
    newProp.setName(part.getName())
    copyMultiplicity(part, newProp)
    MDUtils.copyStereotypes(part, newProp)
    
    bad = []
    for end in ends:
        oppend = findOppositeEnd(part, end, end.get_connectorOfEnd())
        if oppend.getRole() is part:
            bad.append(end)
            continue
        newconn = ef.createConnectorInstance()
        partend = newconn.getEnd().get(0)
        newoppend = newconn.getEnd().get(1)
        partend.setRole(newProp)
        newoppend.setRole(oppend.getRole())
        
        if StereotypesHelper.hasStereotype(end, SRUtils.nestedEndS):
            newconn.setOwner(end.get_connectorOfEnd().getOwner())
            copyPropertyPaths(end, partend)
            StereotypesHelper.setStereotypePropertyValue(partend, SRUtils.nestedEndS, "propertyPath", ownerPart, True)
            copyPropertyPaths(oppend, newoppend)
        elif end.get_connectorOfEnd().getOwner() is part.getType():
            newconn.setOwner(part.getType())
            copyPropertyPaths(oppend, newoppend)
        elif not StereotypesHelper.hasStereotype(oppend, SRUtils.nestedEndS):
            if oppend.getRole() is ownerPart:
                newconn.setOwner(newOwner)
            else:
                newconn.setOwner(end.get_connectorOfEnd().getOwner())              
                StereotypesHelper.setStereotypePropertyValue(partend, SRUtils.nestedEndS, "propertyPath", ownerPart, True)
        else:
            if pathUnderPart(oppend, ownerPart):
                newconn.setOwner(newOwner)
                paths = []
                proppath = StereotypesHelper.getStereotypePropertyValue(oppend, "NestedConnectorEnd", "propertyPath")
                paths.extend(proppath)
                if len(paths) == 1:
                    pass
                else:
                    newpaths = paths[1:]
                    for path in newpaths:
                        StereotypesHelper.setStereotypePropertyValue(newoppend, SRUtils.nestedEndS, "propertyPath", path, True)                
            else:
                newconn.setOwner(end.get_connectorOfEnd().getOwner())
                StereotypesHelper.setStereotypePropertyValue(partend, SRUtils.nestedEndS, "propertyPath", ownerPart, True)
                copyPropertyPaths(oppend, newoppend)
    movePartDownPorts(part, newOwner, ownerPart, newProp)
    movePartDownChildren(part, newOwner, ownerPart, newProp)
    if len(bad) > 0:
        gl.log("There's one or more connector from the original part to itself, those are not replicated")
    mem.removeElement(part)

def movePartDownChildren(part, newOwner, ownerPart, newProp):
    childs = []
    findAllChildrenParts(part, childs)
    for child in childs:
        for end in list(child.getEnd()):
            if StereotypesHelper.hasStereotype(end, SRUtils.nestedEndS):
                paths = StereotypesHelper.getStereotypePropertyValue(end, SRUtils.nestedEndS, "propertyPath")
                if paths is not None and part in paths:
                    newconn = ef.createConnectorInstance()
                    thisend = newconn.getEnd().get(0)
                    newoppend = newconn.getEnd().get(1)
                    thisend.setRole(end.getRole())
                    oppend = findOppositeEnd(end.getRole(), end, end.get_connectorOfEnd())
                    newoppend.setRole(oppend.getRole())
                    
                    opppaths = StereotypesHelper.getStereotypePropertyValue(oppend, SRUtils.nestedEndS, "propertyPath")
                    if opppaths is not None and len(opppaths) > 0 and opppaths.get(0) is ownerPart:
                        newconn.setOwner(newOwner)
                        newpath = [newProp]
                        newpath.extend(paths[1:])
                        for path in newpath:
                            StereotypesHelper.setStereotypePropertyValue(thisend, SRUtils.nestedEndS, "propertyPath", path, True)
                        newpath = opppaths[1:]
                        for path in newpath:
                            StereotypesHelper.setStereotypePropertyValue(newoppend, SRUtils.nestedEndS, "propertyPath", path, True)
                    elif oppend.getRole() is ownerPart:
                        newconn.setOwner(newOwner)
                        newpath = [newProp]
                        newpath.extend(paths[1:])
                        for path in newpath:
                            StereotypesHelper.setStereotypePropertyValue(thisend, SRUtils.nestedEndS, "propertyPath", path, True)
                    else:
                        newconn.setOwner(end.get_connectorOfEnd().getOwner())
                        index = paths.indexOf(part)
                        firstpart = paths[:index]
                        secondpart = paths[index+1:]
                        newpath = []
                        newpath.extend(firstpart)
                        newpath.append(ownerPart)
                        newpath.append(newProp)
                        newpath.extend(secondpart)
                        for path in newpath:
                            StereotypesHelper.setStereotypePropertyValue(thisend, SRUtils.nestedEndS, "propertyPath", path, True)
                        copyPropertyPaths(oppend, newoppend)
                    mem.removeElement(end.get_connectorOfEnd())


def findAllChildrenParts(part, childs):
    type = part.getType()
    if type is not None and isinstance(type, Class):
        for attr in type.getOwnedAttribute():
            if not isinstance(attr, Port):
                if attr not in childs:
                    childs.append(attr)
                    findAllChildrenParts(attr, childs)
        
    
def movePartDownPorts(part, newOwner, ownerPart, newProp):
    portends = findPortEnds(part)
    for end in portends:
        oppend = findOppositeEnd(end.getRole(), end, end.get_connectorOfEnd())
        newconn = ef.createConnectorInstance()
        newconn.getEnd().get(0).setRole(end.getRole())
        newconn.getEnd().get(1).setRole(oppend.getRole())
        portend = newconn.getEnd().get(0)
        newoppend = newconn.getEnd().get(1)
        paths = StereotypesHelper.getStereotypePropertyValue(end, SRUtils.nestedEndS, "propertyPath")
        if len(paths) > 1:
            newconn.setOwner(end.get_connectorOfEnd().getOwner())
            newpaths = paths[:-1]
            for path in newpaths:
                StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", path, True)
            StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", ownerPart, True)
            StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", newProp, True)
            portend.setPartWithPort(newProp)
            copyPropertyPaths(oppend, newoppend)
        else:
            oppathes = StereotypesHelper.getStereotypePropertyValue(oppend, SRUtils.nestedEndS, "propertyPath")
            if len(oppathes) > 0 and oppathes.get(0) is ownerPart:
                newconn.setOwner(newOwner)
                if len(oppathes) > 1:
                    newoppathes = oppathes[1:]
                    for path in newoppathes:
                        StereotypesHelper.setStereotypePropertyValue(newoppend, SRUtils.nestedEndS, "propertyPath", path, True)
                StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", newProp, True)
                portend.setPartWithPort(newProp)
            else:
                newconn.setOwner(end.get_connectorOfEnd().getOwner())
                copyPropertyPaths(oppend, newoppend)
                StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", ownerPart, True)
                StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", newProp, True)
                portend.setPartWithPort(newProp)
        mem.removeElement(end.get_connectorOfEnd())
        
    
        
def movePartUp(part, newOwner, partInOwner):
    ends = part.getEnd()
    
    newasso = ef.createAssociationInstance()
    newasso.setOwner(newOwner.getOwner())
    newProp = newasso.getMemberEnd().get(0)
    newProp2 = newasso.getMemberEnd().get(1)
    newProp.setType(part.getType())
    newProp2.setType(newOwner)
    newProp.setOwner(newOwner)
    newProp2.setOwner(newasso)
    newProp.setAggregation(part.getAggregation())
    newProp.setName(part.getName())
    copyMultiplicity(part, newProp)

    MDUtils.copyStereotypes(part, newProp)
    bad = []
    for end in ends:
        oppend = findOppositeEnd(part, end, end.get_connectorOfEnd())
        if oppend.getRole() is part:
            bad.append(end)
            continue
        newconn = ef.createConnectorInstance()
        partend = newconn.getEnd().get(0)
        newoppend = newconn.getEnd().get(1)
        partend.setRole(newProp)
        newoppend.setRole(oppend.getRole())
        
        if not StereotypesHelper.hasStereotype(end, SRUtils.nestedEndS):
            if end.get_connectorOfEnd().getOwner() is part.getType():
                newconn.setOwner(part.getType())
                copyPropertyPaths(oppend, newoppend)
            else:
                newconn.setOwner(newOwner)
                if oppend.getRole() is not partInOwner:
                    StereotypesHelper.setStereotypePropertyValue(newoppend, SRUtils.nestedEndS, "propertyPath", partInOwner, True)
                    copyPropertyPaths(oppend, newoppend)
        else:
            newconn.setOwner(end.get_connectorOfEnd().getOwner())
            paths = StereotypesHelper.getStereotypePropertyValue(end, SRUtils.nestedEndS, "propertyPath")
            if paths is not None:
                connectorOwner = end.get_connectorOfEnd().getOwner()
                newpaths = []
                findPath(connectorOwner, newProp, newpaths)
                for path in newpaths:
                    StereotypesHelper.setStereotypePropertyValue(partend, SRUtils.nestedEndS, "propertyPath", path, True)
            copyPropertyPaths(oppend, newoppend)
    movePartUpPorts(part, newOwner, partInOwner, newProp)
    movePartUpChildren(part, newOwner, partInOwner, newProp)
    if len(bad) > 0:
        gl.log("There's one or more connector from the original part to itself, those are not replicated")
    mem.removeElement(part)

def movePartUpChildren(part, newOwner, partInOwner, newProp):
    childs = []
    findAllChildrenParts(part, childs)
    for child in childs:
        for end in list(child.getEnd()):
            if StereotypesHelper.hasStereotype(end, SRUtils.nestedEndS):
                paths = StereotypesHelper.getStereotypePropertyValue(end, SRUtils.nestedEndS, "propertyPath")
                if paths is not None and part in paths:
                    index = paths.indexOf(part)
                    if index > 0:
                        if paths[index-1] is partInOwner:
                            newconn = ef.createConnectorInstance()
                            newconn.setOwner(end.get_connectorOfEnd().getOwner())
                            thisend = newconn.getEnd().get(0)
                            newoppend = newconn.getEnd().get(1)
                            thisend.setRole(end.getRole())
                            oppend = findOppositeEnd(end.getRole(), end, end.get_connectorOfEnd())
                            newoppend.setRole(oppend.getRole())
                            firstpart = paths[:index-1]
                            secondpart = paths[index+1:]
                            newpath = []
                            newpath.extend(firstpart)
                            newpath.append(newProp)
                            newpath.extend(secondpart)
                            for path in newpath:
                                StereotypesHelper.setStereotypePropertyValue(thisend, SRUtils.nestedEndS, "propertyPath", path, True)
                            copyPropertyPaths(oppend, newoppend)
                            mem.removeElement(end.get_connectorOfEnd())
                        else:
                            pass #this means this connector is going through some other part whose type originally owns part, but since part's no longer under type, what happens?
                    else:
                        # this means the connector is owned by something that owns a part whose type used to contain part, the connector needs to move up
                        newconn = ef.createConnectorInstance()
                        newconn.setOwner(newOwner)
                        newpath = [newProp]
                        newpath.extend(paths[1:])
                        thisend = newconn.getEnd().get(0)
                        newoppend = newconn.getEnd().get(1)
                        thisend.setRole(end.getRole())
                        oppend = findOppositeEnd(end.getRole(), end, end.get_connectorOfEnd())
                        newoppend.setRole(oppend.getRole())
                        for path in newpath:
                            StereotypesHelper.setStereotypePropertyValue(thisend, SRUtils.nestedEndS, "propertyPath", path, True)
                        opppath = StereotypesHelper.getStereotypePropertyValue(oppend, SRUtils.nestedEndS, "propertyPath")
                        if oppend.getRole() is partInOwner:
                            pass
                        else:
                            newpath = [partInOwner]
                            newpath.extend(opppath)
                            for path in newpath:
                                StereotypesHelper.setStereotypePropertyValue(newoppend, SRUtils.nestedEndS, "propertyPath", path, True)
                        mem.removeElement(end.get_connectorOfEnd())
                else:
                    pass #no need to fix?
            else:
                pass #no need to fix?
            


def movePartUpPorts(part, newOwner, partInOwner, newProp):
    portends = findPortEnds(part)
    for end in portends:
        oppend = findOppositeEnd(end.getRole(), end, end.get_connectorOfEnd())
        newconn = ef.createConnectorInstance()
        newconn.getEnd().get(0).setRole(end.getRole())
        newconn.getEnd().get(1).setRole(oppend.getRole())
        portend = newconn.getEnd().get(0)
        newoppend = newconn.getEnd().get(1)
        portend.setPartWithPort(newProp)
        paths = StereotypesHelper.getStereotypePropertyValue(end, SRUtils.nestedEndS, "propertyPath")
        if len(paths) > 1:
            newconn.setOwner(end.get_connectorOfEnd().getOwner())
            newpaths =  paths[:-2]
            for path in newpaths:
                StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", path, True)
            StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", newProp, True)
            copyPropertyPaths(oppend, newoppend)
        else:
            newconn.setOwner(newOwner)
            StereotypesHelper.setStereotypePropertyValue(portend, SRUtils.nestedEndS, "propertyPath", newProp, True)
            StereotypesHelper.setStereotypePropertyValue(newoppend, SRUtils.nestedEndS, "propertyPath", partInOwner, True)
            copyPropertyPaths(oppend, newoppend)
        mem.removeElement(end.get_connectorOfEnd())
    
                    
def findPath(owner, part, parts):
    for attr in owner.getOwnedAttribute():
        if attr is part:
            return True
        parts.append(attr)
        attrtype = attr.getType()
        if attrtype is not None and isinstance(attrtype, Class):
            if findPath(attrtype, part, parts):
                return True
        parts.remove(attr)
    return False
        
def findPortEnds(part):
    partType = part.getType()
    ports = []
    ends = []
    for attr in partType.getOwnedAttribute():
        if isinstance(attr, Port):
            ports.append(attr)
    for port in ports:
        for end in port.getEnd():
            if end.getPartWithPort() is part:
                ends.append(end)
    return ends
                
def moveup(part):
    partowner = part.getOwner()
    parts = findPartsTypedBy(partowner)
    owners = []
    for part1 in parts:
        if part1.getOwner() not in owners and isinstance(part1.getOwner(), Class):
            owners.append(part1.getOwner())
    owner = MDUtils.getUserDropdownSelection("Choose new owner", "choose new owner", owners)
    ownerpart = None
    for attr in owner.getOwnedAttribute():
        if attr.getType() is partowner:
            ownerpart = attr
    movePartUp(part, owner, ownerpart)
    
def movedown(part):
    parts = findPeerParts(part)
    ownerPart = MDUtils.getUserDropdownSelection("Choose part", "Choose which peer part you want to move under:", parts)
    movePartDown(part, ownerPart.getType(), ownerPart)

def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Property):
        gl.log("You must select a part!!!")
        return
    gl.log('selected: '+ selected.getQualifiedName())
    try:
        SessionManager.getInstance().createSession("movepart")
        choice = JOptionPane.showConfirmDialog(None, "move part up?" , "Delete Type Block?", JOptionPane.YES_NO_OPTION)
        if choice == JOptionPane.YES_OPTION:
            moveup(selected)
        else:
            movedown(selected)
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)