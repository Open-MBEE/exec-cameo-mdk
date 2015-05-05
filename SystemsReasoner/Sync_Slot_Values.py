from java.lang import *
from com.nomagic.magicdraw.uml.symbols import *
from com.nomagic.magicdraw.core import Application
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.openapi.uml import SessionManager
from com.nomagic.magicdraw.openapi.uml import ModelElementsManager
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper
from com.nomagic.magicdraw.ui.dialogs import *
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import LiteralString
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import InstanceValue
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import LiteralBoolean
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import LiteralInteger
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import InstanceSpecification



from javax.swing import JOptionPane

from java.util import Date
from java.text import DateFormat
from java.text import SimpleDateFormat

from com.nomagic.magicdraw.teamwork.application import TeamworkUtils
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

def syncSlotValues(instance):
    for slot in instance.getSlot():
        feature = slot.getDefiningFeature()
        for value in slot.getValue():
            if isinstance(value, InstanceValue):
                syncSlotValues(value.getInstance())
            else:
                feature.setDefaultValue(SRUtils.cloneValueSpec(value))
            
                
def run(mode):
    selected = None
    if mode == 'b':
        selected = Application.getInstance().getMainFrame().getBrowser().getActiveTree().getSelectedNode().getUserObject()
    if mode == 'd':
        selected = Application.getInstance().getProject().getActiveDiagram().getSelected().get(0).getElement()
    if not isinstance(selected, InstanceSpecification):
        gl.log("please selected an instance specification!")
        return
    try:
        SessionManager.getInstance().createSession("generalize")
        syncSlotValues(selected)
        SessionManager.getInstance().closeSession()
    except:
        SessionManager.getInstance().cancelSession()
        exceptionType, exceptionValue, exceptionTraceback = sys.exc_info()
        messages=traceback.format_exception(exceptionType, exceptionValue, exceptionTraceback)
        for message in messages:
            gl.log(message)