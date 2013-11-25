from gov.nasa.jpl.mbee.lib import Utils
from gov.nasa.jpl.mbee import DocGen3Profile
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBParagraph
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBSection

import messaging

from com.nomagic.magicdraw.core import Application
from com.nomagic.magicdraw.core import Project
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.uml2.ext.jmi.helpers import ModelHelper

targets = scriptInput['DocGenTargets']

output = []

thisProject = Application.getInstance().getProject()

INSPIREReqS = StereotypesHelper.getStereotype(thisProject, "INSPIRE.Requirement")

for t in targets:

    messaging.DBG(t.getName())
    
    idSlot = StereotypesHelper.getSlot(t, INSPIREReqS, "Id", False)
    coreText = ''
    for myVal in idSlot.getValue():
        coreText = myVal.getValue()

    output.append(DBParagraph(coreText + ' ' + t.getName()))

    textSlot = StereotypesHelper.getSlot(t, INSPIREReqS, "Text", False)
    coreText = ''
    for myVal in textSlot.getValue():
        coreText = myVal.getValue()
        
    output.append(DBParagraph(coreText))

    textSlot = StereotypesHelper.getSlot(t, INSPIREReqS, "Rationale", False)
    coreText = ''
    for myVal in textSlot.getValue():
        coreText = myVal.getValue()

    output.append(DBParagraph("<emphasis> Rationale: " + coreText + "</emphasis>"))
    output.append(DBParagraph(''))


scriptOutput = {"DocGenOutput":output}

# code stubbbbbbsssss
#output.append(DBParagraph(coreText + ' <emphasis>' + t.getName()+'</emphasis>')))