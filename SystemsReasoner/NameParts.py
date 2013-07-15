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
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures import *

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)

guiLog            = Application.getInstance().getGUILog()
project           = Application.getInstance().getProject()
ProductStereotype = StereotypesHelper.getStereotype(project, 'Product')
WorkPackageStereotype = StereotypesHelper.getStereotype(project, 'WorkPackage')
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

        guiLog.log("--- NamePart: Strating ---")
        if isinstance(selected, StructuredClassifier):
            guiLog.log("--- NamePart: Found a StructuredClassifier ---")
            ownedProperties = getOwnedClassifierProperties(selected)
            guiLog.log("--- NamePart: Found " + str(len(ownedProperties)) + " classifier properties ---")
            ownedPropertiesNames = set()
            types2countDict = dict()
            for ownedProperty in ownedProperties:
                ownedPropertiesNames.add(ownedProperty.getName())
                type = ownedProperty.getType()
                guiLog.log("--- NamePart: " + ownedProperty.getName() + " : " + type.getName() + " ---")
                if not types2countDict.get(type):
                    types2countDict[type] = 1
                else:
                    types2countDict[type] = types2countDict[type] + 1
            for ownedProperty in ownedProperties:
                guiLog.log("--- NamePart: " + ownedProperty.getName() + " : " + ownedProperty.getType().getName() + " ---")
                if ownedProperty.getName() == None or ownedProperty.getName() == "":
                    type = ownedProperty.getType()
                    typeName = type.getName()
                    if types2countDict.get(type) == 1:
                        ownedProperty.setName(typeName)
                    else:
                        for index in range(1, types2countDict.get(type) + 1):
                            indexedName = typeName + "-" + str(index)
                            if not(indexedName in ownedPropertiesNames):
                                guiLog.log("--- NamePart: " + indexedName + " : " + typeName + " ---")
                                ownedProperty.setName(indexedName)
                                ownedPropertiesNames.add(indexedName)
                                break
                                
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        for message in traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback):
            guiLog.log(message)

def getOwnedClassifierProperties(parentElement):
    return filter(lambda element: isinstance(element.getType(), Classifier), 
                  parentElement.getOwnedAttribute())

def propertyName2propertyTypeDict(properties):
    return dict(map(lambda property: [property.getName(), property.getType()], properties))
