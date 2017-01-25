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
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Generalization
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port
from com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses import AssociationClass
from com.nomagic.magicdraw.copypaste import CopyPasteManager
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import Activity
from com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines import StateMachine
from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Interaction
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors import OpaqueBehavior

from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import DurationConstraint
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import TimeConstraint
from com.nomagic.uml2.ext.magicdraw.interactions.mdfragments import InteractionConstraint


from javax.swing import JOptionPane



import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)
import ValidateStructure
reload(ValidateStructure)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

def generalize(selected, options={'checkOnly':False}):
    gl.log("Generalizing: " + selected.getQualifiedName())
    checker = ValidateStructure.SRChecker(selected, options)
    checker.checkObsoleteRedefs()
    checker.removeObsoleteRedefs()
    checker.checkAttrs()
    checker.printErrors()
    
    if not editable(selected):
        gl.log(selected.getQualifiedName() + " is not editable!!! This branch will not be modified further")
        return
    
    if checker.hasErrors():
        if checker.fixErrors():
            if checker.hasMissingProperties():
                populateMissing(checker.missingRedefErrs, selected, options)
        else:
            gl.log("There are errors I don't know how to fix, cannot continue")
    
    else:
        #if checker.hasExtraProperties():
        #    checker.printExtra()
        populateMissing(checker.missingRedefErrs, selected, options)
    
    if isinstance(selected, Class) or isinstance(selected, Interface):
        inheritedOperations = SRUtils.getInheritedOperations(selected)
        for o in inheritedOperations:
            if len(SRUtils.getRedefinedInChild(o, selected)) == 0:
                cloneOperation(selected, o, options)
    
    addConstraints(selected)
    if 'mapping' not in options:
        if 'redefineOnly' not in options or not options['redefineOnly']:
            checkPartProperties(checker.missingRedefErrs, checker.inherited, selected, options)
    
    parents = []
    for r in selected.get_directedRelationshipOfSource():
        if isinstance(r, Generalization):
            parents.append(ModelHelper.getSupplierElement(r))
    if len(parents) > 0:
        parent = parents[0]
        if ModelHelper.getComment(selected) == '' or ModelHelper.getComment(selected) is None:
            ModelHelper.setComment(selected, ModelHelper.getComment(parent))

def addConstraints(child):
    allInherited = filter(lambda e: isinstance(e, Constraint), child.getInheritedMember())
    for c in allInherited:
        consIns = None
        if isinstance(c, DurationConstraint):
            consIns = ef.createDurationConstraintInstance() 
        elif isinstance(c, TimeConstraint):
            consIns = ef.createTimeConstraintInstance()
        elif isinstance(c, InteractionConstraint):
            pass
        elif isinstance(c, Constraint):
            consIns = ef.createConstraintInstance()
        if consIns is not None:
            spec = c.getSpecification()
            newspec = SRUtils.cloneValueSpec(spec)
            if newspec is not None:
                consIns.setSpecification(newspec)
            consIns.getConstrainedElement().add(child)
            consIns.setOwner(child)
    
def checkPartProperties(missing, attrs, child, options):
    for cattr in child.getOwnedAttribute():
        #if attr in missing:
        #    continue
        if (StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.partPropS) or cattr.getAggregation() == AggregationKindEnum.COMPOSITE) and cattr.getType() is not None:
            #c = SRUtils.getRedefinedInChild(attr, child)
            #if len(c) == 1:
                #ctype = c[0].getType()
                #ptype = attr.getType()
                #if ctype is not None and ptype is not None and isinstance(ptype, Class) and isinstance(ctype, Class):
                    #if ptype not in ctype.getSuperClass():
                        #MDUtils.createGeneralizationInstance(ptype, ctype)
                    #if ptype is not attr.getOwner() and ptype is not child and ptype not in SRUtils.getGeneralizationTree(child):
            if cattr.getType() is not child:
                generalize(cattr.getType(), options)
    
def populateMissing(attrs, child, options):
    if 'promptForParts' in options and options['promptForParts']:
        parts = filter(lambda e: StereotypesHelper.hasStereotypeOrDerived(e, SRUtils.partPropS) or (e.getAggregation() == AggregationKindEnum.COMPOSITE and e.getType() is not None and isinstance(e.getType(),Class)), attrs)
        notparts = filter(lambda e: e not in parts, attrs)
        selected = []
        if len(parts) > 0:
            selected = MDUtils.getUserCheckboxSelections("Choose parts", "Choose the ones you want under " + child.getName(), parts)
        notparts.extend(selected)
        newattrs = notparts
    else:
        newattrs = attrs
    for attribute in newattrs:
        name = SRUtils.getNameInChild(attribute.getName(), child)
        if name is not None and name.hasRedefinedProperty():
            r = []
            r.extend(name.getRedefinedProperty())
            r.append(attribute)
            if SRUtils.haveCommonRedefinition(r):
                name.getRedefinedProperty().add(attribute)
                continue
        #need to check if there's already an existing redefine for the case of multiple redefs
        cloneProp(child, attribute, options) #child doesn't already have it
       
    #for constraint in parent.getOwnedRule():
    #    consIns = ef.createConstraintInstance()
    #    consSpec = ef.createOpaqueExpressionInstance()
    #    consSpec.getBody().addAll(constraint.getSpecification().getBody())
    #    consIns.setSpecification(consSpec)
    #    consIns.getConstrainedElement().add(child)
    #    mem.addElement(consIns, child)

def cloneOperation(child, o, options):
    newo = ef.createOperationInstance()
    newo.setOwner(child)
    newo.setName(o.getName())
    MDUtils.copyStereotypes(o, newo)
    newo.getRedefinedOperation().add(o)
    for p in o.getOwnedParameter():
        cloneParameter(newo, p, options)
        
def cloneParameter(childo, p, options):
    newp = ef.createParameterInstance()
    newp.setName(p.getName())
    if p.getType() is not None:
        if 'mapping' in options and p.getType() in options['mapping']:
            newp.setType(options['mapping'][p.getType()])
        else:
            newp.setType(p.getType())
    newp.setOwner(childo)
    copyMultiplicity(p, newp)
    newp.setDirection(p.getDirection())
    MDUtils.copyStereotypes(p, newp)
    
def copyMultiplicity(prop, newprop):
    newprop.setLowerValue(SRUtils.cloneValueSpec(prop.getLowerValue()))
    newprop.setUpperValue(SRUtils.cloneValueSpec(prop.getUpperValue()))
    newprop.setUnique(prop.isUnique())
    newprop.setOrdered(prop.isOrdered())
    
def cloneProp(child, prop, options):
    mul = SRUtils.getMultiplicity(prop)
    if 'expandMultiplicity' in options:
        for i in range(mul):
            clonePropOnce(child, prop, options, i)
    else:
        clonePropOnce(child, prop, options, 0)
        

def clonePropOnce(child, prop, options, i):
    newProp = None
    if prop.getType() is not None and isinstance(prop.getType(), Enumeration):
        newProp = cloneValueProp(child, prop, options)
    elif StereotypesHelper.hasStereotypeOrDerived(prop, SRUtils.consPropS):
        newProp = cloneConstraintProp(child, prop, options)
    elif StereotypesHelper.hasStereotypeOrDerived(prop, SRUtils.valuePropS):
        newProp = cloneValueProp(child, prop, options)
    elif StereotypesHelper.hasStereotypeOrDerived(prop, SRUtils.partPropS):
        newProp = clonePartProp(child, prop, options)
    elif StereotypesHelper.hasStereotypeOrDerived(prop, SRUtils.sharedPropS):
        newProp = cloneSharedProp(child, prop, options)
    elif isinstance(prop, Port):
        newProp = clonePortProp(child, prop, options)
    elif StereotypesHelper.hasStereotypeOrDerived(prop, SRUtils.refPropS):
        newProp = cloneSharedProp(child, prop, options)
    else:
        newProp = cloneUMLProp(child, prop, options)
    if newProp is not None:
        mem.addElement(newProp, child)
        MDUtils.copyStereotypes(prop, newProp)
        MDUtils.setRedefine(prop, newProp)
        if i == 0:
            newProp.setName(prop.getName())
        else:
            newProp.setName(prop.getName() + str(i+1))
        newProp.setAggregation(prop.getAggregation())
        if 'expandMultiplicity' not in options:
            copyMultiplicity(prop, newProp)
        mem.addElement(newProp, child)
    
def cloneValueProp(child, prop, options):
    newType = prop.getType()
    if "mapping" in options and newType in options['mapping']:
        newType = options['mapping'][newType]
        
    if prop.getAssociation() is not None and newType is not None:
        newProp = makeAssociation(child, newType, prop, prop.getAssociation())
    else:
        newProp = ef.createPropertyInstance()
    #newProp = ef.createPropertyInstance()

    newProp.setType(newType)
    default = prop.getDefaultValue()
    if default is not None:
        newProp.setDefaultValue(SRUtils.cloneValueSpec(default))
    return newProp
    
def clonePartProp(child, prop, options):
    #if 'promptForParts' in options and options['promptForParts']:
    #    choice = JOptionPane.showConfirmDialog(None, "Make copy for " + prop.getName() + " under " + child.getName() + "?" , "Propagate part property?", JOptionPane.YES_NO_OPTION)
    #    if choice == JOptionPane.NO_OPTION:
    #        return None
                       
    newProp = None
    newType = None
    #what if type is already made and this is a new instance prop for same type??
    ptype = prop.getType()
    if ptype is prop.getOwner():
        gl.log("[WARNING]: self composition loop detected, only one level will be made: " + prop.getQualifiedName())
        return None
    if ptype is child or (ptype is not None and prop.getOwner() in SRUtils.getGeneralizationTree(ptype)):
        gl.log("[WARNING]: self composition and specialization loop detected, will not follow through: " + prop.getQualifiedName())
        return None
    if ptype is not None:
        if "mapping" in options and ptype in options['mapping']:
            newType = options['mapping'][ptype]
        if 'redefineOnly' in options and options['redefineOnly']:
            newType = ptype
        elif newType is None:
            if isinstance(ptype, Activity):
                newType = ef.createActivityInstance()
            elif isinstance(ptype, Enumeration):
                newType = ef.createEnumerationInstance()
            elif isinstance(ptype, Interface):
                newType = ef.createInterfaceInstance()
            elif isinstance(ptype, StateMachine):
                newType = ef.createStateMachineInstance()
            elif isinstance(ptype, Interaction):
                newType = ef.createInteractionInstace()
            elif isinstance(ptype, OpaqueBehavior):
                newType = ef.createOpaqueBehaviorInstance()
            else:
                newType = ef.createClassInstance()
            if 'useTypeName' in options and options['useTypeName']:
                newType.setName(ptype.getName())
            else:
                newType.setName(prop.getName())
            #if ptype.hasOwnedComment():
            #    ModelHelper.setComment(newType, ModelHelper.getComment(ptype))
            newType.setOwner(child)
            MDUtils.copyStereotypes(ptype, newType)
            MDUtils.createGeneralizationInstance(ptype, newType)
        #if 'mapping' in options: #breaks pakcage clone because it's iterating through mapping
        #    options['mapping'][ptype] = newType
            #generalize(newType, options) this gets done at the end in check part properties
    
    if prop.getAssociation() is not None:
        newProp = makeAssociation(child, newType, prop, prop.getAssociation())
    else:
        newProp = ef.createPropertyInstance()
    if newType is not None:
        newProp.setType(newType)
    default = prop.getDefaultValue()
    if default is not None:
        newProp.setDefaultValue(SRUtils.cloneValueSpec(default))
    return newProp

def cloneSharedProp(child, prop, options):
    newProp = None
    #if prop.getAssociation() is not None:
    #    newProp = self.makeAssociation(child, prop.getType(), prop)
    #else:
    if "mapping" in options and prop.getType() in options['mapping']:
        if prop.getAssociation() is not None:
            newProp = makeAssociation(child, options['mapping'][prop.getType()], prop, prop.getAssociation())
        else:
            newProp = ef.createPropertyInstance()
        newProp.setType(options['mapping'][prop.getType()])
    else:
        if prop.getAssociation() is not None:
            newProp = makeAssociation(child, prop.getType(), prop, prop.getAssociation())
        else:
            newProp = ef.createPropertyInstance()
        newProp.setType(prop.getType())
    return newProp
    
def clonePortProp(child, prop, options):
    newProp = ef.createPortInstance()
    if 'mapping' in options and prop.getType() in options['mapping']:
        newProp.setType(options['mapping'][prop.getType()])
    else:
        newProp.setType(prop.getType())
   
    if StereotypesHelper.hasStereotypeOrDerived(prop, SRUtils.flowPortS):
        StereotypesHelper.addStereotype(newProp, SRUtils.flowPortS)
        StereotypesHelper.setStereotypePropertyValue(newProp, SRUtils.flowPortS, 'direction', StereotypesHelper.getStereotypePropertyFirst(prop, SRUtils.flowPortS, 'direction'))
        StereotypesHelper.setStereotypePropertyValue(newProp, SRUtils.flowPortS, 'isAtomic', StereotypesHelper.getStereotypePropertyFirst(prop, SRUtils.flowPortS, 'isAtomic'))
    return newProp
    
def cloneUMLProp(child, prop, options): # this is if the property is not a sysml property
    #this needs to be checked, how to check if attr is uml value?
    if prop.getType() is not None and isinstance(prop.getType(), Enumeration):
        return cloneValueProp(child, prop, options)
    if prop.getAssociation() is not None or prop.getAggregation() != AggregationKindEnum.NONE:
        a = prop.getAggregation()
        if a == AggregationKindEnum.SHARED:
            return cloneSharedProp(child, prop, options)
        elif a == AggregationKindEnum.COMPOSITE:
            return clonePartProp(child, prop, options)
        else:
            return None #???
    else:
        return cloneValueProp(child, prop, options)
            
def cloneConstraintProp(child, prop, options): #this is for regular constraints as well as tree constraints, wires the new one according to the parent one, tree constraints will be redone later, but the result of the tree constraint will be connected here
    newProp = None
    con = prop.getType()
    if con is None:
        return ef.createPropertyInstance()
    if 'mapping' in options and con in options['mapping']:
        if prop.getAssociation() is not None:
            newProp = makeAssociation(child, options['mapping'][con], prop, prop.getAssociation())
        else:
            newProp = ef.createPropertyInstance()
        newProp.setType(options['mapping'][con])
    else:
        newProp = ef.createPropertyInstance()
        newProp.setType(con)
    return newProp
            
def makeAssociation(owner, oppowner, refprop, refasso): 
    asso = None
    if isinstance(refasso, AssociationClass):
        asso = ef.createAssociationClassInstance()
    else:
        asso = ef.createAssociationInstance()
    newProp = asso.getMemberEnd().get(0)
    newProp2 = asso.getMemberEnd().get(1)
    newProp2.setType(owner)
    #should also set aggregation or redefine?
    if refprop.getOpposite() is not None:
        MDUtils.copyStereotypes(refprop.getOpposite(), newProp2)
    if refprop.getOpposite()is not None and refprop.getOpposite().isNavigable() and oppowner is not None:
        newProp2.setName(refprop.getOpposite().getName())
        mem.addElement(newProp2, oppowner)
    else:
        mem.addElement(newProp2, asso)
    mem.addElement(asso, owner.getOwner())
    MDUtils.createGeneralizationInstance(refasso, asso)
    MDUtils.copyStereotypes(refasso, asso)
    if isinstance(asso, AssociationClass):
        asso.setName(refasso.getName())
        generalize(asso)
    return newProp
        

def checkParentTree(select):
    general = select.getGeneral()
    for g in select.getGeneral():
        checker = ValidateStructure.SRChecker(g, {'checkOnly':True})
        checker.checkAttrs()
        if checker.hasErrors() or checker.partHasErr:
            checker.printErrors()
            return False
    return True

def editable(e):
    if not e.isEditable():
        return False
    for o in e.getOwnedElement():
        if not editable(o):
            return False
    return True

def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, Class) and not isinstance(selected, Interface):
        gl.log("You must select a block/class!!!")
        return
    if not editable(selected):
        gl.log("Selected is not editable all the way!!!")
        return
    try:
        SessionManager.getInstance().createSession("specialize")
        options = {'checkOnly':False}
        mul = JOptionPane.showConfirmDialog(None, "Expand Multiplicities?", "Expand Multiplicities?", JOptionPane.YES_NO_OPTION);
        if mul == JOptionPane.YES_OPTION:
            options['expandMultiplicity'] = True
        promptParts = JOptionPane.showConfirmDialog(None, "Prompt for part properties?", "Prompt for part properties?", JOptionPane.YES_NO_OPTION);
        if promptParts == JOptionPane.YES_OPTION:
            options['promptForParts'] = True
        redefineOnly = JOptionPane.showConfirmDialog(None, "Redefine Only? (Do not make new blocks but set the type of everything to be the parent type)", "Redefine Only?", JOptionPane.YES_NO_OPTION);
        if redefineOnly == JOptionPane.YES_OPTION:
            options['redefineOnly'] = True
        else:
            useTypeName = JOptionPane.showConfirmDialog(None, "Use type name for new blocks? (instead of property name)", "Propagating part properties", JOptionPane.YES_NO_OPTION);
            if useTypeName == JOptionPane.YES_OPTION:
                options['useTypeName'] = True
        if checkParentTree(selected):   
            generalize(selected, options)
        else:
            choice = JOptionPane.showConfirmDialog(None, "There are errors in the generalization tree above, continue with the update?", "Continue?", JOptionPane.YES_NO_OPTION);
            if choice == JOptionPane.YES_OPTION:
                generalize(selected, options)
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
