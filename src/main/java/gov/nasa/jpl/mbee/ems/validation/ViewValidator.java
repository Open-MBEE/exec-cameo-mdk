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
package gov.nasa.jpl.mbee.ems.validation;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportElementComments;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportHierarchy;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportView;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportElementComments;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ViewValidator {

    private ValidationSuite suite = new ValidationSuite("View Sync");
    private ValidationRule exists = new ValidationRule("Does Not Exist", "view doesn't exist yet", ViolationSeverity.ERROR);
    private ValidationRule match = new ValidationRule("View content", "view contents have changed", ViolationSeverity.ERROR);
    private ValidationRule hierarchy = new ValidationRule("View Hierarchy", "view hierarchy", ViolationSeverity.INFO);
    private ValidationRule comments = new ValidationRule("View Comments", "view comments", ViolationSeverity.WARNING);
    private ValidationSuite modelSuite;
    private Project prj;
    private Element view;
    private JSONObject result;
    private boolean recurse;
    
    public ViewValidator(Element view, boolean recursive) {
        this.view = view;
        prj = Application.getInstance().getProject();
        suite.addValidationRule(exists);
        suite.addValidationRule(match);
        suite.addValidationRule(hierarchy);
        suite.addValidationRule(comments);
        this.recurse = recursive;
    }
    
    @SuppressWarnings("unchecked")
    public void validate() {
        DocumentGenerator dg = new DocumentGenerator(view, null, null);
        Document dge = dg.parseDocument(true, recurse);
        (new PostProcessor()).process(dge);
        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return;
        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(recurse);
        book.accept(visitor2);
        JSONObject results = new JSONObject();
        JSONArray resultElements = new JSONArray();
        results.put("elements", resultElements);
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGen3Profile.documentViewStereotype, "Document Profile");
        String url = ViewEditUtils.getUrl(false);
        if (url == null)
            return;//return; //do some error
        for (Object viewid: visitor2.getViews().keySet()) {
            boolean doc = false;
            Element currentView = (Element)Application.getInstance().getProject().getElementByID((String)viewid);
            if (StereotypesHelper.hasStereotypeOrDerived(currentView, documentView))
                doc = true;
            //check to see if view exists on alfresco, if not, export view?
            String existurl = url + "/javawebscripts/views/" + viewid;
            String response = ExportUtility.get(existurl);
            if (response == null || !response.contains("contains")) {
                ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[EXIST] This view doesn't exist on view editor yet");
                v.addAction(new ExportView(currentView, false));
                v.addAction(new ExportView(currentView, true));
                exists.addViolation(v);
            } else {
                String viewElementsUrl = existurl + "/elements";
                JSONArray localElements = (JSONArray)((JSONObject)visitor2.getViews().get(viewid)).get("displayedElements");
                String viewelements = ExportUtility.get(viewElementsUrl);
                if (viewelements == null)
                    continue;
                JSONObject viewresults = (JSONObject)JSONValue.parse(viewelements);
                boolean matches = viewElementsMatch(localElements, viewresults);
                if (!matches) {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[CONTENT] The view editor has an outdated version");
                    v.addAction(new ExportView(currentView, false));
                    v.addAction(new ExportView(currentView, true));
                    
                    v.addAction(new ExportHierarchy(currentView));
                    match.addViolation(v);
                } else {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[Hierarchy]");
                    v.addAction(new ExportHierarchy(currentView));
                    hierarchy.addViolation(v);
                }
                resultElements.addAll((JSONArray)viewresults.get("elements")); //need cinyoung's side
                
                
                String viewCommentsUrl = existurl + "/comments";
                String viewcomments = ExportUtility.get(viewCommentsUrl);
                if (viewcomments == null)
                    continue;
                JSONObject commentresults = (JSONObject)JSONValue.parse(viewcomments);
                resultElements.addAll((JSONArray)commentresults.get("elements"));
                boolean commentMatches = viewCommentsMatch(currentView, commentresults);
                if (!commentMatches) {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[Comments] The view has different comments on either side");
                    v.addAction(new ExportElementComments(currentView));
                    v.addAction(new ImportElementComments(currentView, commentresults));
                    comments.addViolation(v);
                }
            }
        }
        ResultHolder.lastResults = results;
        ModelValidator mv = new ModelValidator(view, results, false);
        mv.validate();
        modelSuite = mv.getSuite();
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        if (modelSuite != null)
            vss.add(modelSuite);
        Utils.displayValidationWindow(vss, "View Web Difference Validation");
    }
    
    @SuppressWarnings("unchecked")
    private boolean viewElementsMatch(JSONArray viewDisplayedElements, JSONObject veResults) {
        Set<String> webElements = new HashSet<String>(viewDisplayedElements);
        Set<String> local = new HashSet<String>();
        for (Object o: (JSONArray)veResults.get("elements")) {
            local.add((String)((JSONObject)o).get("id"));
        }
        if (webElements.containsAll(local) && local.containsAll(webElements))
            return true;
        return false;
    }
    
    private boolean viewCommentsMatch(Element view, JSONObject commentresults) {
        Set<String> modelComments = new HashSet<String>();
        JSONArray elements = (JSONArray)commentresults.get("elements");
        Set<String> webComments = new HashSet<String>();
        for (Object elinfo: elements) {
            webComments.add((String)((JSONObject)elinfo).get("id"));
        }
        for (Comment el: view.get_commentOfAnnotatedElement()) {
            if (!ExportUtility.isElementDocumentation(el))
                modelComments.add(el.getID());
        }
        if (!modelComments.containsAll(webComments) || !webComments.containsAll(modelComments))
            return false;
        return true;
    }
}
