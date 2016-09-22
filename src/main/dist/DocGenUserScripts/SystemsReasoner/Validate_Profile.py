''' Clones a Package
Created on Sep 20, 2011

@author: efosse
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
from com.nomagic.uml2.ext.magicdraw.mdprofiles import Stereotype, Profile, Image
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

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sysmlProf=StereotypesHelper.getProfile(project,"SysML")
sysmlSt=StereotypesHelper.getStereotypesByProfile(sysmlProf)
datapack=project.getModel()





def getPackages(modelPackage):
	if not isinstance(modelPackage, Profile):
	    profilePackage = getProfilePackage(modelPackage)
	    if not profilePackage:
	        gl.log('***ERROR: The code is not being run on a Profile.  Stopping Execution***')
	        return
	else:
	    profilePackage = modelPackage
	    
	queueProps = ["priority", "location in queue", "time in queue", "timestamp", "read capability", "next", "event time", "minClosed", "maxClosed", "minClosed1", "maxClosed1", "start event", "time interval", "duration interval", "end event"]
	
	unusedSterPack = filter(lambda element: isinstance(element, Package) and element.getName() == "_Deprecated Stereotypes", profilePackage.getOwnedElement())
	unusedCustPack = filter(lambda element: isinstance(element, Package) and element.getName() =="_Deprecated Customizations", profilePackage.getOwnedElement())
	energon = filter(lambda element: isinstance(element, Package) and element.getName() == "_Energon", profilePackage.getOwnedElement())
	if energon:
		energon = energon[0]
		energon = filter(lambda element: isinstance(element, Package) and element.getName() =="_Stereotypes", energon.getOwnedElement())[0]
	else:
		gl.log("INFORMATION: Energon Package not present.")
	if not unusedCustPack:
		unusedCustPack = createPackage("_Deprecated Customizations", profilePackage)
	else:
		unusedCustPack = unusedCustPack[0]
	if not unusedSterPack:
		unusedSterPack = createPackage("_Deprecated Stereotypes", modelPackage)
	else:
		unusedSterPack = unusedSterPack[0]
	allElements = getEverything(modelPackage)
	allCustomizations = filter(lambda element: StereotypesHelper.hasStereotype(element, "Customization"), allElements)
	allStereotypes = filter(lambda element: isinstance(element, Stereotype) and element.getOwner() != unusedSterPack and element.getOwner()!= energon, allElements)
	packages = filter(lambda element: isinstance(element, Package), allElements)
	
	'''Validate Package Org: all stereotypes in one top level stereotypes package; all customizations in one top level customizations package.'''
	oldStereotypePackage = filter(lambda element: isinstance(element, Package) and element.getName() == "Stereotypes", profilePackage.getOwnedElement())
	if oldStereotypePackage:
		oldStereotypePackage.setName("_Stereotypes")
		stereotypePackage = oldStereotypePackage
		gl.log("Renamed stereotypes package (added underscore")
	else:
	 	stereotypePackage = filter(lambda element: isinstance(element, Package) and element.getName() == "_Stereotypes", profilePackage.getOwnedElement())
	oldCustomPackage =filter(lambda element: isinstance(element, Package) and element.getName() == "Customizations", profilePackage.getOwnedElement())
	if oldCustomPackage:
		oldCustomPackage.setName("_Customizations")
		customPackage = oldCustomPackage
		gl.log("Renamed customizations package (added underscore)")
	else:
	 	customPackage = filter(lambda element: isinstance(element, Package) and element.getName() == "_Customizations", profilePackage.getOwnedElement())
	return [profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations]

def validateLocation(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	gl.log("**VALIDATION: Package Location**")
	sterPlace = []
	custPlace = []
	
	if not stereotypePackage:
	    gl.log('======No Stereotype Package Exists for This Profile! All stereotypes are in the wrong place')
	else:
	    stereotypePackage = stereotypePackage[0]
	    gl.log('======Stereotypes Not In Stereotype Package')
	    for s in allStereotypes:
	        if s.getOwner() != stereotypePackage and s.getOwner() != unusedSterPack and s.getOwner() != energon:
	            gl.log('\t'+ s.getName())
	            sterPlace.append(s)
	if not customPackage:
	    gl.log('======No Customization Package Exists for This Profile! All stereotypes are in the wrong place')
	else:
	    customPackage = customPackage[0]
	    gl.log('======Customizations Not in Customization Package')
	    for c in allCustomizations:
	        if c.getOwner() != customPackage and c.getOwner() != unusedCustPack:
	            gl.log('\t\t'+ c.getName())
	            custPlace.append(c)
	gl.log('======Validate Package Location Summary')
	gl.log(str(len(sterPlace)) + ' stereotypes need to be moved to proper package*')
	gl.log(str(len(custPlace)) + ' customizations need to be moved to proper package' )
	gl.log('*Validate Package Location Complete*')
	return [stereotypePackage, customPackage, sterPlace, custPlace]
def validateStereotypes(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	gl.log("**VALIDATION: Stereotypes**")
	baseClass = getElements(allElements, profilePackage)
	stereotypeNoCustomization = getMissing(allStereotypes, allCustomizations)
	appliedStereotypes = StereotypesHelper.getAllAssignedStereotypes(baseClass)
	stereotypeNoElement=[]
	for s in allStereotypes:
		if s not in appliedStereotypes:
			stereotypeNoElement.append(s)
	gl.log('======Stereotypes With No Customizations')
	for s in stereotypeNoCustomization:
	        gl.log('\t' + str(s.getName()) + " has no customization")
	gl.log('======Stereotypes With No Element in Base Class')
	for s in stereotypeNoElement:
		sSter = StereotypesHelper.getAllAssignedStereotypes([s])
		sProfSter = filter(lambda element: element.getOwner() == stereotypePackage, sSter)
		if len(sProfSter) == 0:
			gl.log('\t' + str(s.getName()) + " has no element in the Base Class. ")
	gl.log("======Validate Stereotypes Summary")
	gl.log(str(len(stereotypeNoCustomization)) + ' stereotypes don\'t have customizations')
	gl.log(str(len(stereotypeNoElement)) + ' stereotypes don\'t have elements in the Base Class') 
	gl.log("*Validate Stereotypes Complete*")       
	return [stereotypeNoCustomization, stereotypeNoElement]

def validateBaseClass(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	gl.log("**VALIDATION: Base Class")
	queueProps = ["priority", "location in queue", "time in queue", "timestamp", "read capability", "next", "event time", "minClosed", "maxClosed", "minClosed1", "maxClosed1", "start event", "time interval", "duration interval", "end event"]
	baseClass = getElements(allElements, profilePackage)
#	elementNoStereotype = getMissing(baseClass, allStereotypes)
#	elementNoStereotype = filter(lambda element: not isinstance(element, Association), elementNoStereotype)
#	elementNoStereotype = filter(lambda element: not isinstance(element, Dependency),elementNoStereotype)
#	elementNoStereotype = filter(lambda element: not isinstance (element, InstanceSpecification), elementNoStereotype)
#	elementNoStereotype = filter(lambda element: element.getOwner() != customPackage, elementNoStereotype)
	elNoSter = 0
	classifiers = []
	properties = []
	operations=[]
	misc = []
	diagrams = []
	for e in baseClass:
		eSter = StereotypesHelper.getAllAssignedStereotypes([e])
		if not eSter:
		    if e.getName() != "" and e.getName() not in queueProps:
		    	if isinstance(e, Classifier):
		    		classifiers.append(e)
		    	elif isinstance(e, Property):
		    		properties.append(e)
		    	elif isinstance(e, Diagram):
		    		diagrams.append(e)
		    	elif isinstance(e, Operation):
		    		operations.append(e)
		    	else:
		    		misc.append(e)
		        elNoSter+=1
	gl.log('======Classifiers with no Stereotype')
	for c in classifiers:
		gl.log('\t' + str(c.getName()) + ' has no stereotype')
	gl.log('======Properties with no Stereotype')
	for p in properties:
		gl.log('\t' + str(p.getName()) + ' has no stereotype')
	gl.log("======Diagrams with no Stereotype")
	for d in diagrams:
		gl.log('\t' + d.getName() + ' has no stereotype')
	gl.log("=====Operations with no Stereotype")
	for o in operations:
		gl.log('\t' + o.getName() + ' has no stereotype')
	gl.log('======Others with no Stereotype:****')
	for m in misc:
		gl.log('\t' + str(m.getName()) + ' has no stereotype')
	gl.log('* ' + str(elNoSter) + ' stereotypes are missing')
	gl.log('======Validate Base Class Summary')
	gl.log(str(len(classifiers))+ ' classifiers have no stereotype')
	gl.log(str(len(properties))+ ' properties have no stereotype')
	gl.log(str(len(diagrams)) + ' diagrams have no stereotype')
	gl.log(str(len(operations))+ ' operations have no stereotype')
	gl.log(str(len(misc))+' other base class elements have no stereotype')
	gl.log("*Validate Elements Complete")
	return [classifiers, properties, diagrams, operations, misc]

def elementClassifierValidation(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	noClassifier = []
	gl.log("**VALIDATION: Elements Not Assigned Base Classifiers")
	baseClass = getElements(allElements, profilePackage)
	baseClass = filter(lambda element: isinstance(element, Classifier), baseClass)
	for b in baseClass:
	    hasclassifier = 0
	    for r in b.get_directedRelationshipOfSource():
	        if isinstance(r, Generalization):
	            hasclassifier = 1
	    if hasclassifier == 0:
	        gl.log('\t\t ' + b.getName() + ' has no Base Classifiers')
	        gl.log('\t\t\t Location:' + b.getQualifiedName())
	        noClassifier.append(b)
	    
	gl.log('=====Validate Element Classifiers Summary')
	gl.log(str(len(noClassifier)) + ' elements in the Base Class have no base classifier')
	gl.log('*Validate Element Classifiers Complete')
	return [noClassifier]

def stereotypeClassifierValidation(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	sterNoClassifier = []
	
	gl.log('**VALIDATION: Stereotypes Not Assigned Base Classifiers')
	for s in allStereotypes:
	    hasclassifier = 0
	    for r in s.get_directedRelationshipOfSource():
	        if isinstance(r, Generalization):
	            hasclassifier = 1
	    if hasclassifier == 0:
	        if s.getName()!= "":
	            gl.log('\t\t' + s.getName() + ' has no Base Classifiers')
	            gl.log('\t\t\t Location:' + s.getQualifiedName())
	            sterNoClassifier.append(s)
	gl.log('=====Validate Stereotype Classifiers Summary')
	gl.log(str(len(sterNoClassifier)) + ' stereotypes have no base classifier')
	gl.log('*Validate Stereotype Classifiers Complete')
	return [sterNoClassifier]

def nameMismatch(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	mismatch = []
	queueProps = ["priority", "location in queue", "time in queue", "timestamp", "read capability", "next", "event time", "minClosed", "maxClosed", "minClosed1", "maxClosed1", "start event", "time interval", "duration interval", "end event"]
	baseClass = getElements(allElements, profilePackage)
	baseClass = filter(lambda element: isinstance(element, Classifier), baseClass)
	for b in baseClass:
		ster =StereotypesHelper.getAllAssignedStereotypes([b])
		sterName = []
	        for s in ster:
	            sterName.append(s.getName())
		if b.getName() != "":
		    if not StereotypesHelper.hasStereotype(b, b.getName()) and len(ster)>0 and "ValueType" not in sterName and not StereotypesHelper.hasStereotype(b, StereotypesHelper.getStereotype(project, "Customization")):
		        mismatch.append(b)
	gl.log('VALIDATION: Element Name, Applied Stereotype Name Mismatch')
	elMismatch = 0
	for m in mismatch:
	    whichSter = StereotypesHelper.getAllAssignedStereotypes([m])
	    whichSterName = []
	    for w in whichSter:
	        if w.getName() != "ValueType":
	            whichSterName.append(w.getName())
	    if m.getName() not in queueProps:
	    	elMismatch +=1
	    	gl.log('\t\t' + m.getName() + ' does not match applied stereotype name:' + str(whichSterName))
	    	gl.log('\t\t\t Location:' + m.getQualifiedName())
	gl.log("=====Validate Name Mismatch Summary")
	gl.log(str(elMismatch) + ' elements have applied stereotypes not matching name')
	gl.log('*Validate Name Mismatch Complete')
	return [elMismatch]

def multipleStereotypes(profilePackage, stereotypePackage, customPackage, energon, unusedSterPack, unusedCustPack, allStereotypes, allElements, allCustomizations):
	multipleSter = []
	baseClass = getElements(allElements, profilePackage)
	baseClass = filter(lambda element: isinstance(element, Classifier), baseClass)
	gl.log('VALIDATION: Elements with Multiple Stereotypes')
	for b in baseClass:
	    if b.getName() != "":
	        ster =StereotypesHelper.getAllAssignedStereotypes([b])
	        sterName = []
	        for s in ster:
	            sterName.append(s.getName())
	        sterInProfile = filter(lambda element: element.getOwner() == stereotypePackage, ster)
	        if len(sterInProfile) > 1:
	            multipleSter.append(b)
	for d in multipleSter:
	    whichSter = StereotypesHelper.getAllAssignedStereotypes([d])
	    whichSter = filter(lambda element: element.getOwner() == stereotypePackage, whichSter)
	    whichSterName = []
	    for w in whichSter:
	        whichSterName.append(w.getName())
	    gl.log('\t\t' +d.getName() + ' has multiple stereotypes: ' + str(whichSterName))
	    gl.log('\t\t\t Location:' + d.getQualifiedName())
	gl.log("=====Validate Multiple Applied Stereotypes Summary")
	gl.log(str(len(multipleSter))+ ' elements have multiple stereotypes applied')
	gl.log("*Validate Mulitplie Stereotypes Complete") 
	return [multipleSter]
#	gl.log('======Checking Generalizations***Dont Trust!! Still Testing!***====================================================================')
#	badGen = 0
#	stereotypeGen = filter(lambda element: isinstance(element, Generalization), getEverything(stereotypePackage))
#	customGen = filter(lambda element: isinstance(element, Generalization), getEverything(customPackage))
#	baseGen = filter(lambda element: isinstance(element, Generalization) and element not in customGen and element not in stereotypeGen, getEverything(profilePackage))
#	gl.log(str(len(stereotypeGen)))
#	gl.log(str(len(customGen)))
#	gl.log(str(len(baseGen)))
#	return
#	'''Validate Diagrams'''
#	needName = 0
#	missingCompBDD = 0
#	missingMapBDD = 0
#	missingPack = 0
#	missingTimeline = 0
#	missingInterfaces = 0
#	missingLayer = 0
#	gl.log('======Checking Views==================================================================================================================')
#	baseClassPacks = filter(lambda element: isinstance(element, Package) and "_" not in element.getName(), getEverything(profilePackage))
#	baseClassPacks = filter(lambda element: element.getName() != "Ops Con", baseClassPacks)
#	baseClassPacks = filter(lambda element: element.getName() !="Operational Concepts", baseClassPacks)
#	compBDDSter = StereotypesHelper.getStereotype(project, "Composition BDD View")
#	mapBDDSter = StereotypesHelper.getStereotype(project, "Mapping BDD View")
#	packSter = StereotypesHelper.getStereotype(project, "Package Diagram of Views")
#	tSter = StereotypesHelper.getStereotype(project, "Timeline Parametric View")
#	iSter = StereotypesHelper.getStereotype(project, "Interfaces IBD View")
#	layerIBDSter = StereotypesHelper.getStereotype(project, "Layer IBD View")
#	for b in baseClassPacks:
#		noDiagSter = 0
#		compBDD = 0
#		specBDD = 0
#		pack = 0
#		diags = b.getOwnedDiagram()
#		for d in diags:
#			if "FixMe!" in d.getName():
#				needName+=1
#				
#			if StereotypesHelper.isElementStereotypedBy(d, compBDDSter.getName()):
#				compBDD +=1
#			elif StereotypesHelper.isElementStereotypedBy(d, mapBDDSter.getName()):
#				specBDD+=1
#			elif StereotypesHelper.isElementStereotypedBy(d, packSter.getName()):
#				pack+=1
#			else:
#				noDiagSter+=1
#		if compBDD == 0:
#			gl.log(b.getName()+" is missing " + compBDDSter.getName())
#			missingCompBDD+=1
#		if specBDD == 0:
#			gl.log(b.getName()+" is missing " + mapBDDSter.getName())
#			missingMapBDD+=1
#		if pack == 0:
#			gl.log(b.getName()+" is missing " + packSter.getName())	
#			missingPack+=1
#		blocks = filter(lambda element: isinstance(element, Class), b.getOwnedElement())
#		for blo in blocks:
#			noBlockSter = 0
#			layerIBD = 0
#			timelineIBD = 0
#			interfaceIBD = 0
#			diag = blo.getOwnedDiagram()
#			for d in diag:
#				if "FixMe!" in d.getName():
#					needName+=1
#					
#				
#				if StereotypesHelper.isElementStereotypedBy(d, tSter.getName()):
#					timelineIBD+=1
#				elif StereotypesHelper.isElementStereotypedBy(d, iSter.getName()):
#					interfaceIBD+=1
#				elif StereotypesHelper.isElementStereotypedBy(d, layerIBDSter.getName()):
#					layerIBD+=1
#				else:
#					noBlockSter+=1
#		if timelineIBD == 0:
#			gl.log(blo.getQualifiedName()  + " is missing " + tSter.getName())
#			missingTimeline+=1
#		if interfaceIBD == 0:
#			gl.log(blo.getQualifiedName()+ " is missing " + iSter.getName())
#			missingInterfaces+=1
#		if layerIBD == 0:
#			gl.log(blo.getQualifiedName() + " is missing " + layerIBDSter.getName())
#			missingLayer+=1
#			
#			
def validationSummary(stereotypePackage, customPackage, sterPlace, custPlace, stereotypeNoCustomization, stereotypeNoElement,classifiers, properties, diagrams, misc,noClassifier,sterNoClassifier,elMismatch,multipleSter):
	gl.log('[VALIDATION SUMMARY]')  
	if not stereotypePackage:  
	    gl.log('*WARNING: No Stereotype Package Exists for This Profile! All stereotypes are in the wrong place*')
	else:
	    gl.log(str(len(sterPlace)) + ' stereotypes need to be moved to proper package*')
	if not customPackage:
	    gl.log('*WARNING: No Customization Package Exists for This Profile! All stereotypes are in the wrong place*')
	else:
	    gl.log( str(len(custPlace)) + ' customizations need to be moved to proper package*' )
	if stereotypeNoCustomization != 'z':
		gl.log(str(len(stereotypeNoCustomization)) + ' customizations are missing(based on stereotypes)*')
		gl.log(str(len(stereotypeNoElement)) + ' stereotypes have no matching element. Suggest running Deprecate Stereotypes*')
	if noClassifier != 'z':
		gl.log(str(len(noClassifier)) + ' elements in the Base Class have no base classifier*')
	if sterNoClassifier != 'z':
		gl.log(str(len(sterNoClassifier)) + ' stereotypes have no base classifier')
	if elMismatch != 'z':
		gl.log( str(elMismatch)  + ' elements have applied steretoype not matching name')
	if multipleSter != 'z':
		gl.log(str(len(multipleSter)) + ' elements have multiple stereotypes')
	if classifiers != 'z':
		gl.log(str(len(classifiers))+ ' classifiers have no stereotype')
		gl.log(str(len(properties))+ ' properties have no stereotype')
		gl.log(str(len(diagrams)) + ' diagrams have no stereotype')
#		gl.log(str(len(operations))+ ' operations have no stereotype')
		gl.log(str(len(misc))+' other base class elements have no stereotype')
#	gl.log('* ' + str(missingCompBDD)+ ' composition BDD views are missing')
#	gl.log('* ' + str(missingMapBDD) + ' mapping BDD views are missing')
#	gl.log('* ' + str(missingPack) + ' package views are missing')
#	gl.log('* ' + str(missingTimeline) + ' Timeline parametric views are missing')
#	gl.log('* ' + str(missingInterfaces) + ' interface IBD views are missing')
#	gl.log('* ' + str(missingLayer) + ' layer IBD views are missing')
#	gl.log('* ' + str(needName)+ ' views need to be named')

	return

def getElements(allels, profilePackage):
	sterPack = filter(lambda element: isinstance(element, Package) and element.getName() == "_Stereotypes", profilePackage.getOwnedElement())
	custPack = filter(lambda element:isinstance(element, Package) and element.getName() == "_Customizations", profilePackage.getOwnedElement())
	depPack = filter(lambda element: isinstance(element, Package) and element.getName() == "_Deprecated Stereotypes", profilePackage.getOwnedElement())
	baseClass = filter(lambda element: element.getOwner() != sterPack, allels)
	baseClass = filter(lambda element: element.getOwner() != custPack, baseClass)
	baseClass = filter(lambda element: element.getOwner()!=depPack, baseClass)
	baseClass = filter(lambda element: isinstance(element, NamedElement), baseClass)
	baseClass = filter(lambda element: element.getName() != "_Stereotypes" and element.getName() != "_Customizations" and element.getName() != "_Deprecated Stereotypes", baseClass)
	baseClass = filter(lambda element: not isinstance(element, Stereotype), baseClass)
#	baseClass = filter(lambda element: not isinstance(element, Diagram), baseClass)
	baseClass = filter(lambda element: not isinstance(element, Comment), baseClass)
	#    baseClass = filter(lambda element: not isinstance(element, Property), baseClass)
	baseClass = filter(lambda element: not isinstance(element, InputPin), baseClass)
	baseClass = filter(lambda element: not isinstance(element, OutputPin), baseClass)
	baseClass = filter(lambda element: not isinstance(element, ActivityParameterNode), baseClass)
	baseClass = filter(lambda element: not isinstance(element, Parameter), baseClass)
	baseClass = filter(lambda element: not isinstance(element, CallBehaviorAction), baseClass)
#	baseClass = filter(lambda element: not isinstance(element, Operation), baseClass)
	baseClass = filter(lambda element: not isinstance(element, EnumerationLiteral), baseClass)
	baseClass = filter(lambda element: not isinstance(element, ObjectFlow), baseClass)
	baseClass = filter(lambda element: element.getName() != "", baseClass)
	baseClass = filter(lambda element: 'base_' not in element.getName(), baseClass)
	baseClass = filter(lambda element: 'extension_' not in element.getName(), baseClass)
	return baseClass

def getEverything(element, collection = None):
    collection2 = []
    if collection is not None:
        collection2 = collection
    
    for e in element.getOwnedElement():
        collection2.append(e)
        getEverything(e, collection2)
    
    return collection2

def getProfilePackage(package, profilePackage = None):
    profilePackage2 = []
    if profilePackage is not None:
        profilePackage2 = profilePackage
    if isinstance(package, Profile):
        profilePackage = package
    else:
       p = package.getNestingPackage()
       if p:
           if not isinstance(p, Profile):
                profilePackage2 = getProfilePackage(p, profilePackage)
           else: 
                profilePackage2 = p
    return profilePackage2


       

def getMissing(A, B):
    missing =[]
    for a in A:
        bExist = 0
        for b in B:
            if a.getName() == b.getName():
                bExist = 1
        
        if bExist == 0:
            missing.append(a)
    return missing

def assignCustomizationTags(stereotype, customBlock, supertype):
    StereotypesHelper.setStereotypePropertyValue(customBlock, StereotypesHelper.getStereotype(project, "Customization"), "customizationTarget", stereotype)
    StereotypesHelper.setStereotypePropertyValue(customBlock, StereotypesHelper.getStereotype(project, "Customization"), "superTypes", supertype)
    StereotypesHelper.setStereotypePropertyValue(customBlock, StereotypesHelper.getStereotype(project, "Customization"), "hideMetatype", False)
    return      

def createCustomization(stereotypeName, owner):
	MDcustPackage = filter(lambda element: isinstance(element, Package) and element.getName() == "MD Customization for SysML", datapack.getOwnedElement())[0]
	syscustPackage = filter(lambda element: isinstance(element, Package) and element.getName() == "customizations", MDcustPackage.getOwnedElement())[0]
	new = ef.createClassInstance()
	new.setOwner(owner)
	new.setName(stereotypeName)
	StereotypesHelper.addStereotype(new, StereotypesHelper.getStereotype(project, "Customization"))
	ster = StereotypesHelper.getStereotype(project, stereotypeName)
    #get ster generalizations:
	sterGen = filter(lambda element: isinstance(element, Generalization), ster.get_directedRelationshipOfSource())
	for s in sterGen:
		cust = filter(lambda element: element.getName().replace(" ","") == s.getGeneral().getName().replace(" ",""), syscustPackage.getOwnedElement())
		if len(cust) !=0:
			cust = cust[0]
			newgen = ef.createGeneralizationInstance()
			newgen.setGeneral(cust)
			newgen.setSpecific(new)
			newgen.setOwner(new)
	return new


	        
def createStereotype(owner, element):
	if isinstance(element, Package):
		if StereotypesHelper.hasStereotype(element, StereotypesHelper.getStereotype(project, "View")):
			package = element.getName().find("View")
		 	if package == -1:
		 		element.setName(element.getName() + " View")
		else:
		 	package = element.getName().find("Package")
		 	if package == -1:
		 		element.setName(element.getName() + " View")            
	
	if not isinstance(element, EnumerationLiteral):
	    eSter = StereotypesHelper.getAllAssignedStereotypes([element])
	    eSter = filter(lambda element: element != StereotypesHelper.getStereotype(project, "Diagram Info"), eSter)
	    newSter = StereotypesHelper.createStereotype(owner, element.getName(), [StereotypesHelper.getBaseClass(element)])
	    StereotypesHelper.addStereotypeByString(element, element.getName())
	    for e in eSter:
	    	newgen = ef.createGeneralizationInstance()
	    	gl.log(e.getQualifiedName())
	    	gl.log(newSter.getQualifiedName())
	        newgen.setGeneral(e)
	        newgen.setSpecific(newSter)
	        newgen.setOwner(newSter)
	    	icon = filter(lambda element: isinstance(element, Image), e.getOwnedElement())
	    	if icon:
	    		icon = icon[0]
	    		newicon= ef.createImageInstance()
	    		newicon.setLocation(icon.getLocation())
	    		newicon.setContent(icon.getContent())
	    		newicon.setFormat(icon.getFormat())
	    		newicon.setOwner(newSter)	
	        
	return
   
def createPackage(name, owner):
    new = ef.createPackageInstance()
    new.setOwner(owner)
    new.setName(name)
    return new

#
#Doesn't seem to be working. Have to debug.
#def createStereotypedElement(stereotype, owner):
#	new = createElement(stereotype, owner)
#	StereotypesHelper.addStereotypeByString(new, stereotype.getName())
#	return new

def createElement(stereotype, owner, checkEnum):
    new = None
    mclass = StereotypesHelper.getBaseClassesAsClasses(stereotype)
    #gl.log("the metaclass string==========>"+str(mclass) +"stereotype name====>"+stereotype.getName())
    for m in mclass:
        insCreate = str(m)
        findtrunc = insCreate.split(".")
        insCreate = findtrunc[len(findtrunc)-1]
        insCreate = insCreate[:-2]
        ignore = ['InstanceSpecification', 'Property', 'Association', 'Dependency', 'ObjectFlow', 'Relationship', 'Operation', 'EnumerationLiteral', 'Lifeline']
        if insCreate not in ignore:
            if insCreate == "Element" or insCreate =="NamedElement":
                new = ef.createClassInstance()
                gl.log(stereotype.getName() + " Stereotype metaclass was element.  Stereotyped Class was created")
            elif checkEnum==True:
            	new=ef.createEnumerationInstance()
            	gl.log(stereotype.getName() + " Stereotype metaclass was enumeration.  Stereotyped Enumeration was created")
            else:
                instanceCreate = 'create'+insCreate+'Instance'
                new = eval('ef.create'+insCreate+'Instance()')
            new.setOwner(owner)
            if docStereotypeCheck(stereotype) == 0:
                new.setName(stereotype.getName())
    return new

def docStereotypeCheck(element):
    sterCheck = 0
    docProfile = StereotypesHelper.getProfile(project,"Document Profile")
    if docProfile:
        docStereotypes = getEverything(docProfile)
        docStereotypes = filter(lambda element: isinstance(element, Stereotype), docStereotypes)
        for s in StereotypesHelper.getStereotypes(element):
            if s in docStereotypes:
                sterCheck = 1
    return sterCheck
def createStereotypedElement(stereotype, owner):
    new = None
    mclass = StereotypesHelper.getBaseClassesAsClasses(stereotype)
    for m in mclass:
        insCreate = str(m)
        findtrunc = insCreate.split(".")
        insCreate = findtrunc[len(findtrunc)-1]
        insCreate = insCreate[:-2]
        if insCreate != "Property" and insCreate != "Association" and insCreate != "Dependency" and insCreate != "Relationship" and insCreate != "Element" and insCreate != "Operation" and insCreate!= "Property" and insCreate!= "EnumerationLiteral":
            instanceCreate = 'create'+insCreate+'Instance'
            new = eval('ef.create'+insCreate+'Instance()')
            new.setOwner(owner)
            new.setName(stereotype.getName())
            StereotypesHelper.addStereotypeByString(new, stereotype.getName())
    return new

def run(mode):
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    try:
        SessionManager.getInstance().createSession("syncProfile")
        A = getPackages(selected)
        D = validateLocation(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        B = validateBaseClass(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        C = validateStereotypes(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        E = elementClassifierValidation(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        F = stereotypeClassifierValidation(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        G = nameMismatch(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        H = multipleStereotypes(A[0], A[1], A[2], A[3], A[4], A[5], A[6], A[7], A[8])
        validationSummary(D[0], D[1], D[2], D[3], C[0], C[1],B[0], B[1], B[2], B[3], E[0], F[0], G[0], H[0])
        SessionManager.getInstance().closeSession()
    except:
        if SessionManager.getInstance().isSessionCreated():
            SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        gl.log("*** EXCEPTION:")
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)

	