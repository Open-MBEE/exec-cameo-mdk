'''
Created on Oct 13, 2011

@author: Louise Anderson and Elyse Fosse
'''
from sets import Set
from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.mdprofiles import Stereotype
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mddependencies import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions import *
from com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities import *
from com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities import *
from com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import *
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import *
from com.nomagic.uml2.ext.magicdraw.classes.mdpowertypes import GeneralizationSet

from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Interaction 
from SystemsReasoner import Specialize
reload(Specialize)
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
from SystemsReasoner import SRUtils
reload(SRUtils)
from FrameworkValidation import ProfileValidation as VP
reload(VP)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
classmeta = StereotypesHelper.getProfileForStereotype
datapack=project.getModel()
mapping={}

def packageClone(modelPackage):
    csProfileName=modelPackage.getName()
    csElements = modelPackage.getOwnedElement()
    csElements=filter(lambda element: not isinstance(element,GeneralizationSet), csElements)
    csElements=filter(lambda element: not isinstance(element, Package), csElements)
    csElements=filter(lambda element: not isinstance(element, Comment), csElements)
    csElements=filter(lambda element: not isinstance(element, Operation), csElements)
    csElements=filter(lambda element: not isinstance(element, Dependency), csElements)
    csElements=filter(lambda element: not isinstance(element, Diagram), csElements)
    csElements=filter(lambda element: not isinstance(element, InstanceSpecification), csElements)
    csElements=filter(lambda element: not isinstance(element, Stereotype), csElements)
    #csElements=filter(lambda element: not isinstance(element,Generalization), csElements)
    #generalizations=filter(lambda element: isinstance(element,Generalization) and element.hasGeneralizationSet()==True , csElements)
    csDiagrams=modelPackage.getOwnedDiagram()
    newPack=VP.createPackage(csProfileName, datapack)
    mapping[modelPackage]=newPack
    diagramCreate(csDiagrams, newPack)
    MDUtils.copyStereotypes(modelPackage, newPack)
    elementCreate(csElements, newPack)
    createPack(modelPackage, newPack)
    '''Dependencies'''
    csDependencies=filter(lambda element: isinstance(element, Dependency), VP.getEverything(modelPackage))
#    for d in csDependencies:
#        dOwner = d.getOwner()
#        depOwner = mapping[dOwner]
#        dClient = d.getClient()[0]
#        if not isinstance(dClient,Diagram) and not isinstance(dClient, Stereotype):
#            gl.log("dependency Client  "+dClient.getName())
#            depClient = mapping[dClient]
#            dSupply = d.getSupplier()[0]
#            depSupply = mapping[dSupply]
#            newDep = MDUtils.createStereotypedDependency(d.getName(), depOwner, depClient, depSupply, StereotypesHelper.getStereotypes(d))
#            createRedefDep(depOwner, newDep, d)
#    gl.log("========== " + csProfileName + " clone complete.  Now go rename.==========")
    
    #MappingCreate()
    return    
def createRedefDep(depowner, depclient, depsupplier):
    csPackage = StereotypesHelper.getProfile(project, "Mission Service Architecture Framework")
    csSter = filter(lambda element: isinstance(element, Package) and element.getName()=="_Stereotypes_MSAF", csPackage.getOwnedElement())
    csSter=csSter[0]
    #redefinesSter = filter(lambda element: element.getName() == "redefines", csSter.getOwnedElement())[0]
    #MDUtils.createStereotypedDependency("redefines", depowner, depclient, depsupplier, [redefinesSter])
    MDUtils.createDependency("redefines", depowner, depclient, depsupplier)
    return

def createPack(packageOwner, newPack):
    packs = filter(lambda element: isinstance(element, Package), packageOwner.getOwnedElement())
    for p in packs:
        newP = VP.createPackage(p.getName(), newPack)
        MDUtils.copyStereotypes(p, newP)
        els = p.getOwnedElement()
        els=filter(lambda element: not isinstance(element,GeneralizationSet), els)
        els=filter(lambda element: not isinstance(element, Package), els)
        els=filter(lambda element: not isinstance(element, Comment), els)
        els=filter(lambda element: not isinstance(element, Dependency), els)
        els=filter(lambda element: not isinstance(element, Operation), els)
        els=filter(lambda element: not isinstance(element, Diagram), els)
        els=filter(lambda element: not isinstance(element, InstanceSpecification), els)
        elementCreate(els, newP)
        diagramCreate(p.getOwnedDiagram(), newP) 
        mapping[p]=newP
        createPack(p, newP)        
    return

def elementCreate(csElements, newPack):
    newElement=None
    for a in csElements:
        if not isinstance(a,InformationFlow):
            aStereotypes = StereotypesHelper.getStereotypes(a)
            checkEnum=isinstance(a,Enumeration)
            if len(aStereotypes)>0:
                newElement = VP.createElement(aStereotypes[0], newPack, checkEnum)
                if newElement:
                    MDUtils.copyStereotypes(a, newElement)
                    isDoc = VP.docStereotypeCheck(a)
                    if isDoc == 0:
                        newElement.setName(str(a.getName()) + " Fix Me!")
                    else:
                        newElement.setName(str(a.getName()))
                    #if isinstance(a,Namespace):
                    diagramCreate(a.getOwnedDiagram(), newElement)
                    newgen = ef.createGeneralizationInstance()
                    #gl.log("!!!!!!!!!!!!The name of the general element"+a.getQualifiedName())
                    newgen.setGeneral(a)
                    newgen.setSpecific(newElement)
                    interactions = filter(lambda element: isinstance(element, Interaction), a.getOwnedElement())
                    for i in interactions:
                        newInt = ef.createInteractionInstance()
                        newInt.setOwner(newElement)
                        diagramCreate(i.getOwnedDiagram(), newInt)
                        createRedefDep(newPack, newInt, i)
                    if isinstance(newElement, Class):
                        newElement.setAbstract(a.isAbstract())
                mapping[a]=newElement
    return newElement


        
    return sterCheck
def diagramCreate(csDiagrams, owner):
    ###Make Diagrams in the new pack
    for dpe in csDiagrams:
        d = project.getDiagram(dpe)
        #diag=ef.createDiagramInstance()
        #I think this is the correct way but I'm not sure what to put in for namespace createDiagram(type,namespace parent)
        diag=mem.createDiagram(d.getDiagramType().getType(), owner)
        diag.setName(d.getName())
        diag.setOwner(owner)
    return

#def MappingCreate():
    
 
 
#def run(mode):
#    if mode == 'b':
#        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
#    try:
#        SessionManager.getInstance().createSession("syncProfile")
#        packageClone(selected)
#        options={"checkOnly":False, "mapping": mapping, 'fixForOpsrev':True}
#        for m in mapping:
#            if not isinstance(m, Package):
#                newElement=mapping[m]
#                Specialize.generalize(newElement,options)
#        SessionManager.getInstance().closeSession()
#    except:
#        if SessionManager.getInstance().isSessionCreated():
#            SessionManager.getInstance().cancelSession()
#        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
#        gl.log("*** EXCEPTION:")
#        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
#        for message in messages:
#            gl.log(message)