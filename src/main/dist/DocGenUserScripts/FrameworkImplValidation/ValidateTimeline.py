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

from javax.swing import *
from SystemsReasoner import Specialize

from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBParagraph
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText

reload(Specialize)
import sys
import traceback
import os
#import PackageClone as PC
#reload (PC)

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import FixTimeline
reload(FixTimeline)

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

def ValidateTimeline(package,check):
    [GenError,redError,StateTimelineError,EventError,GenAdd,EventAdd,StateAdd,AssocAdd,RedAdd,asscError,StAdd,AbsAdd,MultState,MultEvent,mappingReqState,AbsError,constError,constAdd,litError,litEventAdd,redefCs]=FixTimeline.StateEventTimelineCreation(package,check)
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
        
    output=[]
    output.append(DBParagraph("--------------------------Timeline Summary ERROR Report--------------------------"))
    table = DBTable()
    table.setBody([[DBText("Generalization"),DBText(str(GenError))],
                   [DBText("Redefinition"),DBText(str(redError))],
                   [DBText("Timeline Missing"),DBText(str(StateTimelineError))],
                   [DBText("Event Missing"),DBText(str(EventError))],
                   [DBText("Timeline Multiple Associations"),DBText(str(MultState))],
                   [DBText("Event Multiple Association Warning"),DBText(str(MultEvent))],
                   [DBText("Association"),DBText(str(asscError))],
                   [DBText("Abstract"),DBText(str(AbsError))],
                   [DBText("Constraint"),DBText(str(constError))],
                   [DBText("Literal (no event for literal)"),DBText(str(litError))]])
    table.setHeaders([[DBText("Error Type"),DBText("Count")]])
    table.setTitle("Timeline Pattern Errors")
    table.setCols(2)
    output.append(table)

    output.append(DBParagraph("--------------------------Timeline Requirements Report--------------------------"))
    
    gl.log("--------------------------Summary Redefinition Not Correct--------------------------")
#    for j in redefCs:
#       gl.log("This element was not redefined properly====>"+j.getQualifiedName())
    

    for j in mappingReqState:
        #gl.log("The system shall provide a "+mappingReqState[j].getName()+"  Timeline, that represents the  "+j.getName()+"Variable Type.")
        output.append(DBParagraph("The system shall provide a "+mappingReqState[j].getName()+"  Timeline, that represents the  "+j.getName()+"Variable Type."))
        
    return output

if len(scriptInput['StateVariablePackage'])>0:
    pack = scriptInput['StateVariablePackage'][0]
    if len(scriptInput['StateVariablePackage'])>0:
        check = scriptInput['CheckOnly'][0]
        output=ValidateTimeline(pack,check)
    #Summary(package,csItem)
else:
    gl.log("**ERROR** No Target Provided!")



scriptOutput = {"DocGenOutput":output}

                
    
        