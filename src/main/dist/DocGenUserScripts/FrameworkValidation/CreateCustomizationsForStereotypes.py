'''
Created on Sep 26, 2011

@author: efosse
'''
from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.mdprofiles import Stereotype, Profile
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mddependencies import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions import *
from com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities import *
from com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities import *
from com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import *
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import *

import ProfileValidation as VP
reload(VP)

mem = ModelElementsManager.getInstance()
gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
classmeta = StereotypesHelper.getProfileForStereotype

def createCustomizationsForStereotypes(modelPackage, validateOnly):
###Checks for: 
###stereotypes with no customizations (stereotypesNoCustomizations)
###customizations with no supertype (noType)
###customizations with multiple supertypes (multTypes)
###customizations with no target (noCustTargs)
###customizations with multiple targets (multCust)
###customization name/target name mismatch (stereotypesCustNameMisMatch)
###customization name/type name mismatch (elementCustNameMisMatch)
	newcust = 0
	bummer = 0
	bummerType =[]
###get packages
	if not isinstance(modelPackage, Profile):
	    profilePackage = VP.getProfilePackage(modelPackage)
	    if not profilePackage:
	        gl.log('***ERROR: The code is not being run on a Profile.  Stopping Execution***')
	        return
	else:
	    profilePackage = modelPackage
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	if not infraPack:
		sterPack = filter(lambda element: isinstance(element, Package) and "_Stereotypes" in element.getName(), profilePackage.getOwnedElement())
		if sterPack: sterPack = sterPack[0]
		else: return['no stereotypes package']
	else: infraPack = infraPack[0]
	stereotypePackage = filter(lambda element: isinstance(element, Package) and "_Stereotypes" in element.getName(),infraPack.getOwnedElement())[0]
	customPackage = filter(lambda element: isinstance(element, Package) and element.getName() == "_Customizations", infraPack.getOwnedElement())[0]
	allElements = VP.getEverything(profilePackage)
###Validation
	Customizations = filter(lambda element: StereotypesHelper.hasStereotype(element, "Customization"), allElements)
	Stereotypes = filter(lambda element: isinstance(element, Stereotype) and element.getOwner() == stereotypePackage, allElements)
	Stereotypes = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Dependency") not in StereotypesHelper.getBaseClasses(element), Stereotypes)
	Stereotypes = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Association") not in StereotypesHelper.getBaseClasses(element), Stereotypes)
	Stereotypes = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Property") not in StereotypesHelper.getBaseClasses(element), Stereotypes)
	Stereotypes = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Port") not in StereotypesHelper.getBaseClasses(element), Stereotypes)
	els = VP.getElements(allElements, profilePackage)
	custSter = StereotypesHelper.getStereotype(project, "Customization")
	custTypeNames =[]
	multCust =[]
	multTypes=[]
	custTargsName=[]
	custNames=[]
	custTypes=[]
	cignore=[]
	noCustTargs=[]
	noType=[]
	for c in Customizations:	
		if "part property" in c.getName() or "port" in c.getName():
			mem.removeElement(c)
		else:	
			custNames.append(c.getName())
			custTarg = StereotypesHelper.getStereotypePropertyValue(c, custSter, "customizationTarget")
			if len(custTarg)>0:
				if len(custTarg)>1:multCust.append(c)
				custTargsName.append(StereotypesHelper.getStereotypePropertyValue(c, custSter, "customizationTarget")[0].getName())
			else:
				cignore.append(c)
				noCustTargs.append(c)
				gl.log('[ERROR] No customization target = '  +c.getName())
			custType = StereotypesHelper.getStereotypePropertyValue(c, custSter, "superTypes")
			if len(custType)>0:
				if len(custType)>1:multTypes.append(c)
				custTypes.extend(StereotypesHelper.getStereotypePropertyValue(c, custSter, "superTypes"))
				custTypeNames.append(StereotypesHelper.getStereotypePropertyValue(c, custSter, "superTypes")[0].getName())
			else:
				noType.append(c)
				gl.log('[ERROR] No customization type = ' + c. getName())	
	stereotypesNoCustomizations = filter(lambda element: element.getName() not in custTargsName, Stereotypes)
	stereotypesNoCustomizations = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Association") not in StereotypesHelper.getBaseClasses(element), stereotypesNoCustomizations)
	stereotypesCustNameMisMatch = filter(lambda element: element.getName() not in custNames, Stereotypes)
	stereotypesCustNameMisMatch = filter(lambda element: element not in stereotypesNoCustomizations, stereotypesCustNameMisMatch)
	elementCustNameMisMatch = filter(lambda element: element.getName() not in custNames, custTypes)
###Fixes
	if validateOnly!=True:
###stereotypes with no customizations->Fix: create customization, assign target, superType(if possible), and hide metatype
		multName=[]
		for s in stereotypesNoCustomizations:
			if s.getName() not in custNames:
				gl.log('[FIX]: created customization: '+ s.getName())
				newCust = VP.createCustomization(s.getName(), customPackage)
				newcust+=1
######assign customization tags
				sMeta = StereotypesHelper.getBaseClasses(s)
				if len(sMeta)>1:
					gl.log('\t [FIX ERROR]: stereotype has multiple metaclasses.  No customization tags assigned. Modeler must rectify.')
					bummer+=1
				else:
					sMC = StereotypesHelper.getClassOfMetaClass(sMeta[0])
					sMCer = Classifier.isAssignableFrom(sMC)
					if sMCer == True:
						base = filter(lambda element: element.getName() == s.getName(), els)
						if len(base)>0: base = base[0]
						else: 
							base = False
							bummerType.append(newCust)
						VP.assignCustomizationTags(s, newCust, base)
					else:
						bummerType.append(newCust)
						VP.assignCustomizationTags(s, newCust, False)
			else: 
				gl.log('[FIX ERROR]: Unable to create customization for stereotype '+ s.getName() + '. Customization already exists with that name')
				multName.append(s)	
###stereotype-customization name mismatch->Fix: change name of customization to match name of stereotype if supertype name doesn't match as well. else keep flagged	
		fixSterCustMis=[]
		for s in stereotypesCustNameMisMatch:
			sterCust = filter(lambda element: s in StereotypesHelper.getStereotypePropertyValue(element, custSter, "customizationTarget"), Customizations)
			cusType = StereotypesHelper.getStereotypePropertyValue(sterCust[0], custSter, "superTypes")
			if custType:
				if sterCust[0].getName() != custType[0].getName():
					sterCust[0].setName(s.getName())
					gl.log('[FIX]:Changed name of customization to match name of customizationTarget: ' + sterCust[0].getName())
				else: 
					gl.log('[FIX ERROR]: Unable to change name of customization to match name of customizationTarget since customization name matches superType name: ' + sterCust[0].getName())
					fixSterCustMis.append(sterCust[0])
###element-customization name mismatch->Fix: change name of customization to match name of element
		fixElCustMis=[]
		for e in elementCustNameMisMatch:
			eCust = filter(lambda element: e in StereotypesHelper.getStereotypePropertyValue(element, custSter, "superTypes"), Customizations)
			eTarg = StereotypesHelper.getStereotypePropertyValue(eCust[0], custSter, "customizationTarget")
			if eTarg:
				if eCust[0].getName() != eTarg[0].getName():
					eCust[0].setName(e.getName())
					gl.log('[FIX]: Changed name of customization to match name of superType: '+ eCust[0].getName())
				else:
					gl.log('[FIX ERROR]: Unable to change name of customization to match name of superType since customization name matches customization target name: ' + eCust[0].getName())
					fixElCustMis.append(eCust[0])
###multiple customization targets->Fix: if a target matches name of customization, delete all others. Else delete them all & flag customization
		fixNoCustTarg=[]
		for c in multCust:
			cTargs= StereotypesHelper.getStereotypePropertyValue(c, custSter, "customizationTarget")
			targetSlot = StereotypesHelper.getSlot(c, custSter, "customizationTarget", False, False)
			for t in filter(lambda element: element.getName() != c.getName(), cTargs): 
				StereotypesHelper.removeSlotValue(targetSlot, t)
				gl.log('[FIX]: Removing customizationTarget(s) with names not matching customization name: '+ c.getName())
			if len(StereotypesHelper.getStereotypePropertyValue(c, custSter, "customizationTarget"))==0: 
				fixNoCustTarg.append(c)
				gl.log('[FIX ERROR]: Customization now has no target: '+ c.getName())
###multiple customization types->Fix: if a type matches name of customization, delete all other. Else delete them all & flag customization
		fixNoCustType=[]
		for c in multTypes:
			cTypes = StereotypesHelper.getStereotypePropertyValue(c, custSter, "superTypes")
			typeSlot = StereotypesHelper.getSlot(c, custSter, "superTypes", False, False)
			for t in filter(lambda element: element.getName() != c.getName(), cTypes):
				StereotypesHelper.removeSlotValue(typeSlot, t)
				gl.log('[FIX]: Removing superType(s) with names not matching customization name: '+ c.getName())
			if len(StereotypesHelper.getStereotypePropertyValue(c, custSter, "superTypes"))==0: 
				fixNoCustType.append(c)
				gl.log('[FIX ERROR]: Customization now has no superType: '+ c.getName())
###no customization target->Fix: if a stereotype matches with same name and is not assigned to another customization, assign as target, else keep flagged
		fixNoTargsMult=[]
		fixNoTargs=[]
		for c in noCustTargs:
			custSters = filter(lambda element: element.getName() == c.getName(), Stereotypes)
			if custSters and c.getName() not in custTargsName: 
				StereotypesHelper.setStereotypePropertyValue(c, custSter, "customizationTarget", custSters[0])
				gl.log('[FIX]: Setting customization target for '+ c.getName() +" to " + custSters[0].getName())
			elif custSters and c.getName() in custTargsName: 
				fixNoTargsMult.append(c)
				gl.log('[FIX ERROR]: Another customization already has stereotype of same customization name as target. No target applied for '+ c.getName())
			else: 
				fixNoTargs.append(c)
				gl.log('[FIX ERROR]: No stereotype exists with same name as customization.  No target applied for ' + c.getName()) 
###no customization type->Fix: if an element matches with same name and is not assigned to another customizaiton assign as type, else keep flagged
		fixNoTypeMult=[]
		fixNoType=[]
		for c in noType:
			custEl = filter(lambda element: element.getName() == c.getName(), els)
			if custEl and c.getName() not in custTypeNames: 
				StereotypesHelper.setStereotypePropertyValue(c, custSter, "superTypes", custEl[0])
				gl.log('[FIX]: Setting customization superType for '+c.getName() +" to "+ custEl[0].getName())
			elif custEl and c.getName() in custTypeNames: 
				fixNoTypeMult.append(c)
				gl.log('[FIX ERROR]: Another customization already has this element as its superType.  No superType applied for ' + c.getName())
			else: 
				fixNoType.append(c)	
				gl.log('[FIX ERROR]: No base element exists with same name as customization. Not superType applied for '+ c.getName())	
###Message Logs			
	gl.log('SUMMARY')
	if validateOnly != True:
		gl.log('Customizations created = ' + str(newcust))
		gl.log('Customization FIX ERRORS(Modeler must fix)(See Messages Log):')
		gl.log('\t No tags assigned.  Stereotype has multiple metaclasses = '+str(bummer))
		gl.log('\t Customization not created. Customization with same name already exists = '+str(len(multName)))
		gl.log('\t Customization name not changed. Name already matches Type, wont change to match Target = '+str(len(fixSterCustMis)))	
		gl.log('\t Customization name not changed. Name already matches Target, wont change to match Type = '+str(len(fixElCustMis)))
		gl.log('\t Customizations with no Target = '+str(len(fixNoCustTarg)))
		gl.log('\t Customizations with No Type = '+str(len(fixNoCustType)))
		gl.log('\t Customization Target not assigned. Another customization already exists with the given Target = '+str(len(fixNoTargsMult)))
		gl.log('\t Customization Target not assigned. No stereotype with matching name exists = '+str(len(fixNoTargs)))
		gl.log('\t Customization Type not assigned. Another customization already exists with the given Type = '+str(len(fixNoTypeMult)))
		gl.log('\t Customization Type not assigned.  No element with matching name exists = '+str(len(fixNoType)))
####
	return [stereotypesNoCustomizations, stereotypesCustNameMisMatch, multCust, noCustTargs, noType, multTypes, elementCustNameMisMatch]

   

scriptOutput = '***Creation Complete***'