'''
Created on Oct 13, 2011

@author: lcanders && efosse

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
from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
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
import ValidateTimeline as VT
reload (VT)
import MDUtils._MDUtils as MDUtils
reload(MDUtils)
from SystemsReasoner import SRUtils
reload(SRUtils)
from FrameworkValidation import ProfileValidation as VP
reload(VP)

import FixTimeline as FT
reload (FT)

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
datapack=project.getModel()
mapping={}
#package = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
count={}
stereotypePack=StereotypesHelper.getStereotypes(package) #this needs to not be package anymore but what is passed in, which is target
profile = StereotypesHelper.getProfile("Control Service Architecture Framework")
sysmlProf=StereotypesHelper.getProfile(project,"SysML")
ServiceProf=StereotypesHelper.getProfile(project,"Service")
TimelineActProf=StereotypesHelper.getProfile(project,"Activity Timeline")
TimelineParaProf=StereotypesHelper.getProfile(project,"Parametric Timeline")
TimelineGenProf=StereotypesHelper.getProfile(project,"General Timeline")
InformationProf=StereotypesHelper.getProfile(project,"Information Profile")
sysmlSt=StereotypesHelper.getStereotypesByProfile(sysmlProf)
svPackStereotype=StereotypesHelper.getStereotype(project,"System Under Control State Variables Package","Control Service")
pst=StereotypesHelper.getStereotypesByProfile(profile)
csElem=profile.getOwnedElement()
nElem=package.getOwnedElement()
pattern=re.compile("(?i).*Fix Me!")
CSst=StereotypesHelper.getStereotypesByProfile(profile)

##need to add back in the count for packages for everything


def blah(package,csPack,checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,matchInsElem):
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
            k=findPackage(CS,package,checkOnly)
            if k==None:
                gl.log("***********ERROR******The CS Profile has a package that is not found in the Validation Package======>"+CS.getQualifiedName())
                packError+=1
                csPackOwn=CS.getOwner()
                #newOwn=package.getOwner()
                newPackOwn=findPackage(csPackOwn,package,checkOnly)
                
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
                    [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp]=blah(newPack,CS,checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,matchInsElem)
                
            else:
                #instElem[k]=1
                gl.log(k.getQualifiedName())
                gl.log(CS.getQualifiedName())
                #check owner of package stuff here
                [goodPack,packError]=OwnerCheck(k,CS,goodPack,packError)
                #move the generalization check and class instance check stuff into here
                [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp]=blah(k,CS, checkOnly,diagCount,elemCount,genCount,diagError,elemError,packError,genError,redefError,redefWarning,attrTypeStMatch,redefMatch,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp,matchInsElem)
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
            if len(CS2)==len(Elnew2):
                gl.log("Everything is just like the control service")
                
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
                        #gl.log("*********The Instance element we are on****====>"+n.getQualifiedName())
#                        gl.log("HEY------------------------->"+g.getQualifiedName())
                        #gl.log("*********The Control Service element we are on****====>"+CS.getQualifiedName())
                        if g is CS:
                            #gl.log("Do we get here (check right now for if we hit this spot for a match between CS and instance")
                            [goodElem,elemError]=OwnerCheck(n,CS,goodElem,elemError)#check owner
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
                            
                            countMult={}
                            multCSAttr={}
                            
                            for newAttr in newAttrs:
                                #gl.log("The new attribute Names, want to check to see what we are getting here"+newAttr.getQualifiedName())
                                insProp[newAttr]=newAttr
                                #gl.log("The new Attribute=====>"+newAttr.getQualifiedName())
                                redefMatch=0
                                attrTypeStMatch=0
                                newAttrType=newAttr.getType()
                                #this pulls ports so the newAttr, will have ports
                                #gl.log("The Type of the new Attribute========>"+newAttrType.getQualifiedName())
                                if isinstance(newAttr,Port):
                                    #gl.log("DO WE GET HERE")
                                    newAttrRedef=newAttr.getRedefinedPort()
                                else:
                                    newAttrRedef=newAttr.getRedefinedProperty()
                                #gl.log("The redefined element of the new attribute======>"+newAttrRedef[0].getQualifiedName())
                                newAttrTypeSt=StereotypesHelper.getStereotypes(newAttrType)
                                #I'm doing this wrong here, its not if the stereotype is in the CSst, of course it is, its if the element is in a certain profile
                                newAttrTypeSt=filter(lambda element: element in CSst,newAttrTypeSt)
                                #gl.log("New Attribute=====>"+newAttr.getQualifiedName())
                                for csAttr in csAttrs:
                                    #gl.log("The control service attribute Names, want to check to see what we are getting here====>"+csAttr.getQualifiedName())
                                    #gl.log("Control Service Attribute=====>"+csAttr.getQualifiedName())
                                    multCSAttr[csAttr]=csAttr.getUpper()
                                    csAttrType=csAttr.getType()
                                   # gl.log("The Type of the CS Attribute========>"+csAttrType.getQualifiedName())
                                    csAttrTypeSt=StereotypesHelper.getStereotypes(csAttrType)
                                    csAttrTypeSt=filter(lambda element: element in CSst, csAttrTypeSt)
                                    if isinstance(csAttr,Port):
                                        csAttrRedef=csAttr.getRedefinedPort()
                                    else:
                                        csAttrRedef=csAttr.getRedefinedProperty()
                                    if newAttrRedef:
                                        if csAttrRedef:
                                            if newAttrRedef[0]==csAttr:
                                                goodRedef+=1
                                                redefMatch+=1
                                                redefCS[newAttr]=newAttr
                                    #this would never work because it doesn't know which cs thing its associated to, not connection here, so just going to directly check if the redefintion is info/timeline/service then ok....but give warning
                                            redefPack=newAttrRedef[0].getOwner().getOwner()  #getting owner twice here to account for fact that the properties are owned by blocks
                                            redefProfile=VP.getProfilePackage(redefPack)
                                            if csAttrRedef==newAttrRedef:
                                                redefMatch+=1
                                                redefCS[newAttr]=newAttr
                                                redefWarning+=1 #Warning means it a Service, Information, or Timeline Redefinition
                                            if redefProfile==ServiceProf or redefProfile==InformationProf or redefProfile==TimelineParaProf or redefProfile==TimelineGenProf or redefProfile==TimelineActProf:
                                                if newAttr not in redefCS:
                                                    redefCS[newAttr]=newAttr
                                    if newAttrTypeSt==csAttrTypeSt:
#                                        if csAttr in countMult:
#                                            countMult[csAttr]+=1
#                                        else:
#                                            countMult[csAttr]=1
                                        #I don't think this is right.  Want:  When we start to have multiple things hooked up like events, then read the CS event attribute multiplicity and the newattr, need map on what cs attr this ties to
                                        attrTypeStMatch+=1
                            
                                        #
                                        #gl.log("Attribute Stereotype match====>"+str(attrTypeStMatch))
                                        
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
                        break
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
                if checkOnly!= True:
                    newElement = PC.elementCreate([CS], package)
                    gl.log("*****Adding the Missing Block in New Package*******     ====>"+ package.getQualifiedName())
                    mapping[CS] = newElement
                    elemCount+=1
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
    
    
                                

    
    return elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp
    
                
            #if matcht==False:
                #gl.log("*********ERROR********The top level package is not a control service element")
                            
                                
#def recurse(elinPack,newPack): #what you want to find and where to find it
#    for o in newPack.getOwnedElement():
 
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




def findPackage(package,CSProf,checkOnly):
    packSt=StereotypesHelper.getStereotypes(package)
    if StereotypesHelper.hasStereotype(CSProf,packSt):
            boo=CSProf
            return boo
    CSPacks=CSProf.getNestedPackage()
    for c in CSPacks:
        packSt=filter(lambda element: element in CSst, packSt)
        if StereotypesHelper.hasStereotype(c,packSt):
            boo=c
            #gl.log("hehehehehhe========>"+boo.getName())
            return boo
        if isinstance(c,Package):
            CSPackage=findPackage(package,c,checkOnly)
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
        
def OwnerCheck(insElem,csElem,goodPack,packError):
    newOwnPack=insElem.getOwner()
    newOwnPackSt=StereotypesHelper.getStereotypes(newOwnPack)
    newOwnPackSt=filter(lambda element: element in CSst, newOwnPackSt)
    csPackOwn=csElem.getOwner()
    csPackOwnSt=StereotypesHelper.getStereotypes(csPackOwn)
    csPackOwnSt=filter(lambda element: element in CSst, csPackOwnSt)
    if newOwnPackSt==csPackOwnSt:
        gl.log("The CS Element has Owner===>" +csPackOwnSt[0].getQualifiedName()+ "  While the Instance Element has Owner===>  "+newOwnPackSt[0].getQualifiedName())
        goodPack+=1
    else:
        gl.log("*****ERROR*****The element is not nested appropriately, the CS element has Owner===>" +csPackOwn.getQualifiedName()+ "  While the Instance Element has Owner===>  "+newOwnPack.getQualifiedName())
        packError+=1
    return goodPack,packError

def RedefinitionCheck():
    for csAttr in csAttrs:
        #gl.log("Control Service Attribute=====>"+csAttr.getQualifiedName())
        multCSAttr[csAttr]=csAttr.getUpper()
        csAttrType=csAttr.getType()
                                   # gl.log("The Type of the CS Attribute========>"+csAttrType.getQualifiedName())
        csAttrTypeSt=StereotypesHelper.getStereotypes(csAttrType)
        csAttrTypeSt=filter(lambda element: element in CSst, csAttrTypeSt)
        csAttrRedef=csAttr.getRedefinedProperty()
        if newAttrRedef:
            if newAttrRedef[0]==csAttr:
                goodRedef+=1
                redefMatch+=1
                                    #this would never work because it doesn't know which cs thing its associated to, not connection here, so just going to directly check if the redefintion is info/timeline/service then ok....but give warning
                redefPack=newAttrRedef[0].getOwner().getOwner()  #getting owner twice here to account for fact that the properties are owned by blocks
                redefProfile=VP.getProfilePackage(redefPack)
                                        #gl.log("The Redefinition Profile======>"+redefProfile.getQualifiedName())
            if redefProfile==ServiceProf or redefProfile==InformationProf or redefProfile==TimelineParaProf:
                redefMatch+=1
                redefWarning+=1 #Warning means it a Service, Information, or Timeline Redefinition
            if newAttrTypeSt==csAttrTypeSt:
                attrTypeStMatch+=1
 
                                        
#                                if attrTypeStMatch>1:
#                                    #need to check multiplicity here to ensure correctness, if multiplicity only 1 and this is greater then report error
#                                    #gl.log("There are multiple matches, could be ok if multiplicity says so")
#                                    if multCSAttr[newAttr]>1:
#                                        gl.log("There are multiple attributes that match the same CS item, with a multiplicity of====>"+str(multCSAttr[newAttr]))
#                                    else:
#                                        redefError+=1
#                                        gl.log("*****ERROR*****The Control Service Multiplicity=====>"+str(multCSAttr[newAttr])+"   does not allow for multiple relations")
            if redefMatch==0:
                gl.log("*****ERROR****** The redefinition is not correct====."+newAttr.getQualifiedName())
                #gl.log("ERROR:=======>The property did not correctly redefine a cs property====>  "+newAttr.getName())
                redefError+=1


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



        
 
#def run(mode):
#    if mode == 'b':
#        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
#    try:
#        SessionManager.getInstance().createSession("syncProfile")
#        csPack=findPackage(package,profile, False)
#        if csPack==None:
#            gl.log("******ERROR*****The Validation Package is not found in the CS=======>"+package.getName())
#        else:
#            [elemCount,genCount,diagCount,diagError,elemError,packError,genError,redefError,redefWarning,goodElem,goodRedef,goodGen,goodDiag,goodPack,instElem,csElems,redefCS,insProp]=blah(package,csPack, False,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,{},0,{},{},{})
#            options={"checkOnly":False, "mapping":mapping, 'fixforOpsrev':True}
#            for m in mapping:
#                newElement = mapping[m]
#                Specialize.generalize(newElement, options)
#        gl.log("--------------------------Summary Elements Report--------------------------")
#        gl.log("Instance Elements Found===============>"+str(len(instElem)))
#        gl.log("CS Elements Found===============>"+str(csElems))
#        gl.log("Elements Found Matching===============>"+str(goodElem))
#        gl.log("Packages Found===========>"+str(goodPack))
#        gl.log("Generalizations===============>"+str(goodGen))
#        gl.log("Diagrams Found==============>"+str(goodDiag))
#        gl.log("Redefinitions Found==============>"+str(len(redefCS)))
#        gl.log("Properties Found========>"+ str(len(insProp)))
#        gl.log("--------------------------Summary Error Report--------------------------")
#        gl.log("Elements Not Found===============>"+str(elemError))
#        gl.log("Packages Not Found===========>"+str(packError))
#        gl.log("Elements without Generalization===============>"+str(genError))
#        gl.log("Diagram Not Found===============>"+str(diagError))
#        gl.log("Redefinition Error =======>"+str(len(insProp)-len(redefCS)))
#        gl.log("Redefinition Warning(Information/Service/Timeline Redefinitions Directly)=====>"+str(redefWarning))
#        MoreIns=len(instElem)-goodElem
#        if MoreIns!=0:
#            gl.log("More Elements Found in the Instance than the CS =======>"+str(MoreIns))
#        gl.log("--------------------------Summary Creation Report--------------------------")
#        gl.log("Elements Created===============>"+str(elemCount))
#        gl.log("Generalizations Created===========>"+str(genCount))
#        gl.log("Diagrams Created===============>"+str(diagCount))
#        gl.log("--------------------------Summary Fix Me! Elements--------------------------")
#        [count,Fixes]=countFixes(countFix,elementToFix,package)
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
#                stereotypeReport(CSst,package)
#        gl.log("--------------------------Summary Redefinition Not Correct--------------------------")
#        q = JOptionPane.showConfirmDialog(None,"Do you want to view the List of Elements with improper Redefinitions??","Redefinition Question", JOptionPane.YES_NO_OPTION)
#        if q!=JOptionPane.CLOSED_OPTION:
#            if q==JOptionPane.YES_OPTION:
#                for j in insProp:
#                    if j not in redefCS:
#                        gl.log("This element was not redefined properly====>"+j.getQualifiedName())
#        
#        SessionManager.getInstance().closeSession()
#    except:
#        if SessionManager.getInstance().isSessionCreated():
#            SessionManager.getInstance().cancelSession()
#        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
#        gl.log("*** EXCEPTION:")
#        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
#        for message in messages:
#            gl.log(message)