from com.nomagic.magicdraw.core import Application

targets = scriptInput['DocGenTargets']
output = []
for target in targets:
    # this only gets targets with names that starts with test
    if target.getName().startswith("test"):
        output.append(target)
scriptOutput = {"DocGenOutput":output}