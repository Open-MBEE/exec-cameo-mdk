'''
Created on Oct 13, 2011

@author: lcanders

Description:  This code is for Validation and Fix of the Control Service Architecture Framework.  The main thing this code outputs are the differences between the CS and the Instance.  The main things noted are the, CS Elements Found, Instance elements Found, redefinition Checks, packages, and properties
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

from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Interaction 
from javax.swing import *
from SystemsReasoner import Specialize
reload(Specialize)
import sys
import traceback
import os
import re
import PackageClone as PC
reload (PC)
#import ValidateTimeline as VT
#reload (VT)
import MDUtils._MDUtils as MDUtils
reload(MDUtils)
from SystemsReasoner import SRUtils
reload(SRUtils)
from FrameworkValidation import ProfileValidation as VP
reload(VP)

#import FixTimeline as FT
#reload (FT)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
datapack=project.getModel()
mapping={}
#package = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
count={}
#stereotypePack=StereotypesHelper.getStereotypes(package) #this needs to not be package anymore but what is passed in, which is target
#profile = StereotypesHelper.getProfile("Control Service Architecture Framework")
sysmlProf=StereotypesHelper.getProfile(project,"SysML")
mosTLprof=datapack.getNestedPackage()
mosTLprof=filter(lambda element: element.getName()=="MOS 2.0 Timelines",mosTLprof)
MOStlProf=mosTLprof[0]
#StereotypesHelper.getProfile(project,"MOS 2.0 Timelines")
ServiceProf=StereotypesHelper.getProfile(project,"Service")
TimelineActProf=StereotypesHelper.getProfile(project,"Activity Timeline")
TimelineParaProf=StereotypesHelper.getProfile(project,"Parametric Timeline")
TimelineGenProf=StereotypesHelper.getProfile(project,"General Timeline")
InformationProf=StereotypesHelper.getProfile(project,"Information Profile")
sysmlSt=StereotypesHelper.getStereotypesByProfile(sysmlProf)
svPackStereotype=StereotypesHelper.getStereotype(project,"Variable Types Package","Mission Service Architecture Framework")
SUCmodelPack=StereotypesHelper.getStereotype(project,"System Model Package","Mission Service Architecture Framework")
SUCmodel=StereotypesHelper.getStereotype(project,"System Model","Mission Service Architecture Framework")
TimelineSt=StereotypesHelper.getStereotype(project,"Timeline","Mission Service Architecture Framework")
TimelinePackSt=StereotypesHelper.getStereotype(project,"Timelines Package","Mission Service Architecture Framework")
SystemModelTypes=StereotypesHelper.getStereotype(project,"System Model Types Package")
#pst=StereotypesHelper.getStereotypesByProfile(profile)
#csElem=profile.getOwnedElement()
#nElem=package.getOwnedElement()
pattern=re.compile("(?i).*Fix Me!")
countFix=0
elementToFix=[]    
diagCount=0
elemCount=0
genCount=0
StereotypeCount={}
diagError=0
elemError=0
packError=0
genError=0
attrTypeStMatch=0
redefMatch=0
redefError=0
redefWarning=0
goodElem=0
goodRedef=0
goodGen=0
goodDiag=0
goodPack=0
insElems=0
csElems=0
sucModelFind=0
checkTls=0
sysModels={}
sysTls={}
#CSst=StereotypesHelper.getStereotypesByProfile(profile)

##need to add back in the count for packages for everything


def blah(package,csPack,profile,checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,matchInsElem,sucModelFind,sysModels,sysTls):
    CSst=StereotypesHelper.getStereotypesByProfile(profile)
    #insElem=0#need to fix later
    diagCount=diagCount
    elemCount=elemCount
    genCount=genCount
    El=csPack.getOwnedElement()
    Elnew=package.getOwnedElement()
    countDiagCS=filter(lambda element: isinstance(element,Diagram),El)
    countDiag=filter(lambda element: isinstance(element,Diagram),Elnew)
    goodDiag=goodDiag+len(countDiag)
    #redefCS={}
    #instElem={}
    if len(countDiagCS)!=len(countDiag):
                gl.log("The Control Service and New Package have a different amount of diagrams, the diagram count CS======>"+str(len(countDiagCS))+"    The count of new diagrams=====>" +str(len(countDiag))+ "     The Package Location of inconsistency======>"+package.getName())
                diagError+=1
    
    for CS in El:
        #gl.log(CS.getQualifiedName())
        if isinstance(CS,Package):
            #gl.log("What we are passing into find package====>"+CS.getQualifiedName()+"====>"+package.getQualifiedName())
            k=findPackage(CS,package,checkOnly,CSst)
            
            #small check for owner here
            OwnerCSPack=CS.getOwner().getOwner()
            OwnerNewPack=package.getOwner()
            csPackSt=StereotypesHelper.getStereotypes(OwnerCSPack)
            csPackSt=filter(lambda element: element in CSst,csPackSt)
            newPackSt=StereotypesHelper.getStereotypes(OwnerNewPack)
            newPackSt=filter(lambda element: element in CSst,newPackSt)
            if newPackSt!=csPackSt:
                gl.log("***********ERROR******The packaging structure is wrong on this package====>"+package.getQualifiedName())
                gl.log("The MSAF element has owner----->"+OwnerCSPack.getQualifiedName()+"   while the Service Implementation Package has Owner-----"+OwnerNewPack.getQualifiedName())
            
            if k==None:
                gl.log("***********ERROR******The CS Profile has a package that is not found in the Validation Package======>"+CS.getQualifiedName())
                packError+=1
                csPackOwn=CS.getOwner()
                #newOwn=package.getOwner()
                newPackOwn=findPackage(csPackOwn,package,checkOnly,CSst)
                
                if newPackOwn==None:
                    gl.log("The owner is a top level package in this module")
                
                if checkOnly!= True and newPackOwn!=None:  
                    gl.log("The new pack owner??*****=======>"+newPackOwn.getQualifiedName())
                    gl.log("*****Adding Package******The validation package is being added that matches CS Package")
                    CSstPack=StereotypesHelper.getStereotypes(CS)
                    newPack=ef.createPackageInstance()
                    newPack.setName(CS.getName())
                    newPack.setOwner(newPackOwn)
                    StereotypesHelper.addStereotypes(newPack,CSstPack)
                    [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,sysModels,sysTls]=blah(newPack,CS,profile,checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,matchInsElem,sucModelFind,sysModels,sysTls)
                
            else:
                #instElem[k]=1
                #gl.log("The package that matched====>"+k.getQualifiedName())
                #gl.log("The Control Service package====>"+CS.getQualifiedName())
                #gl.log(k.getQualifiedName())
                #gl.log(CS.getQualifiedName())
                #check owner of package stuff here
                [goodPack,packError]=OwnerCheck(k,CS,goodPack,packError,CSst)
                #move the generalization check and class instance check stuff into here
                [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,sysModels,sysTls]=blah(k,CS, profile,checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,matchInsElem,sucModelFind,sysModels,sysTls)
        elif isinstance(CS,Diagram):
            #countDiagCS+=1
            found=0
            for n in Elnew:
                if isinstance(n,Diagram):
                    #countDiag+=1
                    #gl.log("The name of the diagram====>  "+n.getQualifiedName())
                    typeold=project.getDiagram(CS).getDiagramType().getType()#cs ones
                    typenew=project.getDiagram(n).getDiagramType().getType()#new ones
                    if typeold==typenew:
                        found+=1
            if found==0:
                gl.log("*****ERROR****The diagram is not found*******========>"+CS.getName())
                diagError+=1
                if checkOnly != True:
                    gl.log("*****Adding the Missing Diagram in New Package*******     ====>"+ package.getName())
                    PC.diagramCreate([CS], package)
                    diagCount+=1
                
                                #"need to somehow check if the diagram is made....even though there is no link here"
        #elif isinstance(CS,Class) or isinstance(CS,Interface) or isinstance(CS,Enumeration):
        #elif not isinstance(CS,Interaction) and not isinstance(CS,Diagram) and not isinstance(CS,Package): #potentially just else
        elif isinstance(CS,Class) or isinstance(CS,Constraint) or isinstance(CS,DataType) or isinstance(CS,Port):
            csElems+=1
            
            gfind=0 #means you can't find cs thing
            genfind=0
                
                ##Right here can check the relations of the class that it found
                #CS2=filter(lambda element: isinstance(element,Class) or isinstance(element,Interface) or isinstance(element,Enumeration), El)
            #CS2=filter(lambda element: not isinstance(element,Interaction) and not isinstance(element,Diagram) and not isinstance(element,Package),El)
            CS2=filter(lambda element: isinstance(element,Class) or isinstance(element,Constraint) or isinstance(element,DataType) or isinstance (element,Port),El)
                #Elnew2=filter(lambda element: isinstance(element,Class) or isinstance(element,Interface) or isinstance(element,Enumeration), Elnew)
            #Elnew2=filter(lambda element: not isinstance(element,Interaction) and not isinstance(element,Diagram) and not isinstance(element,Package),Elnew)
            Elnew2=filter(lambda element: isinstance(element,Class) or isinstance(element,Constraint) or isinstance(element,DataType) or isinstance (element,Port) or isinstance (element,Property),Elnew)
#            if len(CS2)==len(Elnew2):
#                gl.log("Everything is just like the control service")
                
                #redefError=0
            portCheck=0
            #gl.log("******The CS Element:=====>" +CS.getQualifiedName())
            for n in Elnew2:
                instElem[n]=1 #think we are double counting or something here
                if not isinstance(n,Port) and not isinstance(n,Property):
                    general=n.getGeneral()
                    if len(general)==0:
                        genError+=1
                        stg=StereotypesHelper.getStereotypes(n)
                        gl.log("*******ERROR*****The Instance Element does not generalize from the Control Service!======>"+n.getName())
                        genfind=1 #setting this here because it doesn't mean that the block wasn't found. just means element in the instance is not generalizing from CS
                        stg=filter(lambda element: element not in sysmlSt, stg)
                        if len(stg)!=0:
                            #gl.log("*****control service element****>>>>>>>>>>"+CS.getName())
                            packEl=CS.getOwner().getOwnedElement()  #need to step up a package here, down one level too many
                            for p in packEl:
                                #gl.log("***CS Element that we are on........"+p.getName())
                                stp=StereotypesHelper.getStereotypes(p)
                                stp=filter(lambda element: element not in sysmlSt, stp)
                                if len(stp)!=0:
                                    #gl.log("********Stereotype Name New********"+stg[0].getName())
                                    #gl.log("*********Stereotype Name CS*********"+stp[0].getName())
                                    if stp[0] is stg[0]:
                                        if checkOnly !=True:
                                            gl.log("*******Adding the Generalization to the New Element*******========>"+n.getName()+"     Generalizing from CS Element =======>" +p.getName())
                                            genCount+=1
                                            newgen = ef.createGeneralizationInstance()
                                            newgen.setGeneral(p)
                                            newgen.setSpecific(n)
                                            newgen.setOwner(n)
                                            ###I think we need mapping and to run systems reasoner here
                                    else:
                                        gl.log("The Stereotypes never match, there is some unknown error type here======>" + n.getName())
                    for g in general:
                        #gl.log("*********The Instance element we are on****====>"+g.getQualifiedName())
#                        gl.log("HEY------------------------->"+g.getQualifiedName())
                        #gl.log("*********The Control Service element we are on****====>"+CS.getQualifiedName())
                        if g is CS:
                            #gl.log("Do we get here (check right now for if we hit this spot for a match between CS and instance")
                            [goodElem,elemError]=OwnerCheck(n,CS,goodElem,elemError,CSst)#check owner
                            #gl.log("I wanna see the pack Error Here" +str(packError))
                            ##need to store the elements where we found a match
                            matchInsElem[CS]=n
                            gfind=1
                            genfind=1
                            #goodElem+=1
                            goodGen+=1
                            csAttrs=CS.getOwnedAttribute()
                            newAttrs=n.getOwnedAttribute()
                            #gl.log("The Control Service thing we are on:=====>"+CS.getQualifiedName())
                            #gl.log("The New thing we are on:=====>"+n.getQualifiedName())
                            #gl.log("The length of the csattrs====>"+str(len(csAttrs)) + "      and the length of the new attributes" + str(len(newAttrs)))
                            
                            countMult={}
                            multCSAttr={}
                            
                            #as of right now this is working, needed to have a couple different use case issues in here, so does it have the correct stereotype....
                            #and is it redefining properly....if there are multiple of the same thing with that stereotype, does the multiplicity say there can be multiple
                            #if you are stereotyped but coming from somewhere else....i will delete you are put you as an error
                            for newAttr in newAttrs:
                                #gl.log("What are the attirbutes"+newAttr.getQualifiedName())
                                newAttrRedef=None
                                #gl.log("The new attribute Names, want to check to see what we are getting here"+newAttr.getQualifiedName())
                                insProp[newAttr]=newAttr
                                #gl.log("The new Attribute=====>"+newAttr.getQualifiedName())
                                redefMatch=0
                                attrTypeStMatch=0
                                #newAttrType=newAttr.getType()   #Don't need this
                                #this pulls ports so the newAttr, will have ports
                                #gl.log("The Type of the new Attribute========>"+newAttrType.getQualifiedName())
                                if isinstance(newAttr,Port):
                                    #gl.log("DO WE GET HERE")
                                    newAttrRedef=newAttr.getRedefinedPort()
                                else:
                                    #gl.log("Do we get here??")
                                    newAttrRedef=newAttr.getRedefinedProperty()
                                #gl.log("The redefined element of the new attribute======>"+newAttrRedef[0].getQualifiedName())
                                #if newAttrType!=None:
                                newAttrSt=StereotypesHelper.getStereotypes(newAttr)
                            #checking to only have cs stereotypes, dropping the sysml/uml ones
                                newAttrSt=filter(lambda element: element in CSst,newAttrSt)
                            #gl.log("New Attribute=====>"+newAttr.getQualifiedName())
                                if len(csAttrs)==0:
                                    insPropT1[newAttr]=newAttr
                                        
                                for csAttr in csAttrs:
                                    #gl.log("The control service attribute Names, want to check to see what we are getting here====>"+csAttr.getQualifiedName())
                                    #gl.log("Control Service Attribute=====>"+csAttr.getQualifiedName())
                                    multCSAttr[csAttr]=csAttr.getUpper()
                                    #csAttrType=csAttr.getType() don't need the type....
                                   # gl.log("The Type of the CS Attribute========>"+csAttrType.getQualifiedName())
                                    #if csAttrType!=None:
                                    csAttrSt=StereotypesHelper.getStereotypes(csAttr)
                                    csAttrSt=filter(lambda element: element in CSst, csAttrSt)
                                    
                                    #Now that we have both stereotypes we should check match....
                                    
                                    
                                    if len(newAttrRedef)!=0:
                                        #gl.log("What is the service implementation attribute??"+newAttr.getQualifiedName())
                                        #gl.log("What is the control Service Element that we are working on!!!!====>"+CS.getQualifiedName())
                                       
                                        #check the owning elements stereotype and if they match then we are golden if not add it to the list
                                        CsElemRedef=newAttrRedef[0].getOwner()
                                        #gl.log("What is the Control Service Element, the redefinition is coming from????=====>"+CsElemRedef.getQualifiedName())
                                        if CsElemRedef!=CS:
                                            #gl.log("This means that the redef is coming from a different element!!!! Mark this as a warning redef")
                                            insPropT1[newAttr]=newAttr
                                        if newAttrRedef[0]:
                                            if len(newAttrSt)!=0 and len(csAttrSt)!=0:
                                                if newAttrSt[0]==csAttrSt[0]:
                                                #gl.log("Seeing what the redefinition says:  ===>"+newAttrRedef[0].getQualifiedName())
                                                    if newAttrRedef[0]==csAttr:
                                                        #gl.log("Good Attribute Match, we are matching current attribute===>"+newAttr.getQualifiedName() + " with MSAF Attribute===>"+ csAttr.getQualifiedName())
                                                        goodRedef+=1
                                                        redefMatch+=1
                                                        redefCS[newAttr]=newAttr
                                                        check="yes"
                                                        break
                                            else:
                                                #gl.log("There is a redef defined, but the elements are not stereotyped")
                                                if newAttr not in insPropT3 and newAttr not in insPropT2 and newAttr not in insPropT1:
                                                    insPropT4[newAttr]=newAttr
                                                
                                    if len(newAttrRedef)==0:
                                        #check stereotypes next
                                        if len(newAttrSt)!=0:
                                            if len(csAttrSt)!=0:
                                                if newAttrSt[0]==csAttrSt[0]:
                                                    #gl.log("The Stereotypes match, but no redefinition defined")
                                                    if newAttr not in insPropT3 and newAttr not in insPropT4 and newAttr not in insPropT1:
                                                        insPropT2[newAttr]=newAttr
                                        else:
                                            #gl.log("The attribute does not have a stereotype or a redefinition")
                                            if newAttr not in insPropT4 and newAttr not in insPropT2 and newAttr not in insPropT1:
                                                insPropT3[newAttr]=newAttr
                                    
                                        
                                             
                                                
                                                        
                                                        
                                          
                                                        
                                                        
                                                        
#                                                        if csAttrRedef:
#                                                            if newAttrRedef[0]==csAttr:
#                                                                goodRedef+=1
#                                                                redefMatch+=1
#                                                                redefCS[newAttr]=newAttr
#                                                    #this would never work because it doesn't know which cs thing its associated to, not connection here, so just going to directly check if the redefintion is info/timeline/service then ok....but give warning
#                                                            redefPack=newAttrRedef[0].getOwner().getOwner()  #getting owner twice here to account for fact that the properties are owned by blocks
#                                                            redefProfile=VP.getProfilePackage(redefPack)
#                                                            if csAttrRedef==newAttrRedef:
#                                                                redefMatch+=1
#                                                                redefCS[newAttr]=newAttr
#                                                                redefWarning+=1 #Warning means it a Service, Information, or Timeline Redefinition
#                                                            if redefProfile==ServiceProf or redefProfile==InformationProf or redefProfile==TimelineParaProf or redefProfile==TimelineGenProf or redefProfile==TimelineActProf:
#                                                                if newAttr not in redefCS:
#                                                                    redefCS[newAttr]=newAttr
#                                            if newAttrTypeSt==csAttrTypeSt:
#        #                                        if csAttr in countMult:
#        #                                            countMult[csAttr]+=1
#        #                                        else:
#        #                                            countMult[csAttr]=1
#                                                #I don't think this is right.  Want:  When we start to have multiple things hooked up like events, then read the CS event attribute multiplicity and the newattr, need map on what cs attr this ties to
#                                                attrTypeStMatch+=1
#                                        else:
#                                            gl.log("The Control Service Attribute does not have a type===>"+csAttr.getQualifiedName())
                                        #
                                        #gl.log("Attribute Stereotype match====>"+str(attrTypeStMatch))
#                                    if newAttr not in redefCS and StereotypesHelper.hasStereotype(newAttr.getType(),TimelineSt):
#                                        gl.log("Are we getting here??")
#                                if attrTypeStMatch>1:
#                                    #need to check multiplicity here to ensure correctness, if multiplicity only 1 and this is greater then report error
#                                    #gl.log("There are multiple matches, could be ok if multiplicity says so")
#                                    if multCSAttr[newAttr]>1:
#                                        gl.log("There are multiple attributes that match the same CS item, with a multiplicity of====>"+str(multCSAttr[newAttr]))
#                                    else:
#                                        redefError+=1
#                                        gl.log("*****ERROR*****The Control Service Multiplicity=====>"+str(multCSAttr[newAttr])+"   does not allow for multiple relations")
            
#                            for z in countMult:
#                                count=countMult[z]
#                                if multCSAttr[z]>1:
#                                      gl.log("There are multiple attributes that match the same CS item, with a multiplicity of====>"+str(multCSAttr[z])+"   for the CS Attribute Element====>"+z.getName())
#                                else:
#                                    redefError+=1
#                                    gl.log("*****ERROR*****The Control Service Multiplicity=====>"+str(multCSAttr[z])+"   does not allow for multiple relations"+"   for the CS Attribute Element====>"+z.getName())
                                #Blasphemy this gives you a collection of stereotypes....now need to check that , that stereotype is a control service one then voila
                            
                            #need to fix this, is going to say I have way to many redef errors
                                    
                        #[elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp]=blah(n,CS, checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,matchInsElem) 
                        #break
                #gl.log("******Checking the Find Flag======>     "+str(gfind))
#                if genfind==1:
#                    break
#                        #Iwant to break out of the for loop here because we found something that matches the CS
#                    
#                          #i don't understand what this is for?
            if CS not in matchInsElem:
                gl.log("*****ERROR****The element was not found in the instance of the CS*******     ====>"+ CS.getQualifiedName())
                elemError+=1 #This is an error for the CS item, meaning that there is a control service item, that was not found in the instance
                #if self.options['FixOnly'] == True:
                if checkOnly!= True and not StereotypesHelper.hasStereotype(package,SUCmodelPack):
                    ##need to add a check in here where I don't want to create this element if its in a SUC Model, the rules are a bit different.....
                    newElement = PC.elementCreate([CS], package)
                    gl.log("*****Adding the Missing Block in New Package*******     ====>"+ package.getQualifiedName())
                    if newElement==None:
                        break
                    
                    if newElement!=None:
                        mapping[CS] = newElement
                    elemCount+=1
                else:
                    newElement=None
                    ##Adding specific patch for if this item is part of SUC model then do something special
                if StereotypesHelper.hasStereotype(package,SystemModelTypes) and newElement!=None:
                    #gl.log("******************WHAT WHAT*******************"+ newElement.getQualifiedName())
                    #create a little list here
                    sysTls[newElement]=newElement
                      
                if StereotypesHelper.hasStereotype(package,SUCmodelPack) and sucModelFind==0:# and newElement!=None:  #now only create the elements if there is a timeline connected
                    gl.log("Do we get here-----System ModelPack Found======>"+package.getQualifiedName())
                    SUCModels=package.getOwnedElement()
                   #Need to do filter here if StereotypesHelper.hasStereotype(SUCmodel,package):
                    SUCModel=filter(lambda element: StereotypesHelper.hasStereotype(element,SUCmodel),SUCModels)
                    if len(SUCModel)==1: #hmm not equal to 1 here....equal to more than one
                        sysModels[SUCModel[0]]=SUCModel[0]
                        gl.log("Do we get here-----System Model Found====>"+SUCModel[0].getName())   #if suc model found then we need to create generalization to the created suc models created
                        sucModelFind=1
                        attrs=SUCModel[0].getOwnedAttribute()
                        attrTls=filter(lambda element:element.getType()!=None,attrs)
                        attrTls=filter(lambda element:StereotypesHelper.hasStereotype(element.getType(),TimelineSt),attrTls)
                        #gl.log(str(len(attrTls)))
#                        if len(attrTls) !=0 and checkOnly!=True:
#                            for attrTl in attrTls:
#                                newElement = PC.elementCreate([CS], package)
#                                elemCount+=1
#                                if newElement !=None:
#                        #if len(attrTl)==1:   #need to fix this to handle multiple timelines
#                            #gl.log("what is in the house here=======>"+attrTl[0].getType().getName())
#                                    theTL=attrTl.getType()
#                                    Name=attrTl.getName()
#                                    #gl.log("I want to see the name we are getting here====>"+Name)
#                                    NameElNew=newElement.getName()
#                                    nameFinal=Name+" "+ NameElNew
#                                    gl.log("Setting the Final Name of the Newly created element in the System Model Package====>"+nameFinal)
#                                    newElement.setName(nameFinal)
#                                    #setting a generalization between the newElement and the Timeline that it is being created from
#                                    FT.CreateGeneralization(theTL,newElement)
#                                    #need to create relationship from suc model to new element, that redefines the CS new element thingy
#                                    [newProp, newassoc]=FT.CreateAssociation(SUCModel[0],newElement,AggregationKindEnum.COMPOSITE)
#                                    gl.log("Creating the Association between the SUC Model and the new Timelines")
#                                    props=newProp.getRedefinedProperty()
#                            #gl.log("The event ins=====>"+EventIns.getName())
#                                    gl.log("Testing the name of the things without the fix me!=====>"+NameElNew.replace(" Fix Me!",""))
#                                    #we need the correct SUC Model from the CS
#                                    genSuc=SUCModel[0].getGeneral()  #assume later that there is only one general....probs not a good assumption (I should check it is the CS thing)
#                                    genSuc=filter(lambda element:StereotypesHelper.hasStereotype(element,SUCmodel),genSuc)
#                                    #gl.log("What is the genral thing here====>"+genSuc[0].getQualifiedName())
#                                    [RedAdd,GenAdd]=FT.RedefinitionGenAdd(genSuc[0].getOwnedElement(),NameElNew.replace(" Fix Me!",""),None,props,newProp,True,newassoc,0,0)
#                        elif len(attrTls)==0:
#                            #gl.log("Are we getting here")
#                            if SUCModel[0] not in sucTLModelCheck:
#                                sucTLModelCheck[SUCModel[0]]=1 #need to create a mapping here that later we can walk through the SUC models without timelines and hook shit up
                                #gl.log("*****ERROR*****There are no timelines connected to this System MODEl!!!!!!!!======>" + SUCModel[0].getName())
#                        elif attrTl>1:
#                            gl.log("Means that there is more than one timeline hooked up to a SUC Model, which is ok")
                            #now need to take action based on a question, to user if they want to connect up timelines
                
                    #gl.log("Seeing the length of system timelines=====>"+str(len(sysTls)))  
                              #need to walk through the MOS 2.0 Timelines Module and ever time we see a package stereotyped timeline, ask the user if they are interested
            
#    for sucModel in sucTLModelCheck: #and checkOnly!=True:
#        if checkOnly!=True:
#            #gl.log("do something")                                    
#            TopLVL=MOStlProf.getNestedPackage()
#            for tops in TopLVL:
#                #ask user if they are interested in this package
#                n = JOptionPane.showConfirmDialog(None,"Do you want to view the Timeline in the Package====>"+tops.getQualifiedName(),"Timeline Package View Question", JOptionPane.YES_NO_OPTION)
#                if n!=JOptionPane.CLOSED_OPTION:
#                    if n==JOptionPane.YES_OPTION:
#                        #find the timelines in this now and put out list
#                        #now we can check packages or elements inside of this
#                        insidePacks=tops.getNestedPackage()
#                        TLpacks=filter(lambda element: StereotypesHelper.hasStereotype(element,TimelinePackSt),insidePacks) #maybe will switch this to an if
#                        Opacks=filter(lambda element: not StereotypesHelper.hasStereotype(element,TimelinePackSt),insidePacks)
#                        for Opack in Opacks:
#                            n = JOptionPane.showConfirmDialog(None,"Do you want to view the Timeline in the Package====>"+Opack.getQualifiedName(),"Timeline Package View Question", JOptionPane.YES_NO_OPTION)
#                            if n!=JOptionPane.CLOSED_OPTION:
#                                if n==JOptionPane.YES_OPTION:
#                                    packs=Opack.getNestedPackage()
#                                    tlPacks=filter(lambda element: StereotypesHelper.hasStereotype(element,TimelinePackSt),packs)
#                                    for tlPack in tlPacks:
#                                        gl.log("What the timeline package actually is??===>"+tlPack.getQualifiedName())
#                                        selects=outPutTL(tlPack,sucModel)
#                                        TimelineSucAssociation(selects,sucModel)
#                        for TLpack in TLpacks:
#                            selects=outPutTL(TLpack,sucModel)
#                            TimelineSucAssociation(selects,sucModel)
    #                                            for Elem in insideElem:
    #                                                if isinstance(Elem,Package):
    #                                                    n = JOptionPane.showConfirmDialog(None,"Do you want to view the Timelines in the Package====>"+tops.getQualifiedName(),"Timeline Package View Question", JOptionPane.YES_NO_OPTION)
    #                                                    if n!=JOptionPane.CLOSED_OPTION:
    #                                                        if n==JOptionPane.YES_OPTION:
                                                                #get the timelines now
                                                            
                                                            
                                                    
                                      
                                                
    #gl.log("Why why why")
    
            
                            
                            
#            if len(instElem)>len(matchInsElem):
#                #not true becaust inst elems contains packages
#                gl.log("*****ERROR*****The instance has more elements than the CS*****")
                
#                if genfind==0:
#                    gl.log("*****ERROR****The element was not found*******     ====>"+ CS.getQualifiedName())
#                    elemError+=1 #This is an error for the CS item, meaning that there is a control service item, that was not found in the instance
#                    #if self.options['FixOnly'] == True:
#                    if checkOnly!= True:
#                        newElement = PC.elementCreate([CS], package)
#                        gl.log("*****Adding the Missing Block in New Package*******     ====>"+ package.getQualifiedName())
#                        mapping[CS] = newElement
#                        elemCount+=1
            
                                #gl.log("ERROR:=======>The property did not correctly redefine a cs property====>  "+newAttr.getName())
    
    
                                

    
    return elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,insPropT1,insPropT2,insPropT3,insPropT4,sysModels,sysTls
    
                
            #if matcht==False:
                #gl.log("*********ERROR********The top level package is not a control service element")
                            
                                
#def recurse(elinPack,newPack): #what you want to find and where to find it
#    for o in newPack.getOwnedElement():
def TimelineSucAssociation(tlElems,sucModel):
    for tlElem in tlElems:
        [newProp, newassoc]=FT.CreateAssociation(sucModel,tlElem,AggregationKindEnum.COMPOSITE)
    

def outPutTL(package,sucModel):
    tls=package.getOwnedElement()
    tls=filter(lambda element: StereotypesHelper.hasStereotype(element,TimelineSt), tls)
    selects=MDUtils.getUserCheckboxSelections("Timeline Selection for System Under Control Model", "Select Timelines, that will be connected to the SUC Model===>  "+sucModel.getQualifiedName(), tls)
    return selects
    

def countFixes(countFix,elementToFix,package):
    Elnew=package.getOwnedElement()
    for t in Elnew:
        if isinstance(t,Package):
            if t.getName().endswith("FixMe!"):
                elementToFix.append(t)
                countFix+=1
            countFixes(countFix,elementToFix,t)
        if isinstance(t,NamedElement):
            if t.getName().endswith("Fix Me!"):
                elementToFix.append(t)
                countFix+=1
    return countFix,elementToFix




def findPackage(package,CSProf,checkOnly,CSst):
    packSt=StereotypesHelper.getStereotypes(package)
    if StereotypesHelper.hasStereotype(CSProf,packSt):
            boo=CSProf
            return boo
    CSPacks=CSProf.getNestedPackage()
    for c in CSPacks:
        #gl.log(c.getQualifiedName())
        packSt=filter(lambda element: element in CSst, packSt)
        if StereotypesHelper.hasStereotype(c,packSt):
            boo=c
            #gl.log("hehehehehhe========>"+boo.getName())
            return boo
        if isinstance(c,Package):
            CSPackage=findPackage(package,c,checkOnly,CSst)
            if CSPackage is not None:
                return CSPackage
    return None


def recursion(packaged,collectionList):
    elements=packaged.getOwnedElement()
    for j in elements:
        collectionList.append(j)
        if isinstance(j,Package):
            recursion(j,collectionList)
    return collectionList

def stereotypeReport(Stereotypes,package):
    for c in Stereotypes:
        Collection=[]
        recursion(package,Collection)
        for r in Collection:
            if StereotypesHelper.hasStereotype(r,c):
                if c in StereotypeCount:
                    StereotypeCount[c]+=1
                else:
                    StereotypeCount[c]=1
        if c not in StereotypeCount:
            StereotypeCount[c]=0          
    for c in Stereotypes:
        gl.log("Stereotype  " +c.getName() + "  has  " + str(StereotypeCount[c])+ "  usages  ")
        
def OwnerCheck(insElem,csElem,goodPack,packError,CSst):
    newOwnPack=insElem.getOwner()
    newOwnPackSt=StereotypesHelper.getStereotypes(newOwnPack)
    newOwnPackSt=filter(lambda element: element in CSst, newOwnPackSt)
    csPackOwn=csElem.getOwner()
    csPackOwnSt=StereotypesHelper.getStereotypes(csPackOwn)
    csPackOwnSt=filter(lambda element: element in CSst, csPackOwnSt)
    if newOwnPackSt==csPackOwnSt:
        #gl.log("The CS Element has Owner===>" +csPackOwnSt[0].getQualifiedName()+ "  While the Instance Element has Owner===>  "+newOwnPackSt[0].getQualifiedName())
        goodPack+=1
    else:
        gl.log("*****ERROR*****The element is not nested appropriately, the CS element has Owner===>" +csPackOwn.getQualifiedName()+ "  While the Instance Element has Owner===>  "+newOwnPack.getQualifiedName())
        packError+=1
    return goodPack,packError

#def RedefinitionCheck():
#    for csAttr in csAttrs:
#        #gl.log("Control Service Attribute=====>"+csAttr.getQualifiedName())
#        multCSAttr[csAttr]=csAttr.getUpper()
#        csAttrType=csAttr.getType()
#                                   # gl.log("The Type of the CS Attribute========>"+csAttrType.getQualifiedName())
#        csAttrTypeSt=StereotypesHelper.getStereotypes(csAttrType)
#        csAttrTypeSt=filter(lambda element: element in CSst, csAttrTypeSt)
#        csAttrRedef=csAttr.getRedefinedProperty()
#        if newAttrRedef:
#            if newAttrRedef[0]==csAttr:
#                goodRedef+=1
#                redefMatch+=1
#                                    #this would never work because it doesn't know which cs thing its associated to, not connection here, so just going to directly check if the redefintion is info/timeline/service then ok....but give warning
#                redefPack=newAttrRedef[0].getOwner().getOwner()  #getting owner twice here to account for fact that the properties are owned by blocks
#                redefProfile=VP.getProfilePackage(redefPack)
#                                        #gl.log("The Redefinition Profile======>"+redefProfile.getQualifiedName())
#            if redefProfile==ServiceProf or redefProfile==InformationProf or redefProfile==TimelineParaProf:
#                redefMatch+=1
#                redefWarning+=1 #Warning means it a Service, Information, or Timeline Redefinition
#            if newAttrTypeSt==csAttrTypeSt:
#                attrTypeStMatch+=1
# 
#                                        
##                                if attrTypeStMatch>1:
##                                    #need to check multiplicity here to ensure correctness, if multiplicity only 1 and this is greater then report error
##                                    #gl.log("There are multiple matches, could be ok if multiplicity says so")
##                                    if multCSAttr[newAttr]>1:
##                                        gl.log("There are multiple attributes that match the same CS item, with a multiplicity of====>"+str(multCSAttr[newAttr]))
##                                    else:
##                                        redefError+=1
##                                        gl.log("*****ERROR*****The Control Service Multiplicity=====>"+str(multCSAttr[newAttr])+"   does not allow for multiple relations")
#            if redefMatch==0:
#                gl.log("*****ERROR****** The redefinition is not correct====."+newAttr.getQualifiedName())
#                #gl.log("ERROR:=======>The property did not correctly redefine a cs property====>  "+newAttr.getName())
#                redefError+=1


#def findPackage(packToFind,CSPack, checkOnly):
#    for j in CSPack.getNestedPackage():  
#        newst=StereotypesHelper.getStereotypes(j)
#        newst=filter(lambda element: element in CSst, newst)
#        tost=StereotypesHelper.getStereotypes(packToFind)
#        tost=filter(lambda element: element in CSst, tost)
#        #gl.log(newst[0].getName())
#        if len(newst)!=0 and len(tost)!=0:
#            gl.log("****************ARE WE GETTING HERE==========")
#            if newst[0]==tost[0]:
#                gl.log("Are we returning")
#                return j
#    return None