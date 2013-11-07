#need to add removal of documentation field
#CREATOR: Louise Anderson


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

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
proxyMan = project.getProxyManager()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()
disposeOrphanProxy=False
#CSAF=StereotypesHelper.getProfile(project,"Mission Service Architecture Framework")

fileStereoMap=open('GoodStereo.csv','r')
filePortMap=open('GoodPort.csv','r')
filePropMap=open('GoodProp.csv','r')
#fileRead=fileStereoMap.read()
#map=json.load(fileRead)
StereoCollect={}
PortCollect={}
PropCollect={}

def ProxyFinish(proxyMan, fileStereoMap):
    for line in fileStereoMap:
        #gl.log(str(line))
        try:
            [proxyID,stereoID]=line.split(',')
            stereoID=stereoID.split()
            stereoID=stereoID[0]
            StereoCollect[proxyID]=stereoID
        except:
            pass
        
    proxies=proxyMan.getProxies() #returns all registered proxies
    count=0
#    StereoCollect=[]
    listPort={}
    listProp={}
#    StereoMap = open("StereoMap.dat","w")
    #GoodStereo=csv.writer(open("GoodStereo.csv","w"),delimiter=',',quoting=csv.QUOTE_ALL)
    proxies=StereotypesHelper.filterByProfile(proxies,profile)
    for proxy in proxies:
        #first need to check if orphan proxy
        if True==proxyMan.isGhostProxy(proxy):
            disposeOrphanProxy=False
            #(need to build in cases here of things we want to dispose)
            ids=proxy.getID()
            elementProxy=project.getElementByID(ids)
            #gl.log("what up what up the id of the proxy is===>"+str(ids))
            if isinstance(proxy,NamedElement):
               # gl.log("What did we get from the orphan proxy thing====>"+proxy.getName())
            if isinstance(proxy,Generalization):  #this works
                count+=1
                disposeOrphanProxy=True
                #gl.log("Removing orphan generalizations")
            if isinstance(proxy,Stereotype):
                proxyId=proxy.getID()
                ####the name of the proxy gotten by element
                #gl.log("Name of proxy gotten by element ID====>"+elementProxy.getQualifiedName())
                #gl.log("The name of the orphaned stereotype=====>"+proxy.getQualifiedName())
                elemSt=StereotypesHelper.getExtendedElements(proxy)
                #stereos=
                #get control service framework
                #CSAF=StereotypesHelper.getProfile(project,"Control Service Framework")
                #stereo=MDUtils.getUserSelections([Stereotype], CSAF, False,'Select Stereotype to Replace Orphan Stereotype==>'+proxy.getName(),[Stereotype,Package])
                for elem in elemSt:
                    stereo=None
                    if proxyId in StereoCollect:
                        #Need to see what StereoCollect[proxyID] is giving us
                        #gl.log("This is the stereocollect ID====>"+StereoCollect[proxyId])
                        stereo=project.getElementByID(StereoCollect[proxyId])
                    if stereo is not None:
                        #StereoCollect[proxyID]=stereo.getID()
                        gl.log("***************************")
                        gl.log("This element is using an orphaned stereotype====>"+elem.getQualifiedName()+ "  and will be replaced with selected Stereotype===>"+stereo.getQualifiedName())
                        StereotypesHelper.removeStereotype(elem,proxy)
                        StereotypesHelper.addStereotype(elem,stereo)
                    else:
                        #StereoCollect[proxyID]=None
                        gl.log("Cancel was selected for the Stereotype, so the element will be left alone======>"+elem.getQualifiedName())
                        StereotypesHelper.removeStereotype(elem,proxy)
                        #need to add flag here
                disposeOrphanProxy=True
#------------------------------------------------------------------------
            if isinstance(proxy,Port):
                portSelect=None
                proxyId=proxy.getID()
                portRedef=proxy.get_portOfRedefinedPort()
                portNew=list(portRedef)
                #portSelect=MDUtils.getUserSelections([Port], CSAF, False,'Select Port to Replace Orphan Port (used in redefintion)==>'+proxy.getQualifiedName(),[Port,Package,Class,Interface])
                if proxyId in PortCollect:
                    portSelect=project.getElementByID(PortCollect[proxyId])
                for port in portNew:
                    #gl.log("I just want to see what ports we are getting here====>"+port.getQualifiedName())
                    #this gets all ports that are using the orphan port as a redefinition
                    redefList=port.getRedefinedPort()
                    redefList.remove(proxy)   
                    if portSelect is not None:
                        #StereoCollect[proxyID]=stereo.getID()
                        gl.log("This port is using an orphaned port for redefinition====>"+port.getQualifiedName()+ "  and will be replaced with selected port===>"+portSelect.getQualifiedName())
                        redefList.add(portSelect)
                        #filePort.write(str(ProxyId)+","+str(portSelect.getID())+"\n")
                    else:
                        #StereoCollect[proxyID]=None
                        gl.log("Cancel was selected for the Port Selection, so the port will be deleted======>"+port.getQualifiedName())
                        #StereotypesHelper.removeStereotype(elem,proxy)
                        #filePort.write(str(ProxyId)+","+"0"+"\n")
                        #need to add flag here
                        if port.isEditable():
                            listPort[port]=port.getQualifiedName()
                            mem.removeElement(port)
                        else:
                            gl.log("Error the element you are trying to delete is not editable")
                disposeOrphanProxy=True 
                
#------------------------------------------------------------------------  
            if isinstance(proxy,Property) and not isinstance(proxy,Port):
                propSelect=None
                proxyId=proxy.getID()
                propertyRedef=proxy.get_propertyOfRedefinedProperty()
                propNew=list(propertyRedef)
                #propSelect=MDUtils.getUserSelections([Property], CSAF, False,'Select Property to Replace Orphan Property (used in redefinition)==>'+proxy.getQualifiedName(),[Property,Package,Class,Interface])
                if proxyId in PropCollect:
                    propSelect=project.getElementbyID(PropCollect[proxyId])
                    
                for prop in propNew:
                    #gl.log("I just want to see what ports we are getting here====>"+port.getQualifiedName())
                    #this gets all ports that are using the orphan port as a redefinition
                    redefList=prop.getRedefinedProperty()
                    redefList.remove(proxy)   
                    if propSelect is not None:
                        #StereoCollect[proxyID]=stereo.getID()
                        gl.log("This property is using an orphaned property for redefinition====>"+prop.getQualifiedName()+ "  and will be replaced with selected property===>"+propSelect.getQualifiedName())
                        redefList.add(propSelect)
                        #fileProperty.write(str(ProxyId)+","+str(propSelect.getID())+"\n")
                    else:
                        #StereoCollect[proxyID]=None
                        gl.log("Cancel was selected for the Port Selection, so the port will be deleted======>"+prop.getQualifiedName())
                        #StereotypesHelper.removeStereotype(elem,proxy)
                        #fileProperty.write(str(ProxyId)+","+"0"+"\n")
                        #need to add flag here
                        if prop.isEditable():
                            listProp[prop]=prop.getQualifiedName()
                            mem.removeElement(prop)
                        else:
                            gl.log("Error the element you are trying to delete is not editable")
                disposeOrphanProxy=True 
                
            if isinstance(proxy,Package):
                p=proxy
                disposeOrphanProxy=True
                p.dispose()

            if disposeOrphanProxy==True:
                    #decide whether or not we want to dispose of orphan proxy
                    gl.log("***********************If we come here we will dispose of an orphan")
                    proxy.dispose()
                    #mem.removeElement(proxy)================>This does not work
            else: #if we are not just getting rid of orphan need to have the fix cases
                gl.log("if we come here we will make something not a proxy anymore")
                #proxyMan.makeNotProxy(proxy) ------this does very scary things
                proxy.dispose()
            
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
                    
    #gl.log("Final Count of generalization====>"+str(count))
    gl.log("************************LIST OF PROPERTIES DELETED**********************")
    for p in listProp:
        gl.log("Property Deleted===>  "+p.getQualifiedName())
    gl.log("************************LIST OF PORTS DELETED**********************")
    for q in listPort:
        gl.log("Port Deleted===>  "+q.getQualifiedName())
    
    return None






if len(scriptInput['Profile'])>0:
    profile = scriptInput['Profile'][0]
    gl.log("===========Orphan Fixing==============")
    ProxyFinish(proxyMan,fileStereoMap)
    ProxyFinish(proxyMan,filePortMap)
    ProxyFinish(proxyMan,filePropMap)
    fileStereoMap.close()
    filePropMap.close()
    filePortMap.close()
else:
    gl.log("**ERROR** No Profile Provided")




scriptOutput = ["--------------------THE ORPHAN FIXING IS COMPLETE----------------------"]
