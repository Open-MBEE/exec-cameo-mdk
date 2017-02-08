
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
'update 2012April09'

import sys
import traceback
import os
gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProjectsManager().getActiveProject()
ef = project.getElementsFactory()

'target is flow port with larger conveyed information; for each conveyed information on flow port; check if flow port already exists of that type; if not create if so attach; do same on others'
'check flow direction'
def createFlowPorts(connectorTargets, validateOnly, connectorType):
#Initialize
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
	badConn =[]
#	Retrieve conveyed information from connector (connectortTargets); Create a list of: information types and connector ends(sources and targets)
	if connectorType == 'assembly' and len(connectorInformation)==0:
		gl.log('ERROR: No Conveyed Information. Stopping Execution')
		return
	for c in connectorInformation:
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
	rE = connectorEnds[0]
	sE = connectorEnds[1]
	
#	Get the roles and property paths for the connector ends
	r=rE.getRole()
	rPaths = StereotypesHelper.getStereotypePropertyValue(rE, nestSter,'propertyPath')
	s=sE.getRole()
	sPaths = StereotypesHelper.getStereotypePropertyValue(sE, nestSter, 'propertyPath')
	gl.log('Validating from ' + r.getName())
# Get the nested ports for each end.  Build lists of qualified names for opposite end of validation and port types of validation end
	rPorts = filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, 'FlowPort'), r.getType().getOwnedElement())
	sPorts=filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, 'FlowPort'), s.getType().getOwnedElement())
	sPortsQual = []
	for spq in sPorts:
		sPortsQual.append(spq.getQualifiedName())
	rPortTypes=[]
	for rpt in rPorts:
		rPortTypes.append(rpt.getType())
#Check to see if all conveyed information types match at least one port type of the validation end
	pcNoMatch = filter(lambda element: element not in rPortTypes, cInfoTypes)
	for p in pcNoMatch:
		gl.log('ERROR: Conveyed information has no matching port: ' + p.getName())
	for rp in rPorts:
		match = 0
#if the port has a connector end and the other end's qualified name is in the list of opposite end qualified names then the connector exists
		for e in rp.getEnd():
			opp = findOppositeEnd(e.getRole(), e, e.getOwner())
			if opp.getRole().getQualifiedName() in sPortsQual:
#have to check property path; otherwise an error will occur.
				
				match = 1
		if rp.getEnd() or match == 1:
			gl.log('Connector already exists for port ' + rp.getName())
			conveyedApplied+=1		
				
		if not rp.getEnd() or match == 0:
			gl.log('WARNING: No connector for port = ' + rp.getName())
			noConnector+=1
			go=0
			if (rp.getType() in cInfoTypes or connectorType == 'delegation') and validateOnly!=True:
				if connectorType =='assembly':
					goAhead = checkDirection(rp, cInfoTarg, cInfoSource, r)
				else: goAhead = 1
#				gl.log('debug ' + str(goAhead))
				if goAhead == 1:	
					new = ef.createConnectorInstance()
					new.setOwner(context[0])
					new.setName(rp.getName() + 'connector')
					StereotypesHelper.addStereotypeByString(new.getEnd()[0], 'NestedConnectorEnd')
					StereotypesHelper.addStereotypeByString(new.getEnd()[1], 'NestedConnectorEnd')
					for rpa in rPaths:
						StereotypesHelper.setStereotypePropertyValue(new.getEnd()[0], nestSter, 'propertyPath', rpa, True)
					StereotypesHelper.setStereotypePropertyValue(new.getEnd()[0], nestSter, 'propertyPath', r, True)
					new.getEnd()[0].setRole(rp)
					new.getEnd()[0].setPartWithPort(r)
					otherEnd=filter(lambda element: StereotypesHelper.hasStereotypeOrDerived(element, "FlowPort"), s.getType().getOwnedElement())
					otherEnd = filter(lambda element: element.getType() == rp.getType(), otherEnd)
					if connectorType=='assembly':
						otherEnd=filter(lambda element: StereotypesHelper.getStereotypePropertyValue(element, flowSter, "direction") != StereotypesHelper.getStereotypePropertyValue(rp, flowSter, 'direction'), otherEnd)
					else:
						otherEnd=filter(lambda element:StereotypesHelper.getStereotypePropertyValue(element, flowSter, "direction")==StereotypesHelper.getStereotypePropertyValue(rp, flowSter, "direction"), otherEnd)
					for sp in sPaths:
						StereotypesHelper.setStereotypePropertyValue(new.getEnd()[1], nestSter, 'propertyPath', sp, True)
					StereotypesHelper.setStereotypePropertyValue(new.getEnd()[1], nestSter, 'propertyPath', s, True)
					new.getEnd()[1].setRole(otherEnd[0])
					new.getEnd()[1].setPartWithPort(s)
					if connectorType == 'assembly':
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
						gl.log('Connector and Conveyed Information created between ' + rp.getQualifiedName() + ' and ' + otherEnd[0].getQualifiedName())
					else:
						gl.log('Connector created between' + rp.getQualifiedName() + 'and' + otherEnd[0].getQualifiedName())
					connectorCreated+=1
		else:
			for endA in filter(lambda element: element.getOwner().getOwner() == context[0], rp.getEnd()):
				endB = findOppositeEnd(endA.getRole(), endA, endA.getOwner())
				'check if correct typing'
				if not endA.getOwner().get_informationFlowOfRealizingConnector() and connectorType == 'assembly':
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
					if connectorType == 'assembly':
						if info.getConveyed()[0].getName() == rp.getType().getName():
							gl.log('Conveyed Information already applied for port of type: ' + str(rp.getType().getName()))
							conveyedApplied+=1
						else:
							gl.log('ERROR: conveyed information does not match for port of type: ' + str(rp.getType().getName()))
							conveyedBadApply+=1


	gl.log('Validation Summary:' )
	gl.log('No Connectors = ' + str(noConnector))
	if connectorType == 'assembly':
		gl.log('Conveyed Information = ' + str(conveyedApplied))
		gl.log('Connectors with No Conveyed Information = ' + str(noConveyed))
	else:
		gl.log('Connectors = ' + str(conveyedApplied))	
	gl.log('Bad Conveyed = ' + str(conveyedBadApply))
	gl.log('Port Type MisMatch = ' + str(typeMis))
	gl.log('Extra Conveyed Information = ' + str(len(pcNoMatch)))
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
	'''checks to see if direction for assembly flow ports are appropriate (i.e. in to out or out to in)'''
	'''this function returns 1 if on the opposing port there exists a flow port with the same type as input port and opposite direction'''
	go = 0
	rpDirection = StereotypesHelper.getStereotypePropertyValue(port, 'FlowPort', 'direction')
	for m in targets:
#		gl.log('DEBUG targets '+ m.getName())
		if m == port.getType():
#			gl.log('DEBUG target: '+str(rpDirection[0].getName()=='in'))
#			gl.log('DEBUG target: '+str(t==r))
			for t in targets[m]:
				if rpDirection[0].getName() == 'out' and t == r:
					go = 1
				
	for m in sources:
#		gl.log('DEBUG sources ' + m.getName())
		if m == port.getType():
#			gl.log('DEBUG: direction '+ rpDirection[0].getName())
			for s in sources[m]:
#				gl.log('DEBUG source: ' + str(rpDirection[0].getName()=='out'))
#				gl.log('DEBUG source: '+ str(s==r))
				if rpDirection[0].getName() == 'in' and s==r:
					go = 1
				
	return go


			
			
		