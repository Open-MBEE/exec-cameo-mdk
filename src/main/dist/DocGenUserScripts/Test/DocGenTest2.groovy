import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText

def output = []
output.add(new DBParagraph("Inside Groovy script! targets are:"))

for (t in scriptInput["DocGenTargets"]) {
	output.add(new DBParagraph(t.getName()))
}

def sproperty = scriptInput['property'][0] //stereotype props are passed as lists
output.add(new DBParagraph("A property on the userscript: " + sproperty))
output.add(new DBParagraph("Magicdraw Installation Dir: " + scriptInput['md_install_dir']))
output.add(new DBParagraph("ForViewEditor: " + scriptInput['ForViewEditor'])) //boolean
output.add(new DBParagraph("DocGen output Dir: " + scriptInput['docgen_output_dir']))

output.add(new DBParagraph("Table example"))

def table = new DBTable()
table.setBody([[new DBText("hello"), new DBText("World")],
	           [new DBText("blah"), new DBText("blah")]])
table.setHeaders([[new DBText("col1"), new DBText("col2")]])
table.setTitle("Table from User Script")

output.add(table)
scriptOutput = ["DocGenOutput":output]
