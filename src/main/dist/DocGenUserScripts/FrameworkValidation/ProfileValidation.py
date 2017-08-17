'''
Created on Sep 20, 2011

@author: efosse
'''
from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
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
from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Interaction
from com.nomagic.uml2.ext.magicdraw.classes.mdpowertypes import GeneralizationSet
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications import Reception, Signal
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import Activity
from com.nomagic.uml2.ext.jmi.helpers import CoreHelper

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sysmlProf=StereotypesHelper.getProfile(project,"SysML")
sysmlSt=StereotypesHelper.getStereotypesByProfile(sysmlProf)
datapack=project.getModel()
noApply = {}

def validateLocation(modelPackage, validateOnly, ignorePack):
	sterPlace =[]
	custPlace =[]
	if validateOnly!=True: gl.log('[FIXES]: for Package, Stereotypes, and Customization Locations')
###Find profile package
	profilePackage = profilePackCheck(modelPackage)
	if not profilePackage: return ['No Profile Package']
###Find infrastructure package
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	if len(infraPack)>0: infraPack = infraPack[0]
	elif validateOnly != True:
		infraPack = ef.createPackageInstance()
		infraPack.setOwner(profilePackage)
		infraPack.setName("_")
		gl.log('[FIX]: Created "_" package')
	else: return['No "_" Package']
###Collect other packages	
	stereotypePackage = packageCheck(profilePackage, infraPack, validateOnly, "_Stereotypes")
	depSterPack = packageCheck(profilePackage, infraPack,  validateOnly, "_Deprecated Stereotypes")
	userscriptsPack = packageCheck(profilePackage, infraPack, validateOnly, "_UserScripts")
	customPackage = packageCheck(profilePackage, infraPack, validateOnly, "_Customizations")
	if not isinstance(stereotypePackage, Package) or not isinstance(customPackage, Package): return[stereotypePackage, customPackage, depSterPack]
###Find locations of all stereotypes and customizations
	allStereotypes = filter(lambda element: isinstance(element, Stereotype), getEverything(profilePackage))
	allCustomizations = filter(lambda element: isinstance(element, Class) and StereotypesHelper.hasStereotype(element, StereotypesHelper.getStereotype(project, "Customization")), getEverything(profilePackage))
	sterPlace = filter(lambda element: element.getOwner() != stereotypePackage, allStereotypes)
	sterPlace = filter(lambda element: element.getOwner()!=depSterPack, sterPlace)
	sterPlace = filter(lambda element: element.getOwner() != userscriptsPack, sterPlace)
	for p in ignorePack:
		sterPlace = filter(lambda element: element.getOwner() != p, sterPlace)
	userscriptPlace = filter(lambda element: element.getOwner()!= userscriptsPack and element.getName().find("opsrev")==0, allStereotypes)
	custPlace = filter(lambda element: element.getOwner()!=customPackage, allCustomizations)
###Move locations appropriately if set to fix
	if validateOnly!=True:
		setSterOwner(sterPlace, stereotypePackage, depSterPack, userscriptsPack)
		setSterOwner(userscriptPlace, stereotypePackage, depSterPack, userscriptsPack)
		setSterOwner(custPlace, customPackage, 'cust', 'beh')
###Check for icons
	sters = filter(lambda element: element.getOwner() != userscriptsPack, allStereotypes)
	sters = filter(lambda element: 'port' not in element.getName(), sters)
	sters = filter(lambda element: GeneralizationSet not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Dependency not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Association not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Package not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Operation not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Interface not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Enumeration not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Activity not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Signal not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	sters = filter(lambda element: Reception not in StereotypesHelper.getBaseClassesAsClasses(element), sters)
	for s in sters:
		icon = filter(lambda element: isinstance(element, Image), s.getOwnedElement())
		if not icon:
			gl.log('No icon for stereotype: ' + s.getName())
	return [stereotypePackage, depSterPack, customPackage, sterPlace, custPlace]

def setSterOwner(ster, usedPackage, unusedPackage, userscriptsPackage):	
###set stereotype owner (either stereotype or deprecated stereotypes package
	for s in ster:
		gl.log('[FIX]: Moved ' + s.getName())
		if s.getName().find("zz")==-1 and s.getName().find("opsrev")==-1:
			if isinstance(usedPackage, array): usedPackage=usedPackage[0]
			s.setOwner(usedPackage)
		elif s.getName().find("opsrev")==0:
			if isinstance(userscriptsPackage, array): userscripts = userscripts[0] 
			s.setOwner(userscriptsPackage)
		elif unusedPackage!= 'cust': 
			if isinstance(unusedPackage, array): unusedPackage=unusedPackage[0]
			s.setOwner(unusedPackage)
	return

def stereotypeGenCheck(modelPackage, validateOnly):
###Packages
	profilePackage = profilePackCheck(modelPackage)
	if not profilePackage: return ['No Profile Package']
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	if len(infraPack)>0: infraPack = infraPack[0]
	else: return['No underscore Package']
	stereotypePackage = packageCheck(profilePackage,  infraPack, validateOnly, "_Stereotypes")
	if not stereotypePackage: return['No Stereotype Package']	
	stereotypes =filter(lambda element: isinstance(element, Stereotype), stereotypePackage.getOwnedElement())
	badG = []
	badGs=[]
	noSys = []
	sBad = []

	for s in stereotypes:
		sGenerals = filter(lambda element: isinstance(element, Generalization), s.getOwnedElement())
		sGeneral =[]
		for sg in sGenerals:
			sGeneral.extend(s.getGeneral())
		sSysML = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) == StereotypesHelper.getProfile(project, "SysML"), sGeneral)
		sAdd = filter(lambda element: StereotypesHelper.getProfileForStereotype(element)==StereotypesHelper.getProfile(project, "additional_stereotypes"), sGeneral)
#		if StereotypesHelper.getProfileForStereotype(sGeneral) in ignoreProfile:
#			sSyMLInh = filter(lambda element: StereotypesHelper.getProfileForStereotype(element)==StereotypesHelper.getProfile(project, "SysML"), SR.getGeneralizationTree(sg))
###Collect stereotypes with no sysml generalization
		if len(sSysML)==0 and len(sAdd)==0: noSys.append(s)
###Collect stereotypes with generalizations to non sysml elements and not part of ignore profiles
		sBad = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) != StereotypesHelper.getProfile(project, "SysML"), sGeneral)
		sBad = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) != StereotypesHelper.getProfile(project, "additional_stereotypes"), sBad)
		sBad = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) != profilePackage, sBad)
		sBad = filter(lambda element: element.getOwner() != stereotypePackage, sBad)
#		for i in ignoreProfile:
#			sBad = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) != StereotypesHelper.getProfile(project, i.getName()), sBad)
		sBad = filter(lambda element: element not in stereotypes, sBad)
		for n in sBad:
			badG.append(filter(lambda element: isinstance(element, Generalization) and element.getGeneral() == n and element.getSpecific() == s, s.get_directedRelationshipOfSource()))
	badG = flatten(badG)			
	if validateOnly!=True:
		for b in badG:
			gl.log('[FIX]: removing generalization between '+ b.getGeneral().getName() + ' and '+ b.getSpecific().getName())
			mem.removeElement(b)
###refine stereotypes with no sysml generalization collection
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Package") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Dependency") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project,"Connector") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Operation") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Diagram") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Port") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Activity") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "DataType") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "GeneralizationSet") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Association") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Reception") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Interaction") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Interface") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Enumeration") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Constraint") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "Signal") not in StereotypesHelper.getBaseClasses(element), noSys)
	noSys = filter(lambda element: StereotypesHelper.getMetaClassByName(project, "SignalEvent") not in StereotypesHelper.getBaseClasses(element), noSys)
	if validateOnly != True: gl.log('['+ str(len(noSys))+'] possible SysML specializations. No codified fix for stereotypes that can specialize SysML.  Modeler must do. ')
	return [sBad, noSys]

def flatten(x):
    result = []
    for el in x:
        if hasattr(el, "__iter__") and not isinstance(el, basestring): result.extend(flatten(el))
        else: result.append(el)
    return result
   
def metaclassCheck(modelPackage, validateOnly):
###get stereotype package
	if not isinstance(modelPackage, Profile):
		profilePackage = getProfilePackage(modelPackage)
		if not profilePackage:
			gl.log('****ERROR: The code is not being run on a Profile. Stopping Execution')
			return
	else: profilePackage = modelPackage
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	if len(infraPack)>0: infraPack = infraPack[0]
	else: return['No underscore Package']
	stereotypePackage = filter(lambda element: isinstance(element, Package) and "_Stereotypes" in element.getName(), infraPack.getOwnedElement())
	if len(stereotypePackage)==0:
		gl.log('****ERROR: There is no package named "_Stereotypes" that is a direct child of the Profile. Stopping Execution')
		return ['no stereotype Package']
	else: stereotypePackage = stereotypePackage[0]
	sMMeta=[]
####Collect stereotypes with >1 metaclasses
	for s in stereotypePackage.getOwnedElement():
		if isinstance(s, Stereotype):
			sMeta = StereotypesHelper.getBaseClasses(s)
			if len(sMeta)>1:
				sMMeta.append(s)
	if validateOnly!= True:
		gl.log('There is no codified fix for this problem.  Modeler must rectify')
	return sMMeta

def profilePackCheck(modelPackage):
	if not isinstance(modelPackage, Profile):
		profilePackage = profilePackCheck(modelPackage.getOwner())
		if not profilePackage: 	
			gl.log('[ERROR]: The code is not being run on a Profile.  Stopping Execution')
			return
	else: 
		profilePackage = modelPackage
	return profilePackage


def packageCheck(profilePackage, infraPack, validateOnly, pName):
	package = filter(lambda element: isinstance(element, Package) and pName in element.getName(), profilePackage.getOwnedElement())
	packageI = filter(lambda element: isinstance(element, Package) and pName in element.getName(), infraPack.getOwnedElement())
	if len(package)==0 and len(packageI)==0:
		if validateOnly != True:
			package=ef.createPackageInstance()
			package.setOwner(infraPack)
			package.setName(pName)
			gl.log('[FIX]: created ' + pName +' Package')
		else: 
			return ['No package found']
	elif len(package)>0:
		if validateOnly!=True:
			package[0].setOwner(infraPack)
			package = package[0]
		else: 
			return ['Package in wrong place']
	else: package=packageI[0]
	return package
	
def stereotypesForElements(modelPackage, validateOnly, elementType, stereotypesIgnore, packagesIgnore):
	if not stereotypesIgnore:
		stereotypesIgnore=[]
	profilePackage = profilePackCheck(modelPackage)
	if not profilePackage: return ['no Profile Package', 'no Profile Package', 'no Profile Package', 'no Profile Package']
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	if len(infraPack)>0: infraPack = infraPack[0]
	else: return['no underscore pacakage, no underscore pacakage,no underscore pacakage,no underscore pacakage']
	stereotypePackage=packageCheck(profilePackage, infraPack, validateOnly, "_Stereotypes")
	if not stereotypePackage: return ['no Stereotype Package', 'no Stereotype Package', 'no Stereotype Package', 'no Stereotype Package']
	baseClass = getElements(getEverything(modelPackage), profilePackage)
	baseClass = filter(lambda element: "_" not in element.getName(), baseClass)
	if not isinstance(modelPackage, Profile): baseClass.append(modelPackage)
###intialize counts
	sterCreate = 0
	nameChange = 0
	unapply = 0
	apply = 0
###
	if elementType != 'Misc':
		baseClass = filter(lambda element: isinstance(element, elementType), baseClass)
		for s in stereotypesIgnore: baseClass = filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, s) == False, baseClass)
		ignoredPackEls = []
		for i in packagesIgnore: baseClass = filter(lambda element: element not in getEverything(i), baseClass)
		result = getMissing(baseClass, filter(lambda element: isinstance(element, NamedElement),stereotypePackage.getOwnedElement()))
		bNoSter = result[0]
		bWrongSter = result[1]
		bWrongMeta = result[2]
		applySter = []
		for n in noApply:
			if n not in bWrongSter and n not in bWrongMeta and n in baseClass: applySter.append(n)
		if validateOnly!=True:
			('[FIXING]:' + str(elementType))
			successful = []
			bWS = []
			bWM=[]
			noFix=[]
			for b in bNoSter:
				bStillMissing = getMissing([b], filter(lambda element: isinstance(element, NamedElement), stereotypePackage.getOwnedElement()))
				bWS = bStillMissing[1]
				bWM = bStillMissing[2]
				if bStillMissing[0]:
					###check if any local stereotype is applied
					bLocalSter =  filter(lambda element: element.getOwner() == stereotypePackage, StereotypesHelper.getStereotypes(b))
					if len(bLocalSter)>0:
						for bL in bLocalSter:
							localSterApply = filter(lambda element:  bL in StereotypesHelper.getStereotypes(element), getElements(getEverything(profilePackage), profilePackage))
							if len(localSterApply)==1:
								###if local stereotype is applied to only this element in the base class then change the name of stereotype. Otherwise create new ster and unapply this one.
								bL.setName(propertyCheck(b))
								gl.log('[FIX]:Stereotype name was changed to match applied element name: ' + b.getQualifiedName())
								nameChange+=1
							elif filter(lambda element: propertyCheck(element) == bL.getName(), getElements(getEverything(profilePackage), profilePackage)):
								createStereotype(stereotypePackage, b, stereotypePackage)
								unapplyStereotypes([b])
								gl.log('[FIX]:Stereotype Created ' +b.getName())
								gl.log('\t\t Location = ' + b.getQualifiedName())	
								sterCreate+=1
							else:
								###get sysml extension, unapply stereotype and apply sysml extension
								bLgeneralizations = getGeneralizationTree(bL)
								bLSysGen = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) == StereotypesHelper.getProfile(project, "SysML") or StereotypesHelper.getProfileForStereotype(element) == StereotypesHelper.getProfile(project, "additional_stereotypes"), bLgeneralizations)
								unapplyStereotypes([b])
								if bLSysGen:
									StereotypesHelper.addStereotype(b, bLSysGen[0])
								gl.log('[FIX]:Stereotype applied to multiple elements, none with matching name.  Unapplied stereotype:' + bL.getName())
								unapply+=1
								createStereotype(stereotypePackage, b, stereotypePackage)
								unapplyStereotypes([b])
								gl.log('[FIX]:Stereotype Created ' +b.getName())
								gl.log('\t\t Location = ' + b.getQualifiedName())
								sterCreate+=1
					else:
						createStereotype(stereotypePackage, b, stereotypePackage)
						unapplyStereotypes([b])
						gl.log('[FIX]:Stereotype Created ' +b.getName())
						gl.log('\t\t Location = ' + b.getQualifiedName())
						sterCreate+=1
			for b in bWS:
				if not isinstance(b, Interface) and StereotypesHelper.getBaseClassAsClasses(b)[0] == Port:
					if b.getName().find("part property")==0:
						bPort = filter(lambda element: element.getName() == b.getName() + " port", filter(lambda element: isinstance(element, NamedElement), stereotypePackage.getOwnedElement()))
						if len(bPort)!=0:
							bApplied = filter(lambda element: StereotypesHelper.isElementStereotypedBy(element, b.getName())==True, baseClass)
							for f in bApplied:
								StereotypesHelper.removeStereotypes(f, [b])
								StereotypesHelper.addStereotype(f, bPort)
			###Apply applicable stereotypes
			for b in noApply:
				if b not in bWrongSter and b not in bWrongMeta and b not in bWS and b not in bWM and b in baseClass:
					if StereotypesHelper.getBaseClasses(noApply[b])[0].getName() != 'Element':
						StereotypesHelper.addStereotype(b, noApply[b])
						unapplyStereotypes([b])
						gl.log('[FIX]: Stereotype Applied ' + b.getName())
						gl.log('\t\t Location = '+b.getQualifiedName())
						apply+=1
			if len(bWrongSter)>0: gl.log('[UNFIXABLE]: Inheritance Mismatch (see validation log for details)')
			for b in bWrongMeta:
					gl.log('[UNFIXABLE]: Metaclass Mismatch: '+ b.getQualifiedName())
#		if elementType == Classifier and validateOnly!=True: localGeneralizations(modelPackage, validateOnly, True)
###Messages Log
		if apply!=0 or sterCreate!=0 or nameChange !=0 or unapply!=0:
			gl.log('[FIX SUMMARY]:')
			gl.log(str(apply) + " existing stereotype(s) applied")
			gl.log(str(sterCreate) + " stereotypes created")
			gl.log(str(nameChange) + " stereotype names changed")
			gl.log(str(unapply) + " stereotypes unapplied")
		else: gl.log('No Fixes Needed')
		if len(bWrongSter)!=0 or bWrongMeta!=0: 
			gl.log('[UNFIXABLE SUMMARY]:')
			if len(bWrongSter)>0: gl.log(str(len(bWrongSter))+ " stereotype inheritance mismatch")
			if len(bWrongMeta)>0: gl.log(str(len(bWrongMeta))+ " stereotype metaclasses and element types not matching")
		else: gl.log('No modeler fixes needed')
################
		return [bNoSter, bWrongSter, bWrongMeta, applySter]
	else:
		baseClass = filter(lambda element: not isinstance(element, Class), baseClass)
		baseClass = filter(lambda element: not isinstance(element, Package), baseClass)
		baseClass = filter(lambda element: not isinstance(element, Property), baseClass)
		baseClass = filter(lambda element: not isinstance(element, Operation), baseClass)
		baseClass = filter(lambda element: not isinstance(element, Diagram), baseClass)
		baseClass = filter(lambda element: not isinstance(element, Dependency), baseClass)
		for s in stereotypesIgnore: baseClass = filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, s) == False, baseClass)
		ignoredPackEls = []
		for i in packagesIgnore: baseClass = filter(lambda element: element not in getEverything(i), baseClass)
		resultMisc = getMissing(baseClass, stereotypePackage.getOwnedElement())
		mNoSter = resultMisc[0]
		mWrongSter = resultMisc[1]
		mWrongMeta = resultMisc[2]
		applySter = []
		for n in noApply:
			if n not in mWrongSter and n not in mWrongMeta and n in baseClass: applySter.append(n)
		if validateOnly!=True:
			gl.log('[FIX]:' + str(elementType))
			successful = []
			for b in mNoSter:
				mStillMissing = getMissing([b], stereotypePackage.getOwnedElement())
				if mStillMissing[0]:
					createStereotype(stereotypePackage, b, stereotypePackage)
					unapplyStereotypes([b])
			    	gl.log('\t Stereotype Created ' +b.getName())
			    	gl.log('\t\t Location = ' + b.getQualifiedName())
			for b in noApply:
				if b not in mWrongSter and b not in mWrongMeta and n in baseClass:
					StereotypesHelper.addStereotype(b, noApply[b])
					unapplyStereotypes([b])
					gl.log('\t Stereotype Applied ' + b.getName())
					gl.log('\t\t Location = '+b.getQualifiedName())
			if len(mWrongSter)>0: gl.log('UNFIXABLE: Inheritance Mismatch (see validation log for details)')
			if len(mWrongMeta)>0: gl.log('UNFIXABLE: Metaclass Mismatch (see validation log for details)')
		return [mNoSter, mWrongSter, mWrongMeta, applySter]

def localGeneralizations(modelPackage, validateOnly, createOnly, sterIgnore):
###Finds generalizations that exist in the base class and checks to see if they exist in the stereotypes. 
	badGen = 0
	warn = 0
	error = 0
	profilePackage = profilePackCheck(modelPackage)
	if not profilePackage: return ['no Profile Package', 'no Profile Package', 'no Profile Package', 'no Profile Package']
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	if len(infraPack)>0: infraPack = infraPack[0]
	else: return['No underscore package','No underscore package','No underscore package','No underscore package']
	stereotypePackage=packageCheck(profilePackage, infraPack, validateOnly, "_Stereotypes")
	if not stereotypePackage: return ['no Stereotype Package', 'no Stereotype Package', 'no Stereotype Package', 'no Stereotype Package']
	stereotypesGen = getEverything(stereotypePackage)
	stereotypesGen = flatten(stereotypesGen)
	sters = filter(lambda element: isinstance(element, Stereotype), stereotypesGen)
	stereotypesGen = filter(lambda element: isinstance(element, Generalization), stereotypesGen)
	localstereotypeGen=filter(lambda element: getProfilePackage(element.getGeneral().getOwner()) == profilePackage and getProfilePackage(element.getSpecific().getOwner()) == profilePackage, stereotypesGen)	
	baseGen = filter(lambda element: isinstance(element, Generalization) and element not in stereotypesGen, getEverything(modelPackage))
	baseGen = filter(lambda element: isinstance(element.getGeneral(), Class), baseGen)
	everything = getEverything(profilePackage)
	BG= []
	# [CYL:2012/10/03] Define these variables since they may not get defined if the if statements are skipped
	sterNoMatch = []
	baseNoMatch = []
	for b in baseGen:
		if b.getGeneral() in everything and b.getSpecific() in everything:
			BG.append(b)
###Check for stereotype Generalizations not in base
	if len(localstereotypeGen)>0:
		sterCheck = generalizationCheck(localstereotypeGen, BG, sterIgnore)
		sterMatch = sterCheck[0]
		sterNoMatch.extend(sterCheck[1])
###FIX->remove local generalizations that exist in stereotypes but not in base
		if validateOnly != True and createOnly!=True:
			for s in sterNoMatch:
				gl.log('[FIX] Removed Stereotype Generalization: '+ s.getGeneral().getName() + ' to '+ s.getSpecific().getName())
				mem.removeElement(s)
###Check for base generalizations not in stereotypes
	stereotypesGen=filter(lambda element: isinstance(element, Generalization), flatten(getEverything(stereotypePackage)))
	localstereotypeGen=filter(lambda element: getProfilePackage(element.getGeneral().getOwner()) == profilePackage and getProfilePackage(element.getSpecific().getOwner()) == profilePackage, stereotypesGen)
	baseCheck = generalizationCheck(BG, localstereotypeGen, sterIgnore)
	baseMatch = baseCheck[0]
	baseNoMatch.extend(baseCheck[1])
###Fix add base generalizations into stereotypes
	if validateOnly != True:
		genError=[]
		for s in baseNoMatch:
			sterParent = filter(lambda element: element.getName()==s.getGeneral().getName(), sters)
			if len(sterParent)!=0: sterParent = sterParent[0]
			else: sterParent = 0
			sterChild = filter(lambda element: element.getName()== s.getSpecific().getName(), sters)
			if len(sterChild)!=0: sterChild = sterChild[0]
			else: sterChild= 0
			if sterChild!=0 and sterParent!=0: 
###Check for potential cyclic generalizations
				Gen = sterChild.get_generalizationOfGeneral()
				cycle = filter(lambda element: element.getSpecific()==sterParent, Gen)
				if len(cycle)>0:
					gl.log('[WARNING]: Fix not implemented, cyclic generalization: '+ sterParent.getQualifiedName() + ' and '+ sterChild.getQualfiedName())
					warn+=1
				else:
					newg = ef.createGeneralizationInstance()
					newg.setGeneral(sterParent)
					newg.setSpecific(sterChild)
					newg.setOwner(sterChild)
					gl.log('[FIX] Created Stereotype Generalization:' + sterParent.getName() + " to " + sterChild.getName())
			else: genError.append(s)
		if len(genError)>0:
			for g in genError:
				error+=1
				gl.log('[ERROR] '+ g.getGeneral().getName() + " to " + g.getSpecific().getName() + " couldn't be made")
		gl.log('[FIX SUMMARY]: ')
		gl.log('[ERRORS] = ' + str(error))
		gl.log('[WARNINGS] = ' + str(warn))
	return [sterNoMatch, baseNoMatch]
	
	
	
def generalizationCheck(setA, setB, sterIgnore):
	match = []
	noMatch = []
	ignore = []
	setB = filter(lambda element: element not in sterIgnore, setB)
	for s in setA:
		check = filter(lambda element: element.getGeneral().getName() == s.getGeneral().getName() and element.getSpecific().getName() == s.getSpecific().getName(), setB)
		if len(check)>0:
			if not StereotypesHelper.hasStereotypeOrDerived(s.getGeneral(), sterIgnore) and not StereotypesHelper.hasStereotypeOrDerived(s.getSpecific(),sterIgnore): 
				match.append(s)	
			else: 
				ignore.append(s)
		else: 
			if not StereotypesHelper.hasStereotypeOrDerived(s.getGeneral(), sterIgnore) and not StereotypesHelper.hasStereotypeOrDerived(s.getSpecific(), sterIgnore):
				noMatch.append(s)
	return [match, noMatch]

def getElements(allels, profilePackage):
	sterPack = filter(lambda element: isinstance(element, Package) and element.getName() == "_Stereotypes", profilePackage.getOwnedElement())
	custPack = filter(lambda element:isinstance(element, Package) and element.getName() == "_Customizations", profilePackage.getOwnedElement())
	depPack = filter(lambda element: isinstance(element, Package) and element.getName() == "_Deprecated Stereotypes", profilePackage.getOwnedElement())
	depCust = filter(lambda element: isinstance(element, Package) and element.getName() == "_Deprecated Customizations", profilePackage.getOwnedElement())
	unusedSter = filter(lambda element: isinstance(element, Package) and element.getName() == '_Unused Stereotypes', profilePackage.getOwnedElement())
	baseClass = filter(lambda element: element.getOwner() != sterPack, allels)
	baseClass = filter(lambda element: element.getOwner() != custPack, baseClass)
	baseClass = filter(lambda element: element.getOwner()!=depPack, baseClass)
	baseClass = filter(lambda element: element.getOwner()!= depCust, baseClass)
	baseClass = filter(lambda element: element.getOwner()!=unusedSter, baseClass)
	baseClass = filter(lambda element: isinstance(element, NamedElement), baseClass)
	baseClass = filter(lambda element: element.getName() != "_Stereotypes" and element.getName() != "_Customizations" and element.getName() != "_Deprecated Stereotypes" and element.getName() != "_Deprecated Customizations", baseClass)
	baseClass = filter(lambda element: not isinstance(element, Stereotype), baseClass)
	baseClass = filter(lambda element: StereotypesHelper.getStereotype(project, "Customization") not in StereotypesHelper.getAllAssignedStereotypes([element]), baseClass)
#	baseClass = filter(lambda element: not isinstance(element, Diagram), baseClass)
	baseClass = filter(lambda element: not isinstance(element, Comment), baseClass)
	baseClass = filter(lambda element: StereotypesHelper.getStereotype(project, "FrameworkValidation.Profile") not in StereotypesHelper.getAllAssignedStereotypes([element]), baseClass)
	#    baseClass = filter(lambda element: not isinstance(element, Property), baseClass)
	baseClass = filter(lambda element: not isinstance(element, InputPin), baseClass)
	baseClass = filter(lambda element: not isinstance(element, OutputPin), baseClass)
	baseClass = filter(lambda element: not isinstance(element, ActivityParameterNode), baseClass)
	baseClass = filter(lambda element: not isinstance(element, Parameter), baseClass)
	baseClass = filter(lambda element: not isinstance(element, CallBehaviorAction), baseClass)
#	baseClass = filter(lambda element: not isinstance(element, Operation), baseClass)
	baseClass = filter(lambda element: not isinstance(element, EnumerationLiteral), baseClass)
	baseClass = filter(lambda element: not isinstance(element, ObjectFlow), baseClass)
	baseClass = filter(lambda element: not isinstance(element, Interaction), baseClass)
#	baseClass = filter(lambda element: not isinstance(element, StateMachine), baseClass)
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

def unapplyStereotypes(elements):
    for e in elements:
        eSter = StereotypesHelper.getAllAssignedStereotypes([e])
        suffix = [' part property', ' constraint property', ' reference property',' shared property', ' value property', ' shared property', ' distributed property', ' port', ' flow property']
        names = []
        for s in suffix:
        	names.append(e.getName() + s)
        names.append(e.getName())
        names.append('Customization')
        for s in eSter:
            if s.getName() not in names:
                StereotypesHelper.removeStereotype(e, s)
    return 
       
def getMissing(A, B):
	missing =[]
	nameMatch = {}
	metaMismatch=[]
	applyMismatch=[]
	for a in A:
		aName = a.getName()
		if isinstance(a, Property):
			aName = propertyCheck(a)
		elif isinstance(a, Stereotype) and StereotypesHelper.getBaseClassesAsClasses(a)[0] == Property:
			aGens = filter(lambda element: isinstance(element, Generalization), a.get_directedRelationshipOfSource())
			if len(filter(lambda element: element.getGeneral() == StereotypesHelper.getStereotype(project, "ConstraintProperty"), aGens))!=0:
				if a.getName().find("constraint property")==-1: aName = a.getName() + " constraint property"
			elif len(filter(lambda element: element.getGeneral() == StereotypesHelper.getStereotype(project, "SharedProperty"), aGens))!=0:
				if a.getName().find("shared property")==-1: aName = a.getName() + " shared property"
			elif len(filter(lambda element: element.getGeneral() == StereotypesHelper.getStereotype(project, "DistributedProperty"), aGens))!=0:
				if a.getName().find("distributed property")==-1: aName = a.getName() + " distributed property"
			elif len(filter(lambda element: element.getGeneral() == StereotypesHelper.getStereotype(project, "FlowProperty"), aGens))!=0:
				if a.getName().find("flow property")==-1: aName = a.getName() + " flow property"
			elif len(filter(lambda element: element.getGeneral() == StereotypesHelper.getStereotype(project, "ValueProperty"), aGens))!=0:
				if a.getName().find("value property")==-1: aName = a.getName() + " value property"
			elif len(filter(lambda element: element.getGeneral() == StereotypesHelper.getStereotype(project, "ReferenceProperty"), aGens))!=0:
				if a.getName().find("reference property")==-1: aName = a.getName() + " reference property"
			else:
				if a.getName().find("part property")==-1: aName=a.getName() + " part property"
		elif isinstance(a, Stereotype) and StereotypesHelper.getBaseClassAsClasses(a)[0] == Port:
			if a.getName().find("port")==-1: aName = a.getName() + " port"
		bExist = 0
		for b in B:
			if aName == b.getName():
				nameMatch[a]=b
				bExist = 1
				if b not in StereotypesHelper.getAllAssignedStereotypes([a]):
					noApply[a]=b
		if bExist == 0:
			missing.append(a)
	for n in nameMatch:
		###get generalizations of stereotype
		stereotype = nameMatch[n]
		nGeneral = filter(lambda element: isinstance(element, Generalization), stereotype.get_directedRelationshipOfSource())
		general = []
		for g in nGeneral:
			general.append(g.getGeneral())
		###keep only SysML generalizations
		gSys = filter(lambda element: StereotypesHelper.getProfileForStereotype(element) == StereotypesHelper.getProfile(project, "SysML"), general)
		if gSys and not StereotypesHelper.checkForAllDerivedStereotypes(n, gSys[0]): applyMismatch.append(n)
		###check if same
		if n.getName() != "_Unused Stereotypes":
			if not isinstance(n, StereotypesHelper.getClassOfMetaClass(StereotypesHelper.getBaseClasses(nameMatch[n])[0])): metaMismatch.append(n)  
	return [missing, applyMismatch, metaMismatch]

def assignCustomizationTags(stereotype, customBlock, supertype):
	StereotypesHelper.setStereotypePropertyValue(customBlock, StereotypesHelper.getStereotype(project, "Customization"), "customizationTarget", stereotype)
	if supertype != False: StereotypesHelper.setStereotypePropertyValue(customBlock, StereotypesHelper.getStereotype(project, "Customization"), "superTypes", supertype)
	else: gl.log('[FIX ERROR]: no supertype found for customization: ' + customBlock.getName())
	StereotypesHelper.setStereotypePropertyValue(customBlock, StereotypesHelper.getStereotype(project, "Customization"), "hideMetatype", False)
	return    
     
def flatten(x):
    result = []
    for el in x:
        if hasattr(el, "__iter__") and not isinstance(el, basestring):
            result.extend(flatten(el))
        else:
            result.append(el)
    return result
   
def createCustomization(stereotypeName, owner):
	new = ef.createClassInstance()
	new.setOwner(owner)
	new.setName(stereotypeName)
	StereotypesHelper.addStereotype(new, StereotypesHelper.getStereotype(project, "Customization"))
	return new
	        
def createStereotype(owner, element, stereotypePackage):
	'''append Package to any package element, this is an OpsRev naming convention '''
	if isinstance(element, Package):
	 	package = element.getName().find("Package")
	 	if package == -1:
			element.setName(element.getName() + " Package")   
	'''stereotypes should not be made for enumeration literals'''         
	if not isinstance(element, EnumerationLiteral):
	    eSter = StereotypesHelper.getAllAssignedStereotypes([element])
	    elementSterName = propertyCheck(element)
	    newSter = StereotypesHelper.createStereotype(owner, elementSterName, [StereotypesHelper.getBaseClass(element)])
	    StereotypesHelper.addStereotype(element, newSter)
	    for e in eSter:
	    	if StereotypesHelper.getProfileForStereotype(e) == StereotypesHelper.getProfile(project, "SysML") or StereotypesHelper.getProfileForStereotype(e) == StereotypesHelper.getProfile(project, "additional_stereotypes"):
		    	newgen = ef.createGeneralizationInstance()
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
	        if isinstance(element, Class) and e.getOwner() == stereotypePackage:
		    	newgen = ef.createGeneralizationInstance()
		    	newgen.setGeneral(e)
		    	newgen.setSpecific(newSter)
		    	newgen.setOwner(newSter)
		    	icon = filter(lambda element: isinstance(element, Image), e.getOwnedElement())
		    	if icon:
		    		icon = icon[0]
		    		newicon = ef.createImageInstance()
		    		newicon.setLocation(icon.getLocation())
		    		newicon.setContent(icon.getContent())
		    		newicon.setFormat(icon.getFormat())
		    		newicon.setOwner(newSter)
	return

def propertyCheck(element):
	'''append part property to any Property or constraint property or port element, this is an OpsRev naming convention '''
	elementSterName = element.getName()
 	if isinstance(element, Property):
 		if StereotypesHelper.isElementStereotypedBy(element, "ConstraintProperty"):
 			if element.getName().find("constraint property") == -1:
 				elementSterName = element.getName() + " constraint property"
 		elif StereotypesHelper.isElementStereotypedBy(element, "PartProperty"):
 			if element.getName().find("part property") == -1:
 				elementSterName = element.getName() + " part property"
	 	elif isinstance(element, Port):
			if element.getName().find("port") == -1:
				elementSterName = element.getName()+ " port"
		elif StereotypesHelper.isElementStereotypedBy(element, "SharedProperty"):
			if element.getName().find("shared property")==-1:
				elementSterName = element.getName() + " shared property"
		elif StereotypesHelper.isElementStereotypedBy(element, "ValueProperty"):
			if element.getName().find("value property")==-1:
				elementSterName = element.getName() + " value property"
		elif StereotypesHelper.isElementStereotypedBy(element,"ReferenceProperty"):
			if element.getName().find("reference property")==-1:
				elementSterName = element.getName() + " reference property"
		elif StereotypesHelper.isElementStereotypedBy(element, "DistributedProperty"):
			if element.getName().find("distributed property")==-1:
				elementSterName = element.getName() + " distributed property"
		elif StereotypesHelper.isElementStereotypedBy(element, "FlowProperty"):
			if element.getName().find("flow property")==-1:
				elementSterName = element.getName() + " flow property"
	return elementSterName

def createPackage(name, owner):
    new = ef.createPackageInstance()
    new.setOwner(owner)
    new.setName(name)
    return new

def createElement(stereotype, owner,checkEnum):
    new = None
    mclass = StereotypesHelper.getBaseClassesAsClasses(stereotype)
    for m in mclass:
        insCreate = str(m)
        findtrunc = insCreate.split(".")
        insCreate = findtrunc[len(findtrunc)-1]
        insCreate = insCreate[:-2]
        if insCreate != "Property" and insCreate != "Association" and insCreate != "Dependency" and insCreate!= "ObjectFlow" and insCreate != "Relationship" and insCreate != "Operation" and insCreate!= "EnumerationLiteral":
            if insCreate == "Element":
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

def getGeneralizationTree(e, tree=None):
	'''returns a map of element to list of elements it specializes, starting from e'''
	res = tree
	if not res:
		res = {}
	parents = []
	for r in e.get_directedRelationshipOfSource():
		if isinstance(r, Generalization):
			parents.append(CoreHelper.getSupplierElement(r))
	#if isinstance(e, Class):
	#    parents = e.getSuperClass()
	res[e] = parents
	for super in parents:
		getGeneralizationTree(super, res)
	return res