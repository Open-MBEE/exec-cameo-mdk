from java.lang import *
from com.nomagic.magicdraw.ui.actions import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.actions import *
from javax.swing import *
from javax.swing.table import *
from com.nomagic.actions import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces import *
from com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures import *



import traceback
import sys

class SRAction(MDAction):
    def __init__(self, name, mode):
        MDAction.__init__( self,"", name, None, None )
        self.mode = mode

    def actionPerformed(self, event):
        gl = Application.getInstance().getGUILog()
        try:
            mod = __import__(self.getName().replace(" ", "_"))
            reload(mod)
            mod.run(self.mode)
        except:
            exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
            gl.log("*** EXCEPTION:")
            messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
            for message in messages:
                gl.log(message)
        
    
class DiagramConfigurator(DiagramContextAMConfigurator):

    def configure(self, manager, diagram, selected, requestor):
        try:
            if manager.getActionFor("Systems Reasoner") is None:
                if requestor is not None and isinstance(requestor.getElement(), Class):
                    category = MDActionsCategory("Systems Reasoner", "Systems Reasoner")
                    category.setNested(True)
                    category.addAction(SRAction("Specialize", 'd'))
                    category.addAction(SRAction("Specialize Many", 'd'))
                    category.addAction(SRAction("DeSpecialize", 'd'))
                    category.addAction(SRAction("Validate Structure", 'd'))
                    category.addAction(SRAction("Create Instances", 'd'))
                    category.addAction(SRAction("Make Copy", 'd'))
                    category.addAction(SRAction("AnalyzeProduct", 'd'))
                    category.addAction(SRAction("Detect Compositional Cycles", 'd'))
                    category.addAction(SRAction("NameParts", 'd'))
            #category.addAction(ExampleAction("Sync Values", 'd'))
                    manager.addCategory(0, category)
                elif requestor is not None and isinstance(requestor.getElement(), Interface):
                    category = MDActionsCategory("Systems Reasoner", "Systems Reasoner")
                    category.setNested(True)
                    category.addAction(SRAction("Specialize", 'd'))
                    manager.addCategory(0, category)
                elif requestor is not None and isinstance(requestor.getElement(),Connector):
                    category = MDActionsCategory("Systems Reasoner", "Systems Reasoner")
                    category.setNested(True)
                    category.addAction(SRAction("EHMChangeToPort", 'd'))
                    manager.addCategory(0, category)
                elif requestor is not None and isinstance(requestor.getElement(),Property):
                    category = MDActionsCategory("Systems Reasoner", "Systems Reasoner")
                    category.setNested(True)
                    category.addAction(SRAction("EHMMovePart", 'd'))
                    manager.addCategory(0, category)
        except:
            print "exception"

    def getPriority(self):
        return AMConfigurator.LOW_PRIORITY

class BrowserContextConfigurator(BrowserContextAMConfigurator):
    def configure(self, manager, tree):
        try:
            if manager.getActionFor("Systems Reasoner") is None:
                if tree.getSelectedNode() is not None:
                    category = MDActionsCategory("Systems Reasoner", "Systems Reasoner")
                    if isinstance(tree.getSelectedNode().getUserObject(), Class):
                        category.setNested(True)
                        category.addAction(SRAction("Specialize", 'b'))
                        category.addAction(SRAction("Specialize Many", 'b'))
                        category.addAction(SRAction("DeSpecialize", 'b'))
                        category.addAction(SRAction("Validate Structure", 'b'))
                        category.addAction(SRAction("Create Instances", 'b'))
                        category.addAction(SRAction("Make Copy", 'b'))
                        category.addAction(SRAction("Detect Compositional Cycles", 'b'))
                        category.addAction(SRAction("OrphanFixer",'b'))
                    if isinstance(tree.getSelectedNode().getUserObject(), Interface):
                        category.setNested(True)
                        category.addAction(SRAction("Specialize", 'b'))
                    if isinstance(tree.getSelectedNode().getUserObject(), InstanceSpecification):
                        category.setNested(True)
                        category.addAction(SRAction("Sync Slot Values", 'b'))
                    if isinstance(tree.getSelectedNode().getUserObject(), Package):
                        category.setNested(True)
                        category.addAction(SRAction("EHMSupplies", 'b'))
                        category.addAction(SRAction("Package Clone", 'b'))
                        category.addAction(SRAction("OrphanFixer",'b'))
                        category.addAction(SRAction("OrphanFinal",'b'))
                    manager.addCategory(0, category)
        except:
            print "exception"
            exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
            messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
            for message in messages:
                print message


    def getPriority(self):
        return AMConfigurator.LOW_PRIORITY

#ActionsConfiguratorsManager.getInstance().addContainmentBrowserContextConfigurator(BrowserContextConfigurator())
#ActionsConfiguratorsManager.getInstance().addBaseDiagramContextConfigurator("SysML Block Definition Diagram", DiagramConfigurator())
#ActionsConfiguratorsManager.getInstance().addBaseDiagramContextConfigurator("SysML Internal Block Diagram", DiagramConfigurator())

#ActionsConfiguratorsManager.getInstance().addBaseDiagramContextConfigurator("Class Diagram", DiagramConfigurator())

