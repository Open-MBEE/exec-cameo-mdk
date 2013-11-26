/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DgvalidationDBSwitch;
import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.actions.RunUserEditableTableAction;
import gov.nasa.jpl.mbee.actions.RunUserScriptAction;
import gov.nasa.jpl.mbee.actions.RunUserValidationScriptAction;
import gov.nasa.jpl.mbee.dgvalidation.Suite;
import gov.nasa.jpl.mbee.dgview.ViewElement;
import gov.nasa.jpl.mbee.lib.ScriptRunner;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class UserScript extends Query {

    public String getStereotypeName() {
        Element e = this.dgElement;
        if (!StereotypesHelper.hasStereotypeOrDerived(this.dgElement, DocGen3Profile.userScriptStereotype)) {
            if (this.dgElement instanceof CallBehaviorAction
                    && ((CallBehaviorAction)this.dgElement).getBehavior() != null
                    && StereotypesHelper.hasStereotypeOrDerived(
                            ((CallBehaviorAction)this.dgElement).getBehavior(),
                            DocGen3Profile.userScriptStereotype))
                e = ((CallBehaviorAction)this.dgElement).getBehavior();
        }
        Stereotype s = StereotypesHelper.checkForDerivedStereotype(e, DocGen3Profile.userScriptStereotype);
        return s.getName();
    }

    public Map<?, ?> getScriptOutput(Map<String, Object> inputs) {
        try {
            Map<String, Object> inputs2 = new HashMap<String, Object>();
            if (this.targets != null)
                inputs2.put("DocGenTargets", this.targets);
            else
                inputs2.put("DocGenTargets", new ArrayList<Element>());
            if (inputs != null)
                inputs2.putAll(inputs);
            if (!inputs2.containsKey("md_install_dir"))
                inputs2.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
            if (!inputs2.containsKey("docgen_output_dir"))
                inputs2.put("docgen_output_dir", ApplicationEnvironment.getInstallRoot());
            if (!inputs2.containsKey("ForViewEditor"))
                inputs2.put("ForViewEditor", false);
            Element e = this.dgElement;
            if (!StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.userScriptStereotype))
                if (e instanceof CallBehaviorAction
                        && ((CallBehaviorAction)e).getBehavior() != null
                        && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)e).getBehavior(),
                                DocGen3Profile.userScriptStereotype))
                    e = ((CallBehaviorAction)e).getBehavior();
            Object o = ScriptRunner.runScriptFromStereotype(e,
                    StereotypesHelper.checkForDerivedStereotype(e, DocGen3Profile.userScriptStereotype),
                    inputs2);
            if (o != null && o instanceof Map)
                return (Map<?, ?>)o;

        } catch (ScriptException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Application.getInstance().getGUILog().log(sw.toString());
        }
        return null;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore())
            return res;
        Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("FixMode", "FixNone");
        inputs.put("ForViewEditor", forViewEditor);
        inputs.put("DocGenTitles", getTitles());
        if (outputDir != null)
            inputs.put("docgen_output_dir", outputDir);
        inputs.put("md_install_dir", ApplicationEnvironment.getInstallRoot());
        Map<?, ?> o = getScriptOutput(inputs);
        if (o != null && o.containsKey("DocGenOutput")) {
            Object l = o.get("DocGenOutput");
            if (l instanceof List) {
                for (Object oo: (List<?>)l) {
                    if (oo instanceof DocumentElement)
                        res.add((DocumentElement)oo);
                }
            }
        }
        if (o != null && o.containsKey("docgenOutput")) {
            Object result = o.get("docgenOutput");
            if (result instanceof List) {
                for (Object r: (List<?>)result) {
                    if (r instanceof NamedElement) {
                        res.add(new DBText(((NamedElement)r).getName()));
                    } else if (r instanceof ViewElement) {
                        res.add(DocGenUtils.ecoreTranslateView((ViewElement)r, forViewEditor));
                    }
                }
            }
        }
        if (o != null && o.containsKey("DocGenValidationOutput")) {
            Object l = o.get("DocGenValidationOutput");
            if (l instanceof List) {
                for (Object oo: (List<?>)l) {
                    if (oo instanceof ValidationSuite)
                        res.addAll(((ValidationSuite)oo).getDocBook());
                }
            }
        }
        if (o != null && o.containsKey("docgenValidationOutput")) {
            Object l = o.get("docgenValidationOutput");
            if (l instanceof List) {
                DgvalidationDBSwitch s = new DgvalidationDBSwitch();
                for (Object object: (List<?>)l) {
                    if (object instanceof Suite)
                        res.addAll(((ValidationSuite)s.doSwitch((Suite)object)).getDocBook());
                }
            }
        }
        return res;
    }

    @Override
    public List<MDAction> getActions() {
        List<MDAction> res = new ArrayList<MDAction>();
        Element action = getDgElement();
        boolean added = false;
        if (StereotypesHelper.hasStereotypeOrDerived(action, DocGen3Profile.editableTableStereotype)
                || ((action instanceof CallBehaviorAction)
                        && ((CallBehaviorAction)action).getBehavior() != null && StereotypesHelper
                            .hasStereotypeOrDerived(((CallBehaviorAction)action).getBehavior(),
                                    DocGen3Profile.editableTableStereotype))) {
            res.add(new RunUserEditableTableAction(this));
            added = true;
        }
        if (StereotypesHelper.hasStereotypeOrDerived(action, DocGen3Profile.validationScriptStereotype)
                || ((action instanceof CallBehaviorAction)
                        && ((CallBehaviorAction)action).getBehavior() != null && StereotypesHelper
                            .hasStereotypeOrDerived(((CallBehaviorAction)action).getBehavior(),
                                    DocGen3Profile.validationScriptStereotype))) {
            res.add(new RunUserValidationScriptAction(this));
            added = true;
        }
        if (!added)
            res.add(new RunUserScriptAction(this));
        return res;
    }

}
