'''
Created on Oct 3, 2011

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


import sys
import traceback
import os
import CreateStereotypesForElements as CSE
reload(CSE)
import OrganizeProfilePackages as OPP
reload(OPP)
import CreateCustomizationsForStereotypes as CCS
reload(CCS)
import CloneBaseClass as CBC
reload(CBC)
import FixNames as FN
reload(FN)
import DeprecateStereotypes as DS
reload(DS)

gl = Application.getInstance().getGUILog()
options = {}
if len(scriptInput['createStereotypesForClassifiers'])>0 and scriptInput['createStereotypesForClassifiers'][0]:
	options['createStereotypesForClassifiers'] = True
else:
	options['createStereotypesForClassifiers'] = False
if len(scriptInput['createStereotypesForProperties'])>0 and scriptInput['createStereotypesForProperties'][0]:
	options['createStereotypesForProperties'] = True
else:
	options['createStereotypesForProperties'] = False
if len(scriptInput['createStereotypesForOperations'])>0 and scriptInput['createStereotypesForOperations'][0]:
	options['createStereotypesForOperations'] = True
else:
	options['createStereotypesForOperations'] = False
if len(scriptInput['createStereotypesForPackages'])>0 and scriptInput['createStereotypesForPackages'][0]:
	options['createStereotypesForPackages'] = True
else:
	options['createStereotypesForPackages'] = False
if len(scriptInput['createStereotypesForMisc'])>0 and scriptInput['createStereotypesForMisc'][0]:
	options['createStereotypesForMisc'] = True
else:
	options['createStereotypesForMisc'] = False
if len(scriptInput['organizePackages'])>0 and scriptInput['organizePackages'][0]:
	options['organizePackages'] = True
else:
	options['organizePackages'] = False

if len(scriptInput['createCustomizations'])>0 and scriptInput['createCustomizations'][0]:
	options['createCustomizations'] = True
else:
	options['createCustomizations'] = False
if len(scriptInput['createGeneralizations'])>0 and scriptInput['createGeneralizations'][0]:
	options['createGeneralizations'] = True
else:
	options['createGeneralizations'] = False
if len(scriptInput['fixNamesForClassifiers'])>0 and scriptInput['fixNamesForClassifiers'][0]:
	options['fixNamesForClassifiers'] = True
else:
	options['fixNamesForClassifiers'] = False
if len(scriptInput['fixNamesForProperties'])>0 and scriptInput['fixNamesForProperties'][0]:
	options['fixNamesForProperties'] = True
else:
	options['fixNamesForProperties'] = False
if len(scriptInput['fixNamesForOperations'])>0 and scriptInput['fixNamesForOperations'][0]:
	options['fixNamesForOperations'] = True
else:
	options['fixNamesForOperations'] = False
if len(scriptInput['fixNamesForPackages'])>0 and scriptInput['fixNamesForPackages'][0]:
	options['fixNamesForPackages'] = True
else:
	options['fixNamesForPackages'] = False
if len(scriptInput['fixNamesForMisc'])>0 and scriptInput['fixNamesForMisc'][0]:
	options['fixNamesForMisc'] = True
else:
	options['fixNamesForMisc'] = False
if len(scriptInput['deprecateStereotypes'])>0 and scriptInput['deprecateStereotypes'][0]:
	options['deprecateStereotypes'] = True
else:
	options['deprecateStereotypes'] = False	
if len(scriptInput['syncGeneralizations'])>0 and scriptInput['syncGeneralizations'][0]:
	options['syncGeneralizations']=True
else:
	options['syncGeneralizations']=False

if len(scriptInput['target'])>0:
	target = scriptInput['target'][0]
	if options['organizePackages'] == True:
		OPP.organizeProfilePackages(target)
	if options['createStereotypesForClassifiers'] == True:
		CSE.createStereotypesForElements(target, True, False,False, False, False)
	if options['createStereotypesForProperties'] == True:
		CSE.createStereotypesForElements(target, False, False,False, True, False)
	if options['createStereotypesForOperations'] == True:
		CSE.createStereotypesForElements(target, False, True,False, False, False)
	if options['createStereotypesForPackages'] == True:
		CSE.createStereotypesForElements(target, False, False,True, False, False)
	if options['createStereotypesForMisc'] == True:
		CSE.createStereotypesForElements(target, False, False,False, False, True)
	if options['createCustomizations'] == True:
		CCS.createCustomizationsForStereotypes(target)
	if options['createGeneralizations'] == True:
		CBC.cloneBaseClass(target, 'create')
	if options['fixNamesForClassifiers'] == True:
		FN.fixNames(target, True, False,False, False, False)
	if options['fixNamesForProperties'] == True:
		FN.fixNames(target, False, True, False, False, False)
	if options['fixNamesForOperations'] == True:
		FN.fixNames(target, False, False,True, False, False)
	if options['fixNamesForPackages'] == True:
		FN.fixNames(target, False, False,False, True, False)
	if options['fixNamesForMisc'] == True:
		FN.fixNames(target, False, False,False, False, True)
	if options['deprecateStereotypes']==True:
		DS.deprecateStereotypes(target)
	if options['syncGeneralizations'] == True:
		CBC.cloneBaseClass(target, 'sync')
		

else:
	gl.log("**ERROR** No Target Provided!")
	

scriptOutput = '***Update Profile Complete***'