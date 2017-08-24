'''
Created on Sep 20, 2011

@author: efosse
'''
from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
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

import traceback

import ProfileValidation as VP
reload(VP)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sysmlProf=StereotypesHelper.getProfile(project,"SysML")
sysmlSt=StereotypesHelper.getStereotypesByProfile(sysmlProf)




def deprecateStereotypes(modelPackage, validateOnly):
	if not isinstance(modelPackage, Profile):
	    profilePackage = VP.getProfilePackage(modelPackage)
	    if not profilePackage:
	        gl.log('***ERROR: The code is not being run on a Profile.  Stopping Execution***')
	        return
	else:
	    profilePackage = modelPackage
	energon = filter(lambda element: isinstance(element, Package) and element.getName() == "_Energon", profilePackage.getOwnedElement())
#	if energon:
#		energon = energon[0]
#		energon = filter(lambda element: isinstance(element, Package) and element.getName() =="_Stereotypes", energon.getOwnedElement())[0]
#	else:
#		gl.log("INFORMATION: Energon Package not present.")
#	gl.log('**FIX: Deprecating Stereotypes')
	allElements = VP.getEverything(modelPackage)
	allCustomizations = filter(lambda element: StereotypesHelper.hasStereotype(element, "Customization"), allElements)
	unusedSterPack = filter(lambda element: isinstance(element, Package) and "_Deprecated Stereotypes" in element.getName(), profilePackage.getOwnedElement())
	if not unusedSterPack: 
		underscore = filter(lambda element: isinstance(element, Package) and "_" == element.getName(), profilePackage.getOwnedElement())
		if underscore:	unusedSterPack = filter(lambda element: isinstance(element, Package) and "_Deprecated Stereotypes" in element.getName(), underscore[0].getOwnedElement())[0]
		else: return['no deprecated package']
	else: unusedSterPack = unusedSterPack[0]
	
	'''find unused stereotypes'''
	baseClass = filter(lambda element: isinstance(element, NamedElement), allElements)
	baseClass = filter(lambda element:"_Stereotypes" not in element.getName() or "_Customizations" not in element.getName() or "_Deprecated Stereotypes" not in element.getName(), baseClass)
	baseClass = filter(lambda element: not StereotypesHelper.hasStereotype(element, "Customization"), baseClass)
	if profilePackage != modelPackage:
		baseClass.append(modelPackage)
		baseClass = VP.flatten(baseClass)
	properties = filter(lambda element: isinstance(element, Property) and element.getName() != "", baseClass)
	'''Validate Package Org: all stereotypes in one top level stereotypes package; all customizations in one top level customizations package.'''
	infraPack = filter(lambda element: isinstance(element, Package) and "_"==element.getName(), profilePackage.getOwnedElement())
	stereotypeIpack =[]
	customIpack = []
	userScriptsIpack=[]
	if len(infraPack)!=0:
		infraPack = infraPack[0]
		stereotypeIpack = filter(lambda element: isinstance(element, Package) and "_Stereotypes" in element.getName(), infraPack.getOwnedElement())
		customIpack = filter(lambda element: isinstance(element, Package) and "_Customizations" in element.getName(), infraPack.getOwnedElement())
		userScriptsIpack = filter(lambda element: isinstance(element, Package) and "_UserScripts" in element.getName(), infraPack.getOwnedElement())
 	stereotypePackage = filter(lambda element: isinstance(element, Package) and "_Stereotypes" in element.getName(), profilePackage.getOwnedElement())
 	customPackage = filter(lambda element: isinstance(element, Package) and "_Customizations" in element.getName(), profilePackage.getOwnedElement())
 	userScriptsPack = filter(lambda element: isinstance(element, Package) and "_UserScripts" in element.getName(), profilePackage.getOwnedElement())
	if not stereotypePackage:
		if len(stereotypeIpack)>0: stereotypePackage = stereotypeIpack[0]
		else:
			gl.log('======No Stereotype Package Exists for This Profile! All stereotypes are in the wrong place. Stopping Execution; Run Validate Location set to "fix"======')
			return ['no stereotype Package']
	else:
	    stereotypePackage = stereotypePackage[0]
	if not customPackage:
		if len(customIpack)>0: customPackage = customIpack[0]
		elif len(customIpack)==0:
			gl.log('======No Customization Package Exists for This Profile! All customizations are in the wrong place. Stopping Execution; Run Validation Location set to "fix"======')
			return ['no customization Package']
	else:
	    customPackage = customPackage[0]
	if not userScriptsPack:
		if len(userScriptsIpack)>0: userScriptsPack = userScriptsIpack[0]
		elif len(userScriptsIpack)==0: gl.log('=======Warning! No UserScripts Package Exists for this Profile.')
	appliedStereotypes = StereotypesHelper.getAllAssignedStereotypes(baseClass)
	unassignedStereotypes = filter(lambda element: element not in appliedStereotypes, filter(lambda element: isinstance(element, Stereotype), stereotypePackage.getOwnedElement()))
	if not userScriptsPack:
		unassignedStereotypes = filter(lambda element: element.getOwner() != userScriptsPack, unassignedStereotypes)
	for u in unassignedStereotypes:
		if validateOnly != True:
			gl.log('FIX: moving '+ u.getQualifiedName() + ' to deprecated stereotypes package')
			depr = u.getName().find("zz")
			if depr == -1:
				u.setName("zz"+u.getName())
			u.setOwner(unusedSterPack)
	gl.log("Number of stereotypes in profile not being applied in base class: " + str(len(unassignedStereotypes)))
	if validateOnly!= True:
		killCust(unusedSterPack, customPackage)
		gl.log('======Deprecate Stereotypes Summary')  
		if not stereotypePackage:  
		    gl.log('*WARNING: No Stereotype Package Exists for This Profile! All stereotypes are in the wrong place*')
		
		if not customPackage:
		    gl.log('*WARNING: No Customization Package Exists for This Profile! All stereotypes are in the wrong place*')
		gl.log("======Deprecation Summary=======")
		gl.log(str(len(unassignedStereotypes)) + ' stereotypes have no matching element. They were moved to unused stereotypes package, customizations deleted')
		gl.log("***Deprecation Complete***")
	return unassignedStereotypes

def killCust(unusedSterPack, customPackage):
	for u in filter(lambda element: isinstance(element, Stereotype), unusedSterPack.getOwnedElement()):
		gl.log('removing generalizations and associations for: ' + u.getName())
		gens = filter(lambda element: isinstance(element, Generalization) or isinstance(element, Association), u.getOwnedElement())
		killGen(gens)
		deprCust = filter(lambda element: element.getName() == u.getName()[2:], filter(lambda element: isinstance(element, NamedElement),customPackage.getOwnedElement()))
		if deprCust:
			gl.log('remove customization for: ' + u.getName())
			mem.removeElement(deprCust[0])
	return

def killGen(owned):
	for o in owned:
		mem.removeElement(o)
	


def run(mode):
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    try:
        SessionManager.getInstance().createSession("syncProfile")
        deprecateStereotypes(selected)
        SessionManager.getInstance().closeSession()
    except:
        if SessionManager.getInstance().isSessionCreated():
            SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        gl.log("*** EXCEPTION:")
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)