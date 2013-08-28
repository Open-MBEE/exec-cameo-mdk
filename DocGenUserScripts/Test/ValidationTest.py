from gov.nasa.jpl.mgss.mbee.docgen.docbook import DBParagraph
from gov.nasa.jpl.mgss.mbee.docgen.validation import ValidationRule
from gov.nasa.jpl.mgss.mbee.docgen.validation import ValidationSuite
from gov.nasa.jpl.mgss.mbee.docgen.validation import ViolationSeverity
from com.nomagic.uml2.ext.magicdraw.classes.mdkernel import *

targets = scriptInput['DocGenTargets']
# make a validation suite, give it a name
vs = ValidationSuite("TestSuite")
vs.setShowSummary(True)
vs.setShowDetail(True)
# each validation suite can have many rules, make one with name, description,
# and severity
vr = ValidationRule("Rule 1", "Stuff with names", ViolationSeverity.WARNING)
vr2 = ValidationRule("Rule 2", "Stuff that's Packages", ViolationSeverity.ERROR)
vs.addValidationRule(vr)
vs.addValidationRule(vr2)
# each rule can have many violations, give it the element and maybe some comment
for e in targets:
    for t in e.getOwnedElement():
        if isinstance(t, NamedElement) and t.getName() != "":
            vr.addViolation(t, "Has Name")
        if isinstance(t, Package):
            vr2.addViolation(t, "Is Package")
        
scriptOutput = {}
scriptOutput["DocGenValidationOutput"] = [vs]
scriptOutput["DocGenOutput"] = [DBParagraph("'Regular' output shows up first.")]