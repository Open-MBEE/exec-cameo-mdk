#Creator:  Louise Anderson

from java.lang import * 
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import Interface
from com.nomagic.uml2.ext.magicdraw.mdprofiles import *

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
import MDUtils._MDUtils as MDUtils
reload(MDUtils)


import sys
import traceback
import os


gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
proxyMan = project.getProxyManager()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()
disposeOrphanProxy=False
profileSysML=StereotypesHelper.getProfile(project,"SysML")#profile of selection and base sysml
gl.log(profileSysML.getName())
#CSAF=StereotypesHelper.getProfile(project,"Mission Service Architecture Framework")
listPort={}
listProp={}
#test
wModel=project.getModel()
    
def ProxyResolver(profile):
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
    proxies=StereotypesHelper.filterByProfile(proxies,profile)
    for proxy in proxies:
        #first need to check if orphan proxy
        #only want to work on proxies in the msaf m  lkn
        
        if True==proxyMan.isGhostProxy(proxy):
            disposeOrphanProxy=False
            #(need to build in cases here of things we want to dispose)
            ProxyId=proxy.getLocalID()
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
                stereo=MDUtils.getUserSelections([Stereotype], wModel, False,'Select Stereotype to Replace Orphan Stereotype==>'+proxy.getName(),[Stereotype,Package,Profile])
                for elem in elemSt:
                    if stereo is not None:
                        #StereoCollect[proxyID]=stereo.getLocalID()
                        gl.log("This element is using an orphaned stereotype====>"+elem.getQualifiedName()+ "  and will be replaced with selected Stereotype===>"+stereo.getQualifiedName())
                        StereotypesHelper.removeStereotype(elem,proxy)
                        StereotypesHelper.addStereotype(elem,stereo)
                        fileStereo.write(str(ProxyId)+","+str(stereo.getLocalID())+"\n")
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
                portSelect=MDUtils.getUserSelections([Port], wModel, False,'Select Port to Replace Orphan Port (used in redefintion)==>'+proxy.getQualifiedName(),[Port,Package,Class,Interface,Profile])
                for port in portNew:
                    #gl.log("I just want to see what ports we are getting here====>"+port.getQualifiedName())
                    #this gets all ports that are using the orphan port as a redefinition
                    redefList=port.getRedefinedPort()
                    redefList.remove(proxy)   
                    if portSelect is not None:
                        #StereoCollect[proxyID]=stereo.getLocalID()
                        gl.log("This port is using an orphaned port for redefinition====>"+port.getQualifiedName()+ "  and will be replaced with selected port===>"+portSelect.getQualifiedName())
                        redefList.add(portSelect)
                        filePort.write(str(ProxyId)+","+str(portSelect.getLocalID())+"\n")
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
                propSelect=MDUtils.getUserSelections([Property], wModel, False,'Select Property to Replace Orphan Property (used in redefinition)==>'+proxy.getQualifiedName(),[Property,Package,Class,Interface,Profile])
                for prop in propNew:
                    #gl.log("I just want to see what ports we are getting here====>"+port.getQualifiedName())
                    #this gets all ports that are using the orphan port as a redefinition
                    redefList=prop.getRedefinedProperty()
                    redefList.remove(proxy)   
                    if propSelect is not None:
                        #StereoCollect[proxyID]=stereo.getLocalID()
                        gl.log("This property is using an orphaned property for redefinition====>"+prop.getQualifiedName()+ "  and will be replaced with selected property===>"+propSelect.getQualifiedName())
                        redefList.add(propSelect)
                        fileProperty.write(str(ProxyId)+","+str(propSelect.getLocalID())+"\n")
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
            if isinstance(proxy,Package):
                p=proxy
                packSelect=MDUtils.getUserSelections([Package], wModel, False,'Select Package to Replace Orphan Package (used in redefinition)==>'+proxy.getQualifiedName(),[Package,Package,Class,Interface,Profile])
                if packSelect is not None:
                    #StereoCollect[proxyID]=stereo.getLocalID()
                    gl.log("This package is orphaned"+proxy.getName()+ "  and will be replaced with selected package===>"+packSelect.getQualifiedName())
                    redefList.add(packSelect)
                    fileProperty.write(str(ProxyId)+","+str(packSelect.getLocalID())+"\n")
                    proxy.dispose()
                else:
                    #StereoCollect[proxyID]=None
                    gl.log("Cancel was selected for the Package Selection, so the package will be deleted======>"+proxy.getName())
                        #StereotypesHelper.removeStereotype(elem,proxy)
                    fileProperty.write(str(ProxyId)+","+"0"+"\n")
                    #need to add flag here
                    #mem.removeElement(p)
                
                #need to add how to handle ports properties associations...whatever we can get
                    
                #get all elements that are stereotyped this
            #check to make sure you have lock on full project
            #first created, creates you a mapping
            #remember what to replace with and what to delete and re-run with lists of items
           # if disposeOrphanProxy==True:
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


if len(scriptInput['Profile'])>0:
    profile = scriptInput['Profile'][0]
    gl.log("===========Orphan Fixing==============")
    ProxyResolver(profile)
else:
    gl.log("**ERROR** No Target Provided!")



scriptOutput = ["--------------------THE ORPHAN FIXING IS COMPLETE----------------------"]
