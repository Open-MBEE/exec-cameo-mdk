from gov.nasa.jpl.mgss.mbee.docgen.table import EditableTable
from gov.nasa.jpl.mgss.mbee.docgen.table import PropertyEnum
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText
from gov.nasa.jpl.mbee.lib import Utils

targets = scriptInput['DocGenTargets']
model = []
headers = ["Connector name", "End A", "Type A", "Box A", "End B", "Type B", "Box B"]
for connector in targets:
    row = []
    row.append(connector.getName())
    for end in connector.getEnd():
        role = end.getRole()
        row.append(role.getName())
        if role.getType() is not None:
            row.append(role.getType().getName())
        else:
            row.append("No type")
        if end.getPartWithPort() is not None:
            owner = end.getPartWithPort()
            row.append(owner.getName())
        else:
            row.append("No box")
#    if connector.getStereotype() is not None:
#        for stereotype in connector.getStereotype():
#            row.append(connector.getStereotype().getName())
#    else:
#        row.append("not stereotyped")
#    for stereotype in connector.getStereotype():
#        if connector.getStereotype() is None:
#            row.append("No Stereotype")
#        else:
#            row.append(connector.getSterotype())
    model.append(row)
    
table = EditableTable("Table of Connectors and their ports", model, headers, None, None, None)
#[True, True, True, True], [PropertyEnum.NAME,PropertyEnum.NAME,PropertyEnum.NAME,PropertyEnum.NAME]
table.prepareTable()
docgentable = Utils.getDBTableFromEditableTable(table, False)
scriptOutput = {"EditableTable": table}
scriptOutput["DocGenOutput"] = [docgentable]


#connector name, ends, names of parts of ends (not blocks), port type
