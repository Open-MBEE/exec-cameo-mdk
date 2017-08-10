'''
Created on Sep 20, 2011

@author: efosse
'''
from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
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
from gov.nasa.jpl.mbee.mdk.validation import ValidationRule
from gov.nasa.jpl.mbee.mdk.validation import ValidationSuite
from gov.nasa.jpl.mbee.mdk.validation import ViolationSeverity

from gov.nasa.jpl.mbee.mdk.docgen.docbook import DBParagraph

import ProfileValidation as PV
reload(PV)
import CreateCustomizationsForStereotypes as CCS
reload(CCS)
import DeprecateStereotypes as DS
reload(DS)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()


###Grab Inputs from User Script###
si = {}
options = ['validateLocation', 'stereotypesForClassifiers', 'stereotypesForProperties', 'stereotypesForOperations','stereotypesForPackages', 'stereotypesForDependencies', 'stereotypesForMisc', 'stereotypeMetaClassCheck','stereotypeSysMLGeneralizationCheck','localGeneralizations', 'customizations','deprecateStereotypes']
for o in options: si[o]='ignore'

if len(scriptInput['validationTarget'])>0:
    target = scriptInput['validationTarget'][0]
elif len(scriptInput['DocGenTargets'])>0:
    target = scriptInput['DocGenTargets'][0]
else:
    gl.log("**ERROR** No Target Provided!")
    output =[]
if len(scriptInput['validateLocation'])>0:
    if scriptInput['validateLocation'][0].getName() =='validate': Location = PV.validateLocation(target, True, scriptInput['ignorePackage'])
    elif scriptInput['validateLocation'][0].getName() == 'fix':  Location = PV.validateLocation(target, False, scriptInput['ignorePackage'])
    si['validateLocation']=scriptInput['validateLocation'][0].getName()
if len(scriptInput['stereotypesForClassifiers'])>0:
    if scriptInput['stereotypesForClassifiers'][0].getName()=='validate': sterClass = PV.stereotypesForElements(target, True, Classifier, scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    elif scriptInput['stereotypesForClassifiers'][0].getName()=='fix': sterClass = PV.stereotypesForElements(target, False, Classifier, scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    si['stereotypesForClassifiers']=scriptInput['stereotypesForClassifiers'][0].getName()
if len(scriptInput['stereotypesForProperties'])>0:
    if scriptInput['stereotypesForProperties'][0].getName()=='validate': sterProp = PV.stereotypesForElements(target, True, Property,scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    elif scriptInput['stereotypesForProperties'][0].getName()=='fix': sterProp = PV.stereotypesForElements(target, False, Property,scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    si['stereotypesForProperties']=scriptInput['stereotypesForProperties'][0].getName()
if len(scriptInput['stereotypesForOperations'])>0:
    if scriptInput['stereotypesForOperations'][0].getName()=='validate': sterOp = PV.stereotypesForElements(target,  True, Operation,scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    elif scriptInput['stereotypesForOperations'][0].getName()=='fix': sterOp= PV.stereotypesForElements(target, False, Operation,scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    si['stereotypesForOperations']=scriptInput['stereotypesForOperations'][0].getName()
if len(scriptInput['stereotypesForPackages'])>0:
    if scriptInput['stereotypesForPackages'][0].getName()=='validate': sterPack = PV.stereotypesForElements(target, True, Package,scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    elif scriptInput['stereotypesForPackages'][0].getName()=='fix': sterPack = PV.stereotypesForElements(target, False, Package,scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    si['stereotypesForPackages']=scriptInput['stereotypesForPackages'][0].getName()
if len(scriptInput['stereotypesForDependencies'])>0:
    if scriptInput['stereotypesForDependencies'][0].getName() =='validate': sterDep = PV.stereotypesForElements(target, True, Dependency, scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    elif scriptInput['stereotypesForDependencies'][0].getName() == 'fix': sterDep = PV.stereotypesForElements(target, False, Dependency, scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    si['stereotypesForDependencies']=scriptInput['stereotypesForDependencies'][0].getName()
if len(scriptInput['stereotypesForMisc'])>0:
    if scriptInput['stereotypesForMisc'][0].getName()=='validate': sterM = PV.stereotypesForElements(target, True, 'Misc', scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    elif scriptInput['stereotypesForMisc'][0].getName()=='fix': sterM = PV.stereotypesForElements(target, False, 'Misc', scriptInput['ignoreStereotype'], scriptInput['ignorePackage'])
    si['stereotypesForMisc']=scriptInput['stereotypesForMisc'][0].getName()
if len(scriptInput['deprecateStereotypes'])>0:
    if scriptInput['deprecateStereotypes'][0].getName() =='validate': depSter = DS.deprecateStereotypes(target, True)
    elif scriptInput['deprecateStereotypes'][0].getName()=='fix': depSter = DS.deprecateStereotypes(target, False)
    si['deprecateStereotypes']=scriptInput['deprecateStereotypes'][0].getName()
if len(scriptInput['customizations'])>0:
    if scriptInput['customizations'][0].getName()=='validate': cust = CCS.createCustomizationsForStereotypes(target, True)
    elif scriptInput['customizations'][0].getName()=='fix': cust = CCS.createCustomizationsForStereotypes(target, False)
    si['customizations']=scriptInput['customizations'][0].getName()
if len(scriptInput['stereotypeMetaClassCheck'])>0:
    if scriptInput['stereotypeMetaClassCheck'][0].getName()=='validate': meta = PV.metaclassCheck(target, True)
    elif scriptInput['stereotypeMetaClassCheck'][0].getName() == 'fix': meta = PV.metaclassCheck(target, False)
    si['stereotypeMetaClassCheck']=scriptInput['stereotypeMetaClassCheck'][0].getName()
if len(scriptInput['stereotypeSysMLGeneralizationCheck'])>0:
    if scriptInput['stereotypeSysMLGeneralizationCheck'][0].getName()=='validate': gen = PV.stereotypeGenCheck(target, True)
    elif scriptInput['stereotypeSysMLGeneralizationCheck'][0].getName()=='fix': gen = PV.stereotypeGenCheck(target, False)
    si['stereotypeSysMLGeneralizationCheck']=scriptInput['stereotypeSysMLGeneralizationCheck'][0].getName()
if len(scriptInput['localGeneralizations'])>0:
    if scriptInput['localGeneralizations'][0].getName()=='validate': localGen = PV.localGeneralizations(target, True, False, scriptInput['ignoreStereotype'])
    elif scriptInput['localGeneralizations'][0].getName()=='fix': localGen = PV.localGeneralizations(target, False, False, scriptInput['ignoreStereotype'])
    si['localGeneralizations']=scriptInput['localGeneralizations'][0].getName()

#declare validation suite
profvs = ValidationSuite("ProfileValidation")
if si['validateLocation']!='ignore':
    #declare validationLocation rules
    sterLoc = ValidationRule("Stereotypes Location", "All stereotypes should be owned by a package with '_Stereotypes' in its name", ViolationSeverity.ERROR)
    profvs.addValidationRule(sterLoc)
    custLoc = ValidationRule("Customizations Location","All customizations should be owned by a package with '_Customizations' in its name", ViolationSeverity.ERROR)
    profvs.addValidationRule(custLoc)
    packExist = ValidationRule("Packages Exist", "A package named '_' should exist as a child of the profile and packages with '_Stereotypes' and '_Customizations' in their names should exist as children of '_' package", ViolationSeverity.ERROR)
    profvs.addValidationRule(packExist)
    profExist = ValidationRule("Executed on Profile", "The input to the viewpoint should be nested within a Profile", ViolationSeverity.ERROR)
    profvs.addValidationRule(profExist)
    depExist = ValidationRule("_Deprecated Stereotypes Package Exists", "A package with '_Deprecated Stereotypes' in its name exists and is owned by '_' Package", ViolationSeverity.WARNING)
    profvs.addValidationRule(depExist)
    ###collect validateLocation errors
    if len(Location)==1:
         if 'ignore' not in Location:
            if '_' in Location[0]: profExist.addViolation(target, "No '_' Package")
            if 'Profile' in Location[0]: packExist.addViolation(target, "Execution Stopped")
    elif len(Location)==3:
            if not isinstance(Location[0], Package): 
                if 'wrong' not in Location[0][0]: packExist.addViolation(target, "No '_Stereotypes' Package")
                else: packExist.addViolation(target, "'_Stereotypes Package' not owned by '_' Package")
            if not isinstance(Location[1], Package): 
                if 'wrong' not in Location[1][0]: packExist.addViolation(target, "No '_Customizations' Package")
                else: packExist.addViolation(target, "'_Customizations' Package not owned by '_' Package")
            if not isinstance(Location[2], Package): 
                if 'wrong' not in Location[2][0]: depExist.addViolation(target, "No '_Deprecated Stereotypes' Package")
                else: depExist.addViolation(target, "'_Deprecated Stereotypes' Package not owned by '_' Package")
    else:
        if not Location[0]: packExist.addViolation(target, "No '_Stereotypes' Package")    
        if not Location[1]: depExist.addViolation(target, "No '_Deprecated Stereotypes' Package")
        if not Location[2]: packExist.addViolation(target, "No '_Customizations' Package")
        for s in Location[3]: sterLoc.addViolation(s, "")
        for c in Location[4]: custLoc.addViolation(c, "")

if si['stereotypesForClassifiers']!= 'ignore':
    noSterClass = ValidationRule("Classifier Stereotype Missing", "There is no stereotype name that matches classifier name", ViolationSeverity.ERROR)
    profvs.addValidationRule(noSterClass)
    wrongMetaClass=ValidationRule("Conflicting Classifier Metaclass", "Stereotype metaclass and element type does not match", ViolationSeverity.ERROR)
    profvs.addValidationRule(wrongMetaClass)
    applySterClass = ValidationRule("Stereotype Can Be Applied to a Classifier", "A stereotype exists with the same name as the classifier", ViolationSeverity.ERROR)
    profvs.addValidationRule(applySterClass)
    ###collect errors
    for s in sterClass[0]:noSterClass.addViolation(s, s.getName())
#    for c in sterClass[1]:wrongSter.addViolation(c, c.getName())
    for d in sterClass[2]:wrongMetaClass.addViolation(d, "Stereotype Metaclass")
    for e in sterClass[3]:applySterClass.addViolation(e, e.getName())
if si['stereotypesForProperties']!='ignore':
    noSterProp = ValidationRule("Property Stereotype Missing", "There is no stereotype name that matches property name", ViolationSeverity.ERROR)
    profvs.addValidationRule(noSterProp)
    wrongMetaProp=ValidationRule("Conflicting Property Metaclass", "Stereotype metaclass and element type does not match", ViolationSeverity.ERROR)
    profvs.addValidationRule(wrongMetaProp)
    applySterProp = ValidationRule("Stereotype Can Be Applied to a Property", "A stereotype exists with the same name as the property", ViolationSeverity.ERROR)
    profvs.addValidationRule(applySterProp)
    ###collect errors
    for s in sterProp[0]:noSterProp.addViolation(s, "")
#    for c in sterClass[1]:wrongSter.addViolation(c, c.getName())
    for d in sterProp[2]:wrongMetaProp.addViolation(d, "Stereotype Metaclass")
    for e in sterProp[3]:applySterProp.addViolation(e, e.getName())
if si['stereotypesForOperations']!='ignore':
    noSterOp = ValidationRule("Operation Stereotype Missing", "There is no stereotype name that matches operation name", ViolationSeverity.ERROR)
    profvs.addValidationRule(noSterOp)
    wrongMetaOp=ValidationRule("Conflicting Operation Metaclass", "Stereotype metaclass and element type does not match", ViolationSeverity.ERROR)
    profvs.addValidationRule(wrongMetaOp)
    applySterOp = ValidationRule("Stereotype Can Be Applied to a Operation", "A stereotype exists with the same name as the operation", ViolationSeverity.ERROR)
    profvs.addValidationRule(applySterOp)
    for s in sterOp[0]:noSterOp.addViolation(s, s.getName())
#    for c in sterClass[1]:wrongSter.addViolation(c, c.getName())
    for d in sterOp[2]:wrongMetaOp.addViolation(d, "Stereotype Metaclass")
    for e in sterOp[3]:applySterOp.addViolation(e, e.getName())
if si['stereotypesForPackages']!='ignore':
    noSterPack = ValidationRule("Package Stereotype Missing", "There is no stereotype name that matches package name", ViolationSeverity.ERROR)
    profvs.addValidationRule(noSterPack)
    wrongMetaPack=ValidationRule("Conflicting Package Metaclass", "Stereotype metaclass and element type does not match", ViolationSeverity.ERROR)
    profvs.addValidationRule(wrongMetaPack)
    applySterPack = ValidationRule("Stereotype Can Be Applied to a Package", "A stereotype exists with the same name as the package", ViolationSeverity.ERROR)
    profvs.addValidationRule(applySterPack)
    for s in sterPack[0]:noSterPack.addViolation(s, s.getName())
#    for c in sterClass[1]:wrongSter.addViolation(c, c.getName())
    for d in sterPack[2]:wrongMetaPack.addViolation(d, "")
    for e in sterPack[3]:applySterPack.addViolation(e, e.getName())
if si['stereotypesForDependencies']!='ignore':
    noSterDep = ValidationRule("Dependency Stereotype Missing", "There is no stereotype name that matches package name", ViolationSeverity.ERROR)
    profvs.addValidationRule(noSterDep)
    wrongMetaDep=ValidationRule("Conflicting Dependency Metaclass", "Stereotype metaclass and element type does not match", ViolationSeverity.ERROR)
    profvs.addValidationRule(wrongMetaDep)
    applySterDep = ValidationRule("Stereotype Can Be Applied to a Dependency", "A stereotype exists with the same name as the dependency", ViolationSeverity.ERROR)
    profvs.addValidationRule(applySterDep)
    for s in sterDep[0]:noSterDep.addViolation(s, s.getName())
#    for c in sterClass[1]:wrongSter.addViolation(c, c.getName())
    for d in sterDep[2]:wrongMetaDep.addViolation(d, "Stereotype Metaclass")
    for e in sterDep[3]:applySterDep.addViolation(e, e.getName())
if si['stereotypesForMisc']!='ignore':
    noSterMisc = ValidationRule("Misc Stereotype Missing", "There is no stereotype name that matches element name", ViolationSeverity.ERROR)
    profvs.addValidationRule(noSterMisc)
    wrongMetaMisc=ValidationRule("Conflicting Misc Metaclass", "Stereotype metaclass and element type does not match", ViolationSeverity.ERROR)
    profvs.addValidationRule(wrongMetaMisc)
    applySterMisc = ValidationRule("Stereotype Can Be Applied to a Misc", "A stereotype exists with the same name as the dependency", ViolationSeverity.ERROR)
    profvs.addValidationRule(applySterMisc)
    for s in sterM[0]:noSterMisc.addViolation(s, s.getName())
#    for c in sterClass[1]:wrongSter.addViolation(c, c.getName())
    for d in sterM[2]:wrongMetaMisc.addViolation(d, "Stereotype Metaclass")
    for e in sterM[3]:applySterMisc.addViolation(e, e.getName())

if si['stereotypeMetaClassCheck']!='ignore':
###declare rules for stereotype meta class check
    multMeta = ValidationRule("Stereotype has multiple metaclasses", "The stereotype has multiple metaclasses assigned", ViolationSeverity.WARNING)
    profvs.addValidationRule(multMeta)
###collect errors
    for m in meta: multMeta.addViolation(m,"")

if si['stereotypeSysMLGeneralizationCheck']!='ignore':
###declare rules for stereotype sysml generalization check
    sysmlGen = ValidationRule("SysML Generalization", "The stereotype should generalize a SysML stereotype", ViolationSeverity.ERROR)
    nonSysmlGen = ValidationRule('Non SysML Generalization', 'The stereotype has generalizations to profiles besides SysML', ViolationSeverity.WARNING)
    profvs.addValidationRule(sysmlGen)
    profvs.addValidationRule(nonSysmlGen)
###collect errors
    for n in gen[0]: nonSysmlGen.addViolation(n, "")
    for g in gen[1]: sysmlGen.addViolation(g, "")

if si['localGeneralizations']!='ignore':
###declare rules for local generalizations check
    sterNoMatch = ValidationRule("Stereotypes Generalization", "A stereotype generalization exists that is not reflected in the model", ViolationSeverity.ERROR)
    baseNoMatch = ValidationRule("Model Generalization", "A local generalization exists in the model that is not reflected in the stereotypes", ViolationSeverity.ERROR)
    profvs.addValidationRule(sterNoMatch)
    profvs.addValidationRule(baseNoMatch)
###collect errors
    for lg in localGen[0]: 
        if lg.getGeneral() is not None and lg.getSpecific() is not None: sterNoMatch.addViolation(lg.getGeneral(), "Generalization from "+ lg.getGeneral().getName() + " to "+ lg.getSpecific().getName())
        else: sterNoMatch.addViolation(lg.getGeneral(), "")
    for lgs in localGen[1]: baseNoMatch.addViolation(lgs.getGeneral(), "Generalization from "+ lgs.getGeneral().getName()+ " to "+ lgs.getSpecific().getName())

if si['customizations']!= 'ignore':
###declare rules for customizations
###add in:
    noCust = ValidationRule("Stereotypes With No Customizations", "A stereotype exists without a corresponding customization", ViolationSeverity.ERROR)
    nameMismatch = ValidationRule("Stereotype-Customization Name Mismatch", "The customization name does not match the name of the customization target", ViolationSeverity.ERROR)
    multCust = ValidationRule("Multiple Customization Targets", "Multiple customization targets exist for the customization", ViolationSeverity.ERROR)
    noCustTargs = ValidationRule("No Customization Target", "Customization does not have a target", ViolationSeverity.ERROR)
    noType = ValidationRule("No Customization Type", "Customization does not have a type", ViolationSeverity.ERROR)
    multTypes = ValidationRule("Multiple Customization superTypes", "Multiple customization superTypes exist for the customizations", ViolationSeverity.ERROR)
    elNameMisMatch = ValidationRule("Element-Customization Name Mismatch", "The customization name does not match the name of the customization supertype", ViolationSeverity.ERROR)
    profvs.addValidationRule(noCust)
    profvs.addValidationRule(nameMismatch)
    profvs.addValidationRule(multCust)
    profvs.addValidationRule(noCustTargs)
    profvs.addValidationRule(noType)
    profvs.addValidationRule(multTypes)
    profvs.addValidationRule(elNameMisMatch)
###collect errors
    for nc in cust[0]: noCust.addViolation(nc, "Stereotype")
    for nm in cust[1]: nameMismatch.addViolation(nm, "Stereotype")
    for mc in cust[2]: multCust.addViolation(mc, "Stereotype")
    for nt in cust[3]: noCustTargs.addViolation(nt, "Customization")
    for nty in cust[4]: noType.addViolation(nty, "Customization")
    for mt in cust[5]: multTypes.addViolation(mt, "Customization")
    for e in cust[6]: elNameMisMatch.addViolation(e, "Element Name")
if si['deprecateStereotypes']!='ignore':
###declare rules
    unused = ValidationRule("Unused Stereotypes", "A stereotype is not applied in the model and can be deprecated", ViolationSeverity.ERROR)
    profvs.addValidationRule(unused)
###collect errors
    gl.log(str(depSter))
    for d in depSter:
        gl.log(str(d))
        unused.addViolation(d, "")

scriptOutput = {"DocGenValidationOutput":[profvs]}
if len(scriptInput['reportKind'])>0:
    if scriptInput['reportKind'][0].getName()=='log':
        profvs.setShowDetail(True)
        profvs.setShowSummary(False)
    else: 
        profvs.setShowSummary(True)
        profvs.setShowDetail(False)
else:
    profvs.setShowSummary(True)
    profvs.setShowDetail(False)
    
###Collect Fix Onlys
fixOutput = []
for f in si: 
    if si[f] == 'fix': fixOutput.append(DBParagraph(f))
gl.log('[FINISHED]')
scriptOutput["DocGenOutput"]=[DBParagraph("Profile Validation ")]

    