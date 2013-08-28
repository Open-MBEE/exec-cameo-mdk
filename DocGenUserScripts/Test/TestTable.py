from gov.nasa.jpl.mgss.mbee.docgen.table import EditableTable
from gov.nasa.jpl.mgss.mbee.docgen.table import PropertyEnum
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText


#This gives a one row table for editing names of the targets and also 
#a output table for docgen

targets = scriptInput['DocGenTargets']
model = []      #this is for the editable table body
headers = []    #this is for the editable table header
editable = []   #this is whether the table cells are editable
prop = []       #this is what should be edited

docgenHeaders = []  #this is for the DocGen UserScript table headers
docgenBody = []     #this is for the DocGen UserScript table body
count = 1
for t in targets:
    model.append(t)
    headers.append("header " + str(count))
    editable.append(True)
    prop.append(PropertyEnum.NAME)
    docgenBody.append(DBText(t.getName()))
    docgenHeaders.append(DBText("header " + str(count)))
    count += 1
    
docgenTable = DBTable() 
docgenTable.setBody([docgenBody])
docgenTable.setHeaders([docgenHeaders])
docgenTable.setTitle("Example UserScript Table (editable in md)")
docgenTable.setCols(count-1)

table = EditableTable("Hello World", [model], headers, [editable], [prop], None)
table.prepareTable()
scriptOutput = {"EditableTable":table}          #this is for the editable table
scriptOutput["DocGenOutput"] = [docgenTable]    #this is for the docgen output table

