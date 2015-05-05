from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.magicdraw.openapi.uml import PresentationElementsManager

from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mddependencies import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions import *
from com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities import *
from com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities import *
from com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows import *
from com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities import *
from com.nomagic.uml2.ext.magicdraw.interactions.mdfragments import *
from com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions import *
from com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines import *
from com.nomagic.magicdraw.uml.symbols.layout import *
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper

from gov.nasa.jpl.mgss.mbee.docgen.validation import ValidationRule
from gov.nasa.jpl.mgss.mbee.docgen.validation import ValidationSuite
from gov.nasa.jpl.mgss.mbee.docgen.validation import ViolationSeverity

import sys, traceback

from gov.nasa.jpl.mbee.lib import Utils

gl = Application.getInstance().getGUILog()

# logging levels as globals...
ERROR   = 0
WARNING = 1
INFO    = 2
CONSOLE = 3 # always print console
LOG_STRINGS         = ["ERROR", "WARNING", "INFO", "CONSOLE"]
DGVIEW_ST           = "view"
HYPERLINK_OWNER_ST  = "HyperlinkOwner"
HYPERLINK_MODEL_ST  = "hyperlinkModelActive"
DG_STS              = ["NoSection", "First", "Next"]

class DocGenAutoOrganize:
    
    def __init__(self):
        '''
        Constructor initializes member variables
        '''
        self.debugLevel = INFO

        # Grab all project and application specific managers
        self.gl = Application.getInstance().getGUILog()
        self.project = Application.getInstance().getProject()
        self.ef = self.project.getElementsFactory()
        self.mem = ModelElementsManager.getInstance()
        self.pem = PresentationElementsManager.getInstance()
        
        # check to see if called from user script then get DG to validate
        self.isUserScript = self.checkIfUserScript()
        self.getRootNode()
        
        self.log(CONSOLE, "Running DocGenAutoOrganize with debug level %d" % (self.debugLevel))

    
    def checkIfUserScript(self):
        '''
        Simple method that checks for scriptInput variable that indicates it was called
        as a User Script.
        
        @return: TRUE if called from User Script, FALSE if called from Macro engine
        '''
        # only way to check for existence of variable is NameError exception
        try:
            inputs = scriptInput["DocGenTargets"]
            self.fixMode = scriptInput["FixMode"]
        except NameError:
            return False
        return True
    
    
    def getRootNode(self):
        '''
        Gets the root node (view diagram) based on User Script inputs or the macro (which
        gets the selected node in the containment tree)
        '''
        if self.isUserScript:
            self.root = scriptInput["DocGenTargets"][0]
        else:
            if Utils.getUserYesNoAnswer("Select YES to check rules or NO to enforce rules"):
                self.fixMode = "FixNone"
            else:
                self.fixMode = "FixSelected"
            self.root = Application.getInstance().getMainFrame().getBrowser().getContainmentTree().getSelectedNode()
            if self.root:
                self.root = self.root.getUserObject()
    
    
    def log(self, level, message):
        '''
        Debugging feature that only logs when self.debugLevel is greater than the log message level
        
        @param level:    Level of the log message
        @param message:  String to be logged
        '''
        if self.debugLevel >= level or level == CONSOLE:
            self.gl.log("[%s] %s" % (LOG_STRINGS[level], message))

    def getViewElements(self, parent, views=set()):
        '''
        Recursive method for gathering all the views in a view.
        
        @param parent:             The parent view to find all views in
        @param views:              The set of views found so far in the parent package
        @return:                   The set of all views found in the parent package
        '''
        views.add(parent)
        
        for child in parent.getOwnedElement():
            if StereotypesHelper.hasStereotypeOrDerived(child, "View"):
                views.update(self.getViewElements(child, views))
        
        for dep in parent.getClientDependency():
            for st in DG_STS:
                if StereotypesHelper.hasStereotypeOrDerived(dep, st):
                    suppliers = dep.getSupplier()
                    for supplier in suppliers: 
                        self.log(INFO, "found supplier view %s" % (supplier.getName()))
                        views.update(self.getViewElements(supplier, views))
                    break
        return views


    def generateConformsDiagram(self, fix=False):
        '''
        Generates the conforms diagram recursively based on the package structure
        '''
        diagramName = self.root.getName() + "ViewpointView"
        
        # lets look for the diagram first
        diagram = None
        for oe in self.root.getOwnedElement():
            if isinstance(oe, Diagram):
                if oe.getName() == (diagramName):
                    diagram = oe
                    break
        
        # create diagram if necessary
        if diagram:
            diagramPel = self.project.getDiagram(diagram)
        else:
            self.log(WARNING, "No Viewpoint View diagram found")
            if not fix:
                return
            else:
                self.log(WARNING, "Creating Viewpoint View diagram")
                # create the diagram and get presentation element
                diagram = self.mem.createDiagram("SysML Package Diagram", self.root)
                diagram.setName(diagramName)
                diagram.setOwner(self.root)
                diagramPel = self.project.getDiagram(diagram)
        
        # lets grab all the views
        views = self.getViewElements(self.root, set())

        # lets add get all the conforms relationships
        viewpoints = set()
        dependencies = []
        for view in views:
            self.log(INFO, "found view: %s" % (view.getName()))
            for dep in view.getClientDependency():
                if StereotypesHelper.hasStereotypeOrDerived(dep, "Conform"):
                    viewpoints.update(dep.getSupplier())
                    dependencies.append(dep)
                    break # since only one viewpoint per view (TODO: Add check in)
        
        # lets consolidate all the elements        
        elements = set()
        elements.update(views)
        elements.update(viewpoints)
        elements.update(dependencies)
        
        elementsToAdd        = []
        elementsAlreadyExist = []
        
        # lets grab all the elements and filter
        diagramElements = diagramPel.getUsedModelElements(False)
        for element in elements:
            if element not in diagramElements:
                elementsToAdd.append(element)
            else:
                self.log(INFO, "[%s] already in diagram" % (element.getName()))
                elementsAlreadyExist.append(element)

        # add elements first
        for element in elementsToAdd:
            self.log(WARNING, "%s missing element %s" % ("adding" if fix else "", element.getName()))
            if fix:
                if not isinstance(element, Dependency):
                    self.pem.createShapeElement(element, diagramPel)
                    
        # add paths (need the elements first so they show up)
        for element in elementsToAdd:
            if isinstance(element, Dependency):
                for supplier in element.getSupplier():
                    supplierPel = diagramPel.findPresentationElement(supplier, None)
                    for client in element.getClient():
                        clientPel = diagramPel.findPresentationElement(client, None)
                        self.log(WARNING, "%s missing conforms %s to %s" % ("adding" if fix else "", supplier.getName(), client.getName()))
                        if supplierPel and clientPel and fix:
                            self.pem.createPathElement(element, clientPel, supplierPel)
        
        # remove extraneous elements
        for diagramElement in diagramElements:
            if diagramElement not in elements:
                self.log(WARNING, "%s extraneous element found: %s" % ("deleting" if fix else "", diagramElement.getName()))
                if fix:
                     self.pem.deletePresentationElement(diagramPel.findPresentationElement(diagramElement, None))
        
        # layout diagram if fixed
        if len(elementsToAdd) > 0:
            diagramPel.open()
            if not diagramPel.layout(True, HierarchicDiagramLayouter()):
                self.log(ERROR, "Could not layout diagram")
            else:
                self.log(INFO, "Diagram laid out successfully")


    def checkAndFix(self, fix=False):
        '''
        Checks and fixes a View by creating a hyperlinked diagram that includes all the imports,
        conforms, First, NoSections in the diagram.
        
        @param fix:    FALSE if just checking for conformance, TRUE if enforcing conformance
        '''
        diagramName = self.root.getName() + "DocumentView"
        
        # grab the hyperlinked diagram if any
        diagram = None
        stereotype = StereotypesHelper.getStereotype(self.project, HYPERLINK_OWNER_ST)
        hyperlinks = StereotypesHelper.getStereotypePropertyValue(self.root, stereotype, HYPERLINK_MODEL_ST)
        for hyperlink in hyperlinks:
            if isinstance(hyperlink, Diagram):
                diagram = hyperlink
                self.log(INFO, "Found hyperlink to diagram: %s" % (hyperlink.getName()))
                if fix:
                    if diagram.getOwner() != self.root:
                        self.log(WARNING, "Setting %s as owner of active hyperlinked diagram" % (self.root.getName()))
                        diagram.setOwner(self.root)
                    if diagram.getName() != self.root.getName():
                        self.log(WARNING, "Setting name of active hyperlinked diagram to %s" % (self.root.getName()))
                        diagram.setName(diagramName)

        # get or create the DiagramPresentationElement if it doesn't exist
        if diagram:
            diagramPel = self.project.getDiagram(diagram)
        else:
            self.log(WARNING, "No hyperlink to diagram found")
            if not fix:
                return
            else:
                self.log(WARNING, "Creating diagram and setting it as active hyperlink")
                # create the diagram and get presentation element
                diagram = self.mem.createDiagram("SysML Package Diagram", self.root)
                diagram.setName(diagramName)
                diagram.setOwner(self.root)
                diagramPel = self.project.getDiagram(diagram)
                
                # set the active hyperlink
                StereotypesHelper.addStereotype(self.root, stereotype)
                slot = StereotypesHelper.getSlot(self.root, stereotype, HYPERLINK_MODEL_ST, True, False)
                if slot:
                    slotValues = slot.getValue()
                    elementValue = self.project.getElementsFactory().createElementValueInstance()
                    elementValue.setElement(diagram)
                    slotValues.add(elementValue)
        
        # get all the diagram elements from the diagram
        diagramElements = diagramPel.getUsedModelElements(False)

        # grab all the DG dependencies of interest
        dependencies  = []
        for dep in self.root.getClientDependency():
            for st in DG_STS:
                if StereotypesHelper.hasStereotypeOrDerived(dep, st):
                    dependencies.append(dep)
                    break
                        
        # convert the dependencies into a list of the suppliers (e.g., elements)
        suppliers = set()
        for dep in dependencies:
            for supplier in dep.getSupplier():
                suppliers.add(supplier)

        # get all package and element imports
        elementImports = self.root.getElementImport()
        for elementImport in elementImports:
            suppliers.add(elementImport.getImportedElement())
        packageImports = self.root.getPackageImport()
        for packageImport in packageImports:
            suppliers.add(packageImport.getImportedPackage())
        
        # check for self and add if necessary
        fixed = False
        if self.root in diagramElements:
            self.log(INFO, "root element %s already in diagram" % (self.root.getName()))
        else:
            self.log(WARNING, "%s missing root element %s" % ("adding" if fix else "", self.root.getName()))
            if fix:
                self.pem.createShapeElement(self.root, diagramPel)
                fixed = True
        
        # check for missing suppliers in diagram
        for supplier in filter(lambda x: hasattr(x, "getName"), suppliers):
            if supplier in diagramElements:
                self.log(INFO, "%s already in diagram" % (supplier.getName()))
            else:
                self.log(WARNING, "%s missing supplier: %s" % ("adding" if fix else "", supplier.getName()))
                if fix:
                    self.pem.createShapeElement(supplier, diagramPel)
                    fixed = True
                            
        # check for extraneous diagram elements (reload the diagram elements)
        diagramElements = diagramPel.getUsedModelElements(False)
        for diagramElement in diagramElements:
            # note that the diagramElements also include itself for some reason (hence the != diagram)
            if diagramElement not in suppliers and diagramElement != diagram and diagramElement != self.root and diagramElement not in dependencies and diagramElement not in elementImports and diagramElement not in packageImports:
                name = ""
                if hasattr(diagramElement, "getName"):
                    name = self.log(WARNING, "%s extraneous diagram element: %s" % ("removing" if fix else "", diagramElement.getName()))
                self.log(WARNING, "%s extraneous diagram element: %s" % ("removing" if fix else "", name))
                if fix:
                    self.pem.deletePresentationElement(diagramPel.findPresentationElement(diagramElement, None))

        # add in the dependencies, package and element imports paths
        for dep in dependencies:
            if dep not in diagramElements:
                for ds in dep.getSupplier():
                    dsPel = diagramPel.findPresentationElement(ds, None)
                    for dc in dep.getClient():
                        dcPel = diagramPel.findPresentationElement(dc, None)
                        if dsPel and dcPel:
                            self.log(WARNING, "%s missing dependency from %s to %s" % ("adding" if fix else "", ds.getName(), dc.getName()))
                            if fix:
                                self.pem.createPathElement(dep, dcPel, dsPel)
                                fixed = True
        for ei in elementImports:
            if ei not in diagramElements:
                self.log(WARNING, "%s missing element import %s" % ("adding" if fix else "", ei.getImportedElement().getName()))
                if fix:
                    clientPel = diagramPel.findPresentationElement(self.root, None)
                    supplierPel = diagramPel.findPresentationElement(ei.getImportedElement(), None)
                    self.pem.createPathElement(ei, clientPel, supplierPel)
                    fixed = True
        for pi in packageImports:
            if pi not in diagramElements:
                self.log(WARNING, "%s missing element import %s" % ("adding" if fix else "", pi.getImportedPackage().getName()))
                if fix:
                    clientPel = diagramPel.findPresentationElement(self.root, None)
                    supplierPel = diagramPel.findPresentationElement(pi.getImportedPackage(), None)
                    self.pem.createPathElement(pi, clientPel, supplierPel)
                    fixed = True

        # only re-lay out if a fix was made
        if fixed:
            diagramPel.open()
            if not diagramPel.layout(True, OrthogonalDiagramLayouter()):
                self.log(ERROR, "Could not layout diagram")
            else:
                self.log(INFO, "Diagram laid out successfully")
                
        self.generateConformsDiagram(fix)
                            
                
    def run(self):
        '''
        Executes the auto-organization of the view
        '''
        # check that a valid view (formerly DGView) is selected
        if not self.root:
            self.log(ERROR, "Nothing selected\n\tPlease select a <<View>> in the containment tree")
            return -1
        if not StereotypesHelper.hasStereotypeOrDerived(self.root, DGVIEW_ST) and not StereotypesHelper.hasStereotypeOrDerived(self.root, "View"):
            self.log(ERROR, "Please select a <<View>> in the containment tree")
            return -1
        else:
            self.log(INFO, "User selected <<View>>: %s" % (self.root.getName()))

        # see whether or not to fix the diagram
        fix = False if self.fixMode == "FixNone" else True

        # User script already has Session Manager
        if self.isUserScript:
            self.checkAndFix(fix)
        else:
            try:
                SessionManager.getInstance().createSession("AutoOrganizing document view")
                self.checkAndFix(fix)
                SessionManager.getInstance().closeSession()
            except:
                SessionManager.getInstance().cancelSession()
                exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
                messages = traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
                for message in messages:
                    self.log(ERROR, message)


# wrap the execution in try block so we can see any exceptions
try:            
    dgv = DocGenAutoOrganize()
    dgv.run()
except:
    exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
    messages = traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
    for message in messages:
        gl.log(message)


