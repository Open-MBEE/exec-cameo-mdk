from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Package
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Class
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import InstanceValue
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import LiteralReal
from javax.swing import JOptionPane

from java.util import Date
from java.text import DateFormat
from java.text import SimpleDateFormat

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

def createInstances(owner, block, prefix):
    ins = ef.createInstanceSpecificationInstance()
    ins.setName(prefix)
    ins.getClassifier().add(block)
    ins.setOwner(owner)
    
    gl.log("making instance for " + block.getQualifiedName())
    allInherited = SRUtils.getAllInheritedAttributes(block)
    genTree = SRUtils.getGeneralizationTree(block)
    redefTree = SRUtils.getRedefinitionTree(genTree, block)
    inherited = SRUtils.getInheritedAttributes(allInherited, redefTree)
    for attr in block.getOwnedAttribute():
        for redefinedProp in attr.getRedefinedProperty():
            if redefinedProp in inherited:
                inherited.remove(redefinedProp)
        inherited.append(attr)
    slotmap = {}
    for attr in inherited:
        if (attr.getType() is not None and attr.getType() is attr.getOwner()): #recursive composition!
            continue;
        if (StereotypesHelper.hasStereotype(attr, SRUtils.valuePropS) and attr.getType() is not None and attr.getType().getOwnedAttribute().size() == 0):
        #if StereotypesHelper.hasStereotype(attr, SRUtils.valuePropS):
            slot = ef.createSlotInstance()
            slot.setDefiningFeature(attr)
            slot.setOwner(ins)
            slotmap[attr] = slot
            multiple = findMultiplicity(attr)
            for i in range(multiple):
                value = SRUtils.cloneValueSpec(attr.getDefaultValue())
                if value is not None:
                    slot.getValue().add(value)
                elif isinstance(attr.getDefaultValue(), LiteralReal):
                    real = ef.createLiteralRealInstance()
                    real.setValue(attr.getDefaultValue().getValue())
                    slot.getValue().add(real)
                else:
                    value = ef.createLiteralStringInstance()
                    value.setValue('0')
        elif StereotypesHelper.hasStereotype(attr, SRUtils.partPropS) or StereotypesHelper.hasStereotype(attr, SRUtils.valuePropS):
            slot = None
            for redefinedProp in attr.getRedefinedProperty():
                if redefinedProp.getType() is not None and redefinedProp.getType() is redefinedProp.getOwner():
                    if redefinedProp not in slotmap:
                        slot = ef.createSlotInstance()
                        slot.setDefiningFeature(redefinedProp)
                        slot.setOwner(ins)
                        slotmap[redefinedProp] = slot
                    else:
                        slot = slotmap[redefinedProp]
                    break
            if slot is None:
                slot = ef.createSlotInstance()
                slot.setDefiningFeature(attr)
                slot.setOwner(ins)
                slotmap[attr] = slot
            multiple = findMultiplicity(attr)
            newPrefix = attr.getName()
            if newPrefix == '' or newPrefix is None:
                if (StereotypesHelper.hasStereoytpe(attr, SRUtils.valuePropS)):
                    newPrefix = 'valueProperty'
                else:
                    newPrefix = 'partProperty'
            if attr.getDefaultValue() is not None and isinstance(attr.getDefaultValue(), InstanceValue) and attr.getDefaultValue().getInstance() is not None:
                if (multiple > 1):
                    for i in range(multiple): 
                        c = cloneInstanceSpec(attr.getDefaultValue().getInstance(), owner, prefix + '.' + newPrefix + '[' + str(i+1) + ']')  
                        iv = ef.createInstanceValueInstance()
                        iv.setInstance(c)
                        slot.getValue().add(iv)
                else:
                    c = cloneInstanceSpec(attr.getDefaultValue().getInstance(), owner, prefix + '.' + newPrefix)
                    iv = ef.createInstanceValueInstance()
                    iv.setInstance(c)
                    slot.getValue().add(iv)
                continue
            
            if multiple > 1:
                for i in range(multiple):
                    value = ef.createInstanceValueInstance()
                    ins2 = createInstances(owner, attr.getType(), prefix + '.' + newPrefix + '[' + str(i+1) + ']')
                    value.setInstance(ins2)
                    slot.getValue().add(value)
            else:
                value = ef.createInstanceValueInstance()
                ins2 = createInstances(owner, attr.getType(), prefix + '.' + newPrefix)
                value.setInstance(ins2)
                slot.getValue().add(value)           
    return ins

def cloneInstanceSpec(inspec, owner, prefix):
    newinspec = ef.createInstanceSpecificationInstance()
    newinspec.setName(prefix)
    newinspec.setOwner(owner)
    newinspec.getClassifier().addAll(inspec.getClassifier())
    for slot in inspec.getSlot():
        newslot = ef.createSlotInstance()
        newslot.setOwner(newinspec)
        newslot.setOwningInstance(newinspec)
        newslot.setDefiningFeature(slot.getDefiningFeature())
        count = 1
        for value in slot.getValue():
            if not isinstance(value, InstanceValue):
                newvalue = SRUtils.cloneValueSpec(value)
                if newvalue is not None:
                    newslot.getValue().add(newvalue)
                elif isinstance(value, LiteralReal):
                    real = ef.createLiteralRealInstance()
                    real.setValue(value.getValue())
                    newslot.getValue().add(real)
            else:
                if value.getInstance() is not None:
                    if slot.getValue().size() > 1:
                        newinspec2 = cloneInstanceSpec(value.getInstance(), owner, prefix + '.' + slot.getDefiningFeature().getName() + '[' + str(count) + ']')
                    else:
                        newinspec2 = cloneInstanceSpec(value.getInstance(), owner, prefix + '.' + slot.getDefiningFeature().getName())
                    newslot.getValue().add(newinspec2)
                    count = count + 1
    return newinspec
    
def findMultiplicity(attr):
    lower = attr.getLower()
    upper = attr.getUpper()
    if lower == upper:
        return lower
    s = JOptionPane.showInputDialog("How many instances do you want for " + attr.getQualifiedName() + "?\n", "Input multiplicity")
    if s is not None:
        try:
            s = int(s)
            return s 
        except:
            return 1
    return 1

def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Class):
        gl.log('you must select a block!')
        return
    try:
        SessionManager.getInstance().createSession("generalize")
        package = MDUtils.getUserSelections([Package], project.getModel(), False, 'Select a package to put instances in')
        if package is not None:
            createInstances(package, selected, selected.getName())
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)