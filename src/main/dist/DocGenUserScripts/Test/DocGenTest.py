from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBParagraph
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText

output = []
output.append(DBParagraph("Inside Jython script! targets are:"))

for t in scriptInput['DocGenTargets']:
    output.append(DBParagraph(t.getName()))
    
sproperty = scriptInput['property'][0] #stereotype properties are passed as lists
output.append(DBParagraph("A property on the userscript: " + sproperty))
output.append(DBParagraph("Magicdraw Installation Dir: " + scriptInput['md_install_dir']))
output.append(DBParagraph("ForViewEditor: " + str(scriptInput['ForViewEditor']))) #boolean
output.append(DBParagraph("DocGen Output Dir: " + scriptInput['docgen_output_dir']))

output.append(DBParagraph("Table example"))

table = DBTable()
table.setBody([[DBText("hello"), DBText("World")], 
               [DBText("blah"), DBText("blah")]])
table.setHeaders([[DBText("col1"), DBText("col2")]])
table.setTitle("Table from User Script")

output.append(table)
scriptOutput = {"DocGenOutput":output}
