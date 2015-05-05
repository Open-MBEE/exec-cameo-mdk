'''
Created on Nov 3, 2010

@author: dlam
'''
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
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port

from javax.swing import JOptionPane
from javax.swing import JCheckBox
from jarray import array

project         =  Application.getInstance().getProjectsManager().getActiveProject()
BlockStereotype               = StereotypesHelper.getStereotype(project,"Block")
ConstraintBlockStereotype     = StereotypesHelper.getStereotype(project, 'ConstraintBlock')

PartPropertyStereotype        = StereotypesHelper.getStereotype(project, 'PartProperty')
ValuePropertyStereotype       = StereotypesHelper.getStereotype(project, 'ValueProperty')
ConstraintPropertyStereotype  = StereotypesHelper.getStereotype(project, 'ConstraintProperty')
SharedPropertyStereotype      = StereotypesHelper.getStereotype(project, 'SharedProperty')
ReferencePropertyStereotype   = StereotypesHelper.getStereotype(project, 'ReferenceProperty')

BindingConnectorStereotype    = StereotypesHelper.getStereotype(project, 'BindingConnector')
NestedConnectorEndStereotype  = StereotypesHelper.getStereotype(project, 'NestedConnectorEnd')

ConstraintParameterStereotype = StereotypesHelper.getStereotype(project, 'ConstraintParameter')
FlowPortStereotype            = StereotypesHelper.getStereotype(project, 'FlowPort')

RealValueType    = filter(lambda element: element.getName() == 'Real',
                          ModelHelper.getElementsOfType(project.getModel(), [DataType], False))[0]
IntegerValueType = filter(lambda element: element.getName() == 'Integer',
                          ModelHelper.getElementsOfType(project.getModel(), [DataType], False))[0]
BooleanValueType = filter(lambda element: element.getName() == 'Boolean',
                          ModelHelper.getElementsOfType(project.getModel(), [DataType], False))[0]

TrueStrings  = ['true', 't']
FalseStrings = ['false', 'f']

guiLog          = Application.getInstance().getGUILog()
elementsFactory = project.getElementsFactory()

def getRealizedInterfaces(cs):
    """
    get the interfaces that given model elements realize (Interface Realization)
    params:
        a list of model elements
    returns:
        a list of interfaces, empty list if nothing found"""
    i = []
    for c in cs:
        for r in c.get_directedRelationshipOfSource():
            if isinstance(r, InterfaceRealization):
                i.append(ModelHelper.getSupplierElement(r))
    return i

def getInfoFlows(source, target):
    """
    get the conveyed information from source to target
    assumes there's at most one information flow for a given source and target
    uses the first information flow it finds otherwise
    params:
        source model element
        target model element
    returns:
        a list of conveyed information model elements, empty list if none"""
    for r in source.get_directedRelationshipOfSource():
        if isinstance(r, InformationFlow):
            if ModelHelper.getSupplierElement(r) == target:
                return r.getConveyed()
    return []

def getInfoFlowsConveyed(e, out):
    '''
    params:
        a model element
        boolean: direction
    returns:
        information conveyed on information flows into or out of element (toggle by out)'''
    all = []
    if not out:
        for r in e.get_directedRelationshipOfTarget():
            if isinstance(r, InformationFlow):
                for i in r.getConveyed():
                    if i not in all:
                        all.append(i)
    else:
        for r in e.get_directedRelationshipOfSource():
            if isinstance(r, InformationFlow):
                for i in r.getConveyed():
                    if i not in all:
                        all.append(i)
    return all

def expandGeneralizations(es):
    res = []
    for e in es:
        res.append(e)
        res.extend(collectGeneralizations(e, True))
        res.extend(collectGeneralizations(e, False))
    return res

def collectRelationshipEndsByStereotype(source, s, supplier):
    """
    get related elements related with given stereotype (includes derived)
    params:
        a model element
        a stereotype element
        boolean: whether to treat model element as source or target of relationship
    returns:
        list of model elements, empty list if none"""
    relevant = []
    rs = None
    if supplier:
        rs = source.get_directedRelationshipOfTarget()
    else:
        rs = source.get_directedRelationshipOfSource()
    for r in rs:
        if StereotypesHelper.hasStereotypeOrDerived(r, s):
            if supplier:
                relevant.append(ModelHelper.getClientElement(r))
            else:
                relevant.append(ModelHelper.getSupplierElement(r))
    return relevant

def collectManyRelationshipEndsByStereotype(sources, s, supplier):
    """
    get related elements related with given stereotype (includes derived)
    params:
        list of model elements
        a stereotype element
        boolean: whether to treat model elements as source or target of relationship
    returns:
        list of model elements, empty list if none"""
    alls = []
    for e in sources:
        blah = collectRelationshipEndsByStereotype(e, s, supplier)
        for b in blah:
            if b not in alls:
                alls.append(b)
    return alls

# internal func
def _collectRecursivePartsElements(source, alls):
    #alls.append(source)
    parts = source.getPart()
    for p in parts:
        t = p.getType()
        if t is not None:
            alls.append(t)
            _collectRecursivePartsElements(t, alls)

def collectPartElementsByStereotypes(source, filters):
    """
    get all children of model element by composition recursively (not including model element) with given stereotypes (including derived)
    params:
        a model element (has to be subclass of StructuredClassifier)
        list of stereotype elements
    returns:
        list of children that fits stereotypes, or empty list"""
    alls = []
    _collectRecursivePartsElements(source, alls)
    return filterElementsByStereotypes(alls, filters)

def collectRecursivePartsElements(source):
    """
    get all children of model element by composition recursively (not including model element)
    params:
        a model element (has to be subclass of StructuredClassifier)
        list of stereotype elements
    returns:
        list of children or empty list"""
    alls = []
    _collectRecursivePartsElements(source, alls)
    return alls

# input is a list of elements, stereotype is a stereotype element
def filterElementsByStereotypes(sourceList, stereotypes):
    ret = []
    for s in sourceList:
        for t in stereotypes:
            if StereotypesHelper.hasStereotypeOrDerived(s, t) and s not in ret:
                ret.append(s)
    return ret

def filterElementsByStereotypedType(elements, stereotype):
    return filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element.getType(), stereotype),
                  elements)

# internal func
def _collectRecursiveOwnedElements(source, alls):
    if not isinstance(source, Package):
        return
    owned = source.getOwnedElement()
    alls.extend(owned)
    for e in owned:
        _collectRecursiveOwnedElements(e, alls)

def collectElementsByStereotypes(source, filters):
    """
    get all owned elements of model element (diagram or package) with given stereotypes (including derived)
    if source is diagram, gives all model elements on that diagram (not presentation elements)
    params:
        a model element (can be diagram or Package)
        list of stereotype elements
    returns:
        list of elements that fits stereotypes, or empty list"""
    alls = []
    if isinstance(source, Diagram):
        project = Application.getInstance().getProject()
        alls.extend(project.getDiagram(source).getUsedModelElements(False))
    else:
        _collectRecursiveOwnedElements(source, alls)
    return filterElementsByStereotypes(alls, filters)

# internal func
def _collectGeneralizationsRecursive(source, down, alls):
    rs = None
    if down:
        rs = source.get_directedRelationshipOfTarget()
    else:
        rs = source.get_directedRelationshipOfSource()
    for r in rs:
        if isinstance(r, Generalization):
            if down:
                alls.append(ModelHelper.getClientElement(r))
                _collectGeneralizationsRecursive(ModelHelper.getClientElement(r), down, alls)
            else:
                alls.append(ModelHelper.getSupplierElement(r))
                _collectGeneralizationsRecursive(ModelHelper.getSupplierElement(r), down, alls)

def collectGeneralizations(source, down):
    """
    get all generalize related elements from model element, regardless of depth
    params:
        model element
        boolean: more specific or more general
    returns:
        list of generalization elements"""
    relevant = []
    _collectGeneralizationsRecursive(source, down, relevant)
    return relevant

def getUserSelections(types, root, multiple, name='Select element(s)', display=None):
    """
    shows a selection box
    params:
        a list of model metaclasses selectable (this is the actual class from imports)
          note to display nested elements (for example, pins on actions), you have to provide
          all metaclasses that owns those elements, and they have to be "concrete" classes (as shown in MD's spec dialog)
          ex. to show pins as selectables: give [Activity, CallBehaviorAction, OutputPin, InputPin]
              [Activity, Action, Pin] WILL NOT WORK!!!
        root of the selection (usually just project.getModel() to display the entire model tree)
        boolean multiple: whether can select multiple
    returns:
        if multiple is false, returns selected element
        if multiple is true, returns list of selected elements
        if user didn't click ok, returns None"""
    a = SelectElementTypes(display,types)
    #SelectElementType(display, select)
    b = SelectElementInfo(False, True, True, True)
    b.root = root
    b.showDiagrams = False
    b.showNone = False
    b.sortable = True
    z = None
    if not multiple:
        z = SelectElementDlg(MDDialogParentProvider.getProvider().getDialogParent(), None, a, b)
        z.setTitle(name)
    else:
        z = SelectElementsDlg(MDDialogParentProvider.getProvider().getDialogParent(), a, b, False, False, None)
        z.setTitle(name)
    z.setVisible(True)
    if z.isOk():
        return z.getSelected()
    return None

def getUserDropdownSelection(title, message, selectionElements):
    elementStrings = [e.getQualifiedName() for e in selectionElements]
    input = JOptionPane.showInputDialog(None, message, title, JOptionPane.PLAIN_MESSAGE, None, elementStrings, None)
    if input is not None:
        index = elementStrings.index(input)
        return selectionElements[index]
    return input

def getUserCheckboxSelections(title, message, selectionElements):
    elementStrings = [e.getName() for e in selectionElements]
    checkboxes = [JCheckBox(e) for e in elementStrings]
    l = [message]
    l.extend(checkboxes)
    
    input = JOptionPane.showMessageDialog(None, array(l, Object), title, JOptionPane.QUESTION_MESSAGE)
    checked = [e.isSelected() for e in checkboxes]
    res = []
    i = 0
    for check in checked:
        if check:
            res.append(selectionElements[i])
        i = i+1
    return res   

def getConnectedPins(pin, out=True):
    """
    gets the pins this is connected to (in the context of one activity diagram or in one activity)
    params: 
        an input or output pin (can also be fork/join or decision/merge node, or parameter node)
        optional boolean if fork/join or decision/merge: True is find all pins connected from the node (default), false is pins to node
    returns:
        list of pin elements or parameter nodes"""
    pins = []
    #todo: take care of actiivty parameter node that are INOUT or RETURN? account for circular loops
    if isinstance(pin, InputPin) or (isinstance(pin, ControlNode) and not out) or (isinstance(pin, ActivityParameterNode) and pin.getParameter().getDirection() == ParameterDirectionKindEnum.OUT):
        for f in pin.getIncoming():
            p = f.getSource()
            if isinstance(p, ControlNode):
                pins.extend(getConnectedPins(p, False))
            else: #what if activity param node?
                pins.append(p)
    elif isinstance(pin, OutputPin) or (isinstance(pin, ControlNode) and out) or (isinstance(pin, ActivityParameterNode) and pin.getParameter().getDirection() == ParameterDirectionKindEnum.IN):
        for f in pin.getOutgoing():
            p = f.getTarget()
            if isinstance(p, ControlNode):
                pins.extend(getConnectedPins(p, True))
            else:
                pins.append(p)
    return pins
    
def getBaseOfPin(pin):
    """
    gets the lowest level pin(s) connected to the given pin
    ex. given a pin on an action, find its parameter/parameter node if available 
      and drill down recursively to see what the lowest level pins it represents,
      this can be more than one due to fork and decision nodes
    input:
        a pin 
    returns:
        a list of the lowest level pins, if pin given is already lowest level, returns given pin in a list"""
    param = pin.getParameter()
    if param is None:
        return [pin]
    paramNode = param.get_activityParameterNodeOfParameter()
    if not paramNode.isEmpty():
        paramNode = paramNode.iterator().next()
    else:
        return [pin]
    paramNodeConnected = getConnectedPins(paramNode)
    if len(paramNodeConnected) == 0:
        return [pin]
    else:
        collect = []
        for p in paramNodeConnected:
            collect.extend(getBaseOfPin(p))
        return collect

def getAllRelatedParamNodePins(param, excludes=[]):
    """
    gets all the related parameter nodes, pins, and parameters related to input parameter
    This will trace from the input parameter, to its pin instances, its parameter nodes, to all the other
    pins and nodes and their parameters that this is connected to. In other words, everything that's 
    related to input parameter regardless of degree of separation
    input:
        parameter of an activity
        optional: list of parameters to exclude
    returns:
        list of pins, activity parameter nodes, and parameters"""
    res = []
    newExcludes = list(excludes)
    newExcludes.append(param)
    paramPins = param.get_pinOfParameter()
    for pin in paramPins:
        related = getConnectedPins(pin)
        for r in related:
            rparam = r.getParameter()
            if rparam is not None and rparam not in excludes:
                res.extend(getAllRelatedParamNodePins(rparam, newExcludes))
                res.append(rparam)
    paramNodes = param.get_activityParameterNodeOfParameter()
    for node in paramNodes:
        related = getConnectedPins(node)
        for r in related:
            rparam = r.getParameter()
            if rparam not in excludes:
                res.extend(getAllRelatedParamNodePins(rparam, newExcludes))
                res.append(rparam)
    res.extend(paramPins)
    res.extend(paramNodes)
    return res

def getContainingActivities(act):
    """
    shows where an activity is used as an action in all levels
    input:
        activity
    returns:
        list of activities that contain an instance of given activity
        empty list if none"""
    returns = []
    calls = act.get_callBehaviorActionOfBehavior()
    for c in calls:
        returns.append(c.getActivity())
    return returns

def getActivityDiagram(act):
    '''
    input:
        activity
    returns:
        the first diagram element found in activity, None if none'''
    for e in act.getOwnedElement():
        if isinstance(e, Diagram):
            return e
    return None
        

def intersectionOfLists(a, b):
    #return list(set(a).intersection(set(b)))
    r = []
    for i in a:
        if i in b:
            r.append(i)
    return r
    
def getNonRedefinedAttrs(e):
    ref = []
    for a in e.getOwnedAttribute():
        if not a.hasRedefinedProperty():
            ref.append(a)
    return ref

def getRedefinedAttrs(e):
    ref = []
    for a in e.getOwnedAttribute():
        if a.hasRedefinedProperty():
            ref.append(a)
    return ref

def setRedefine(parent, child):
    redefs = child.getRedefinedProperty()
    if isinstance(child, Port):
        redefs = child.getRedefinedPort()
    redefs.clear()
    redefs.add(parent)


def findRedefinedInChild(prop, child):
    '''
    returns the first property in child found to redefine prop'''
    for cprop in child.getOwnedAttribute():
        if cprop.hasRedefinedProperty():
            for r in cprop.getRedefinedProperty():
                if r is prop:
                    return cprop
    return None

def findAllRedefinedInChild(prop, child):
    '''
    returns all properties in child found to redefine prop as a list'''
    res = []
    for attr in child.getOwnedAttribute():
        if attr.hasRedefinedProperty():
            for redef in attr.getRedefinedProperty():
                if redef is prop:
                    res.append(attr)
    return res

def findConstraintPartBindings(conpart):
    '''
    for a given part, returns a map of ports to a map of connectors to the connected in the context of this part
    input:
        a part (this was originally written for sysml constraint parts, but should work for any part)
    returns:
        format: {port: {connector: opposite_role, ...}, ...}'''
    bindings = {}
    con = conpart.getType()
    if con is None:
        return bindings
    for p in con.getOwnedPort():
        connectors = {}
        for cend in p.getEnd():
            if cend.getPartWithPort() is conpart:
                connector = cend.get_connectorOfEnd()
                ends = connector.getEnd()
                if ends.size() == 2:
                    iterator = ends.iterator()
                    end1 = iterator.next()
                    if end1 is not cend:
                        connectors[connector] = end1.getRole()
                    else:
                        connectors[connector] = iterator.next().getRole()
                else:
                    pass
                    #connector have to have 2 ends?!
        bindings[p] = connectors
    return bindings
    
def copyStereotypes(a, b):
    for s in StereotypesHelper.getStereotypes(a):
        if not StereotypesHelper.hasStereotypeOrDerived(b, s):
            StereotypesHelper.addStereotype(b, s)
        smap = StereotypesHelper.getPropertiesIncludingParents(s)
        for sp in smap.keySet():
            props = smap.get(sp)
            for p in props:
                values = StereotypesHelper.getStereotypePropertyValue(a, s, p.getName())
                if len(values) == 1:
                    StereotypesHelper.setStereotypePropertyValue(b, s, p.getName(), values.get(0))
                elif len(values) > 1:
                    StereotypesHelper.setStereotypePropertyValue(b, s, p.getName(), values)


def createClass(name, owner):
    new = createOwnedElement(elementsFactory.createClassInstance, name, owner)
    return new

def createStereotypedClass(name, owner, stereotypes):
    new = createClass(name, owner)
    addStereotypes(new, stereotypes)
    return new

def createPackage(name, owner):
    new = createOwnedElement(elementsFactory.createPackageInstance, name, owner)
    return new

def createStereotypedPackage(name, owner, stereotypes):
    new = createOwnedElement(elementsFactory.createPackageInstance, name, owner)
    addStereotypes(new, stereotypes)
    return new

def createDependency(name, owner, client, supplier):
    new = createOwnedElement(elementsFactory.createDependencyInstance, name, owner)
    ModelHelper.setSupplierElement(new, supplier)
    ModelHelper.setClientElement(new, client)
    return new

def createStereotypedDependency(name, owner, client, supplier, stereotypes):
    new = createDependency(name, owner, client, supplier)
    addStereotypes(new, stereotypes)
    return new

def addStereotypes(element, stereotypes):
    for stereotype in stereotypes:
        StereotypesHelper.addStereotype(element, stereotype)
        
def createOwnedElement(createFunction, name, owner):
    new = createFunction()
    new.setOwner(owner)
    new.setName(name)
    return new

def findPackages(name, parentPackage):
    return filter(lambda x: x.getName() == name, parentPackage.getNestedPackage())

def removePackage(package):
    ModelElementsManager.getInstance().removeElement(package)

def removePackages(packages):
    for package in packages:
        removePackage(package)
        
def getOwnedElementsByStereotype(parentElement, stereotype = "Block"):
    return filter(lambda element: StereotypesHelper.hasStereotype(element, stereotype), parentElement.getOwnedElement())

# Given an element, it returns all elements owned by the element that have the specified stereotypes
def getOwnedElementsByStereotypes(parentElement, stereotypes):
    return filter(lambda element: all(StereotypesHelper.hasStereotype(element, stereotype) \
                                      for stereotype in stereotypes), 
                  parentElement.getOwnedElement())

def getOwnedElementsByAnyStereotypes(parentElement, stereotypes):
    return filter(lambda element: StereotypesHelper.hasStereotype(element, stereotypes), 
                  parentElement.getOwnedElement())

def getOwnedPackages(element):
    return getOwnedElementsByStereotype(element, "Package")

def getOwnedElementsByName(parentElement, name=""):
    if "" == name:
        name = parentElement.getName()
    namedChildren = filter(lambda element: isinstance(element, NamedElement), parentElement.getOwnedElement())
    return filter(lambda element: str(name) == str(element.getName()), namedChildren)

def getOwnedAttributesByStereotype(parentElement, stereotype = "Block"):
    return filter(lambda element: StereotypesHelper.hasStereotype(element, stereotype),
                  parentElement.getOwnedAttribute())

def getOwnedAttributesByType(parentElement, type):
    return filter(lambda attribute: attribute.getType() == type,
                  parentElement.getOwnedAttribute())

def getOwnedAttributesByAnyTypeStereotypes(parentElement, stereotypes):
    return filter(lambda attribute: StereotypesHelper.hasStereotypeOrDerived(attribute.getType(), stereotypes),
                  filter (lambda attribute: attribute.getType() != None, parentElement.getOwnedAttribute()))

def getOwnedAttributesByAllTypeStereotypes(parentElement, stereotypes):
    return filter(lambda attribute: all(StereotypesHelper.hasStereotypeOrDerived(attribute.getType(), stereotype) \
                                        for stereotype in stereotypes),
                  parentElement.getOwnedAttribute())

# Given an element, it returns all elements owned by the element that have the specified stereotypes
def getOwnedAttributesByAllStereotypes(parentElement, stereotypes):
    return filter(lambda element: all(StereotypesHelper.hasStereotype(element, stereotype) \
                                      for stereotype in stereotypes), 
                  parentElement.getOwnedAttribute())

def getOwnedAttributesByAnyStereotypes(parentElement, stereotypes):
    return filter(lambda element: StereotypesHelper.hasStereotype(element, stereotypes), 
                  parentElement.getOwnedAttribute())

def getOwnedAttributesByName(parentElement, name=""):
    if "" == name:
        name = parentElement.getName()
    namedChildren = filter(lambda element: isinstance(element, NamedElement), parentElement.getOwnedAttribute())
    return filter(lambda element: str(name) == str(element.getName()), namedChildren)

# Todo:
#   * Need to check that property is UML property type.
#   * Need to make sure that UML instance values are handled correctly
def getDefault(property):
    if property != None:
        default = property.getDefault()
        if default != None:
            if StereotypesHelper.hasStereotypeOrDerived(property, ValuePropertyStereotype):
                if default == '':
                    return None
                else:
                    return float(default)
            #elif isinstance(property, BooleanValueType):
            #    if   lower(default) in TrueStrings:
            #        return True
            #    elif lower(default) in FlaseStrings:
            #        return False
            #    else:
            #        return None
            else:
                return default
        else:
            return default
    else:
        return None

# Todo:
#   * Need to check that property is UML property type.
#   * Need to make sure that UML instance values are handled correctly
def setDefault(property, default):
    if default != None:
        #if isinstance(property, RealValueType):
        #    defaultValue = elementsFactory.createLiteralUnlimitedNaturalInstance()
        #    defaultValue.setValue(float(default))
        #elif isinstance(property, IntegerValueType):
        #    defaultValue = elementsFactory.createLiteralIntegerInstance()
        #    defaultValue.setValue(int(round(float(default))))
        #elif isinstance(property, BooleanValueType):
        #    defaultValue = elementsFactory.createBooleanInstance()
        #    if not default in [True, False]:
        #        default = lower(default) in TrueStrings
        #    defaultValue.setValue(default)
        #else:
        defaultValue = elementsFactory.createLiteralStringInstance()
        defaultValue.setValue(str(default))
        property.setDefaultValue(defaultValue)
        return defaultValue
    else:
        return None

def doRecursively(parent, getChildrenFunction, doForEachChildFunction, doOnExitFunction, *args):
    try:
        for child in getChildrenFunction(parent):
            doForEachChildFunction(child, *args)
            doRecursively(child, getChildrenFunction, doForEachChildFunction, doOnExitFunction, *args)
        doOnExitFunction(parent, *args)
    except:
        raise

def createGeneralizationInstance(parent, child):
    ''' Creates a generalization instance and:
    1. Sets the generalization supplier as the parent.
    2. Sets the generalization client as the child.
    3. Sets the owner of the generalization as the child. '''
    generalizationInstance = elementsFactory.createGeneralizationInstance()
    ModelHelper.setClientElement(generalizationInstance, child)
    ModelHelper.setSupplierElement(generalizationInstance, parent)
    generalizationInstance.setOwner(child)
