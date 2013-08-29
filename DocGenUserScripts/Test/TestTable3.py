from gov.nasa.jpl.mgss.mbee.docgen.table import EditableTable
from gov.nasa.jpl.mgss.mbee.docgen.table import PropertyEnum
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *
from gov.nasa.jpl.mbee.lib import Utils
from com.nomagic.uml2.ext.jmi.helpers import StereotypesHelper

from com.nomagic.magicdraw.core import Application #this seems to want its own import.

gl = Application.getInstance().getGUILog()
proj = Application.getInstance().getProject()

targets = scriptInput['DocGenTargets']

model = []      #this is for the editable table body
headers = []    #this is for the editable table header
editable = []   #this is whether the table cells are editable
prop = []       #this is what should be edited for each cell

# Add table columns for each target.
count = 1
for t in targets:
    # Add the name of the target as editable. 
    model.append(t)
    headers.append("name " + str(count))
    editable.append(True)
    prop.append(PropertyEnum.NAME)
    count = count + 1
    
    # Add the skipIfNoDoc slot of "the editable table" element as editable
    stereotype = StereotypesHelper.getStereotype(proj, "DocumentationSkippable")
    if stereotype is None:
        gl.log("No Test.TestTable2 stereotype! THIS IS BAD!!!")
        continue
    if t.getName() == "the editable table":
        slot = StereotypesHelper.getSlot(t, stereotype, "skipIfNoDoc", False, False)
        if slot is None:
            gl.log("No skipIfNoDoc slot! THIS IS BAD!!!")
            continue
        model.append(slot)
        headers.append("skipIfNoDoc slot value")
        editable.append(True)
        prop.append(PropertyEnum.VALUE)
    
# the editable and prop arguments need to be 2d since it's for each cell
table = EditableTable("Hello World", [model], headers, [editable], [prop], None)

# you can also set the flags for each column, in which case give it a 1d list
# this is optional if you already gave the cell by cell argument
table.setEditableCol(editable)
table.setWhatToShowCol(prop)

table.prepareTable()

# if you have a simple table that should render the same in docgen as the popup table, 
# use this, the second argument is whether to add line numbers at the left
docgenTable = Utils.getDBTableFromEditableTable(table, False)

scriptOutput = {"EditableTable": table}          #this is for the editable table
scriptOutput["DocGenOutput"] = [docgenTable]    #this is for the docgen output table