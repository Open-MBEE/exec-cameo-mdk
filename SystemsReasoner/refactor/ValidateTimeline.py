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
import Fix_Timeline
reload(Fix_Timeline)

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

def ValidateTimeline(package,check):
    [GenError,redError,StateTimelineError,EventError,GenAdd,EventAdd,StateAdd,AssocAdd,RedAdd,asscError,StAdd,AbsAdd,MultState,MultEvent,mappingReqState,AbsError,constError,constAdd,litError,litEventAdd,redefCs]=Fix_Timeline.StateEventTimelineCreation(package,check)
    gl.log("--------------------------Timeline Summary ERROR Report--------------------------")
    gl.log("Generalization Errors===============>"+str(GenError))
    gl.log("Redefinition Errors===============>"+str(redError))
    gl.log("State Timeline Missing Error====>"+str(StateTimelineError))
    gl.log("Event Missing Error====>"+str(EventError))
    gl.log("State Timeline Multiple Association Error====>"+str(MultState))
    gl.log("Event Multiple Association Warning====>"+str(MultEvent))
    gl.log("Association Error======>"+str(asscError))
    gl.log("Abstract Error=======>"+str(AbsError))
    gl.log("Constraint Error=======>"+str(constError))
    gl.log("Literal Error(no event for literal)=====>"+str(litError))
    gl.log("--------------------------Timeline Summary Addition Report--------------------------")
    gl.log("Generalizations Added=========>"+str(GenAdd))
    gl.log("State Timelines Added=========>"+str(StateAdd))
    gl.log("Event Timelines Added=========>"+str(EventAdd))
    gl.log("Associations Added==========>"+str(AssocAdd))
    gl.log("Redefinition Added==========>"+str(RedAdd))
    gl.log("Abstract Added=========>"+str(AbsAdd))
    gl.log("Stereotypes Added======>"+str(StAdd))
    gl.log("Constraints Added======>"+str(constAdd))
    gl.log("Literal Event Added=====>"+str(litEventAdd))
    
    gl.log("--------------------------Summary Redefinition Not Correct--------------------------")
    q = JOptionPane.showConfirmDialog(None,"Do you want to view the List of Elements with improper Redefinitions??","Redefinition Question", JOptionPane.YES_NO_OPTION)
    if q!=JOptionPane.CLOSED_OPTION:
        if q==JOptionPane.YES_OPTION:
            for j in redefCs:
               gl.log("This element was not redefined properly====>"+j.getQualifiedName())
    
    n = JOptionPane.showConfirmDialog(None,"Do you want to view Requirements Report","Requirements Question", JOptionPane.YES_NO_OPTION)
    if n!=JOptionPane.CLOSED_OPTION:
        if n==JOptionPane.YES_OPTION:
            for j in mappingReqState:
                gl.log("The system shall provide a "+mappingReqState[j].getName()+"  State Timeline, that represents the  "+j.getName()+"  State Variable Type.")
#    return
 
def run(mode):
    try:
        
        SessionManager.getInstance().createSession("LouiseTest")
        ValidateTimeline(package,True)
        
    #gl.log("Generalizations Created===========>"+str(genCount))
        #gl.log("Diagrams Created===============>"+str(diagCount))
        
                    
        SessionManager.getInstance().closeSession()
    except:
        if SessionManager.getInstance().isSessionCreated():
            SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        gl.log("*** EXCEPTION:")
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
            
            
                
    
        