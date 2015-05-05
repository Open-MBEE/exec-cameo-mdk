#Creator:  Louise Anderson

from java.lang import * 
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import Interface
from com.nomagic.uml2.ext.magicdraw.mdprofiles import Stereotype

#from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Property
#from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Package
#from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import AggregationKindEnum
#from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Enumeration
#from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Constraint
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port
from com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses import AssociationClass
from com.nomagic.magicdraw.copypaste import CopyPasteManager
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import Activity

from javax.swing import JOptionPane


from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
import sys
import traceback
import os
#import json

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)
import Validate_Structure
reload(Validate_Structure)
import Specialize
reload(Specialize)
import csv

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
proxyMan = project.getProxyManager()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()
disposeOrphanProxy=False
CSAF=StereotypesHelper.getProfile(project,"Mission Service Architecture Framework")
listPort={}
listProp={}

def ProxyResolver(proxyMan):
    
    proxies=proxyMan.getProxies() #returns all registered proxies
    count=0
    StereoCollect=[]
    listPort={}
    listProp={}
    #StereoMap = open("StereoMap.dat","w")
    fileStereo=open("GoodStereo.csv","w")
    filePort=open("GoodPort.csv","w")
    fileProperty=open("GoodProp.csv","w")
    #GoodStereo=(fileStereo,delimiter=',',quoting=csv.QUOTE_ALL)
    ##trying to filter proxies for only msaf ones
    proxies=StereotypesHelper.filterByProfile(proxies,CSAF)
    for proxy in proxies:
        #first need to check if orphan proxy
        #only want to work on proxies in the msaf
        
        if True==proxyMan.isGhostProxy(proxy):
            disposeOrphanProxy=False
            #(need to build in cases here of things we want to dispose)
            ProxyId=proxy.getID()
            #gl.log("what up what up the id of the proxy is===>"+str(ProxyId))
            if isinstance(proxy,Generalization):  #this works
                count+=1
                disposeOrphanProxy=True
                gl.log("Removing orphan generalizations")
            if isinstance(proxy,Stereotype):
                gl.log("The name of the orphaned stereotype=====>"+proxy.getQualifiedName())
                elemSt=StereotypesHelper.getExtendedElements(proxy)
                #stereos=
                #get control service framework
                for elem in elemSt:
                    gl.log("This element==>  "+elem.getQualifiedName()+"   is using the orphan proxy===>  "+proxy.getQualifiedName())
                stereo=MDUtils.getUserSelections([Stereotype], CSAF, False,'Select Stereotype to Replace Orphan Stereotype==>'+proxy.getName(),[Stereotype,Package])
                for elem in elemSt:
                    if stereo is not None:
                        #StereoCollect[proxyID]=stereo.getID()
                        gl.log("This element is using an orphaned stereotype====>"+elem.getQualifiedName()+ "  and will be replaced with selected Stereotype===>"+stereo.getQualifiedName())
                        StereotypesHelper.removeStereotype(elem,proxy)
                        StereotypesHelper.addStereotype(elem,stereo)
                        fileStereo.write(str(ProxyId)+","+str(stereo.getID())+"\n")
                    else:
                        #StereoCollect[proxyID]=None
                        gl.log("Cancel was selected for the Stereotype, so the element will be left alone======>"+elem.getQualifiedName())
                        StereotypesHelper.removeStereotype(elem,proxy)
                        fileStereo.write(str(ProxyId)+","+"0"+"\n")
                        #need to add flag here
                disposeOrphanProxy=True
            if isinstance(proxy,Port):
                portRedef=proxy.get_portOfRedefinedPort()
                portNew=list(portRedef)
                for port in portNew:
                    
                    gl.log("This port==>  "+port.getQualifiedName()+"   is using the orphan proxy===>  "+proxy.getQualifiedName())
                portSelect=MDUtils.getUserSelections([Port], CSAF, False,'Select Port to Replace Orphan Port (used in redefintion)==>'+proxy.getQualifiedName(),[Port,Package,Class,Interface])
                for port in portNew:
                    #gl.log("I just want to see what ports we are getting here====>"+port.getQualifiedName())
                    #this gets all ports that are using the orphan port as a redefinition
                    redefList=port.getRedefinedPort()
                    redefList.remove(proxy)   
                    if portSelect is not None:
                        #StereoCollect[proxyID]=stereo.getID()
                        gl.log("This port is using an orphaned port for redefinition====>"+port.getQualifiedName()+ "  and will be replaced with selected port===>"+portSelect.getQualifiedName())
                        redefList.add(portSelect)
                        filePort.write(str(ProxyId)+","+str(portSelect.getID())+"\n")
                    else:
                        #StereoCollect[proxyID]=None
                        gl.log("Cancel was selected for the Port Selection, so the port will be deleted======>"+port.getQualifiedName())
                        #StereotypesHelper.removeStereotype(elem,proxy)
                        filePort.write(str(ProxyId)+","+"0"+"\n")
                        #need to add flag here
                        if port.isEditable():
                            listPort[port]=port.getQualifiedName()
                            mem.removeElement(port)
                        else:
                            gl.log("Error the element you are trying to delete is not editable")
                disposeOrphanProxy=True   
            if isinstance(proxy,Property) and not isinstance(proxy,Port):
                propertyRedef=proxy.get_propertyOfRedefinedProperty()
                propNew=list(propertyRedef)
                for prop in propNew:
                    gl.log("This property==>  "+prop.getQualifiedName()+"   is using the orphan proxy===>  "+proxy.getQualifiedName())
                propSelect=MDUtils.getUserSelections([Property], CSAF, False,'Select Property to Replace Orphan Property (used in redefinition)==>'+proxy.getQualifiedName(),[Property,Package,Class,Interface])
                for prop in propNew:
                    #gl.log("I just want to see what ports we are getting here====>"+port.getQualifiedName())
                    #this gets all ports that are using the orphan port as a redefinition
                    redefList=prop.getRedefinedProperty()
                    redefList.remove(proxy)   
                    if propSelect is not None:
                        #StereoCollect[proxyID]=stereo.getID()
                        gl.log("This property is using an orphaned property for redefinition====>"+prop.getQualifiedName()+ "  and will be replaced with selected property===>"+propSelect.getQualifiedName())
                        redefList.add(propSelect)
                        fileProperty.write(str(ProxyId)+","+str(propSelect.getID())+"\n")
                    else:
                        #StereoCollect[proxyID]=None
                        gl.log("Cancel was selected for the Port Selection, so the port will be deleted======>"+prop.getQualifiedName())
                        #StereotypesHelper.removeStereotype(elem,proxy)
                        fileProperty.write(str(ProxyId)+","+"0"+"\n")
                        #need to add flag here
                        if prop.isEditable():
                            listProp[prop]=prop.getQualifiedName()
                            mem.removeElement(prop)
                        else:
                            gl.log("Error the element you are trying to delete is not editable")
                disposeOrphanProxy=True 
                #need to add how to handle ports properties associations...whatever we can get
                    
                #get all elements that are stereotyped this
            #check to make sure you have lock on full project
            #first created, creates you a mapping
            #remember what to replace with and what to delete and re-run with lists of items
            if disposeOrphanProxy==True:
                    #decide whether or not we want to dispose of orphan proxy
                    #gl.log("***********************If we come here we will dispose of an orphan")
                    proxy.dispose()
                    #mem.removeElement(proxy)================>This does not work
#            else: #if we are not just getting rid of orphan need to have the fix cases
#                gl.log("if we come here we will make something not a proxy anymore")
#                #proxyMan.makeNotProxy(proxy) ------this does very scary things
    gl.log("************************LIST OF PROPERTIES DELETED**********************")
    for p in listProp:
        gl.log("Property Deleted===>  "+listProp[p])
    gl.log("************************LIST OF PORTS DELETED**********************")
    for q in listPort:
        gl.log("Port Deleted===>  "+listPort[q])
        
        
    
    
    #now do the jason dump and the file write
    #strStereo=json.dumps([StereoCollect])
    #StereoMap.write(strStereo)
    fileStereo.close()
    filePort.close()
    fileProperty.close()
    
#    redefs = p.getRedefinedProperty()
#    if len(redefs) > 0:
#        redef = redefs[0]
#        type = redef.getType()
#        if type is not None:
#            for g in type.get_generalizationOfGeneral():
#                potential = g.getSpecific()
#                if potential.getName().endswith("Fix Me!"):
#                    return potential
    return None
def blah(selects):
    ids=selects.getID()
    gl.log("what up what up===>"+(ids))
    return


def run(mode):
    selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    try:
        SessionManager.getInstance().createSession("orphan")
        #blah(selected)
        ProxyResolver(proxyMan)
        
        
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
