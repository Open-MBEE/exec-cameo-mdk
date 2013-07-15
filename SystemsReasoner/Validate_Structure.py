from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Enumeration
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Class
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdports import Port
from com.nomagic.magicdraw.teamwork.application import TeamworkUtils

from javax.swing import JOptionPane
from javax.swing import JCheckBox
from java.lang import Object
from jarray import array
import sys
import traceback
import os

import MDUtils._MDUtils as MDUtils
reload(MDUtils)
import SRUtils
reload(SRUtils)


currentproject = Application.getInstance().getProject()
gl = Application.getInstance().getGUILog()
MEM = ModelElementsManager.getInstance()
Rolester = StereotypesHelper.getStereotype(currentproject, 'Role')

class SRChecker:
    '''this class will check for inheritance rules between a list of properties the child should inherit and the child, 
       options: {
           'checkOnly': boolean #if this is true, it won't fix any errors and will only report errors it found, including part blocks, on the child only
           'fixAggregationDeleteBlock': boolean #whether to delete the typing block of a part property when changing it to shared
            'fixWrongRedef': boolean #whether to fix wrong redefinitions
            'fixWrongType': boolean #whether totally wrong things should be fixed
            'fixWrongTypeDeleteBlock': boolean #during fixWrongType, if cattr is a part property, should its type block be deleted
            'fixWrongValueType': boolean
            'fixRedefName': boolean
            'fixExtra': boolean
            'fixAll': boolean #try to autofix all errors (except extra errors)
        }'''
    def __init__(self, child, options):
        self.dupRedefErrs = {}          #child have multiple properties that redefine same parent property
        self.multipleRedefErrs = {}     #child have property that redefines multiple properties (at least one from parent)
        self.wrongRedefErrs = {}        #child have property with same name as a parent property that redefines wrong propertie(s) (not necessarily from parent)
        self.missingRedefErrs = []      #child is missing properties
        self.redefNameErrs = {}         #child property redefines a parent property but have different name
        self.nonRedefNameErrs = {}      #child have a property with same name as parent property but doesn't redefine anything
        self.aggregationErrs = {}       #part aggregation type has changed (shared and composite)
        self.wrongSharedType = {}       #shared property have wrong type as parent
        self.wrongRefType = {}
        self.wrongValueType = {}        #value property have wrong type
        self.wrongPortType = {}         #port have wrong type
        self.wrongType = {}             #all other wrong types (this usually means the category itself is wrong - ex. value instead of part, etc)
        self.extraErrs = []             #extra properties in child
        self.missingGeneralizationErrs = {} #for part types
        self.missingAssociationGeneralizationErrs = {}
        self.wrongRedefineOnlyType = {} #if redefine only is set, and the types don't match (like shared type)
        self.constraintTypeErrs = {}    #for constraint types
        self.obsoleteRedefs = {}        #child properties is redefining a property from a class it doesn't specialize
        self.addRedefine = {}
        self.sameValue = {}
        self.child = child
        self.options = options
        
        self.aggregationErrPartBlocks = []  #user doesn't want to delete these blocks when a part changed to shared
        self.wrongRedefErrOk = []           #user doesn't want to fix the wrong redefines??
        self.redefNameErrOk = []            #user wants a different name for the redefinition
        self.wrongValueTypeOk = []          #user says wrong value (or port) type is ok
        self.constraintTypeErrOk = []
        self.extraErrOk = []                #unique property is ok
        self.wrongTypeOk = []
        
        self.partHasErr = False
        
        allInherited = SRUtils.getAllInheritedAttributes(child)
        genTree = SRUtils.getGeneralizationTree(child)
        redefTree = SRUtils.getRedefinitionTree(genTree, child)
        self.inherited = SRUtils.getInheritedAttributes(allInherited, redefTree)
    
    def printErrors(self):
        '''prints all errors, except missing redef errors'''
        gl.log('===== Printing validation results for Block ' + self.child.getQualifiedName() + ' =====')
        #if not self.hasErrors():
        #    gl.log('\tno existing errors')
        #    return
        
        if len(self.dupRedefErrs) > 0:
            gl.log('\tParent propertie(s) that are redefined by multiple child properties:')
            for attr in self.dupRedefErrs:
                gl.log('\t\t' + attr.getQualifiedName() + ' is redefined by ' + ', '.join([prop.getQualifiedName() for prop in self.dupRedefErrs[attr]]))
        
        if len(self.multipleRedefErrs) > 0:
            gl.log('\tChild propertie(s) that redefined multiple properties (at least one of which is a parent property):')
            for cattr in self.multipleRedefErrs:
                gl.log('\t\t' + cattr.getQualifiedName() + ' redefines ' + ', '.join([prop.getQualifiedName() for prop in self.multipleRedefErrs[cattr]]))
        
        if len(self.redefNameErrs) > 0:
            gl.log('\tChild propertie(s) that redefines a parent property but have different name:')
            for cattr in self.redefNameErrs:
                gl.log('\t\t' + self.redefNameErrs[cattr].getQualifiedName() + ' is redefined by ' + cattr.getQualifiedName())
        
        if len(self.nonRedefNameErrs) > 0:
            gl.log('\tChild propertie(s) that have the same name as a parent property but does not redefine it or anything else:')
            for cattr in self.nonRedefNameErrs:
                gl.log('\t\t' + self.nonRedefNameErrs[cattr].getQualifiedName() + ' is not redefined by ' + cattr.getQualifiedName())
        
        if len(self.wrongRedefErrs) > 0:
            gl.log('\tPropertie(s) that have the same name but redefines something else:')
            for cattr in self.wrongRedefErrs:
                gl.log('\t\t' + self.wrongRedefErrs[cattr].getQualifiedName() + ' is not redefined by ' + cattr.getQualifiedName())
        
        if len(self.wrongValueType) > 0:
            gl.log('\tPropertie(s) that have the wrong value type:')
            for cattr in self.wrongValueType:
                gl.log('\t\t' + self.wrongValueType[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
        
        if len(self.wrongPortType) > 0:
            gl.log('\tPropertie(s) that have the wrong port type:')
            for cattr in self.wrongPortType:
                gl.log('\t\t' + self.wrongPortType[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
        
        if len(self.aggregationErrs) > 0:
            gl.log('\tPropertie(s) that have wrong aggregation:')
            for cattr in self.aggregationErrs:
                gl.log('\t\t' + self.aggregationErrs[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
    
        if len(self.wrongSharedType) > 0:
            gl.log('\tPropertie(s) that have wrong shared type:')
            for cattr in self.wrongSharedType:
                gl.log('\t\t' + self.wrongSharedType[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
        
        if len(self.wrongRedefineOnlyType) > 0:
            gl.log('\tPropertie(s) that have wrong type because of redefine only:')
            for cattr in self.wrongRedefineOnlyType:
                gl.log('\t\t' + self.wrongRedefineOnlyType[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
        
        if len(self.wrongRefType) > 0:
            gl.log('\tPropertie(s) that have wrong reference type:')
            for cattr in self.wrongRefType:
                gl.log('\t\t' + self.wrongRefType[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
        
        if len(self.constraintTypeErrs) > 0:
            gl.log('\tConstraint propertie(s) that have wrong constraint block type:')
            for cattr in self.constraintTypeErrs:
                gl.log('\t\t' + self.constraintTypeErrs[cattr].getQualifiedName() + ' and ' + cattr.getQualifiedName())
            
        if len(self.wrongType) > 0:
            gl.log('\tPropertie(s) that have totally wrong type and property:')
            for cattr in self.wrongType:
                gl.log('\t\t' + self.wrongType[cattr].getQualifiedName() + 'and ' + cattr.getQualifiedName())
        
        if len(self.missingGeneralizationErrs) > 0:
            gl.log('\tMissing generalizations between part property types:')
            for cattr in self.missingGeneralizationErrs:
                gl.log('\t\t' + cattr.getType().getQualifiedName() + ' should specialize ' + self.missingGeneralizationErrs[cattr].getType().getQualifiedName())
        
        if len(self.missingAssociationGeneralizationErrs) > 0:
            gl.log('\tMissing generalizations between associations for:')
            for cattr in self.missingAssociationGeneralizationErrs:
                gl.log('\t\t' + cattr.getQualifiedName() + '\'s association should specialize ' + self.missingAssociationGeneralizationErrs[cattr].getQualifiedName() + '\'s association')
            
        if len(self.missingRedefErrs) > 0:
            gl.log('\tMissing Properties in child:')
            for attr in self.missingRedefErrs:
                gl.log('\t\t' + attr.getQualifiedName())
    
        
            
        if len(self.obsoleteRedefs) > 0:
            gl.log('\tProperties redefining a property that is not in a superclass:')
            for cattr in self.obsoleteRedefs:
                gl.log('\t\t' + cattr.getQualifiedName())
        
        if len(self.addRedefine) > 0:
            gl.log('\tProperties that should be redefining some parent property due to association generalization:')
            for cattr in self.addRedefine:
                gl.log('\t\t' + cattr.getQualifiedName() + ' should be redefining ' + self.addRedefine[cattr].getQualifiedName())
                
        if len(self.extraErrs) > 0:
            gl.log('\tProperties unique to this block:')
            for cattr in self.extraErrs:
                gl.log('\t\t' + cattr.getQualifiedName())
                
        if len(self.sameValue) > 0:
            gl.log('\tValue properties that has the same default value as parent (or is nan):')
            for cattr in self.sameValue:
                gl.log('\t\t' + cattr.getQualifiedName())
   
    def printExtra(self):
        if len(self.extraErrs) > 0:
            gl.log('\tProperties unique to this block:')
            for cattr in self.extraErrs:
                gl.log('\t\t' + cattr.getQualifiedName())
                
    def fixAggregation(self, attr, cattr, doNotRemove):
        '''
        if parent is part, child is shared:
            child's prop is removed, will be redone by generalization
        if parent is shared, child is part:
            if child's part type is same as parent's shared type, just fix child's aggregation type
            if child's part type is not parent's shared type, remove the child's part type (should ask user?)
            fix the part stereotypes also
        '''
        if StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.partPropS): #parent has part type, child has shared type, just remove child's prop to redo
            gl.log('[Validation]: removing ' + cattr.getName() + ' to repropagate (was shared or ref, now part)')
            MEM.removeElement(cattr) 
        else: #parent has share type, child has part type, remove child's part type if it was duplicated and point the child property to parent's share type                
            if attr.getType() is not cattr.getType():
                if cattr.getType() is not None and cattr.getType() not in doNotRemove:
                    message = attr.getQualifiedName() + ' is a shared property while ' + cattr.getQualifiedName() + ' is a part or ref property. Validation will now change it to shared. Would you like to delete the extra typing block (' + cattr.getType().getQualifiedName() + ')?'
                    check = JCheckBox("Use this answer for the rest of this session for this type of error")
                    choice = None
                    if 'fixAggregationDeleteBlock' not in self.options:
                        choice = JOptionPane.showConfirmDialog(None, array([message, check], Object) , "Delete Type Block?", JOptionPane.YES_NO_OPTION)
                    if choice == JOptionPane.YES_OPTION or 'fixAggregationDeleteBlock' in self.options and self.options['fixAggregationDeleteBlock']:
                        if check.isSelected():
                            self.options['fixAggregationDeleteBlock'] = True
                        gl.log('[Validation]: removing block ' + cattr.getType().getQualifiedName() + ' (was part or ref, now shared)')
                        MEM.removeElement(cattr.getType())
                    else:
                        if check.isSelected():
                            self.options['fixAggregationDeleteBlock'] = False
                        doNotRemove.append(cattr.getType())
                cattr.setType(attr.getType())
                cattr.setAggregation(attr.getAggregation())
            else: #just fix aggregation
                gl.log('[Validation]: changing aggregation of ' + cattr.getQualifiedName() + ' from part to shared')
                cattr.setAggregation(attr.getAggregation())
            StereotypesHelper.removeStereotype(cattr, SRUtils.partPropS)
            StereotypesHelper.removeStereotype(cattr, SRUtils.sharedPropS)
            StereotypesHelper.removeStereotype(cattr, SRUtils.refPropS)
            MDUtils.copyStereotypes(attr, cattr)
           # if StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.partPropS):
            #    StereotypesHelper.removeStereotype(cattr, SRUtils.partPropS)
             #   StereotypesHelper.addStereotype(cattr, SRUtils.sharedPropS)
    
    def fixDupRedefErr(self, attr, cattrs):
        '''
        from cattrs that redefine attr, choose the cattr that have the same name and remove attr from the redefined list of other cattrs
        '''
        aname = attr.getName()
        dups = []
        choose = None
        for cattr in cattrs:
            if cattr.getName() == aname:
                if choose is not None: #multiple child properties have same name 
                    choose = None
                    break #cannot decide, ask user
                choose = cattr
        if choose is None: #none have the same name
            title = "Choose property"
            message = "There are more than one property in " + self.child.getQualifiedName() + " that redefines " + attr.getQualifiedName() + ". Choose the right one."
            choose = MDUtils.getUserDropdownSelection(title, message, cattrs) #ask user
            if choose is None:
                return #user doesn't know either??? What to do??
            
        for cattr in cattrs:
            if cattr is not choose:
                dups.append(cattr) #get all the ones user didn't choose
        for dup in dups:
            iter = dup.getRedefinedElement().iterator()
            while iter.hasNext():
                if iter.next() is attr:
                    gl.log('[Validation] - duplicate redef error: removing ' + attr.getQualifiedName() + ' from ' + dup.getQualifiedName() + '\'s redefined list')
                    iter.remove() #remove from redefinitions
    
    def shouldRedefines(self, attrs, cattr):
        for attr in attrs:
            if not self.shouldRedefine(attr, cattr):
                return False
            
    def shouldRedefine(self, attr, cattr):
        if attr.getName() != cattr.getName():
            return False
        if attr not in self.inherited:
            return False
        if StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.partPropS) and not StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.partPropS) or \
           StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.sharedPropS) and not StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.sharedPropS) or \
           StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.valuePropS) and not StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.valuePropS) or \
           StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.consPropS) and not StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.consPropS) or \
           isinstance(attr, Port) and not isinstance(cattr, Port):
            return False
        return True

    def fixMultipleRedefErr(self, cattr, attrs):
        '''
        for the attrs that cattr redefines, choose the one with matching name and remove all others this needs to be looked at
        '''
        cname = cattr.getName()
        choose = []
        for attr in attrs:
            if self.shouldRedefine(attr, cattr):
                choose.append(attr)
        choose2 = None
        if len(choose) == 0:
            title = "Choose property"
            message = cattr.getQualifiedName() + " redefines more than one property, choose the right one." 
            choose2 = MDUtils.getUserDropdownSelection(title, message, attrs) #ask user
            if choose2 is None:
                return #user doesn't know either???        

        iter = cattr.getRedefinedElement().iterator()
        while iter.hasNext():
            bad = iter.next()
            if bad in choose or bad is choose2:
                continue
            gl.log('[Validation] - multiple redef error: removing ' + bad.getQualifiedName() + ' from ' + cattr.getQualifiedName() + '\'s redefined list')
            iter.remove()
    
    def fixWrongRedefErr(self, attr, cattr, doNotFix):
        '''
        ask user if they want to fix wrong redefinitions
        '''
        if cattr in doNotFix:
            return
        l = []
        if cattr.hasRedefinedProperty():
            common = []
            common.extend(cattr.getRedefinedProperty())
            common.append(attr)
            if self.shouldRedefines(common, cattr):
                gl.log('[Validation]: adding ' + attr.getQualifiedName() + ' to redefinition list of ' + cattr.getQualifiedName())
                cattr.getRedefinedProperty().add(attr)
                return
            wrongs = cattr.getRedefinedProperty()
            for wrong in wrongs:
                if wrong is not attr:
                    l.append(wrong)
        message = cattr.getQualifiedName() + ' does not redefine ' + attr.getQualifiedName() + ' but redefines ' + ', '.join([e.getQualifiedName() for e in l]) + '. Would you like to fix the redefinition?'
        choice = None
        checkbox = JCheckBox("Use this answer for the rest of this session for this type of error")
        if 'fixWrongRedef' not in self.options and not self.options['fixAll']:
            choice = JOptionPane.showConfirmDialog(None, array([message, checkbox], Object), "Fix Redefine?", JOptionPane.YES_NO_OPTION);
        if choice == JOptionPane.YES_OPTION or 'fixWrongRedef' in self.options and self.options['fixWrongRedef'] or self.options['fixAll']:
            if checkbox.isSelected():
                self.options['fixWrongRedef'] = True
            MDUtils.setRedefine(attr, cattr)
            gl.log('[Validation]: setting redefinition of ' + cattr.getQualifiedName() + ' to ' + attr.getQualifiedName())
        else:
            if checkbox.isSelected():
                self.options['fixWrongRedef'] = False
            doNotFix.append(cattr)
    
    def fixWrongType(self, attr, cattr, doNotRemove):
        '''
        wrong type means the child's property is so different from parent's I don't know what to do with it except remove and repropagate, asks user if that's ok
        '''
        choice = None
        check = JCheckBox("Use this answer for the rest of this session for this type of error")
        message = cattr.getQualifiedName() + ' is completely different from ' + attr.getQualifiedName() + ', remove and repropagate?'
        if 'fixWrongType' not in self.options and not self.options['fixAll']:
            choice = JOptionPane.showConfirmDialog(None, array([message, check], Object), "Fix redefinition?", JOptionPane.YES_NO_OPTION);
        if choice == JOptionPane.YES_OPTION or 'fixWrongType' in self.options and self.options['fixWrongType'] or self.options['fixAll']:
            if check.isSelected():
                self.options['fixWrongType'] = True
            gl.log('[Validation]: attribute redefines parent property with totally different property type - marking ' + cattr.getQualifiedName() + ' for redo')
            if StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.partPropS) and cattr.getType() is not None: #child's property is a part property, should I remove the typing block also?
                message2 = cattr.getQualifiedName() + ' is a part property, would you also like to delete its typing block? (' + cattr.getType().getQualifiedName() + ')'
                choice2 = None
                if 'fixWrongTypeDeleteBlock' not in self.options:
                    choice2 = JOptionPane.showConfirmDialog(None, array([message2, check], Object), "Remove Typing Block?", JOptionPane.YES_NO_OPTION);  
                if choice2 == JOptionPane.YES_OPTION or 'fixWrongTypeDeleteBlock' in self.options and self.options['fixWrongTypeDeleteBlock']: 
                    if check.isSelected():
                        self.options['fixWrongTypeDeleteBlock'] = True
                    gl.log('[Validation]: removed ' + cattr.getType().getQualifiedName())
                    MEM.removeElement(cattr.getType())
                else:
                    self.options['fixWrongTypeDeleteBlock'] = False
            MEM.removeElement(cattr)
        else:
            if check.isSelected():
                self.options['fixWrongType'] = False
            pass #don't konw what to do, cannot proceed
    
    def fixWrongValueType(self, attr, cattr, doNotFix):
        if cattr in doNotFix:
            return
        message = None
        if attr.getType() is not None and cattr.getType() is not None:
            message = cattr.getQualifiedName() + ' has type ' + cattr.getType().getName() + ' but ' + attr.getQualifiedName() + ' has type ' + attr.getType().getName() + ', fix it?'
        else:
            message = cattr.getQualifiedName() + '\'s type is different from ' + attr.getQualifiedName() + '\'s type, fix it?'
        choice = None
        check = JCheckBox("Use this answer for the rest of this session for this type of error")
        if 'fixWrongValueType' not in self.options and not self.options['fixAll']:
            choice = JOptionPane.showConfirmDialog(None, array([message, check], Object), "Fix wrong value or port type?", JOptionPane.YES_NO_OPTION);
        if choice == JOptionPane.YES_OPTION or 'fixWrongValueType' in self.options and self.options['fixWrongValueType'] or self.options['fixAll']:
            if check.isSelected():
                self.options['fixWrongValueType'] = True
            if 'mapping' in self.options and attr.getType() in self.options['mapping']:
                cattr.setType(self.options['mapping'][attr.getType()])
            else:
                cattr.setType(attr.getType())
            if cattr.getType() is not None:
                gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to ' + cattr.getType().getName())
            else:
                gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to null')
        else:
            if check.isSelected():
                self.options['fixWrongValueType'] = False
            doNotFix.append(cattr)
    
    def fixRedefNameErr(self, attr, cattr, doNotFix):
        if cattr in doNotFix:
            return
        choice = None
        check = JCheckBox("Use this answer for the rest of this session for this type of error")
        message = cattr.getQualifiedName() + ' redefines ' + attr.getQualifiedName() + ' but has a different name, fix it?'
        if 'fixRedefName' not in self.options and not self.options['fixAll']:
            choice = JOptionPane.showConfirmDialog(None, array([message, check], Object), "Fix wrong name?", JOptionPane.YES_NO_OPTION);
        if choice == JOptionPane.YES_OPTION or 'fixRedefName' in self.options and self.options['fixRedefName'] or self.options['fixAll']:
            if check.isSelected():
                self.options['fixRedefName'] = True
            cattr.setName(attr.getName())
            gl.log('[Validation]: setting name of ' + cattr.getQualifiedName() + ' to ' + attr.getName())
        else:
            if check.isSelected():
                self.options['fixRedefName'] = False
            doNotFix.append(cattr)
    
    def fixExtraErrs(self, attr, doNotFix):
        if attr in doNotFix:
            return
        choice = None
        check = JCheckBox("Use this answer for the rest of this session for this type of error")
        message = attr.getQualifiedName() + ' is unique to its block and may be obsolete, do you want to delete it?'
        if 'fixExtra' not in self.options:
            choice = JOptionPane.showConfirmDialog(None, array([message, check], Object), "Fix extra?", JOptionPane.YES_NO_OPTION);
        if choice == JOptionPane.YES_OPTION or 'fixExtra' in self.options and self.options['fixExtra']:
            if check.isSelected():
                self.options['fixExtra'] = True
            if StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.partPropS):
                message = 'The unique property to delete is a part property, do you also want to delete its type block?'
                choice = JOptionPane.showConfirmDialog(None, message, "Fix extra?", JOptionPane.YES_NO_OPTION); 
                if choice == JOptionPane.YES_OPTION:
                    MEM.removeElement(attr.getType())
                    gl.log('[Validation]: deleting ' + attr.getQualifiedName() + ' and its type')
            MEM.removeElement(attr)
        else:
            if check.isSelected():
                self.options['fixExtra'] = False
            doNotFix.append(attr)
        
    def fixErrors(self):
        if 'fixAll' not in self.options:
            choice = JOptionPane.showConfirmDialog(None, "There are errors, try to auto fix them all for this session?", "Fix all errors?", JOptionPane.YES_NO_OPTION); 
            if choice == JOptionPane.YES_OPTION:
                self.options['fixAll'] = True
            else:
                self.options['fixAll'] = False
        count = 0
        while True:    
            self.removeObsoleteRedefs()
            for cattr in self.aggregationErrs:
                self.fixAggregation(self.aggregationErrs[cattr], cattr, self.aggregationErrPartBlocks)
            for attr in self.dupRedefErrs:
                self.fixDupRedefErr(attr, self.dupRedefErrs[attr])
            for cattr in self.multipleRedefErrs:
                self.fixMultipleRedefErr(cattr, self.multipleRedefErrs[cattr])
            for cattr in self.wrongRedefErrs:
                self.fixWrongRedefErr(self.wrongRedefErrs[cattr], cattr, self.wrongRedefErrOk)
            for cattr in self.redefNameErrs:
                self.fixRedefNameErr(self.redefNameErrs[cattr], cattr, self.redefNameErrOk)
            for cattr in self.nonRedefNameErrs:
                gl.log('[Validation]: setting property of same name to redefine: ' + self.nonRedefNameErrs[cattr].getQualifiedName())
                cattr.getRedefinedProperty().add(self.nonRedefNameErrs[cattr])
            for cattr in self.wrongSharedType:
                attr = self.wrongSharedType[cattr]
                if 'mapping' in self.options and attr.getType() in self.options['mapping']:
                    gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to ' + self.options['mapping'][attr.getType()].getQualifiedName())
                    cattr.setType(self.options['mapping'][attr.getType()])
                else:
                    gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to ' + attr.getType().getQualifiedName())
                    cattr.setType(attr.getType())
            for cattr in self.wrongRedefineOnlyType:
                oldtype = cattr.getType()
                attr = self.wrongRedefineOnlyType[cattr]
                gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to ' + attr.getType().getQualifiedName())
                cattr.setType(attr.getType())
                if oldtype.getOwner() is cattr.getOwner():
                    MEM.removeElement(oldtype)
            for cattr in self.wrongRefType:
                attr = self.wrongRefType[cattr]
                gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to ' + attr.getType().getQualifiedName())
                cattr.setType(attr.getType())
            for cattr in self.constraintTypeErrs:
                attr = self.constraintTypeErrs[cattr]
                if 'mapping' in self.options:
                    self.fixWrongValueType(attr, cattr, self.wrongValueTypeOk)
                else:
                    gl.log('[Validation]: setting type of ' + cattr.getQualifiedName() + ' to ' + attr.getType().getQualifiedName())
                    cattr.setType(attr.getType())
            for cattr in self.wrongValueType:
                self.fixWrongValueType(self.wrongValueType[cattr], cattr, self.wrongValueTypeOk)
            for cattr in self.wrongPortType:
                self.fixWrongValueType(self.wrongPortType[cattr], cattr, self.wrongValueTypeOk)
            for cattr in self.wrongType:
                self.fixWrongType(self.wrongType[cattr], cattr, self.wrongTypeOk)
            for cattr in self.missingGeneralizationErrs:
                self.removeGeneralizations(cattr.getType())
                gl.log('[Validation]: adding generalization relationship from ' + cattr.getType().getQualifiedName() + ' to ' + self.missingGeneralizationErrs[cattr].getType().getQualifiedName())
                MDUtils.createGeneralizationInstance(self.missingGeneralizationErrs[cattr].getType(), cattr.getType())
            for cattr in self.missingAssociationGeneralizationErrs:
                self.removeGeneralizations(cattr.getAssociation())
                gl.log('[Validation]: adding generalization relationship from ' + cattr.getQualifiedName() + '\'s association to ' + self.missingAssociationGeneralizationErrs[cattr].getQualifiedName() + '\'s association')
                MDUtils.createGeneralizationInstance(self.missingAssociationGeneralizationErrs[cattr].getAssociation(), cattr.getAssociation())
            for cattr in self.addRedefine:
                attr = self.addRedefine[cattr]
                cattr.getRedefinedProperty().add(attr)
            
            #for attr in self.extraErrs:
            #    self.fixExtraErrs(attr, self.extraErrOk)
                
            self.clearErrors()
            self.checkAttrs()
            if self.hasErrors():
                self.printErrors()
                count = count + 1
                if count < 4:
                    continue
                else:
                    return False
            return True
        
    
    def hasErrors(self):
        #any errors besides missing attributes
        if len(self.dupRedefErrs) > 0 or \
            len(self.multipleRedefErrs) > 0 or \
            len(self.wrongRedefErrs) > 0 or \
            len(self.redefNameErrs) > 0 or \
            len(self.nonRedefNameErrs) > 0 or \
            len(self.aggregationErrs) > 0 or \
            len(self.wrongSharedType) > 0 or \
            len(self.wrongRefType) > 0 or \
            len(self.wrongValueType) > 0 or \
            len(self.wrongPortType) > 0 or \
            len(self.wrongType) > 0 or \
            len(self.constraintTypeErrs) > 0 or \
            len(self.missingGeneralizationErrs) > 0 or \
            len(self.missingAssociationGeneralizationErrs) > 0 or \
            len(self.obsoleteRedefs) > 0 or \
            len(self.wrongRedefineOnlyType) > 0 or \
            len(self.addRedefine) > 0:
            return True
        return False
    
    def hasMissingProperties(self):
        return len(self.missingRedefErrs) > 0
    
    def hasExtraProperties(self):
        return len(self.extraErrs) > 0
    
    def clearErrors(self):
        self.dupRedefErrs.clear()          #child have multiple properties that redefine
        self.multipleRedefErrs.clear()     #child have property that redefines multiple
        self.wrongRedefErrs.clear()        #child have property with same name that redefines wrong property
        del self.missingRedefErrs[:]     #child have missing properties based on generalization
        self.redefNameErrs.clear()         #child redefine property have different name
        self.nonRedefNameErrs.clear()      #child have a property with same name that's not redefined
        self.aggregationErrs.clear()       #aggregation type has changed (shared and composite)
        self.wrongSharedType.clear()
        self.wrongRefType.clear()
        self.wrongValueType.clear()
        self.wrongType.clear()
        self.wrongPortType.clear()
        del self.extraErrs[:]
        self.wrongRedefineOnlyType.clear()
        self.missingGeneralizationErrs.clear()
        self.missingAssociationGeneralizationErrs.clear()
        self.constraintTypeErrs.clear()
        self.obsoleteRedefs.clear()
        self.addRedefine.clear()
    
    def removeGeneralizations(self, c):
        remove = []
        remove.extend(c.getGeneralization())
        for i in range(0, len(remove)):
            MEM.removeElement(remove[i])
        
    def checkAttrs(self): 
        accountedFor = []
        for attr in self.inherited: 
            cattrs = SRUtils.getRedefinedInChild(attr, self.child) 
            accountedFor.extend(cattrs)
            if len(cattrs) == 0: #check by name match
                cattr = SRUtils.getNameInChild(attr.getName(), self.child)
                if cattr is not None:
                    accountedFor.append(cattr)
                    if not cattr.hasRedefinedProperty():
                        self.nonRedefNameErrs[cattr] = attr
                    else:
                        if cattr not in self.wrongRedefErrOk:
                            self.wrongRedefErrs[cattr] = attr
                else: 
                    self.missingRedefErrs.append(attr)
                continue
            if len(cattrs) > 1: #error for multiple properties redefining the same thing
                upper = attr.getUpper()
                if upper != -1 and len(cattrs) > upper:
                    self.dupRedefErrs[attr] = cattrs
                    continue
            for cattr in cattrs: #there should be only one? or maybe not if accounting for multiplicity
                redefs = cattr.getRedefinedProperty()
                if len(redefs) > 1: # error for multiple redfines one of which is the parent
                    bad = False
                    #if not SRUtils.haveCommonRedefinition(redefs):
                    #    self.multipleRedefErrs[cattr] = redefs
                    #    continue
                    for redef in redefs:
                        if not self.shouldRedefine(redef, cattr):
                            bad = True
                            break
                    if bad:
                        self.multipleRedefErrs[cattr] = redefs
                        continue
                if cattr.getName() != attr.getName(): #name error check
                    if len(cattrs) == 1 or not cattr.getName().startswith(attr.getName()):
                        if cattr not in self.redefNameErrOk:
                            self.redefNameErrs[cattr] = attr
                if StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.partPropS): # check if part propety
                    if StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.partPropS):
                        if attr.getType() is not None:
                            if cattr.getType() is not None:
                                if 'redefineOnly' in self.options and self.options['redefineOnly']:
                                    if attr.getType() is not cattr.getType():
                                        self.wrongRedefineOnlyType[cattr] = attr
                                else:
                                    if not isinstance(cattr.getType(), Class):
                                        gl.log("You have a part property that's not typed by a class: " + cattr.getQualifiedName())
                                    elif attr.getType() not in cattr.getType().getSuperClass():
                                        if attr.getType() is not cattr.getType():
                                            self.missingGeneralizationErrs[cattr] = attr
                                    if attr.getAssociation() is not None and cattr.getAssociation() is not None:
                                        if attr.getAssociation() not in cattr.getAssociation().getGeneral():
                                            self.missingAssociationGeneralizationErrs[cattr] = attr
                                    if self.options['checkOnly'] == True:
                                        check = SRChecker(cattr.getType(), self.options)
                                        check.checkAttrs()
                                        check.printErrors()
                                        if check.hasErrors() or check.partHasErr:
                                            self.partHasErr = True
                            else: # child isn't typed but parent is
                                self.wrongType[cattr] = attr 
                        else:
                            if cattr.getType() is not None: #parent isn't typed but child is
                                self.wrongType[cattr] = attr
                    elif StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.sharedPropS): #child is part while parent is shared
                        self.aggregationErrs[cattr] = attr
                    elif StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.refPropS):
                        self.aggregationErrs[cattr] = attr
                    else:
                        self.wrongType[cattr] = attr # no idea???
                elif StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.sharedPropS):
                    if StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.partPropS): # parent is part while child is shared
                        self.aggregationErrs[cattr] = attr
                    elif StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.refPropS):
                        self.aggregationErrs[cattr] = attr
                    elif not StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.sharedPropS): # child is not shared and not part????
                        self.wrongType[cattr] = attr
                    elif 'mapping' in self.options and attr.getType() in self.options['mapping']:
                        if cattr.getType() is not self.options['mapping'][attr.getType()]:
                            self.wrongSharedType[cattr] = attr
                    elif cattr.getType() is not attr.getType(): # child's share type is not the same as parents
                        self.wrongSharedType[cattr] = attr
                elif StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.refPropS):
                    if not StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.refPropS):
                        self.aggregationErrs[cattr] = attr
                    elif cattr.getType() is not attr.getType():
                        self.wrongRefType[cattr] = attr
                elif StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.valuePropS):
                    if not StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.valuePropS):
                        self.wrongType[cattr] = attr # totally wrong property tpe
                    elif 'mapping' in self.options and attr.getType() in self.options['mapping']:
                        if cattr.getType() is not self.options['mapping'][attr.getType()]:
                            self.wrongValueType[cattr] = attr
                    elif cattr.getType() is not attr.getType():
                        if cattr not in self.wrongValueTypeOk:
                            self.wrongValueType[cattr] = attr # checks to see value types are the same
                    #check to see if value property  is still the same
                    attrvalue = attr.getDefault()
                    cattrvalue = cattr.getDefault()
                    if attrvalue == cattrvalue:
                        self.sameValue[cattr] = attr
                    elif cattrvalue is None or cattrvalue.lower() == 'nan':
                        self.sameValue[cattr] = attr
                elif StereotypesHelper.hasStereotypeOrDerived(cattr, SRUtils.consPropS):
                    if not StereotypesHelper.hasStereotypeOrDerived(attr, SRUtils.consPropS):
                        self.wrongType[cattr] = attr
                    elif 'mapping' in self.options and attr.getType() in self.options['mapping']:
                        if cattr.getType() is not self.options['mapping'][attr.getType()]:
                            self.constraintTypeErrs[cattr] = attr
                    elif attr.getType() != cattr.getType():
                        if cattr not in self.constraintTypeErrOk:
                            self.constraintTypeErrs[cattr] = attr
                elif isinstance(attr.getType(), Enumeration): # enumeration types are treated the same as value properties
                    if 'mapping' in self.options and attr.getType() in self.options['mapping']:
                        if cattr.getType() is not self.options['mapping'][attr.getType()]:
                            self.wrongValueType[cattr] = attr
                    elif attr.getType() is not cattr.getType():
                        if cattr not in self.wrongValueTypeOk:
                            self.wrongValueType[cattr] = attr
                elif isinstance(attr, Port):
                    if isinstance(cattr, Port):
                        if 'mapping' in self.options and attr.getType() in self.options['mapping']:
                            if cattr.getType() is not self.options['mapping'][attr.getType()]:
                                self.wrongPortType[cattr] = attr
                        elif attr.getType() is not cattr.getType():
                            if cattr not in self.wrongValueTypeOk:
                                self.wrongPortType[cattr] = attr
                    else:
                        self.wrongType[cattr] = attr
                        
        self.checkExtras(accountedFor)
        self.checkObsoleteRedefs()
        
    def checkExtras(self, accountedFor):
        if Rolester is not None:
            if not StereotypesHelper.hasStereotype(self.child, Rolester):
                for attr in self.child.getOwnedAttribute():
                    if attr not in accountedFor and attr not in self.extraErrOk:
                        found = False
                        asso = attr.getAssociation()
                        if asso is not None:
                            gens = asso.getGeneral()
                            for gen in gens:
                                for maybeProp in gen.getMemberEnd():
                                    if maybeProp in self.inherited:
                                        self.addRedefine[attr] = maybeProp
                                        found = True
                                    if found:
                                        break
                                if found:
                                    break
                        if not found:
                            self.extraErrs.append(attr)
                if self.options['checkOnly'] == True:
                    for part in self.extraErrs:
                        if part.getType() is not None and StereotypesHelper.hasStereotypeOrDerived(part, SRUtils.partPropS):
                            check = SRChecker(part.getType(), self.options)
                            check.checkAttrs()
                            check.printErrors()
                            if check.hasErrors() or check.partHasErr:
                                self.partHasErr = True
        else:
            for attr in self.child.getOwnedAttribute():
                if attr not in accountedFor and attr not in self.extraErrOk:
                    found = False
                    asso = attr.getAssociation()
                    if asso is not None:
                        gens = asso.getGeneral()
                        for gen in gens:
                            for maybeProp in gen.getMemberEnd():
                                if maybeProp in self.inherited:
                                    self.addRedefine[attr] = maybeProp
                                    found = True
                                if found:
                                    break
                            if found:
                                break
                    if not found:
                        self.extraErrs.append(attr)
            if self.options['checkOnly'] == True:
                for part in self.extraErrs:
                    if part.getType() is not None and StereotypesHelper.hasStereotypeOrDerived(part, SRUtils.partPropS):
                        check = SRChecker(part.getType(), self.options)
                        check.checkAttrs()
                        check.printErrors()
                        if check.hasErrors() or check.partHasErr:
                            self.partHasErr = True
                            
    def checkObsoleteRedefs(self):
        if Rolester is not None:   
            if not StereotypesHelper.hasStereotype(self.child, Rolester):
                for attr in self.child.getOwnedAttribute():
                    redefs = []
                    for redef in attr.getRedefinedProperty():
                        if redef not in self.inherited:
                            redefs.append(redef)
                    if len(redefs) > 0:
                        self.obsoleteRedefs[attr] = redefs
        else:
            for attr in self.child.getOwnedAttribute():
                redefs = []
                for redef in attr.getRedefinedProperty():
                    if redef not in self.inherited:
                        redefs.append(redef)
                if len(redefs) > 0:
                    self.obsoleteRedefs[attr] = redefs
            

    def removeObsoleteRedefs(self):
        if Rolester is not None:
            if not StereotypesHelper.hasStereotype(self.child, rolester):
                for attr in self.obsoleteRedefs:
                    for redef in self.obsoleteRedefs[attr]:
                        gl.log('[Validation] Removing ' + redef.getQualifiedName() + ' from ' + attr.getQualifiedName() + ' \'s redefined list.')
                    attr.getRedefinedProperty().remove(redef)
        else:
            for attr in self.obsoleteRedefs:
                for redef in self.obsoleteRedefs[attr]:
                    gl.log('[Validation] Removing ' + redef.getQualifiedName() + ' from ' + attr.getQualifiedName() + ' \'s redefined list.')
                attr.getRedefinedProperty().remove(redef)
                    
                

def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    try:
        SessionManager.getInstance().createSession("validate")
        checker = SRChecker(selected, {'checkOnly':True})  
        checker.checkAttrs()
        checker.printErrors()
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)