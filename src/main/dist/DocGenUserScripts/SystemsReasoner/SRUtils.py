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

from javax.swing import JOptionPane


from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

from MDUtils import _MDUtils as MDUtils
reload(MDUtils)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

partPropS = StereotypesHelper.getStereotype(project, 'PartProperty')
valuePropS = StereotypesHelper.getStereotype(project, 'ValueProperty')
consPropS = StereotypesHelper.getStereotype(project, 'ConstraintProperty')
sharedPropS = StereotypesHelper.getStereotype(project, 'SharedProperty')
refPropS = StereotypesHelper.getStereotype(project, 'ReferenceProperty')
bindingS = StereotypesHelper.getStereotype(project, 'BindingConnector')
nestedEndS = StereotypesHelper.getStereotype(project, 'NestedConnectorEnd')
consParamS = StereotypesHelper.getStereotype(project, 'ConstraintParameter')
consBlockS = StereotypesHelper.getStereotype(project, 'ConstraintBlock')
flowPortS = StereotypesHelper.getStereotype(project, 'FlowPort')

def setMultiplicity(param, l, u):
    lower = ef.createLiteralIntegerInstance()
    lower.setValue(l)
    param.setLowerValue(lower)
    upper = ef.createLiteralUnlimitedNaturalInstance()
    upper.setValue(u)
    param.setUpperValue(upper)


def getMultiplicity(attr):
    lower = attr.getLower()
    upper = attr.getUpper()
    if lower == upper:
        return lower
    if upper == -1:
        if lower > 0:
            return lower
    return 1

def cloneValueSpec(valueSpec):
    #just do literal string for now
    v = None
    if isinstance(valueSpec, Duration):
        v = ef.createDurationInstance()
        v.setExpr(cloneValueSpec(valueSpec.getExpr()))
    elif isinstance(valueSpec, DurationInterval):
        v = ef.createDurationIntervalInstance()
        v.setMax(cloneValueSpec(valueSpec.getMax()))
        v.setMin(cloneValueSpec(valueSpec.getMin()))
    elif isinstance(valueSpec, ElementValue):
        v = ef.createElementValueInstance()
        v.setElement(valueSpec.getElement())
    #elif isinstance(valueSpec, Expression):
    #    v = self.ef.createExpressionInstance()
    #    v.setSymbol(valueSpec.getSymbol())
    elif isinstance(valueSpec, InstanceValue):
        v = ef.createInstanceValueInstance()
        v.setInstance(valueSpec.getInstance())
    #elif isinstance(valueSpec, Interval):
    #    v = self.ef.createIntervalInstance()
    #    v.setMax(self.cloneDefaultValue(valueSpec.getMax()))
    #    v.setMin(self.cloneDefaultValue(valueSpec.getMin()))
    elif isinstance(valueSpec, LiteralBoolean):
        v = ef.createLiteralBooleanInstance()
        v.setValue(valueSpec.isValue())
    elif isinstance(valueSpec, OpaqueExpression):
        v = ef.createOpaqueExpressionInstance()
        v.getBody().addAll(valueSpec.getBody())
        v.getLanguage().addAll(valueSpec.getLanguage())
        v.setBehavior(valueSpec.getBehavior())
    elif isinstance(valueSpec, LiteralNull):
        v = ef.createLiteralNullInstance()
    elif isinstance(valueSpec, LiteralInteger):
        v = ef.createLiteralIntegerInstance()
        v.setValue(valueSpec.getValue())
    elif isinstance(valueSpec, LiteralUnlimitedNatural):
        v = ef.createLiteralUnlimitedNaturalInstance()
        v.setValue(valueSpec.getValue())
    #elif isinstance(valueSpec, StringExpression):
    #    pass
    elif isinstance(valueSpec, TimeExpression):
        v = ef.createTimeExpressionInstance()
        v.setExpr(cloneValueSpec(valueSpec.getExpr()))
    elif isinstance(valueSpec, TimeInterval):
        v = ef.createTimeIntervalInstance()
        v.setMax(cloneValueSpec(valueSpec.getMax()))
        v.setMin(cloneValueSpec(valueSpec.getMin()))
    elif isinstance(valueSpec, LiteralString):
        v = ef.createLiteralStringInstance() 
        v.setValue(valueSpec.getValue())
    elif isinstance(valueSpec, LiteralReal):
        v = ef.createLiteralRealInstance()
        v.setValue(valueSpec.getValue())
    return v

def setNewValue(prop, value):
    #value is a string
    valueSpec = prop.getDefaultValue()
    v = None
    #if isinstance(valueSpec, ElementValue):
    #    v = ef.createElementValueInstance()
    #    v.setElement(valueSpec.getElement())
    #elif isinstance(valueSpec, Expression):
    #    v = self.ef.createExpressionInstance()
    #    v.setSymbol(valueSpec.getSymbol())
    #elif isinstance(valueSpec, InstanceValue):
    #    v = ef.createInstanceValueInstance()
    #    v.setInstance(valueSpec.getInstance())
    #elif isinstance(valueSpec, Interval):
    #    v = self.ef.createIntervalInstance()
    #    v.setMax(self.cloneDefaultValue(valueSpec.getMax()))
    #    v.setMin(self.cloneDefaultValue(valueSpec.getMin()))
    if isinstance(valueSpec, LiteralBoolean):
        v = ef.createLiteralBooleanInstance()
        if value == 'false' or value == 'False' or value == 'F' or value == 'f' or value == 'no' or value == '' or value == 'n':
            v.setValue(False)
        else:
            v.setValue(True)
    #elif isinstance(valueSpec, OpaqueExpression):
    #    v = self.ef.createOpaqueExpressionInstance()
    #    v.getBody().addAll(valueSpec.getBody())
    #    v.getLanguage().addAll(valueSpec.getLanguage())
    #    v.setBehavior(valueSpec.getBehavior())
    #elif isinstance(valueSpec, LiteralNull):
    #    v = ef.createLiteralNullInstance()
    #elif isinstance(valueSpec, LiteralSpecification):
    #    pass
    elif isinstance(valueSpec, LiteralInteger):
        v = ef.createLiteralIntegerInstance()
        v.setValue(int(round(float(value))))
    elif isinstance(valueSpec, LiteralUnlimitedNatural):
        v = ef.createLiteralUnlimitedNaturalInstance()
        v.setValue(int(round(float(value))))
    #elif isinstance(valueSpec, StringExpression):
    #    pass
    #elif isinstance(valueSpec, TimeExpression):
    #    v = self.ef.createTimeExpressionInstance()
    #    v.setExpr(self.cloneDefaultValue(valueSpec.getExpr()))
    #elif isinstance(valueSpec, TimeInterval):
    #    v = self.ef.createTimeIntervalInstance()
    #    v.setMax(self.cloneDefaultValue(valueSpec.getMax()))
    #    v.setMin(self.cloneDefaultValue(valueSpec.getMin()))
    elif isinstance(valueSpec, LiteralString):
        v = ef.createLiteralStringInstance() 
        v.setValue(value)
    else:
        v = ef.createLiteralStringInstance()
        v.setValue(value)
    prop.setDefaultValue(v)   

def getNonRedefinedAttrs(e):
    '''takes an element, returns list of its attributes, includes property sets'''
    res = []
    for a in e.getOwnedAttribute():
        if not a.hasRedefinedProperty(): ##added special case for property sets, this can be bad
            res.append(a)
            if StereotypesHelper.hasStereotype(a, partPropS) and a.getType() is not None:
                res.extend(getNonRedefinedAttrs(a.getType()))
    return res



def findNameInChild(name, child, propset): 
    '''returns first property found in child, propset indicates whether to search in property sets'''
    res = None
    if name == "":
        return res 
    for p in child.getOwnedAttribute():
        if p.getName() == name:
            return p
    if propset:
        for p in child.getOwnedAttribute():
            if StereotypesHelper.hasStereotype(p, partPropS):
                if p.getType() is not None:
                    res = findNameInChild(name, p.getType(), propset)
    return res

def findAllNameInChild(name, child, propset):
    '''returns all properties with name in child as list, propset indicates whether to search in property sets'''
    res = []
    if name == "":
        return res
    for p in child.getOwnedAttribute():
        if p.getName() == name:
            res.append(p)
    if propset:
        for p in child.getOwnedAttribute():
            if StereotypesHelper.hasStereotype(p, partPropS):
                if p.getType() is not None:
                    res.extend(findAllNameInChild(name, p.getType(), propset))
    return res
    
def findNameInChildWithParts(name, child, propset, parts):
    '''returns a dictionary of property as key and list of parts to get to it, 
    initial parts list should be given, propset indicates whether to search in property sets'''
    res = {}
    if name == '':
        return res
    for p in child.getOwnedAttribute():
        if p.getName() == name:
            res[p] = parts
            return res
    if propset:
        for p in child.getOwnedAttribute():
            if StereotypesHelper.hasStereotype(p, partPropS):
                if p.getType() is not None:
                    newparts = list(parts) #make a new copy of the parts list so it won't get changed in recursive calls!!!
                    newparts.append(p)
                    res.update(findNameInChildWithParts(name, p.getType(), propset, newparts))
    return res
        
def findRedefinedInChild(prop, child, propset):
    '''returns first property in child that redefines prop'''
    res = MDUtils.findRedefinedInChild(prop, child)
    if res is not None:
        return res
    if propset:
        for cprop in child.getOwnedAttribute():
            if StereotypesHelper.hasStereotype(cprop, partPropS):
                if cprop.getType() is not None:
                    res = MDUtils.findRedefinedInChild(prop, cprop.getType())
                    if res is not None:
                        return res
    return None            
    
def findRedefinedInChildWithParts(prop, child, propset, parts):
    '''returns a dictionary of properties as keys and list of parts to get to it,
    initial parts list should be given, propset indicates whether to search in property sets'''
    res = {}
    redef = MDUtils.findRedefinedInChild(prop, child)
    if redef is not None:
        res[redef] = parts
        return res
    if propset:
        for cprop in child.getOwnedAttribute():
            if StereotypesHelper.hasStereotype(cprop, partPropS):
                if cprop.getType() is not None:
                    newparts = list(parts)
                    newparts.append(cprop)
                    res.update(findRedefinedInChildWithParts(prop, cprop.getType(), propset, newparts))
    return res

def findAllRedefinedInChild(prop, child):
    '''returns a list of all properties in child that redefines prop, includes property sets'''
    res = []
    for attr in child.getOwnedAttribute():
        if attr.hasRedefinedProperty():
            for redef in attr.getRedefinedProperty():
                if redef is prop:
                    res.append(attr)
        if StereotypesHelper.hasStereotype(attr, partPropS) and attr.getType() is not None:
            res.extend(findAllRedefinedInChild(prop, attr.getType()))
    return res

def findAllRedefinedInChild2(prop, child):
    '''returns a list of all properties in child that redefines prop, includes property sets'''
    res = []
    for attr in child.getOwnedAttribute():
        if attr.hasRedefinedProperty():
            for redef in attr.getRedefinedProperty():
                if redef is prop:
                    res.append(attr)
        #if StereotypesHelper.hasStereotype(attr, partPropS) and attr.getType() is not None and StereotypesHelper.hasStereotype(attr.getType(), propSetS):
         #   res.extend(findAllRedefinedInChild(prop, attr.getType()))
    return res

def findPropertyInChild(child, propset, s = None):
    '''returns list of properties in child that has stereotype s, propset indicates whether to include property sets'''
    res = []
    for attr in child.getOwnedAttribute():
        if s is not None and StereotypesHelper.hasStereotype(attr, s):
            res.append(attr)
        elif s is None:
            res.append(attr)
        if propset and StereotypesHelper.hasStereotype(attr, partPropS) and attr.getType() is not None:
            res.extend(findPropertyInChild(attr.getType(), propset, s))
    return res


                
def getChildren(e):
    '''returns a list of the types of all compositional children of e (excluding property sets)'''
    res = []
    for p in e.getOwnedAttribute():
        if StereotypesHelper.hasStereotype(p, partPropS):
            t = p.getType()
            if t is not None:
                res.append(t)
    return res


#######################################################################################
def getChildrenWithParts(e):
    '''returns a dictionary with the types of all compositional children of e as keys (excluding property sets) and their corresponding part property in e'''
    res = {}
    for p in e.getOwnedAttribute():
        if StereotypesHelper.hasStereotype(p, partPropS):
            t = p.getType()
            if t is not None:
                res[p] = t
    return res

def getRedefinedInChild(prop, child):
    '''
    returns all properties in child found to redefine prop as a list'''
    res = []
    for attr in prop.get_redefinableElementOfRedefinedElement():
        if attr.getOwner() == child:
            res.append(attr)
    return res


def getNameInChild(name, child):
    res = None
    for attr in child.getOwnedAttribute():
        if attr.getName() == name:
            res = attr
    return res

def getGeneralizationTree(e, tree=None):
    '''returns a map of element to list of elements it specializes, starting from e'''
    res = tree
    if not res:
        res = {}
    parents = []
    for r in e.get_directedRelationshipOfSource():
        if isinstance(r, Generalization):
            parents.append(ModelHelper.getSupplierElement(r))
    #if isinstance(e, Class):
    #    parents = e.getSuperClass()
    res[e] = parents
    for super in parents:
        getGeneralizationTree(super, res)
    return res

def getAllInheritedAttributes(e):
    '''returns list of all inherited members of e except those not a property'''
    res = []
    res.extend(e.getInheritedMember())
    for i in e.getInheritedMember():
        if not isinstance(i, Property):
            res.remove(i)
    return res

def getAllInheritedOperations(e):
    res = []
    res.extend(e.getInheritedMember())
    for i in e.getInheritedMember():
        if not isinstance(i, Operation):
            res.remove(i)
    return res

def getRedefinitionTree(genTree, c=None):
    '''given generalization tree from getGeneralizationTree, returns map of property in all classes to list of property it redefines, excludes properties in c'''
    res = {}
    for e in genTree:
        if c == e:
            continue
        for p in e.getOwnedAttribute():
            res[p] = p.getRedefinedProperty()
    return res

def getInheritedOperations(e):
    all = getAllInheritedOperations(e)
    gentree = getGeneralizationTree(e)
    redeftree = {}
    for i in gentree:
        if e is i:
            continue
        for o in i.getOwnedOperation():
            redeftree[o] = o.getRedefinedOperation()
    for o in redeftree:
        for redef in redeftree[o]:
            if redef in all:
                all.remove(redef)
    return all
    
def getInheritedAttributes(allInherited, redefTree):
    '''returns list of attributes from allInherited that's the 'leaf' level inheritance, based on redefTree'''
    res = []
    res.extend(allInherited)
    for p in redefTree:
        for redef in redefTree[p]:
            if redef in res:
                res.remove(redef)
    return res

def getAllRedefs(p, redefSet=None):
    s = redefSet
    if redefSet is None:
        s = set()
    for redefProp in p.getRedefinedProperty():
        s.add(redefProp)
        getAllRedefs(redefProp, s)
    return s
    
def haveCommonRedefinition(redefs):
    #m = []
    #for redef in redefs:
    #    m.append(getAllRedefs(redef))
    #if len(m) > 1 and len(m[0].intersection(*tuple(m[1:]))) == 0:
    #    return False
    #name = redefs[0].getName()
    #for redef in redefs:
    #    if redef.getName() != name:
    #        return False
    return True

def multipleRedef(redefs, should):
    name = redefs[0].getName()
    
    for redef in redefs:
        if redef not in should:
            return False
        if redef.getName() != name:
            return False
    return True