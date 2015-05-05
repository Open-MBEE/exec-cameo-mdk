# Assumption:
#   * Currently I assume that there is exactly one mass CBE property per element, but this should be validateed. Because of this assumption, you see zero indexing, [0] below.
#   * If an element has a mass current best estimate (CBE) property, it is assumed to be named CbeMassPerUnitName. Thus, an element's mass CBE is retrieved by search for the property name.

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

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)

import Graph
reload(Graph)

guiLog            = Application.getInstance().getGUILog()
project           = Application.getInstance().getProject()
ProductStereotype = StereotypesHelper.getStereotype(project, 'Product')
WorkPackageStereotype = StereotypesHelper.getStereotype(project, 'Work Package')
MassDurativeEvent = StereotypesHelper.getStereotype(project, 'Mass Durative Event')
MassStatePrototypeStereotype = StereotypesHelper.getStereotype(project, 'Mass State Prototype')

kgValueType    = filter(lambda element: element.getName() == 'kg',
                        ModelHelper.getElementsOfType(project.getModel(), [DataType], False))[0]

CbeMassPerUnitName                = 'Mass Current Best Estimate'
CbePlusContingencyFactorName      = 'Mass Contingency'
CbePlusContingencyMassPerUnitName = 'Mass CBE_+_Contingency'

def run(mode):
    selected = None
    megatron = 0
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    try:
        SessionManager.getInstance().createSession("Analyzer")
        elementFactory = project.getElementsFactory()
        modelElementManager = ModelElementsManager.getInstance()

        G = Graph.Graph(getChildren = getOwnedProductPropertiesTypes)
        pi, d, f = Graph.dfs(G = G, vertices = [selected])
        orderedProduct = Graph.topological_sort_using_finished_time(f)
        updateProduct = dict()
        
        for product in orderedProduct:
            guiLog.log("---- Current Element: " + product.getQualifiedName() + " ----")
            isValid = isValidProduct(product, updateProduct.get(product, True))
            # If the product is not valid, there is no point in updating the product's parent product. But for now, go ahead since there may be a valid mass event.
            #if not isValid:
            #    updateProduct[pi[product]] = False
        if isValid:
            guiLog.log("Valid: The product is valid.")
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        for message in traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback):
            guiLog.log(message)

def getOwnedProductPropertiesTypes(parentElement):
    return map(lambda productProperty: productProperty.getType(), getOwnedProductProperties(parentElement))

def getOwnedProductProperties(parentElement):
    return MDUtils.getOwnedAttributesByAnyTypeStereotypes(parentElement, [ProductStereotype, WorkPackageStereotype])

def getOwnedMassStateProperties(parentElement):
    return MDUtils.getOwnedAttributesByAnyTypeStereotypes(parentElement, [MassStatePrototypeStereotype])

def getOwnedMassProperties(parentElement):
    return MDUtils.getOwnedAttributesByType(parentElement, kgValueType)

def getOwnedMassDurativeEventProperties(parentElement):
    return filter(lambda property: StereotypesHelper.hasStereotypeOrDerived(property.getType(), MassDurativeEvent),
                  filter(lambda property: property.getType(),
                         MDUtils.getOwnedAttributesByStereotype(parentElement, MDUtils.PartPropertyStereotype)))

def getOwnedMassDurativeEventPropertiesTypes(parentElement):
    return map(lambda property: property.getType(), getOwnedMassDurativeEventProperties(parentElement))

def getMassStateProperties(element):
    if element != None:
        return  filter(lambda element: element.getType() == kgValueType,
                       MDUtils.getOwnedAttributesByStereotype(MDUtils.getOwnedAttributesByStereotype(element, MassStatePrototypeStereotype)[0],
                                                              MDUtils.ValuePropertyStereotype))
    else:
        return []

def elementName2elementDict(elements):
    return dict(map(lambda element: [str(element.getName()), element],
                    filter(lambda element: isinstance(element, NamedElement), elements)))

def propertyName2propertyTypeDict(properties):
    return dict(map(lambda property: [property.getName(), property.getType()], properties))

def isnumber(x):
    return isinstance(x, (float,int,long,complex))

def isLeafProduct(product):
    return len(getOwnedProductProperties(product)) == 0

def isValidProductMass(product, update = False):
    #guiLog.log("--- Analyzing Mass: " + product.getQualifiedName() + " ---")
    if isLeafProduct(product):
        return isValidLeafProductMass(product, update)
    else:
        return isValidNonLeafProductMass(product, update)

def isValidProduct(product, update = False):
    return isValidProductMass(product, update)

def isValidLeafProductMass(product, update = False):
    return all(map(lambda massEvent: isValidLeafMassEvent(massEvent, update), getOwnedMassDurativeEventPropertiesTypes(product)))

def isValidLeafMassEvent(massEvent, update = False):
    massPropertiesDict = elementName2elementDict(getOwnedMassStateProperties(massEvent)[0].getType().getOwnedAttribute())
    cbeMassPerUnitProperty                = massPropertiesDict.get(CbeMassPerUnitName)
    cbePlusContingencyFactorProperty      = massPropertiesDict.get(CbePlusContingencyFactorName)
    cbePlusContingencyMassPerUnitProperty = massPropertiesDict.get(CbePlusContingencyMassPerUnitName)
    if propertyHasNumericDefaultValue(cbeMassPerUnitProperty) and propertyHasNumericDefaultValue(cbePlusContingencyFactorProperty):
        cbePlusContingencyMassPerUnit = MDUtils.getDefault(cbeMassPerUnitProperty) * MDUtils.getDefault(cbePlusContingencyFactorProperty)
        return isValidPropertyDefaultValue(cbePlusContingencyMassPerUnitProperty, cbePlusContingencyMassPerUnit, update)
    return False

def isValidNonLeafProductMass(product, update = False):
    parentEventsDict    = propertyName2propertyTypeDict(getOwnedMassDurativeEventProperties(product))
    childrenEventsDicts = map(lambda childProduct: propertyName2propertyTypeDict(getOwnedMassDurativeEventProperties(childProduct)),
                              getOwnedProductPropertiesTypes(product))
    childrenNumOfUnitsProperty = map(lambda childProduct: MDUtils.getOwnedAttributesByName(childProduct, "Number of Units")[0],
                                     getOwnedProductPropertiesTypes(product))
    if all(map(propertyHasNumericDefaultValue, childrenNumOfUnitsProperty)):
        return all(map(lambda eventName: isValidNonLeafMassEvent(parentEventsDict.get(eventName),
                                                                 map(lambda childEventsDict: childEventsDict.get(eventName), childrenEventsDicts),
                                                                 map(MDUtils.getDefault, childrenNumOfUnitsProperty),
                                                                 update),
                       parentEventsDict.keys()))
    return False

def isValidNonLeafMassEvent(parentMassEvent, childrenMassEvents, childrenNumOfUnits, update = False):
    if all(map(lambda childMassEvent: childMassEvent != None,childrenMassEvents)):
        parentMassPropertiesDict = elementName2elementDict(getOwnedMassStateProperties(parentMassEvent)[0].getType().getOwnedAttribute())
        parentCbeMassPerUnitProperty                = parentMassPropertiesDict.get(CbeMassPerUnitName)
        parentCbePlusContingencyFactorProperty      = parentMassPropertiesDict.get(CbePlusContingencyFactorName)
        parentCbePlusContingencyMassPerUnitProperty = parentMassPropertiesDict.get(CbePlusContingencyMassPerUnitName)
        childrenMassPropertiesDicts = map(lambda childMassEvent: elementName2elementDict(getOwnedMassStateProperties(childMassEvent)[0].getType().getOwnedAttribute()),
                                          childrenMassEvents)
        childrenCbeMassPerUnitProperties                = map(lambda childMassPropertiesDict: childMassPropertiesDict.get(CbeMassPerUnitName),
                                                              childrenMassPropertiesDicts)
        childrenCbePlusContingencyMassPerUnitProperties = map(lambda childMassPropertiesDict: childMassPropertiesDict.get(CbePlusContingencyMassPerUnitName),
                                                              childrenMassPropertiesDicts)
        if all(map(propertyHasNumericDefaultValue, childrenCbeMassPerUnitProperties)):
            parentCbeMassPerUnit = sum(map(lambda childCbeMassPerUnit, childNumOfUnits: childCbeMassPerUnit * childNumOfUnits,
                                           map(MDUtils.getDefault, childrenCbeMassPerUnitProperties),
                                           childrenNumOfUnits))
            if isValidPropertyDefaultValue(parentCbeMassPerUnitProperty,
                                           parentCbeMassPerUnit,
                                           update) and all(map(propertyHasNumericDefaultValue,
                                                               childrenCbePlusContingencyMassPerUnitProperties)):
                parentCbePlusContingencyMassPerUnit = sum(map(lambda childCbePlusContingencyMassPerUnit,
                                                              childNumOfUnits: childCbePlusContingencyMassPerUnit * childNumOfUnits,
                                                              map(MDUtils.getDefault, childrenCbePlusContingencyMassPerUnitProperties),
                                                              childrenNumOfUnits))
                if isValidPropertyDefaultValue(parentCbePlusContingencyMassPerUnitProperty,
                                               parentCbePlusContingencyMassPerUnit,
                                               update):
                    if parentCbeMassPerUnit != 0.0:
                        parentCbePlusContingencyFactor = parentCbePlusContingencyMassPerUnit / parentCbeMassPerUnit
                    else:
                        parentCbePlusContingencyFactor = "nan"
                    if isValidPropertyDefaultValue(parentCbePlusContingencyFactorProperty,
                                                   parentCbePlusContingencyFactor,
                                                   update):
                        return True
        return False
    else:
        guiLog.log("Warning: Children products do not have the events corresponding to :" + parentMassEvent.getQualifiedName())
    return True

def isValidPropertyDefaultValue(property, value, update = False):
    if not propertyHasDefaultValueOf(property, value):
        # Shoud cehck if unlocked before setting
        if update:
            if property.isEditable():
                guiLog.log("Fixed: " + property.getQualifiedName() + ".defaultValue = " + str(MDUtils.getDefault(property)) + " -> " + str(value))
                MDUtils.setDefault(property, value)
                return True
            else:
                guiLog.log("Error: " + property.getQualifiedName() + " is not editable.")
                return False
        else:
            return False
    return True

def propertyHasNumericDefaultValue(property):
    defaultValue = MDUtils.getDefault(property)
    if isnumber(defaultValue):
        #guiLog.log("Read: " + property.getQualifiedName() + ".defaultValue = " + str(defaultValue))
        return True
    else:
        guiLog.log("Error: " + property.getQualifiedName() + ".defaultValue = NaN")
        return False

def propertyHasDefaultValueOf(property, value, significantDigit = 4):
    defaultValue = MDUtils.getDefault(property)
    if isnumber(value) and isnumber(defaultValue):
        if round(defaultValue, significantDigit) == round(value, significantDigit):
            return True
    elif defaultValue == value:
        #guiLog.log("Correct: " + property.getQualifiedName() + ".defaultValue = " + str(defaultValue) + " == " + str(value))
        return True
    guiLog.log("Error: " + property.getQualifiedName() + ".defaultValue = " + str(defaultValue) + " != " + str(value))
    return False

