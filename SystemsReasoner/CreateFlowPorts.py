
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
from java.lang import Object
from java.lang import String

'updated 2012Mar29'
import sys
import traceback
import os
gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProjectsManager().getActiveProject()
ef = project.getElementsFactory()

'target is flow port with larger conveyed information; for each conveyed information on flow port; check if flow port already exists of that type; if not create if so attach; do same on others'
'check flow direction'
def createFlowPorts(connectorTargets):
	validateOnly=False
	nestSter = StereotypesHelper.getStereotype(project, 'NestedConnectorEnd')
	flowSter = StereotypesHelper.getStereotype(project, 'FlowPort')
	conveyedApplied=0
	conveyedBadApply=0
	noConveyed=0
	noConnector=0
	connectorCreated=0
	typeMis=0
	badConveyed=0
	context = connectorTargets.getFeaturingClassifier()
	contextPackage=context[0].getOwningPackage()
	connectorEnds=connectorTargets.getEnd()
	connectorInformation=connectorTargets.get_informationFlowOfRealizingConnector()
	cInfoNames=[]
	cInfoTypes=[]
	cInfoSource={}
	cInfoTarg={}
	for c in connectorInformation:
		if len(c.getConveyed())==0:
			gl.log('ERROR: No Conveyed Information.')
		else:
			cInfoTypes.append(c.getConveyed())
			for cg in c.getConveyed():
				if cg in cInfoSource:
					cInfoSource[cg].append(ModelHelper.getSupplierElement(c))
				else:
					cInfoSource[cg]=[ModelHelper.getSupplierElement(c)]
				if cg in cInfoTarg:
					cInfoTarg[cg].append(ModelHelper.getClientElement(c))
				else:
					cInfoTarg[cg]=[ModelHelper.getClientElement(c)]
		for con in c.getConveyed():
			cInfoNames.append(con.getName())
	cInfoTypes=flatten(cInfoTypes)
	r=connectorEnds[0].getRole()
	rPaths = StereotypesHelper.getStereotypePropertyValue(connectorEnds[0], nestSter,'propertyPath')
	s=connectorEnds[1].getRole()
	sPaths = StereotypesHelper.getStereotypePropertyValue(connectorEnds[1], nestSter, 'propertyPath')
	gl.log('Validating from ' + r.getName())
	rPorts = filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, 'FlowPort'), r.getType().getOwnedElement())
	rPortTypes=[]
	for rpt in rPorts:
		rPortTypes.append(rpt.getType())
	pcNoMatch = filter(lambda element: element not in rPortTypes, cInfoTypes)
	for p in pcNoMatch:
		gl.log('ERROR: Conveyed information has no matching port: ' + p.getName())
	for rp in rPorts:
		if not rp.getEnd() or rp.getEnd()[0].getPartWithPort() !=r:
			gl.log('WARNING: No connector for port = ' + rp.getName())
			noConnector+=1
			go=0
			if rp.getType() in cInfoTypes and validateOnly!=True:
				goAhead = checkDirection(rp, cInfoTarg, cInfoSource, r)
#				gl.log('DEBUG: ' +str(goAhead))
				if goAhead == 1:	
					new = ef.createConnectorInstance()
					new.setOwner(context[0])
					StereotypesHelper.addStereotypeByString(new.getEnd()[0], 'NestedConnectorEnd')
					StereotypesHelper.addStereotypeByString(new.getEnd()[1], 'NestedConnectorEnd')
					for rpa in rPaths:
						StereotypesHelper.setStereotypePropertyValue(new.getEnd()[0], nestSter, 'propertyPath', rpa, True)
					StereotypesHelper.setStereotypePropertyValue(new.getEnd()[0], nestSter, 'propertyPath', r, True)
					new.getEnd()[0].setRole(rp)
					new.getEnd()[0].setPartWithPort(r)
					otherEnd=filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, "FlowPort"), s.getType().getOwnedElement())
					otherEnd = filter(lambda element: element.getType() == rp.getType(), otherEnd)
					otherEnd=filter(lambda element: StereotypesHelper.getStereotypePropertyValue(element, flowSter, "direction") != StereotypesHelper.getStereotypePropertyValue(rp, flowSter, 'direction'), otherEnd)
					for sp in sPaths:
						StereotypesHelper.setStereotypePropertyValue(new.getEnd()[1], nestSter, 'propertyPath', sp, True)
					StereotypesHelper.setStereotypePropertyValue(new.getEnd()[1], nestSter, 'propertyPath', s, True)
					new.getEnd()[1].setRole(otherEnd[0])
					new.getEnd()[1].setPartWithPort(s)
					newC = ef.createInformationFlowInstance()
					newC.setOwner(contextPackage)
					StereotypesHelper.addStereotypeByString(newC, 'ItemFlow')
					newC.setName('flow for '+rp.getType().getName())
					newC.getConveyed().add(rp.getType())
					new.get_informationFlowOfRealizingConnector().add(newC)
					if StereotypesHelper.getStereotypePropertyValue(rp, 'FlowPort', 'direction')[0].getName() == 'in':
						ModelHelper.setSupplierElement(newC, rp)
						ModelHelper.setClientElement(newC, otherEnd[0])
					else:
						ModelHelper.setSupplierElement(newC, otherEnd[0])
						ModelHelper.setClientElement(newC, rp)
					gl.log('FIX: Connector and Conveyed Information created between ' + rp.getQualifiedName() + ' and ' + otherEnd[0].getQualifiedName())
					connectorCreated+=1
		else:
			for endA in filter(lambda element: element.getOwner().getOwner() == context[0], rp.getEnd()):
				endB = findOppositeEnd(endA.getRole(), endA, endA.getOwner())
				'check if correct typing'
				if not endA.getOwner().get_informationFlowOfRealizingConnector():
					gl.log('ERROR: No conveyed information applied to connector between ' + endA.getRole().getName() + ' and ' + endB.getRole().getName())
					noConveyed+=1
					if validateOnly!=True and rp.getType() in cInfoTypes:
						goAhead1 = checkDirection(rp, cInfoTarg, cInfoSource, r)
#						gl.log('port: '+ endA.getRole().getType().getName())
#						gl.log('other:' + endB.getRole().getType().getName())
						if goAhead1 == 1 and endA.getRole().getType() == endB.getRole().getType():
							new = ef.createInformationFlowInstance()
							new.setOwner(contextPackage)
							StereotypesHelper.addStereotypeByString(new, 'ItemFlow')
							new.setName('flow for '+rp.getType().getName())
							new.getConveyed().add(rp.getType())
							endA.getOwner().get_informationFlowOfRealizingConnector().add(new)
							if StereotypesHelper.getStereotypePropertyValue(rp,"FlowPort","direction")[0].getName() =='in':
								ModelHelper.setSupplierElement(new, rp)
								ModelHelper.setClientElement(new, endB.getRole())
							else:
								ModelHelper.setSupplierElement(new, endB.getRole())
								ModelHelper.setClientElement(new, rp)
								gl.log('FIX: conveyed information applied to connector between ' + rp.getQualifiedName() + ' and '+endB.getRole().getQualifiedName())
						else:
							gl.log('ERROR port types do not match: '+endA.getRole().getName() + ' and ' + endB.getRole().getName())
							typeMis+=1
				for info in endA.getOwner().get_informationFlowOfRealizingConnector():
					if info.getConveyed()[0].getName() == rp.getType().getName():
						gl.log('Conveyed Information already applied for port of type: ' + str(rp.getType().getName()))
						conveyedApplied+=1
					else:
						gl.log('ERROR: conveyed information does not match for port of type: ' + str(rp.getType().getName()))
						conveyedBadApply+=1


	gl.log('Validation Summary:' )
	gl.log('No Connectors = ' + str(noConnector))
	gl.log('Connectors with No Conveyed Information = ' + str(noConveyed))
	gl.log('Conveyed Information = ' + str(conveyedApplied))		
	gl.log('Bad Conveyed = ' + str(conveyedBadApply))
	gl.log('Port Type MisMatch = ' + str(typeMis))
#	gl.log('Extra Conveyed Information = ' + str(len(pcNoMatch)))
	gl.log('Fix Summary: ')
	gl.log('Connectors Created = ' + str(connectorCreated))
	
def findOppositeEnd(role1, end1, connector):
    for end in connector.getEnd():
        if end.getRole() is not role1:
            return end
    for end in connector.getEnd():
        if end is not end1:
            return end
    return None
def flatten(x):
    result = []
    for el in x:
        if hasattr(el, "__iter__") and not isinstance(el, basestring):
            result.extend(flatten(el))
        else:
            result.append(el)
    return result
   
def checkDirection(port, targets, sources, r):
	go = 0
	rpDirection = StereotypesHelper.getStereotypePropertyValue(port, 'FlowPort', 'direction')
	for m in targets:
#		gl.log('DEBUG targets '+ m.getName())
		if m == port.getType():
#			gl.log('DEBUG: direction '+ rpDirection[0].getName())
			for t in targets[m]:
#				gl.log('DEBUG: mapping ' + t.getName())
#				gl.log('DEBUG: port '+ r.getName())
				if rpDirection[0].getName() == 'out' and t == r:
					go = 1
	for m in sources:
#		gl.log('DEBUG sources ' + m.getName())
		if m == port.getType():
#			gl.log('DEBUG: direction '+ rpDirection[0].getName())
			for s in sources[m]:
#				gl.log('DEBUG: mapping ' + s.getName())
#				gl.log('DEBUG: port '+ r.getName())
				if rpDirection[0].getName() == 'in' and s==r:
					go = 1
	return go



def run(mode):
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
       selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    try:
        SessionManager.getInstance().createSession("createFlowPorts")
        createFlowPorts(selected)
        SessionManager.getInstance().closeSession()
    except:
        if SessionManager.getInstance().isSessionCreated():
            SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        gl.log("*** EXCEPTION:")
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)
			
			
			
			
		