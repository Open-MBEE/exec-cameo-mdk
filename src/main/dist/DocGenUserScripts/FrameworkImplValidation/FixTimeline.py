##################################################################################################################
#Script is designed to work on a package stereotyped control service 
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
from java.lang import Object
from java.lang import String

from SystemsReasoner import Specialize
reload(Specialize)
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
from FrameworkValidation import ProfileValidation as VP
reload(VP)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProjectsManager().getActiveProject()
#package = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
ef = project.getElementsFactory()
StateVariableStereotype=StereotypesHelper.getStereotype(project,"Variable Type", "Mission Service Architecture Framework")
StateTLPackStereotype=StereotypesHelper.getStereotype(project,"Timelines Package","Mission Service Architecture Framework")
#sucTLPackStereotype=StereotypesHelper.getStereotype(project,"System Under Control Timelines Package","Control Service Framework") #we don't need this anymore
sucEventPackStereotype=StereotypesHelper.getStereotype(project,"Event Package","Mission Service Architecture Framework")
#StateTimelinePackage=package.getOwner()
mem = ModelElementsManager.getInstance()
StateTimelineStereotype=StereotypesHelper.getStereotype(project,"Timeline","Mission Service Architecture Framework")
CSProf=StereotypesHelper.getProfile(project,"Mission Service Architecture Framework")
allCS=VP.getEverything(CSProf)
#gl.log("Are we getting things here===>"+str(len(allCS)))

CSst=StereotypesHelper.getStereotypesByProfile(CSProf)
svStereotype=StereotypesHelper.getStereotype(project,"Variable","Mission Service Architecture Framework")
svPackStereotype=StereotypesHelper.getStereotype(project,"Variable Types Package","Mission Service Architecture Framework") #need to fix in the CS
eventStereotype=StereotypesHelper.getStereotype(project,"Event","Mission Service Architecture Framework") #no stereotype, fix in CS
#gl.log("The event Stereotype====>"+eventStereotype.getQualifiedName())
eventSpecsSt=StereotypesHelper.getStereotype(project,"Event Constraints View Package","Mission Service Architecture Framework")
SUCeventSpecSt=StereotypesHelper.getStereotype(project,"Event Constraint","Mission Service Architecture Framework")
constParamSt=StereotypesHelper.getStereotype(project,"ConstraintParameter","additional_stereotypes")
#Service Stereotypes
mappingSys={}
mappingReqState={}

#gl.log("Testing the name of something=======>"+SUCeventSpecSt.getName())

##Locate all of the states in the timelines module, should be in a specific state package 
##Locate State Package

###Assuming Click on the State Variable Types Package
##Owner of state variable types package is the State Timelines Package

#for each state variable need to create a constriant that is typed by the SV and inherits from the CS system under control SV


def StateEventTimelineCreation(package,checkOnly):
    Elements=package.getOwnedElement()
    GenError=0
    redError=0
    asscError=0
    MultState=0
    MultEvent=0
    StateTimelineError=0
    EventError=0
    GenAdd=0
    StateAdd=0
    EventAdd=0
    AssocAdd=0
    RedAdd=0
    StAdd=0
    AbsAdd=0
    AbsError=0
    constError=0
    constAdd=0
    checkDiag=0
    mappingSys={}
    redefCs={}
    litError=0
    litEventAdd=0
    abs=False
    
                #newEv=PC.elementCreate(csElements, newPack)
                        #mappingSys[EventIns]==newElement
                        #options={"checkOnly":False, "mapping":mappingSys, 'fixforOpsrev':True,'fixRedefName': True,'fixAll': False}
                        #Specialize.generalize(newElement, options)
                
#    if abs==True:
#        return GenError,redError,StateTimelineError,EventError,GenAdd,EventAdd,StateAdd,AssocAdd,RedAdd,asscError,StAdd,AbsAdd,MultState,MultEvent,mappingReqState,AbsError,constError,constAdd                
    gl.log("--------------------Working on Validation of Relationships to State, Event, Constraint-------------------")
    for e in Elements:
        gl.log("-------------State Variable Working On-------------------"+e.getQualifiedName())
        valueS=0;
        valueE=0;
        create1=False
        get1=False
        create2=False
        if StereotypesHelper.hasStereotype(e,StateVariableStereotype):
            absSv=e.isAbstract()
            if absSv==False:
                AbsError+=1
            if checkOnly != True:
                #gl.log("Note: Fixing the Abstract")
                AbsAdd+=1
                e.setAbstract(True)
            ###Need to check if element has association from State Timeline
            ##Need to check the general of E here and set it to CSStateVariableType if not 
            #get CS StateVariable Element
            
            #instead of control service generalization here we want to do generalization to the state timeline from the base you are constructing from##
            
            #see where the state variable is generalizing from?
            genSvs=e.getGeneral()
            genCheck=0
            for genSv in genSvs:
                #gl.log(genSv.getName())
            #now check to see if the element is in the CSAF, if it's not then, assume that is the base, if it is and there is no other base element, then we can assume the base is the csaf
                if genSv not in allCS:
                    genSvElem=genSv
                    genCheck=1
                    #gl.log("The name of the good generalization element====>"+genSv.getName())
                if genCheck==1:
                    break
            if genCheck==0:
                genSvElem=genSv
                
            genElem=genSvElem.getOwner().getOwner()
            CSProf=genElem
            
            #gl.log("General State Variable Element======>"+genSvElem.getQualifiedName())
            
            StateTlCS=ControlServiceGeneralization(genSvElem,e,StateTLPackStereotype,StateTimelineStereotype,False)
            #gl.log("statetlcs===>"+StateTlCS.getQualifiedName())
            StTlPack=StateTlCS.getOwner()
            svPack=StTlPack.getNestedPackage()
            for packs in svPack:
                if StereotypesHelper.hasStereotype(packs,svPackStereotype):
                    #gl.log("******hey did we get here****"+packs.getQualifiedName())
                    svElems=packs.getOwnedElement()
                    for svElem in svElems:
                        if StereotypesHelper.hasStereotype(svElem,StateVariableStereotype):
                            csSVElem=svElem
        
#            generalSV=e.getGeneral()
#            if len(generalSV)==0:
#                gl.log("*****ERROR*****The Element Does not have the correct generalization to CS")
#                GenError+=1
#                if checkOnly != True:
#                    gl.log("*******Adding the Generalization to the System Under Control SV*******========>"+e.getName())
#                    GenAdd+=1
#                    #put function here
#                    CreateGeneralization(csSVElem,e)
#            elif generalSV==csSVElem:
#                gl.log("The Correct Generalization is made to the CS State Variable Type Element")
                
            
            propE=e.get_typedElementOfType()
            #rela=filter(lambda element: isinstance(element,Class),rela)
            for k in propE:
                blockE=k.getOwner()
                #elemR=k.getTarget()
                #gl.log("yoyoyo===>"+str(len(blockE)))
                #elemR=filter(lambda element: isinstance(element,Class),elemR)
                if isinstance(blockE,Class):
                    #gl.log("element name===>" + blockE.getName())
                    if StereotypesHelper.hasStereotype(blockE,StateTimelineStereotype):
                        valueS+=1
                        #gl.log("This is the element that has the stateTimelineStereotype=====>"+blockE.getQualifiedName())
                        eventS=blockE
                    if StereotypesHelper.hasStereotype(blockE,eventStereotype):
                        valueE+=1
                        eventG=blockE
            gl.log("This is the value of the State Timeline Count:======>"+str(valueS))
            gl.log("This is the value of the Event Count:======>"+str(valueE))            
            
            if valueS>1:
                gl.log("*****ERROR******There is more than one State Timeline connected to the State Variable Type====>"+str(valueS))
                MultState+=1
                #when this happens....can still check event rule but hard to say what new is
            if valueE>1:
                gl.log("*****ERROR*****There is more than one Event connected to the State Variable Type====>"+str(valueE))
                MultEvent+=1
            
            ##Creating the Events
            if valueS==0 and valueE==1:
                propEvents=eventG.get_typedElementOfType()
                blah=ControlServiceGeneralization(genSvElem,eventG,StateTLPackStereotype,StateTimelineStereotype,get1)
                happy=0
                if len(propEvents)!=0:
                    for propEvent in propEvents:
                        stateBlock=propEvent.getOwner()
#                        gl.log("the state timeline block is????"+stateBlock.getQualifiedName())
#                        #if isinstance(stateBlock,Class):
                        if StereotypesHelper.hasStereotype(stateBlock,StateTimelineStereotype):
                            gl.log("*****ERROR******: There is a State Timeline Connected, but not directly to State Variable Type")
                            gl.log("*****ERROR******: No Association between State Variable and State Timeline")
                            asscError+=1
                            if checkOnly != True:
                                [newProp,newassoc]=CreateAssociation(stateBlock,e,kind)
                                AssocAdd+=1
                            #now setting the variable properties
                                CSState=blah.getOwnedElement()
                                props=newProp.getRedefinedProperty()
                                [RedAdd,GenAdd]=RedefinitionGenAdd(CSState,"variable",svStereotype,props,newProp,True,newassoc,RedAdd,GenAdd)
                            valueS=1
                            eventS=stateBlock
                                        
                                        
            if valueS==0:
                gl.log("*****ERROR****:This element has no association to state====>"+e.getName())
                StateTimelineError+=1
            ###Creating the State Timelines
                if checkOnly != True:
                    new = ef.createClassInstance()
                    StateAdd+=1
                    new.setAbstract(True)
                    AbsAdd+=1
                    new.setOwner(StateTimelinePackage)
                    StereotypesHelper.addStereotype(new,StateTimelineStereotype)
                    StAdd+=1
                    new.setName(e.getName())
                    #gl.log("The System Shall Provide a State Timeline=>  "+new.getName()+"  that represents state variable type=>  "+e.getName())
                    create1=True
                    blah=ControlServiceGeneralization(genSvElem,new,StateTLPackStereotype,StateTimelineStereotype,create1)
                    GenAdd+=1
                    mappingSys[blah]=new
                    kind=AggregationKindEnum.COMPOSITE
                    [newProp,newassoc]=CreateAssociation(new,e,kind)
                    AssocAdd+=1
                    #now setting the variable properties
                    CSState=blah.getOwnedElement()
                    props=newProp.getRedefinedProperty()
                    [RedAdd,GenAdd]=RedefinitionGenAdd(CSState,"variable",svStereotype,props,newProp,True,newassoc,RedAdd,GenAdd)
                    valueS=1
                    eventS=new
                            
                            
                
            if valueS==1 and valueE==0:
                #add a check here to see if the timeline has an event associated.....if it has an event....but the variable type did not then hook that shit up
                new=eventS
                #eventS.setName(e.getName())
                varTypeAttrs=eventS.getOwnedAttribute()
                for varTypeAttr in varTypeAttrs:
                    if StereotypesHelper.hasStereotype(varTypeAttr.getType(),eventStereotype):
                        gl.log("*****ERROR*****There is an event associated to the Timeline but not to the variable....remedy this situation=====>")
                        eventElem=varTypeAttr.getType()
                        if checkOnly != True:
                            blah2=ControlServiceGeneralization(genSvElem,eventElem,sucEventPackStereotype,eventStereotype,False)
                            CSEvent=blah2.getOwnedElement()
                            gl.log("*******Adding the Shared Association between event and State Variable*******========>")
                            kindSh=AggregationKindEnum.SHARED
                            [newPropSh,newassocSh]=CreateAssociation(eventElem,e,kindSh)
                            AssocAdd+=1
                            propsSh=newPropSh.getRedefinedProperty()
                            [RedAdd,GenAdd]=RedefinitionGenAdd(CSEvent,"variable",svStereotype,propsSh,newPropSh,True,newassocSh,RedAdd,GenAdd)
                            valueE=1
                            eventG=eventElem
                        
                kind=AggregationKindEnum.COMPOSITE
                get1=False
                blah=ControlServiceGeneralization(genSvElem,eventS,StateTLPackStereotype,StateTimelineStereotype,get1)
                CSState=blah.getOwnedElement()
                absS=eventS.isAbstract()
                if absS==False:
                    AbsError+=1
                    if checkOnly != True:
                        gl.log("Note: Fixing the Abstract")
                        AbsAdd+=1
                        eventS.setAbstract(True)
                
                generalST=eventS.getGeneral()
                #gl.log("*****The Correct State Timeline is associated====>"+eventS.getQualifiedName())
                
                if len(generalST)==0:
                    gl.log("*****ERROR*****The State Timeline generalization is not correct")
                    GenError+=1
                    if checkOnly != True:
                        #gl.log(str(GenError))
                        #gl.log("see what blah and eventS are===>   "+blah.getQualifiedName()+"  ==>  "+ eventS.getQualifiedName())
                        CreateGeneralization(blah,eventS)
                        GenAdd+=1
                        mappingSys[blah]=eventS
                elif generalST[0]==blah:
                    gl.log("The generalization is correct for element====>"+eventS.getQualifiedName())
                
                redefName="variable"
                [redError,RedAdd,StAdd,GenAdd,GenError,redefCs]=RedefinedCheck(e,StateTimelineStereotype,CSState,svStereotype,redefName,checkOnly,redError,RedAdd,StAdd,GenAdd,GenError,redefCs)

                            
            
            if valueE==0:
            ##looking for event package
                gl.log("*****ERROR*****:There is no Event associated with the State Variable======>"+e.getName())
                EventError+=1
                tlPacks=StateTimelinePackage.getNestedPackage()
                for t in tlPacks:
                    #gl.log("Just checking the value of the packages we are finding here====>"+t.getQualifiedName())
                    if StereotypesHelper.hasStereotype(t,sucEventPackStereotype):
                        #gl.log("what is happening here?")
                        if checkOnly != True:
                            newEvent = ef.createClassInstance()
                            newEvent.setAbstract(True)
                            AbsAdd+=1
                            EventAdd+=1
                            newEvent.setOwner(t)
                            StereotypesHelper.addStereotype(newEvent,eventStereotype)
                            StAdd+=1
                            newEvent.setName(e.getName())
                            create1=True
                        #gl.log(str(create1))
                            blah2=ControlServiceGeneralization(genSvElem,newEvent,sucEventPackStereotype,eventStereotype,create1) #this is not going to work
                            GenAdd+=1
                            mappingSys[blah2]=newEvent
                            gl.log("*******Adding the Composition between event and State Timeline*******========>")
                            [newPropEv,newassocEv]=CreateAssociation(eventS,newEvent,AggregationKindEnum.COMPOSITE)
                            AssocAdd+=1
                            propsEv=newPropEv.getRedefinedProperty()
                        #now setting the generalization up
                            CSEvent=blah2.getOwnedElement()
                        #gl.log("the Control Service Event================================>"+blah2.getName())
                            blah=ControlServiceGeneralization(genSvElem,eventS,StateTLPackStereotype,StateTimelineStereotype,get1)
                            CSState=blah.getOwnedElement()
                            
                            [RedAdd,GenAdd]=RedefinitionGenAdd(CSState,"Event",None,propsEv,newPropEv,True,newassocEv,RedAdd,GenAdd)
                if checkOnly != True:
                    gl.log("*******Adding the Shared Association between event and State Variable*******========>")
                    kindSh=AggregationKindEnum.SHARED
                    [newPropSh,newassocSh]=CreateAssociation(newEvent,e,kindSh)
                    AssocAdd+=1
                    propsSh=newPropSh.getRedefinedProperty()
                    [RedAdd,GenAdd]=RedefinitionGenAdd(CSEvent,"variable",svStereotype,propsSh,newPropSh,True,newassocSh,RedAdd,GenAdd)
                    valueE=1
                    eventG=newEvent
            
            if valueE==1:
                ##Check for the association to the state timeline, if its not there then add it, if there is a state timeline
                
                #since there is an event associated need to check and see if there is constraint with appropriate redefinition
                #[redError,RedAdd,StAdd,GenAdd,GenError]=RedefinedCheck(eventG,StateTimelineStereotype,CSState,None,"State Event",checkOnly,redError,RedAdd,StAdd,GenAdd,GenError)
                new=eventG
                #eventG.setName(e.getName())
                
                absE=eventG.isAbstract()
                if absE==False:
                    AbsError+=1
                    if checkOnly != True:
                        #gl.log("Note: Fixing the Abstract")
                        AbsAdd+=1
                        eventG.setAbstract(True)
                        #need to add an abs count
                        
                    
                #gl.log("The Event we are working on ====================>"+new.getQualifiedName())
                propEvents=new.get_typedElementOfType()
                blah=ControlServiceGeneralization(genSvElem,new,StateTLPackStereotype,StateTimelineStereotype,get1)
                happy=0
                if valueS==1:
                    for propEvent in propEvents:
                        stateBlock=propEvent.getOwner()
#                        gl.log("the state timeline block is????"+stateBlock.getQualifiedName())
#                        #if isinstance(stateBlock,Class):
                        if StereotypesHelper.hasStereotype(stateBlock,StateTimelineStereotype):
                            #gl.log("The Correct Association!!!!!!!!!!!WHOOO")
                            happy=1
                    if happy !=1:
                        gl.log("ERROR:  There is not an association between the State Timeline======>"+eventS.getName()+"    and Event=====>:"+eventG.getQualifiedName())
                        asscError+=1
                        if checkOnly != True:
                            [newPropEv,newassocEv]=CreateAssociation(eventS,eventG,kind)
                            propsEv=newPropEv.getRedefinedProperty()
                            AssocAdd+=1
                            CSState=blah.getOwnedElement()
                            [RedAdd,GenAdd]=RedefinitionGenAdd(CSState,"Event",None,propsEv,newPropEv,True,newassocEv,RedAdd,GenAdd)
                                   
                else:
                    gl.log("*****ERROR*****: There is not a state timeline associated with this event=======>"+new.getQualifiedName())
                    
                kind=AggregationKindEnum.COMPOSITE
                get1=False
                blah2=ControlServiceGeneralization(genSvElem,new,sucEventPackStereotype,eventStereotype,get1)
                CSEvent=blah2.getOwnedElement()
                evAttrs=eventG.getOwnedAttribute()
                csEvAttrs=blah2.getOwnedAttribute()
                checkConst=0
                #gl.log("What event are we on?====>"+eventG.getName())
                if evAttrs:
                    for evAttr in evAttrs:
                        evAttrType=evAttr.getType()
                        #gl.log("What is the type of the event attribute?===>"+evAttrType.getName())
                        evAttrTypeSt=StereotypesHelper.getStereotypes(evAttrType)
                        if SUCeventSpecSt in evAttrTypeSt:
                            checkRedefConst=evAttrType
                            evCProp=evAttr.getRedefinedProperty() #event constraint property, its redefined property should be the cs event attribute property
#                            csEventPack=blah2.getOwner()
#                            csEventSpecs=csEventPack.getNestedPackage()
#                            csEventSpecPack=filter(lambda element: StereotypesHelper.hasStereotype(element,eventSpecsSt),csEventSpecs)
#                            gl.log(csEventSpecPack[0].getQualifiedName())
                            CSConst=ControlServiceGeneralization(genSvElem,None,eventSpecsSt,SUCeventSpecSt,False)
                            if CSConst!=None:
                                CSConst=CSConst.getOwnedElement()
                            #gl.log("the control service constraint?===>"+CSConst.getQualifiedName())
                            redefName="constraint"
                            [redError,RedAdd,StAdd,GenAdd,GenError,redefCs]=RedefinedCheck(evAttrType,eventStereotype,CSEvent,None,redefName,checkOnly,redError,RedAdd,StAdd,GenAdd,GenError,redefCs)
                            
                            for csEvAttr in csEvAttrs:
                                #gl.log("The name of the control service attribute...."+csEvAttr.getQualifiedName())
                                csEvAttrType=csEvAttr.getType()
                                if csEvAttrType !=None:
                                    #gl.log("The name of the Control Service Event...."+csEvAttrType.getQualifiedName())
                                    csEvAttrTypeSt=StereotypesHelper.getStereotypes(csEvAttrType)
                                    if SUCeventSpecSt in csEvAttrTypeSt:
                                        if csEvAttr in evCProp:
                                            checkConst=1
                                            break
                                if checkConst==1:
                                    break
                else:
                    gl.log("*****ERROR*****The event has no attributes")
                    checkConst=1
                        
                #gl.log("What is the event?====>"+eventG.getName())
                general=eventG.getGeneral()
                #gl.log("What is general?====>"+general[0].getName())
                #gl.log("What is blah?=====>"+blah2.getName())
                
                if len(general)==0:
                    gl.log("*****ERROR*****:The generalization is not correct!")
                    GenError+=1
                    if checkOnly != True:
                        gl.log("*******Adding the Generalization to the New Element*******========>"+eventG.getQualifiedName()+"     Generalizing from CS Element =======>" +blah2.getName())
                        GenAdd+=1
                        CreateGeneralization(blah2,eventG)
                        mappingSys[blah2]=eventG
                elif general[0]==blah2:
                    gl.log("The generalization is correct for element====>"+eventG.getQualifiedName())
                    
                CSState=blah.getOwnedElement()
                CSEvent=blah2.getOwnedElement()
                #gl.log("*****The Correct EVENT is associated")
                #propE=e.get_typedElementOfType()
                #rela=filter(lambda element: isinstance(element,Class),rela)
                redefName="variable"
                [redError,RedAdd,StAdd,GenAdd,GenError,redefCs]=RedefinedCheck(e,eventStereotype,CSEvent,svStereotype,redefName,checkOnly,redError,RedAdd,StAdd,GenAdd,GenError,redefCs)
                #gl.log("The Redefinition Error count!!!!!!"+str(redError))
                
            if valueE==1 and valueS==1:
                absE=eventG.isAbstract()
                absS=eventS.isAbstract()
                mappingReqState[e]=eventS
                #eventG.setName(e.getName())
                #eventS.setName(e.getName())
                        #need to add in a count here
                blah=ControlServiceGeneralization(genSvElem,new,StateTLPackStereotype,StateTimelineStereotype,get1)
                CSState=blah.getOwnedElement()
                blah2=ControlServiceGeneralization(genSvElem,new,sucEventPackStereotype,eventStereotype,get1)
                CSEvent=blah2.getOwnedElement()
                #means that there is an event and state timeline for the state variable, just need to check the association now and its redefinition
                if StereotypesHelper.hasStereotype(eventG,eventStereotype):
                    
                    [redError,RedAdd,StAdd,GenAdd,GenError,redefCs]=RedefinedCheck(eventG,StateTimelineStereotype,CSState,None,"Event",checkOnly,redError,RedAdd,StAdd,GenAdd,GenError,redefCs)
            
                csEvent=ControlServiceGeneralization(genSvElem,eventG,sucEventPackStereotype,eventStereotype,get1)
                csEventPack=csEvent.getOwner()
                csEventSpecs=csEventPack.getNestedPackage()
                for csEventSpecPack in csEventSpecs:
                    checkPort=0
                    if StereotypesHelper.hasStereotype(csEventSpecPack,eventSpecsSt):
                        #need to fix this
                        #CsConst=ControlServiceGeneralization(CSProf,None,csEventSpecPack,SUCeventSpecSt,False) #this will give the constraint block in the CS
                        #insConst=ControlServiceGeneralization(csEvent.getOwner().getOwner(),None,csEventSpecPack,SUCeventSpecSt,False)
                        test=e.getOwner().getOwner().getOwner()
                        EventSpecPack=findPackage(eventSpecsSt,e.getOwner().getOwner(),True)
                        EventSpecs=EventSpecPack.getOwnedElement()
                        csEventSpecs=csEventSpecPack.getOwnedElement()
                        csEventSpecs=filter(lambda element: StereotypesHelper.hasStereotype(element,SUCeventSpecSt),csEventSpecs)
                        EventSpecs=filter(lambda element: StereotypesHelper.hasStereotype(element,SUCeventSpecSt),EventSpecs)
                        #when we do this, need to add in association to the event
                        for constraint in EventSpecs:
                            ports=constraint.getRole()
                            for port in ports:
                                if port.getType()==e:
    #                                gl.log("port name===>"+port.getName())
    #                                gl.log("port type===>"+port.getType().getName())
    #                                gl.log("sv name===>"+e.getName())
                                    checkPort=1
                        if checkPort!=1:
                            gl.log("******ERROR******There is not a constraint separately defined for this state variable type=====>"+e.getName())
                            constError+=1
                            if checkOnly != True:
                                gl.log("*******Adding the Constraint Port for the State Variable*******")
                                newC=ef.createClassInstance()
                                newC.setOwner(EventSpecPack)
                                newC.setName(e.getName())
                                newC.setAbstract(True)
                                StereotypesHelper.addStereotype(newC,SUCeventSpecSt)
                                [newPropEC,newassocEC]=CreateAssociation(eventG,newC,AggregationKindEnum.COMPOSITE)  # I think this should be from the event to the new thing 
                                AssocAdd+=1
                                propsEC=newPropEC.getRedefinedProperty()
                                [RedAdd,GenAdd]=RedefinitionGenAdd(CSEvent,"Event Constraint",None,propsEC,newPropEC,True,newassocEC,RedAdd,GenAdd)
                                for constraintCS in csEventSpecs:
                                    genConstAttrs=csEvent.getOwnedAttribute()
                                    if StereotypesHelper.hasStereotype(constraintCS,SUCeventSpecSt):  #this alone is not good enough anymore, need to check the relations as well
                                        for genConstAttr in genConstAttrs:
                                            if genConstAttr.getType()==constraintCS:
                                                CreateGeneralization(constraintCS,newC)
                                                portCS=constraintCS.getRole()
                                                if len(portCS)!=0:
                                                    portCS=portCS[0]
                                                    newport=ef.createPortInstance()
                                                    newport.setOwner(newC)
                                                    portConst=newport.getRedefinedPort()
                                                    portConst.add(portCS)
                                                    newport.setType(e)
                                                    newport.setName(e.getName())
                                
                                constAdd+=1

    gl.log("--------------------Working State Variable Types with Literals-------------------")
    for e in Elements:
        if StereotypesHelper.hasStereotype(e,StateVariableStereotype) and isinstance(e,Enumeration):
            #gl.log("What state variable are we on====>"+e.getName())
            stlPack=e.getOwner().getOwner()
            diags=stlPack.getOwnedDiagram()
            literals=e.getOwnedLiteral()
            EventIns=None
            constOwn=None
            if literals:
                abs=True
                ######DONT NEED DIAGRAM STUFF RIGHT NOW
#                for diag in diags:
#                    if diag.getName()==e.getName()+" View":
#                        checkDiag=1
#                if checkDiag==0 and checkOnly!=True:
#                    gl.log("Creating Concrete Diagram for State Variable")
#                    d=mem.createDiagram("SysML Block Definition Diagram", stlPack)
#                    d.setName(e.getName()+" View")
#                    d.setOwner(stlPack)
#                elif checkDiag==0 and checkOnly==True:
#                    gl.log("Error there is no diagram for the Concrete State Variable===>"+e.getName())
                #if checkOnly!=True:  #we can take this out
                    #CSEvent=ControlServiceGeneralization(CSProf,e,sucEventPackStereotype,eventStereotype,False) # I don't know that i need this
                propEs=e.get_typedElementOfType()
                for propE in propEs:
                    #gl.log("properties====>" +propE.getQualifiedName())
                    blockE=propE.getOwner()
                    if isinstance(blockE,Class):
                        #gl.log("the Class,====>"+blockE.getQualifiedName())
                        if StereotypesHelper.hasStereotype(blockE,eventStereotype) and e.getName()==blockE.getName():
                            #gl.log("should be the event,====>"+blockE.getQualifiedName())
                            #need this to be more specific
                            EventIns=blockE
                            #gl.log("the event again====>"+EventIns.getQualifiedName())
                            csEvAttr=EventIns.getOwnedAttribute()
                            #element.getType().getOwner().getOwner()==EventIns.getOwner()
                            ConstAttr=filter(lambda element: StereotypesHelper.hasStereotype(element.getType(),SUCeventSpecSt) and element.getType().getName()!="Event Constraint",csEvAttr)
                            if len(ConstAttr)!=0:
                                #gl.log("The constraint attribute====>"+ConstAttr[0].getQualifiedName())
                                csEvConst= ConstAttr[0].getType()
                                #gl.log("The Event Constraint====>"+csEvConst.getQualifiedName())
                            else:
                                gl.log("ERROR....There is not an Event Constraint connected to the Event ")
                            #gl.log("Constraint======>"+csEvConst.getQualifiedName())
                litMap={}
                check=0
                if EventIns!=None and csEvConst!=None:
                    packOwn=EventIns.getOwner()
                    constOwn=csEvConst.getOwner()  #this is wrong here
                    #gl.log("Lets look at the constraint Owner=====>"+constOwn.getQualifiedName())
                    #gl.log("the CS Event Constraint===>"+csEvConst.getQualifiedName())
                    #checkEnum=isinstance(blockE,Enumeration)
                else:
                    packS=stlPack.getNestedPackage()
                    for pack in packS:
                        if StereotypesHelper.hasStereotype(pack,sucEventPackStereotype):
                            packOwn=pack
                evElems=packOwn.getOwnedElement()
                #evElems=filter(lambda element: element.getName(),evElems)
                #literals=filter(lambda element: element.getName() is in evElems.getName(),literals)
                
                for m in evElems:
                    for l in literals:
                        string=l.getName()+" Event"
                        #gl.log("hey====>"+string+"   ===>"+m.getName())
                        if string==m.getName():
                            litMap[l]=1
                            #gl.log(evName)
                constCheck=0
                for l in literals:
                    if l not in litMap:
                        gl.log("*****ERROR*****:There is a literal in the SV without an Event and Equals Constraint=====>"+l.getQualifiedName())
                        litError+=1
                        if checkOnly!=True and constOwn!=None:
                            newElement = VP.createElement(eventStereotype, packOwn, False)
                            litEventAdd+=1
                            newElement.setName(l.getName()+" Event")
                            newElement.setOwner(packOwn)
                            StereotypesHelper.addStereotype(newElement,eventStereotype)
                            CreateGeneralization(EventIns,newElement)
                            if check==0:
                                ##need to check here if we need to create a constraint or not
                                ConstE=constOwn.getOwnedElement()
                                for Const in ConstE:
                                    if Const.getName()==e.getName()+" Equals Constraint":
                                        newConst=Const
                                        constCheck=1
                                if constCheck==0:
                                    newConst=VP.createElement(SUCeventSpecSt,constOwn,False)
                                    newConst.setName(e.getName()+" Equals Constraint")
                                    newConst.setOwner(constOwn)
                                    StereotypesHelper.addStereotype(newConst,SUCeventSpecSt)
                                    CreateGeneralization(csEvConst,newConst)
                                    newConstr=ef.createConstraintInstance()
                            #need to create opaque expression first
                                    cPort=ef.createPortInstance()
                                    cPort.setType(e)
                                    cPort.setOwner(newConst)
                                    cPort.setName("value")
                                    oE=ef.createOpaqueExpressionInstance()
                                    bodyOE=oE.getBody()
                                    bodyOE.add(e.getName() +"  ==  value")
                                    newConstr.setOwner(newConst)
                                    oE.setOwner(newConstr)
                                    newConstr.setSpecification(oE)
                                    constElem=newConstr.getConstrainedElement()
                                    constElem.add(newConst)
                                    check=1
                                    StereotypesHelper.addStereotype(cPort,constParamSt)
                        
                        #now create Association for event and sv
                            [newPropEv,newassocEv]=CreateAssociation(newElement,e,AggregationKindEnum.COMPOSITE)
                            newPropEv.setReadOnly(True)
                            newPropEv.setStatic(True)
                            newPropEv.setName("value")
                        #need to create a composite association between the event created and the equals constraint, that redefines from what it inherits
                            [newPropEC,newassocEC]=CreateAssociation(newElement,newConst,AggregationKindEnum.COMPOSITE)  # I think this should be from the event to the new thing 
                            AssocAdd+=1
                            propsEC=newPropEC.getRedefinedProperty()
                        #gl.log("The event ins=====>"+EventIns.getName())
                        
                            [RedAdd,GenAdd]=RedefinitionGenAdd(EventIns.getOwnedElement(),"Event Constraint",None,propsEC,newPropEC,True,newassocEC,RedAdd,GenAdd)
        
        
        options={"checkOnly":False, "mapping":mappingSys, 'fixforOpsrev':True,'fixRedefName': True,'fixAll': False, 'fixWrongValueType':False, 'fixWrongRedef':False}
        for m in mappingSys:
            newElement = mappingSys[m]
            Specialize.generalize(newElement, options)
    return GenError,redError,StateTimelineError,EventError,GenAdd,EventAdd,StateAdd,AssocAdd,RedAdd,asscError,StAdd,AbsAdd,MultState,MultEvent,mappingReqState,AbsError,constError,constAdd,litError,litEventAdd,redefCs
                    
            
def ControlServiceGeneralization(genSvElem,Elem,StereoPack,StereoElem,create):
    CSProf=genSvElem.getOwner().getOwner()
    #gl.log("CSProf name=========>"+CSProf.getQualifiedName())
    #All this does is based on a stereotype of package and element passed in, find that package and the element, and create generalization if needed
    #CSPacks=CSProf.getNestedPackage()
    CSPack=findPackage(StereoPack,CSProf,True)
    #gl.log("What package did we find=====>"+CSPack.getQualifiedName())
    CcsElem=None
    #CSP=filter(lambda element: not isinstance(element,GeneralizationSet), csElements)
    if StereotypesHelper.hasStereotype(CSPack,StereoPack): #sucPacks need to be changed to lifecycle Domain Package, I don't think I need this now
        csTLPack=CSPack.getOwnedElement()
        for csElem in csTLPack:
            #gl.log("What are the things we are getting here...."+csElem.getQualifiedName())
            if StereotypesHelper.hasStereotype(csElem,StereoElem):  
                genAttrs=csElem.getOwnedAttribute()
                for genAttr in genAttrs:
                    #gl.log("The general attribute type Name()===>"+genAttr.getType().getQualifiedName())
                    #gl.log("The general element===>"+genSvElem.getQualifiedName())
                    if genAttr.getType()==genSvElem:#this is going to get more than one thing if not in the control service where there is only one
                        CcsElem=csElem
#                    #gl.log(m.getName())
#                    StateElem=m.getOwnedElement()
#                    for j in StateElem:
#                        #gl.log("Hey are we gettin here======>"  + j.getName())
#                        if StereotypesHelper.hasStereotype(j,s):
                        if create==True:
                            gl.log("*******Adding the Generalization to the New Element*******========>"+Elem.getName()+"     Generalizing from CS Element =======>" +csElem.getName())
                            newgen = ef.createGeneralizationInstance()
                            newgen.setGeneral(csElem)
                            newgen.setSpecific(Elem)
                            newgen.setOwner(Elem)
                        break
                    #I can't have this here, because we rely on it finding a match 
                    #else:
                        #CcsElem=None
                
            
    return CcsElem

def CreateAssociation(OwnerB,elem,kindy):
    newassoc=ef.createAssociationInstance()
    newProp= newassoc.getMemberEnd().get(0) #block dragged from, that has black diamond, the one I want to redefine and stereotype to be state variable Stereotype applied
    newProp2 = newassoc.getMemberEnd().get(1)
    newProp.setOwner(OwnerB)
    newProp.setName(elem.getName())
    newProp2.setOwner(newassoc)
    newProp.setType(elem)
    newProp2.setType(OwnerB)
    newassoc.setOwner(OwnerB.getOwner())
    newProp.setAggregation(kindy)
    
    return newProp, newassoc

def CreateGeneralization(GeneralEl,SpecificEl):
    if SpecificEl is not None and GeneralEl is not None:
        gl.log("*******Adding the Generalization to the Profile Element*******========>"+SpecificEl.getQualifiedName()+"     Generalizing from CS Element =======>" +GeneralEl.getQualifiedName())
        newgen = ef.createGeneralizationInstance()
        newgen.setGeneral(GeneralEl)
        newgen.setSpecific(SpecificEl)
        newgen.setOwner(SpecificEl)
    
def RedefinitionGenAdd(redefElem,redefName,redefSt,Props,property,gen,newassoc,RedAdd,GenAdd):
    #gl.log("********Do we even get into this script**********"+redefName)
    #gl.log("******The property Name====>"+property.getQualifiedName())
    check=0
    for l in redefElem:
        if isinstance(l,Property) and check==0:
            #NameR=l.getName().strip()
            NameR=l.getName().replace("Abstract","")
            NameR=NameR.strip()
            #gl.log("the name of the redefined element=====>"+NameR.replace("Astract","").strip()+"     and the name of the redefinition=====>"+redefName)  # this is not a good check here at all
            #if isinstance(l,Property):
            #gl.log("*******====>"+l.getName())
            #probelm here with this name matching....state variable...does not match variable
            if property.getType()!=None and l.getType()!=None:
                if NameR==redefName or StereotypesHelper.getStereotypes(property.getType())==StereotypesHelper.getStereotypes(l.getType()):
                    gl.log("******Adding the appropriate redefinition! To the Property=====>"+property.getQualifiedName())
                    check=1
                    Props.add(l)
                    RedAdd+=1
                    if len(Props)==1:
                        Props[0].setName(NameR)
                        if gen==True:
                            csAssoc=l.getAssociation()
                            CreateGeneralization(csAssoc,newassoc)
                            GenAdd+=1
                        if redefSt !=None:
                            #gl.log("------------------------HEY------------------"+redefSt.getName())
                            StereotypesHelper.addStereotype(property,redefSt)
                        break

                
    return RedAdd,GenAdd
                    
def findPackage(packSt,CSProf,checkOnly):  #pass in stereotype of package your looking for, and a base starting package for where you want to start looking
    #packSt=StereotypesHelper.getStereotypes(package)
    if StereotypesHelper.hasStereotype(CSProf,packSt):
            boo=CSProf
            return boo
    CSPacks=CSProf.getNestedPackage()
    for c in CSPacks:
        #packSt=filter(lambda element: element in CSst, packSt)
        #gl.log("Checking out what package we are on====>"+c.getName())
        if StereotypesHelper.hasStereotype(c,packSt):
            boo=c
            #gl.log("hehehehehhe========>"+boo.getName())
            return boo
        if isinstance(c,Package):
            CSPackage=findPackage(packSt,c,checkOnly)
            if CSPackage is not None:
                return CSPackage
    return None


def RedefinedCheck(elem,StereoAssoc,redefElem,roleSt,redefName,checkOnly,redError,RedAdd,StAdd,GenAdd,GenError,redefCs):
    #redef element, is the generalization item of owned elements that the thing owns
    propE=elem.get_typedElementOfType()
    #gl.log("the Redefined element====>"+redefElem[0].getQualifiedname())
    for property in propE:
        inc=0
        blockE=property.getOwner()
        if isinstance(blockE,Class):
            if StereotypesHelper.hasStereotype(blockE,StereoAssoc):
                props=property.getRedefinedProperty()
                
                if len(props)==0:
                    gl.log("*****ERROR: This state variable property does not have the correct redefinition..........no redefined property at all*****"+property.getQualifiedName())
                    redError+=1
                    redefCs[property]=property
                    if checkOnly!=True:
                        #gl.log("ARE WE GETTING HERE")
                        [RedAdd,GenAdd]=RedefinitionGenAdd(redefElem,elem.getName(),roleSt,props,property,False,None,RedAdd,GenAdd)
                else:
                    props2=[]
                    props2.extend(props)
                    for q in props2:
                        #gl.log("The redefined property name....."+q.getQualifiedName())
                        #gl.log("The redefname======>"+redefName)
                        for thing in redefElem:
                            if q==thing:  #this is a really bad check, should probably check that this constaint type is associated to an event or some such....but this is not only for constraints
                                inc=1
                            if inc==1:
                                break
                                #gl.log("This property has the appropriate redefinition")
                        if inc==1:
                            break
                    if inc!=1:
                        gl.log("*****ERROR: This state variable property does not have the correct redefinition*****"+property.getQualifiedName())
                        redefCs[property]=property
                        redError+=1
                        if checkOnly != True:
                            #gl.log("ARE WE GETTING HERE")
                            [RedAdd,GenAdd]=RedefinitionGenAdd(redefElem,redefName,roleSt,props,property,False,None,RedAdd,GenAdd)
                for q in redefElem:
                    if isinstance(n,Property):
                        if n.getName()==redefName:
                            csAssoch=q.getAssociation()
                            AssochNew=property.getAssociation()
                            if AssochNew!=None:
                                general=AssochNew.getGeneral()
                                if len(general)==0:
                                    gl.log("*****ERROR*****: The generalization is not correct!")
                                    GenError+=1
                                    if checkOnly != True:
                                        CreateGeneralization(csAssoch,AssochNew)
                                        GenAdd+=1
                                    elif len(general)==1 and general==csAssoch:
                                        gl.log("The appropriate generalization exists")
                                    elif len(general)>1:
                                        gl.log("*****ERROR: The correct generalization is not there and cannot be fixed")
                                        GenError+=1
                
                                    
                                    
    return redError,RedAdd,StAdd,GenAdd,GenError,redefCs