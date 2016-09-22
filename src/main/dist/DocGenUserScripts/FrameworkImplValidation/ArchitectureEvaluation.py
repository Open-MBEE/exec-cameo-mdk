'''
Created on Oct 13, 2011

@author: Louise Anderson
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
from SystemsReasoner import Specialize
reload(Specialize)
import sys
import traceback
import os
import PackageClone as PC
reload (PC)

from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBParagraph
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
from SystemsReasoner import SRUtils
reload(SRUtils)
import FixPackage
reload(FixPackage)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
datapack=project.getModel()
#mapping={}
#package = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
count={}
#stereotypePack=StereotypesHelper.getStereotypes(package)
#profile = StereotypesHelper.getProfileForStereotype(stereotypePack[0])
#pc=StereotypesHelper.getAppliedProfiles(package)
#profile=pc[0]
#CSst=StereotypesHelper.getStereotypesByProfile(profile)
#csElem=profile.getOwnedElement()
#nElem=package.getOwnedElement()
#countFix=0
#elementToFix=[]
#matcht=False


#def blah(package, csPack, False):
#    ValidatePackage.blah(package, csPack, False)   
#    return


def ControlServiceFind(package,CSProf,st):
    CSPacks=CSProf.getNestedPackage()
    for c in CSPacks:
        packSt=StereotypesHelper.getStereotypes(package)
        packSt=filter(lambda element: element in st, packSt)
        if StereotypesHelper.hasStereotype(c,packSt):
            blah=c
            #gl.log("hehehehehhe========>"+blah.getName())
            return blah
        if isinstance(c,Package):
            CSPackage=ControlServiceFind(package,c,st)
            if CSPackage is not None:
                return CSPackage
    return None
output=[] 
def Summary(package,profile,check,fullReport):
    stpC=StereotypesHelper.getStereotypesByProfile(profile)
    csPack=ControlServiceFind(package,profile, stpC)
    if csPack==None:
            gl.log("******ERROR*****The Validation Package is not found in the CS=======>"+package.getName())
    else:
        [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,sysModels,sysTls]=FixPackage.blah(package,csPack, profile, check,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,{},0,{},{},{},{},{},{},{},0,{},{})
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
        gl.log("Attribute is not redefined or stereotyped"+str(len(insPropT3)))
        gl.log("Redefinition Error/Warning Total =======>"+str(len(insProp)-len(redefCS)))
        gl.log("--------------------------Summary Warning Report--------------------------")
        gl.log("Element Redefined but Redefinition is found in different MSAF Model Element======>"+str(len(insPropT1)))
        gl.log("Attribute Element matched MSAF Attribute Element Stereotype, but no redefinition Defined======>"+str(len(insPropT2)))
        gl.log("Element Redefined, but missing appropriate Stereotype======>"+str(len(insPropT4)))
        gl.log("--------------------------Summary Creation Report Report--------------------------")
        gl.log("Elements Created===============>"+str(elemCount))
        gl.log("Generalizations Created===========>"+str(genCount))
        gl.log("Diagrams Created===============>"+str(diagCount))
        gl.log("--------------------------Summary Fix Me! Elements--------------------------")
        
        ##NEED TO REMOVE Question Asking##
        [count,Fixes]=FixPackage.countFixes(0,[],package)
#        gl.log("The total number of fix me elements===>"+str(count))
#        n = JOptionPane.showConfirmDialog(None,"Do you want to view the name of the Elements that still contains Fix Me!??","View Name Question", JOptionPane.YES_NO_OPTION)
#        if n!=JOptionPane.CLOSED_OPTION:
#            if n==JOptionPane.YES_OPTION:
#                for j in Fixes:
#                    gl.log("The name of the element to fix is:========>"+j.getName())
#        gl.log("--------------------------Summary Stereotypes Report--------------------------")
#        q = JOptionPane.showConfirmDialog(None,"Do you want to view the Stereotype Summary??","Stereotype Question", JOptionPane.YES_NO_OPTION)
#        if q!=JOptionPane.CLOSED_OPTION:
#            if q==JOptionPane.YES_OPTION:
#                Fix_Package.stereotypeReport(CSst,package)
        
        frmwrkVal = StereotypesHelper.getStereotype(project, 'FrameworkImplValidation.ValidatePackage')
        frmwrkDes = frmwrkVal.getOwnedComment()[0].getBody()
        output.append(DBParagraph(frmwrkDes))
        
        output.append(DBParagraph("--------------------------Model INFORMATION Report--------------------------"))
        table = DBTable()
        table.setBody([[DBText("Service Implementation Elements Found"),DBText(str(len(instElem)))],
                       [DBText("MSAF Elements Found (in matching location)"),DBText(str(csElems))],
                       [DBText("Elements Found Matching (between Service Implementation and MSAF)"),DBText(str(goodElem))],
                       [DBText("Service Implementation Packages Found"),DBText(str(goodPack))],
                       [DBText("Generalizations Found"),DBText(str(goodGen))],
                       [DBText("Diagrams Found"),DBText(str(goodDiag))],
                       [DBText("Redefinitions Found"),DBText(str(len(redefCS)))],
                       [DBText("Properties Found"),DBText(str(len(insProp)))]])
        table.setHeaders([[DBText("Model Elements"),DBText("Count")]])
        table.setTitle("Model Information Report")
        table.setCols(2)
        output.append(table)
        
        output.append(DBParagraph("--------------------------Model ERROR Report--------------------------"))
        table = DBTable()
        table.setBody([[DBText("MSAF Comparison Validation"),DBText("Elements in MSAF not found in Service Implementation"),DBText(str(elemError))],
                       [DBText("MSAF Comparison Validation"),DBText("More Elements in Service Implementation then MSAF"),DBText(str(MoreIns))],
                       [DBText("MSAF Comparison Validation"),DBText("Packages Not Found"),DBText(str(packError))],
                       [DBText("MSAF Comparison Validation"),DBText("Elements without generalization"),DBText(str(genError))],
                       [DBText("MSAF Comparison Validation"),DBText("Diagram Not Found"),DBText(str(diagError))],
                       [DBText("MSAF Comparison Validation"),DBText("Redefinition Error: Property is Not Redefined or Stereotyped"),DBText(str(len(insPropT3)))],
                       [DBText("MSAF Comparison Validation"),DBText("Total Redefinition Error/Warning"),DBText(str(len(insProp)-len(redefCS)))]])
#                       [DBText("MSAF Comparison Validation"),DBText("Redefinition Defined but No Stereotype on Part"),DBText(str(len(insPropT1)-len(redefCS)))],
#                       [DBText("MSAF Comparison Validation"),DBText("Redefinition Defined but No Stereotype on Part"),DBText(str(len(insPropT2)-len(redefCS)))]])
        table.setHeaders([[DBText("Validation Suite"),DBText("Error Type"),DBText("Count")]])
        table.setTitle("Model Errors Report")
        table.setCols(3)
        output.append(table)


        output.append(DBParagraph("--------------------------Model Warning Report--------------------------"))
        table = DBTable()
        table.setBody([[DBText("MSAF Comparison Validation"),DBText("Redefinition Warning:  Element Redefined but Redefinition is found in different MSAF Model Element"),DBText(str(len(insPropT1)))],
                       [DBText("MSAF Comparison Validation"),DBText("Redefinition Warning:  Service Implementation Property matched MSAF Property Element Stereotype, but no redefinition Defined"),DBText(str(len(insPropT2)))],
                       [DBText("MSAF Comparison Validation"),DBText("Redefinition Warning:  Element Redefined, but missing appropriate Stereotype"),DBText(str(len(insPropT4)))]])

        
        table.setHeaders([[DBText("Validation Suite"),DBText("Warning Type"),DBText("Count")]])
        table.setTitle("Model Warnings Report")
        table.setCols(3)
        output.append(table)
    
        
        
        if fullReport==True:
            output.append(DBParagraph("--------------------------Summary Redefinitions Errors/Warning--------------------------"))
            
            gl.log("--------------------------Summary Redefinition Not Correct--------------------------")
            gl.log("-------Total of the different Errors----should match redef error---"+str(len(insPropT1)+len(insPropT2)+len(insPropT3)+len(insPropT4)))
            gl.log(" the length of the insPropT1??====>"+str(len(insPropT1)))
            
            output.append(DBParagraph("--------------------------REDEFINITION WARNING: Element Redefined but Redefinition is found in different MSAF Model Element--------------------------"))
            for m in insPropT1:
                gl.log("Property not found in the correct MSAF Element=====>"+m.getQualifiedName())
                output.append(DBParagraph(m.getQualifiedName()))
            output.append(DBParagraph("--------------------------REDEFINITION WARNING: Service Implementation Property matched MSAF Property Element Stereotype, but no redefinition Defined--------------------------"))
            for n in insPropT2:
                gl.log("Service Implementation Property matched MSAF Property Element Stereotype, but no redefinition Defined=====>"+n.getQualifiedName())
                output.append(DBParagraph(n.getQualifiedName()))
            output.append(DBParagraph("--------------------------REDEFINITION WARNING: Service Implementation Property has redefinition, but no Stereotype--------------------------"))
            for q in insPropT4:
                gl.log("This part is redefined, but not stereotyped"+q.getQualifiedName())
                output.append(DBParagraph(q.getQualifiedName()))
            output.append(DBParagraph("--------------------------REDEFINITION ERROR: Service Implementation Property has no redefinition and no Stereotype--------------------------"))  
            for p in insPropT3:
                gl.log("This element is not stereotyped, nor does it have a redefinition"+p.getQualifiedName())
                output.append(DBParagraph(p.getQualifiedName()))
            
                
            
    #        for j in mappingReqState:
    #            #gl.log("The system shall provide a "+mappingReqState[j].getName()+"  Timeline, that represents the  "+j.getName()+"Variable Type.")
    #            output.append(DBParagraph("The system shall provide a "+mappingReqState[j].getName()+"  Timeline, that represents the  "+j.getName()+"Variable Type."))
            output.append(DBParagraph("--------------------------Summary of Elements with Fix ME! Notation--------------------------"))
    #        gl.log("The total number of fix me elements:===>"+str(count))
            output.append(DBParagraph("The total number of fix me elements:=====> "+ str(count)))
            for j in Fixes:
                gl.log("The name of the element with Fix Me!:========>"+j.getName())         
                output.append(DBParagraph("The name of the element with Fix Me!:=====> "+ j.getQualifiedName()))


if len(scriptInput['target'])>0:
    target = scriptInput['target'][0]
    if len(scriptInput['target'])>0:
        profile = scriptInput['Profile'][0]
        if len(scriptInput['Profile'])>0:
            checkOnly=scriptInput['CheckOnly'][0]
            if len(scriptInput['CheckOnly'])>0:
                fullReport=scriptInput['FullReport'][0]
                Summary(target,profile,checkOnly,fullReport)
    #Summary(package,csItem)
else:
    gl.log("**ERROR** No Target Provided!")



scriptOutput ={"DocGenOutput":output}
                    
            
            
                
    
        