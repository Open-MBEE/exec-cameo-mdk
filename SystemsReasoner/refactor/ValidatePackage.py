'''
Created on Oct 13, 2011

@author: lcanders
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
from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
from javax.swing import *
import Specialize
reload(Specialize)
import sys
import traceback
import os
import Package_Clone as PC
reload (PC)

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)
import Fix_Package
reload(Fix_Package)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
datapack=project.getModel()
#mapping={}
package = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
count={}
stereotypePack=StereotypesHelper.getStereotypes(package)
profile = StereotypesHelper.getProfileForStereotype(stereotypePack[0])
#pc=StereotypesHelper.getAppliedProfiles(package)
#profile=pc[0]
CSst=StereotypesHelper.getStereotypesByProfile(profile)
csElem=profile.getOwnedElement()
nElem=package.getOwnedElement()
countFix=0
elementToFix=[]
#matcht=False


#def blah(package, csPack, False):
#    ValidatePackage.blah(package, csPack, False)   
#    return


def ControlServiceFind(package,CSProf):
    CSPacks=CSProf.getNestedPackage()
    for c in CSPacks:
        packSt=StereotypesHelper.getStereotypes(package)
        packSt=filter(lambda element: element in CSst, packSt)
        if StereotypesHelper.hasStereotype(c,packSt):
            blah=c
            #gl.log("hehehehehhe========>"+blah.getName())
            return blah
        if isinstance(c,Package):
            CSPackage=ControlServiceFind(package,c)
            if CSPackage is not None:
                return CSPackage
    return None
 
def run(mode):
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    try:
        SessionManager.getInstance().createSession("syncProfile")
        csItem=ControlServiceFind(package,profile)
        #passIn=csItem.getOwner()
        #csPack=Fix_Package.findPackage(package,passIn, True)
        #if csPack is None:
            #gl.log("******ERROR*****One of your packages does not have a stereotype=======>"+package.getName())
        #else:
        [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,sysModels,sysTls]=Fix_Package.blah(package,csItem, True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,{},0,{},{},{},0,{},{})
        gl.log("--------------------------Summary Elements Report--------------------------")
        gl.log("Instance Elements Found===============>"+str(len(instElem)))
        gl.log("CS Elements Found===============>"+str(csElems))
        gl.log("Elements Found Matching===============>"+str(goodElem))
        gl.log("Packages Found===========>"+str(goodPack))
        gl.log("Generalizations===============>"+str(goodGen))
        gl.log("Diagrams Found==============>"+str(goodDiag))
        gl.log("Redefinitions Found==============>"+str(len(redefCS)))
        gl.log("Properties Found========>"+ str(len(insProp)))
            
        gl.log("--------------------------Summary Error Report--------------------------")
        gl.log("More Elements in CS Not Instance Found===============>"+str(elemError))
        MoreIns=len(instElem)-goodElem
        if MoreIns!=0:
            gl.log("More Elements Found in the Instance than the CS =======>"+str(MoreIns))
        gl.log("Packages Not Found===========>"+str(packError))
        gl.log("Elements without Generalization===============>"+str(genError))
        gl.log("Diagram Not Found===============>"+str(diagError))
        gl.log("Redefinition Error =======>"+str(len(insProp)-len(redefCS)))
        gl.log("--------------------------Summary Creation Report Report--------------------------")
        gl.log("Elements Created===============>"+str(elemCount))
        gl.log("Generalizations Created===========>"+str(genCount))
        gl.log("Diagrams Created===============>"+str(diagCount))
        gl.log("--------------------------Summary Fix Me! Elements--------------------------")
        [count,Fixes]=Fix_Package.countFixes(countFix,elementToFix,package)
        gl.log("The total number of fix me elements===>"+str(count))
        n = JOptionPane.showConfirmDialog(None,"Do you want to view the name of the Elements that still contains Fix Me!??","View Name Question", JOptionPane.YES_NO_OPTION)
        if n!=JOptionPane.CLOSED_OPTION:
            if n==JOptionPane.YES_OPTION:
                for j in Fixes:
                    gl.log("The name of the element to fix is:========>"+j.getName())
        gl.log("--------------------------Summary Stereotypes Report--------------------------")
        q = JOptionPane.showConfirmDialog(None,"Do you want to view the Stereotype Summary??","Stereotype Question", JOptionPane.YES_NO_OPTION)
        if q!=JOptionPane.CLOSED_OPTION:
            if q==JOptionPane.YES_OPTION:
                Fix_Package.stereotypeReport(CSst,package)
        gl.log("--------------------------Summary Redefinition Not Correct--------------------------")
        q = JOptionPane.showConfirmDialog(None,"Do you want to view the List of Elements with improper Redefinitions??","Redefinition Question", JOptionPane.YES_NO_OPTION)
        if q!=JOptionPane.CLOSED_OPTION:
            if q==JOptionPane.YES_OPTION:
                for j in insProp:
                    if j not in redefCS:
                        gl.log("This element was not redefined properly====>"+j.getQualifiedName())
                    
        SessionManager.getInstance().closeSession()
    except:
        if SessionManager.getInstance().isSessionCreated():
            SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        gl.log("*** EXCEPTION:")
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
            
            
                
    
        