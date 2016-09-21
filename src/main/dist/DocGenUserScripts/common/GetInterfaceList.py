from gov.nasa.jpl.mgss.mbee.docgen.table import EditableTable
from gov.nasa.jpl.mgss.mbee.docgen.table import PropertyEnum
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBTable
from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBText
from gov.nasa.jpl.mbee.lib import Utils

targets = scriptInput['DocGenTargets']
model = []
headers = ["Interface name", "Interface Type", "On Part", "How many connections?"]#, "Name of connecting interface", "Type of connecting interface", "Box of connecting interface", "Type of connector"]
for part in targets:
    if part.getType() is not None:
        box = part.getType()
        interfaces = box.getOwnedPort()
        for port in interfaces:
            row = []
            row.append(port.getName())
            if port.getType() is not None:
                row.append(port.getType().getName())
            else:
                row.append("No type")
            if port.getOwner() is not None:
                row.append(port.getOwner().getName())
            else:
                row.append("No owner")
            num_of_connectors = 0
            for end in port.getEnd():
                num_of_connectors+=1
            row.append(num_of_connectors)
            model.append(row)
    
table = EditableTable("Table of Connectors and their ports", model, headers, None, None, None)
#[True, True, True, True], [PropertyEnum.NAME,PropertyEnum.NAME,PropertyEnum.NAME,PropertyEnum.NAME]
table.prepareTable()
docgentable = Utils.getDBTableFromEditableTable(table, False)
scriptOutput = {"EditableTable": table}
scriptOutput["DocGenOutput"] = [docgentable]


#for part in targets:
#    box = part.getType()
#    if box.getOwnedPort() is not None:
#        interfaces = box.getOwnedPort()
#    else:
#        row.append("No port")
#        row.append("No port")
#        row.append("No port")
#    for port in interfaces:
#        row = []
#        row.append(port.getName())
#        if port.getType() is not None:
#            row.append(port.getType().getName())
#        else:
#            row.append("No type")
#        if port.getOwner() is not None:
#            row.append(port.getOwner().getName())
#        else:
#            row.append("No part")
#        num_of_connectors = 0
#        for end in port.getEnd():
#            num_of_connectors+=1
#        row.append(num_of_connectors)
#        model.append(row)
    
