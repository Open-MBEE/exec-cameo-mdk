from gov.nasa.jpl.mgss.mbee.docgen.table import EditableTable
from gov.nasa.jpl.mgss.mbee.docgen.table import PropertyEnum
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBParagraph
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from gov.nasa.jpl.mbee.lib import Utils
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper
from com.nomagic.magicdraw.core import Application 

proj = Application.getInstance().getProject()
targets = scriptInput['DocGenTargets']

model = []                          #this is for the editable table body
headers = ['Document', 'Doc ID']    #this is for the editable table header
editable = [True, True]             #this is whether the table cells are editable
prop = [PropertyEnum.NAME, PropertyEnum.VALUE]       #this is what should be edited for each cell
stereotype = StereotypesHelper.getStereotype(proj, "Product", "Document Profile") #this is needed to get the slots

for t in targets:
    for doc in t.getOwnedElement():
        if StereotypesHelper.hasStereotype(doc, stereotype):
            #in this case, we want to edit the name and the JPL Document ID slot (stereotype tag) value
            #editing values can also apply to property default values - substitute the property element in
            model.append([doc, StereotypesHelper.getSlot(doc, stereotype, "JPL Document ID", False, False)])
    
# the None arguments can be replace with 2d lists if you want to specify 'editable' and 'prop' for each cell
table = EditableTable("Documents and Their ID", model, headers, None, None, None)
table.setEditableCol(editable)  #specify editability by column
table.setWhatToShowCol(prop)    #specify what property to edit by column
table.prepareTable()

# if you have a simple table that should render the same in docgen as the popup table, 
# use this, the second argument is whether to add line numbers at the left
docgenTable = Utils.getDBTableFromEditableTable(table, False)

scriptOutput = {}
scriptOutput["EditableTable"] = table          #this is for the editable table from gui
scriptOutput["DocGenOutput"] = [DBParagraph("Result:"), docgenTable] #regular output