# Written by David Coren, JPL
# May 15, 2013

# To use this script you must first be using DocGen, then create a new view,
# a new viewpoint, and a new stereotype where the name is
# common.CustomDependencyValidation. Set the metaclasses of this stereotype
# to Activity and CallBehaviorAction, and set the base classifier of the new
# stereotype as «EditableTable». The view you create must
# import three stereotypes, two of which can be extensions of any element,
# and one of which must extend dependencies. The viewpoint needs to have an
# action in it stereotyped as «common.CustomDependencyValidation» and be
# imported by the view. You can use this script for multiple view/viewpoint
# combinations, importing different stereotypes and changing options on the
# rules of the validation.



from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Enumeration
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import AggregationKindEnum
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port

from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Lifeline
from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import StateInvariant
from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import Interaction
from com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines import State
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import ElementValue
from com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime import DurationConstraint

from javax.swing import JOptionPane
from javax.swing import JCheckBox
from java.lang import Object
from jarray import array
import sys
import traceback
import os

from gov.nasa.jpl.mbee.lib import Utils
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBColSpec
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTableEntry
from gov.nasa.jpl.mbee import DocGenUtils
from gov.nasa.jpl.mgss.mbee.docgen.table import EditableTable
from gov.nasa.jpl.mgss.mbee.docgen.table import PropertyEnum
import MDUtils._MDUtils as MDUtils
reload(MDUtils)


gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
mem = ModelElementsManager.getInstance()
ef = project.getElementsFactory()
es = StereotypesHelper().getExtendedElements(StereotypesHelper().getStereotype(project, "ValueType"))
for e in es:
    if "Boolean" in e.getName():
        Boolean = e
ds = StereotypesHelper().getExtendedElements
        

scriptOutput = {}
targets = scriptInput['DocGenTargets']
action = scriptInput['inputElement']

error = []
baderror = []
model = []
action_stereotype = (StereotypesHelper().getStereotypes(action))[0]

try:
    if len(scriptInput['supplier']) == 1:
        supplierdefined = True
    else:
        supplierdefined = False
    supplierpropdefined = True
except:
    supplierdefined = False
    supplierpropdefined = False

try:
    if len(scriptInput['client']) == 1:
        clientdefined = True
    else:
        clientdefined = False
    clientpropdefined = True
except:
    clientdefined = False
    clientpropdefined = False

try:
    if len(scriptInput['can depend on many']) == 1:
        manysupplierdefined = True
    else:
        manysupplierdefined = False
    manysupplierpropdefined = True
except:
    manysupplierdefined = False
    manysupplierpropdefined = False

try:
    if len(scriptInput['can have many dependent elements']) == 1:
        manyclientdefined = True
    else:
        manyclientdefined = False
    manyclientpropdefined = True
except:
    manyclientdefined = False
    manyclientpropdefined = False

def collect(sty):
    elements = StereotypesHelper.getExtendedElementsIncludingDerived(sty)
    return elements

if len(targets) != 3:
    error = "This script requires that you import three stereotypes, two that extend classes and one that extends dependencies. Check that you're importing all of those and not something else by mistake."
    
ele = []
output = []
type1 = []
type2 = []
type3 = []
type4 = []
D = []
if len(targets) == 3 or len(targets) == 2:
    for t in targets:
        meta = StereotypesHelper().getBaseClasses(t, True)
        for m in meta:
            mn = m.getName()
            if "Dependency" in mn:
                D = t
            elif "Abstraction" in mn:
                D = t
            elif "Relationship" in mn:
                D = t
        if t != D:
            ele.append(t)

    if supplierdefined is True:
        S1 = scriptInput['supplier'][0]
    else:
        S1 = MDUtils.getUserDropdownSelection("First time setup", "This window should only appear the first time you run the script.\nMake sure the viewpoint you're using is locked recursively. Here you need to specify which stereotype applies to the DEPENDENT element.\nThat is, which one has the dependency arrows pointing AWAY from it?\nDon't be confused by the long qualified names, just check the bit after the last ::", ele)
        if supplierpropdefined is False:
            p = ef.createPropertyInstance()
            p.setName('supplier')
            p.setType(StereotypesHelper().getBaseClass(S1))
            mem.addElement(p, action_stereotype)
        slot = StereotypesHelper().getSlot(action, action_stereotype, 'supplier', True, False)
        StereotypesHelper().addSlotValue(slot, S1, False)
    if clientdefined is True:
        S2 = scriptInput['client'][0]
    else:
        S2 = MDUtils.getUserDropdownSelection("First time setup", "This window should only appear the first time you run the script.\nMake sure the viewpoint you're using is locked recursively. Here you need to specify which stereotype applies to the INDEPENDENT element.\nThat is, which one has the dependency arrows pointing TOWARDS it?\nDon't be confused by the long qualified names, just check the bit after the last ::", ele)
        if S1 == S2 and len(targets) == 3:
            baderror = "You selected the same stereotype as the independent element and the dependent element! Don't do that! Go remove the tagged value from action with the stereotype " + action_stereotype.getName()
        elif clientpropdefined is False:
            p = ef.createPropertyInstance()
            p.setName('client')
            p.setType(StereotypesHelper().getBaseClass(S2))
            mem.addElement(p, action_stereotype)
        slot = StereotypesHelper().getSlot(action, action_stereotype, 'client', True, False)
        if slot is not None:
            StereotypesHelper().addSlotValue(slot, S2, False)
    if manysupplierdefined is True:
        manysupplier = scriptInput['can depend on many']
    else:
        options = [False, True]
        input = JOptionPane.showInputDialog(None, "Here you need to decided if things stereotyped as " + S1.getName() + " are allowed to have multiple dependencies going out of them. \nThat is, can elements stereotyped as " + S1.getName() + " depend on multiple elements stereotyped as " + S2.getName() + "? \nIf this is okay, select true. If not, select false. If you're unsure, select false so you see those items anyway.", "First time setup", JOptionPane.PLAIN_MESSAGE, None, options, None)
        if input is not None:
            manysupplier = input
        if manysupplierpropdefined is False:
            p = ef.createPropertyInstance()
            p.setName('can depend on many')
            p.setType(Boolean)
            mem.addElement(p, action_stereotype)
        slot = StereotypesHelper().getSlot(action, action_stereotype, 'can depend on many', True, False)
        if slot is not None:
            StereotypesHelper().addSlotValue(slot, manysupplier, False)
    if manyclientdefined is True:
        manyclient = scriptInput['can have many dependent elements']
    else:
        options = [False, True]
        input = JOptionPane.showInputDialog(None, "Here you need to decided if things stereotyped as " + S2.getName() + " are allowed to have multiple dependencies going into them. \nThat is, can elements stereotyped as " + S2.getName() + " have many elements stereotyped as " + S1.getName() + " dependent on them? \nIf this is okay, select true. If not, select false. If you're unsure, select false so you see those items anyway.", "First time setup", JOptionPane.PLAIN_MESSAGE, None, options, None)
        if input is not None:
            manyclient = input
        if manyclientpropdefined is False:
            p = ef.createPropertyInstance()
            p.setName('can have many dependent elements')
            p.setType(Boolean)
            mem.addElement(p, action_stereotype)
        slot = StereotypesHelper().getSlot(action, action_stereotype, 'can have many dependent elements', True, False)
        if slot is not None:
            StereotypesHelper().addSlotValue(slot, manyclient, False)
    if len(baderror) == 0:
        As = collect(S1)
        Bs = collect(S2)
        Ds = collect(D)
        As_deps = []
        Bs_deps = []
        for Aa in As:
            Aa_dep = []
            row = []
            if Aa.hasClientDependency() is True:
                Aa_dep = Aa.getClientDependency()
                As_deps.append(Aa_dep)
                num_w_d = 0
                for d in Aa_dep:
                    if d in Ds:
                        num_w_d+=1
                if num_w_d == 0:
                    row.append(Aa.getName())
                    row.append(StereotypesHelper().getFirstVisibleStereotype(Aa).getName() + ' ' + Aa.getName() + " has no dependency stereotyped as " + D.getName() + "!")
                    row.append(Aa.getQualifiedName())
                    type2.append(row)
                elif num_w_d > 1:
                    if manysupplier is False:
                        row.append(Aa.getName())
                        row.append(StereotypesHelper().getFirstVisibleStereotype(Aa).getName() + ' ' + Aa.getName() + " has " + str(num_w_d) + " client dependencies stereotyped as " + D.getName() + "!")
                        row.append(Aa.getQualifiedName())
                        type4.append(row)
            else:
                row.append(Aa.getName())
                row.append(StereotypesHelper().getFirstVisibleStereotype(Aa).getName() + ' ' + Aa.getName() + " has no client dependency!")
                row.append(Aa.getQualifiedName())
                type1.append(row)
            if Aa.hasSupplierDependency() is True:
                Aa_bad = Aa.getSupplierDependency()
                for d in Aa_bad:
                    if d in Ds:
                        row2 = []
                        row2.append(Aa.getName())
                        row2.append(StereotypesHelper().getFirstVisibleStereotype(Aa).getName() + ' ' + Aa.getName() + " is a supplier of a " + D.getName() + " dependency! Elements with the stereotype " + StereotypesHelper().getFirstVisibleStereotype(Aa).getName() + " should not supply these kinds of dependencies.")
                        row2.append(Aa.getQualifiedName())
                        type3.append(row2)
        for Bb in Bs:
            Bb_dep = []
            row = []
            if Bb.hasSupplierDependency() is True:
                Bb_dep = Bb.getSupplierDependency()
                Bs_deps.append(Bb_dep)
                num_w_d = 0
                for d in Bb_dep:
                    if d in Ds:
                        num_w_d+=1
                if num_w_d == 0:
                    row.append(Bb.getName())
                    row.append(StereotypesHelper().getFirstVisibleStereotype(Bb).getName() + ' ' + Bb.getName() + " has no dependency stereotyped as " + D.getName() + "!")
                    row.append(Bb.getQualifiedName())
                    type2.append(row)
                elif num_w_d > 1:
                    if manyclient is False:
                        row.append(Bb.getName())
                        row.append(StereotypesHelper().getFirstVisibleStereotype(Bb).getName() + ' ' + Bb.getName() + " supplies " + str(num_w_d) + " dependencies stereotyped as " + D.getName() + ". This is not necessarily a problem.")
                        row.append(Bb.getQualifiedName())
                        type4.append(row)
            else:
                row.append(Bb.getName())
                row.append(StereotypesHelper().getFirstVisibleStereotype(Bb).getName() + ' ' + Bb.getName() + " has no supplier dependency!")
                row.append(Bb.getQualifiedName())
                type1.append(row)
            if Bb.hasClientDependency() is True:
                Bb_bad = Bb.getClientDependency()
                for d in Bb_bad:
                    if d in Ds:
                        row2 = []
                        row2.append(Bb.getName())
                        row2.append(StereotypesHelper().getFirstVisibleStereotype(Bb).getName() + ' ' + Bb.getName() + " is a client of a " + D.getName() + " dependency! Elements with the stereotype " + StereotypesHelper().getFirstVisibleStereotype(Bb).getName() + " should not be clients of these kinds of dependencies.")
                        row2.append(Bb.getQualifiedName())
                        type3.append(row2)

def appendmodel(m, t):
    i = len(m)
    for r in t:
        m.append(0)
        m[i] = r
        i+=1        

appendmodel(model, type1)
appendmodel(model, type2)
appendmodel(model, type3)
appendmodel(model, type4)

if len(baderror) != 0:
    gl.log(baderror)
elif len(error) != 0:
    gl.log(error)
elif len(model) == 0:
    gl.log("All rules validated.")

headers = ["Problem element", "Problem", "Qualified Name"]
table = EditableTable("List of problem items", model, headers, None, None, None)
table.prepareTable()


scriptOutput["EditableTable"] = table
docgenTable = Utils.getDBTableFromEditableTable(table, False)
scriptOutput["DocGenOutput"] = [docgenTable]
