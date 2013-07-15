from com.nomagic.magicdraw.core import Application
import ValidateStructure
reload(ValidateStructure)
import DeSpecialize
reload(DeSpecialize)
import Specialize
reload(Specialize)

gl = Application.getInstance().getGUILog()

options = {'expandMultiplicity': False,
           'fixAll': False,
           'fixRedefName': False,
           'useTypeName': False,
           'redefineOnly': False,
           'fixExtra': False,
           'promptForParts': False,
           'checkValue': False}

if len(scriptInput['expandMultiplicity']) > 0 and scriptInput['expandMultiplicity'][0]:
    options['expandMultiplicity'] = True
if len(scriptInput['fixAll']) > 0 and scriptInput['fixAll'][0]:
    options['fixAll'] = True
if len(scriptInput['fixRedefName']) > 0 and scriptInput['fixRedefName'][0]:
    options['fixRedefName'] = True
if len(scriptInput['useTypeName']) > 0 and scriptInput['useTypeName'][0]:
    options['useTypeName'] = True
if len(scriptInput['redefineOnly']) > 0 and scriptInput['redefineOnly'][0]:
    options['redefineOnly'] = True
if len(scriptInput['deleteExtras']) > 0 and scriptInput['deleteExtras'][0]:
    options['fixExtra'] = True
if len(scriptInput['promptForParts']) > 0 and scriptInput['promptForParts'][0]:
    options['promptForParts'] = True
if len(scriptInput['checkValue']) > 0 and scriptInput['checkValue'][0]:
    options['checkValue'] = True
    
targets = []
if 'DocGenTargets' in scriptInput:
    targets.extend(scriptInput['DocGenTargets'])
#targets.extend(scriptInput['targets'])
    
if len(targets) > 0:
    if scriptInput['FixMode'] == 'FixNone':
        options['checkOnly'] = True;
        for e in targets:
            checker = ValidateStructure.SRChecker(e, options)  
            checker.checkAttrs()
            checker.printErrors()
    elif len(scriptInput['despecialize']) > 0 and scriptInput['despecialize'][0]:
        for e in targets:
            DeSpecialize.undo(e)
    else:
        options['checkOnly'] = False
        for e in targets:
            Specialize.generalize(e, options)
            
scriptOutput = "script done!"