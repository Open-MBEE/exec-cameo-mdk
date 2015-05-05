from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import Enumeration
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

gl = Application.getInstance().getGUILog()
project = Application.getInstance().getProject()
ef = project.getElementsFactory()
mem = ModelElementsManager.getInstance()
sm = SessionManager.getInstance()

class EHM:
    def __init__(self, folder):
        self.errors = {} # {workpackage: {'assos':[properties that need to be converted...], 'delete':[properties that need to be deleted...], 'duplicate':[duplicate dependencies...]
        self.folder = folder
        self.reverse = {} # {product:[wp,...]}
        self.missing = []
        
    def findErrors(self):
        workpackages = self.findWorkPackages()
        for wp in workpackages:
            errors = {}
            self.errors[wp] = errors
            self.fillwperrors(wp, errors)
        products = self.findProducts()
        self.fillProductErrors(products)
        return self.errors
    
    def fillProductErrors(self, products):
        for p in products:
            if p not in self.reverse:
                self.missing.append(p)
        
    def fillwperrors(self, wp, errors):
        found = []
        assos = []
        wpassos = []
        delete = []
        keep = []
        duplicate = []
        wpduplicate = []
        for p in wp.getOwnedAttribute():
            ptype = p.getType()
            if ptype is not None and StereotypesHelper.hasStereotypeOrDerived(ptype, 'Product'):
                existing = self.getSupplies(wp, ptype)
                if len(existing) == 0 and ptype not in found:
                    assos.append(p)
                    found.append(ptype)
                elif ptype in found:
                    delete.append(p)
                elif len(existing) == 1:
                    delete.append(p)
                    found.append(ptype)
                    keep.extend(existing)
                elif len(existing) > 1:
                    assos.append(p)
                    found.append(ptype)
                    duplicate.extend(existing)
                if ptype not in self.reverse:
                    self.reverse[ptype] = [wp]
                elif wp not in self.reverse[ptype]:
                    self.reverse[ptype].append(wp)
            if ptype is not None and StereotypesHelper.hasStereotypeOrDerived(ptype, 'Work Package'):
                existing = self.getAuthorizes(wp, ptype)
                if len(existing) == 0 and ptype not in found:
                    wpassos.append(p)
                    found.append(ptype)
                elif ptype in found:
                    delete.append(p)
                elif len(existing) == 1:
                    delete.append(p)
                    found.append(ptype)
                    keep.extend(existing)
                elif len(existing) > 1:
                    wpassos.append(p)
                    found.append(ptype)
                    wpduplicate.extend(existing)
        for rel in wp.get_directedRelationshipOfSource():
            supplier = ModelHelper.getSupplierElement(rel)
            if StereotypesHelper.hasStereotypeOrDerived(rel, 'supplies') and StereotypesHelper.hasStereotypeOrDerived(supplier, 'Product'):
                if supplier not in found:
                    found.append(supplier)
                elif rel not in duplicate and rel not in keep:
                    duplicate.append(rel)
                if supplier not in self.reverse:
                    self.reverse[supplier] = [wp]
                elif wp not in self.reverse[supplier]:
                    self.reverse[supplier].append(wp)
            if StereotypesHelper.hasStereotypeOrDerived(rel, 'authorizes') and StereotypesHelper.hasStereotypeOrDerived(supplier, 'Work Package'):
                if supplier not in found:
                    found.append(supplier)
                elif rel not in duplicate and rel not in keep:
                    duplicate.append(rel)
        errors['assos'] = assos
        errors['delete'] = delete
        errors['duplicate'] = duplicate
        errors['wpassos'] = wpassos
        errors['wpduplicate'] = wpduplicate
                
                    
    
    def getAuthorizes(self, wp, ptype):
        res = []
        for rel in wp.get_directedRelationshipOfSource():
            if StereotypesHelper.hasStereotypeOrDerived(rel, 'authorizes') and ModelHelper.getSupplierElement(rel) is ptype:
                res.append(rel)
        return res
            
    def getSupplies(self, wp, ptype):
        res = []
        for rel in wp.get_directedRelationshipOfSource():
            if StereotypesHelper.hasStereotypeOrDerived(rel, 'supplies') and ModelHelper.getSupplierElement(rel) is ptype:
                res.append(rel)
        return res
    
    def findWorkPackages(self):
        return MDUtils.collectElementsByStereotypes(self.folder, ['Work Package'])
    
    def findProducts(self):
        return MDUtils.collectElementsByStereotypes(self.folder, ['Product'])
    
    def printErrors(self):
        for wp in self.errors:
            gl.log("Printing Errors for WorkPackage " + wp.getQualifiedName() + ":")
            errors = self.errors[wp]
            if len(errors['assos']) > 0:
                gl.log("\tProperties to be converted to supplies:")
                for p in errors['assos']:
                    gl.log("\t\t" + p.getQualifiedName())
            if len(errors['wpassos']) > 0:
                gl.log("\tProperties to be converted to authorizes:")
                for p in errors['wpassos']:
                    gl.log("\t\t" + p.getQualifiedName())
            if len(errors['delete']) > 0:
                gl.log("\tProperties to be deleted:")
                for p in errors['delete']:
                    gl.log("\t\t" + p.getQualifiedName())
            if len(errors['duplicate']) > 0:
                gl.log("\tDuplicate supplies relationships to be deleted (to):")
                for rel in errors['duplicate']:
                    gl.log("\t\t" + ModelHelper.getSupplierElement(rel).getQualifiedName())
            if len(errors['wpduplicate']) > 0:
                gl.log("\tDuplicate authorizes relationships to be deleted (to):")
                for rel in errors['wpduplicate']:
                    gl.log("\t\t" + ModelHelper.getSupplierElement(rel).getQualifiedName())
        gl.log("Products with multiple work packages associated:")
        for product in self.reverse:
            if len(self.reverse[product]) > 1:
                gl.log("\t" + product.getQualifiedName())
                for wp in self.reverse[product]:
                    gl.log("\t\t" + wp.getQualifiedName())
        gl.log("Products with no work package associated:")
        for p in self.missing:
            gl.log("\t" + p.getQualifiedName())
                
    def fixErrors(self):
        for wp in self.errors:
            if not wp.isEditable():
                gl.log("ERROR: " + wp.getQualifiedName() + " is not editable")
                continue
            errors = self.errors[wp]
            for p in errors['delete']:
                mem.removeElement(p)
            for d in errors['duplicate']:
                mem.removeElement(d)
            for d in errors['wpduplicate']:
                mem.removeElement(d)
            for p in errors['assos']:
                ptype = p.getType()
                depen = ef.createDependencyInstance()
                ModelHelper.setClientElement(depen, wp)
                ModelHelper.setSupplierElement(depen, ptype)
                depen.setOwner(wp.getOwner())
                StereotypesHelper.addStereotypeByString(depen, "supplies") #shodul make sure this is the right stereotype
                mem.removeElement(p)
            for p in errors['wpassos']:
                ptype = p.getType()
                depen = ef.createDependencyInstance()
                ModelHelper.setClientElement(depen, wp)
                ModelHelper.setSupplierElement(depen, ptype)
                depen.setOwner(wp.getOwner())
                StereotypesHelper.addStereotypeByString(depen, "authorizes") #shodul make sure this is the right stereotype
                mem.removeElement(p)
   


def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    try:
        validator = EHM(selected)
        errors = validator.findErrors()
        mul = JOptionPane.showConfirmDialog(None, "Validate Only?", "Validate Only?", JOptionPane.YES_NO_OPTION);
        validator.printErrors()
        if mul == JOptionPane.YES_OPTION:
            return
        SessionManager.getInstance().createSession("validate")
        validator.fixErrors()
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)