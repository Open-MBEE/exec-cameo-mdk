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
import gov.nasa.jpl.mbee.ems.validation.actions.InitializeProjectModel;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.Section;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ViewValidator {

    private ValidationRule projectExist = new ValidationRule("Project Exist", "Project doesn't exist", ViolationSeverity.ERROR);
    private ValidationSuite suite = new ValidationSuite("View Sync");
    private ValidationRule exists = new ValidationRule("Does Not Exist", "view doesn't exist yet", ViolationSeverity.ERROR);
    private ValidationRule match = new ValidationRule("View content", "view contents have changed", ViolationSeverity.ERROR);
    private ValidationRule hierarchy = new ValidationRule("View Hierarchy", "view hierarchy", ViolationSeverity.WARNING);
    private ValidationRule comments = new ValidationRule("View Comments", "view comments", ViolationSeverity.WARNING);
    private ValidationRule baselineTag = new ValidationRule("Baseline Tag Set", "Baseline Tag isn't set", ViolationSeverity.WARNING);
    
    private ValidationSuite modelSuite;
    private ValidationSuite imageSuite;
    private DocumentValidator dv;
    private Element view;
    private boolean recurse;
    
    public ViewValidator(Element view, boolean recursive) {
        this.view = view;
        this.dv = new DocumentValidator( view );
        suite.addValidationRule(exists);
        suite.addValidationRule(match);
        suite.addValidationRule(hierarchy);
        suite.addValidationRule(comments);
        suite.addValidationRule(projectExist);
        suite.addValidationRule(baselineTag);
        this.recurse = recursive;
    }
    
    public boolean checkProject() {
        if (ExportUtility.baselineNotSet)
            baselineTag.addViolation(new ValidationRuleViolation(Project.getProject(view).getModel(), "The baseline tag isn't set, baseline check wasn't done."));
        String projectUrl = ExportUtility.getUrlWithSiteAndProject();
        if (projectUrl == null)
            return false;
        String response = ExportUtility.get(projectUrl);
        if (response == null) {
            ValidationRuleViolation v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "This project doesn't exist on the web yet, or the site has been moved");
            v.addAction(new InitializeProjectModel(true));
            v.addAction(new InitializeProjectModel(false));
            projectExist.addViolation(v);
            return false;
        }
        if (ProjectUtilities.isElementInAttachedProject(view)){
            Utils.showPopupMessage("You should not validate or export elements not from this project! Open the right project and do it from there");
            return false;
        }
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public boolean validate() {
        DocumentGenerator dg = new DocumentGenerator(view, dv, null);
        Document dge = dg.parseDocument(true, true);
        (new PostProcessor()).process(dge);
        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        dge.accept(visitor);
        DBBook book = visitor.getBook();
        if (book == null)
            return false;
        ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
        dge.accept(vhv);
        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(recurse);
        book.accept(visitor2);
        JSONObject results = new JSONObject();
        JSONArray resultElements = new JSONArray();
        results.put("elements", resultElements);
        
        String url = ExportUtility.getUrl();
        if (url == null)
            return false;//return; //do some error
        
        Element startView = getStartView();
        Map<String, JSONObject> cachedResultElements = new HashMap<String, JSONObject>();
        for (Object viewid: visitor2.getViews().keySet()) {
            if (!recurse && !viewid.equals(startView.getID()))
                continue;
            Element currentView = (Element)Application.getInstance().getProject().getElementByID((String)viewid);
            
            //check to see if view exists on alfresco, if not, export view?
            String existurl = url + "/javawebscripts/views/" + viewid;
            String response = ExportUtility.get(existurl, false);
            if (!ViewEditUtils.isPasswordSet())
                return false;
            if (response == null || !response.contains("contains")) {
                ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[EXIST] This view doesn't exist on view editor yet");
                v.addAction(new ExportView(currentView, false));
                v.addAction(new ExportView(currentView, true));
                exists.addViolation(v);
            } else {
                String viewElementsUrl = existurl + "/elements";
                JSONArray localElements = (JSONArray)((JSONObject)visitor2.getViews().get(viewid)).get("displayedElements");
                JSONArray localContains = (JSONArray)((JSONObject)visitor2.getViews().get(viewid)).get("contains");
                JSONObject webView = (JSONObject)((JSONArray)((JSONObject)JSONValue.parse(response)).get("views")).get(0);
                Object containsObj= webView.get("contains");
                JSONArray webContains = null;
                if ( containsObj instanceof JSONArray ) {
                    webContains = (JSONArray)containsObj;
                } else if ( containsObj instanceof String ) {
                    Object arr = JSONValue.parse( (String)containsObj );
                    if ( arr instanceof JSONArray ) {
                        webContains = (JSONArray)arr;
                    } else {
                        webContains = new JSONArray();
                        webContains.add( containsObj );
                    }
                } else {
                    webContains = new JSONArray();
                    webContains.add( containsObj );
                }
                String viewelements = ExportUtility.get(viewElementsUrl, false);
                if (viewelements == null)
                    continue;
                JSONObject viewresults = (JSONObject)JSONValue.parse(viewelements);
                boolean matches = viewElementsMatch(localElements, viewresults) && viewContentsMatch(localContains, webContains);
                boolean hierarchyMatches = viewHierarchyMatch(currentView, dge, vhv, response);
                if (!matches) {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[CONTENT] The view editor has an outdated version");
                    v.addAction(new ExportView(currentView, false));
                    v.addAction(new ExportView(currentView, true));
                    //v.addAction(new ExportHierarchy(currentView));
                    match.addViolation(v);
                } 
                if (!hierarchyMatches) {
                    //if (!viewHierarchyMatch(currentView, dge, vhv, response)) {
                        ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[Hierarchy] The hierarchy from this view/doc is outdated");
                        v.addAction(new ExportHierarchy(currentView));
                        hierarchy.addViolation(v);
                    //}
                }
                for (Object reselement: (JSONArray)viewresults.get("elements")) {
                    if (cachedResultElements.containsKey(((JSONObject)reselement).get("id")))
                            continue;
                    cachedResultElements.put((String)((JSONObject)reselement).get("id"), (JSONObject)reselement);
                }
                //resultElements.addAll((JSONArray)viewresults.get("elements")); //need cinyoung's side
                
                String viewCommentsUrl = url + "/javawebscripts/elements/" + viewid + "/comments";
                String viewcomments = ExportUtility.get(viewCommentsUrl, false);
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
        resultElements.addAll(cachedResultElements.values());
        ResultHolder.lastResults = results;
        ModelValidator mv = new ModelValidator(view, results, false, visitor2.getElementSet());
        mv.validate();
        modelSuite = mv.getSuite();
        ImageValidator iv = new ImageValidator(visitor2.getImages());
        iv.validate();
        imageSuite = iv.getSuite();
        if (!exists.getViolations().isEmpty()) {
            Utils.showPopupMessage("There's view(s) that are missing on the server, see validation window.");
        }
        return true;
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        if (modelSuite != null)
            vss.add(modelSuite);
        if (imageSuite != null)
            vss.add(imageSuite);
        Utils.displayValidationWindow(vss, "View Web Difference Validation");
    }
    
    @SuppressWarnings("unchecked")
    private boolean viewElementsMatch(JSONArray viewDisplayedElements, JSONObject veResults) {
        Set<String> localElements = new HashSet<String>(viewDisplayedElements);
        Set<String> webElements = new HashSet<String>();
        for (Object o: (JSONArray)veResults.get("elements")) {
            webElements.add((String)((JSONObject)o).get("id"));
        }
        if (webElements.containsAll(localElements) && localElements.containsAll(webElements))
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
    
    private boolean viewHierarchyMatch(Element view, Document dge, ViewHierarchyVisitor vhv, String response) {
        JSONObject hierarchy = vhv.getView2View();
        if (dge.getDgElement() != null && dge.getDgElement() == view) {//document
            String url = ExportUtility.getUrl();
            url += "/javawebscripts/products/" + view.getID();
            String docresponse = ExportUtility.get(url);
            if (docresponse  == null)
                return false;
            JSONObject docResponse = (JSONObject)JSONValue.parse(docresponse);
            JSONArray docs = (JSONArray)docResponse.get("products");
            for (Object docresult: docs) {
                if (((JSONObject)docresult).get("id").equals(view.getID())) {
                    JSONArray view2view = (JSONArray)((JSONObject)docresult).get("view2view");
                    if (view2view == null)
                        return false;
                    JSONObject keyed = ExportUtility.keyView2View(view2view);
                    if (hierarchy.size() != keyed.size())
                        return false;
                    for (Object key: hierarchy.keySet()) {
                        JSONArray modelChildren = (JSONArray)hierarchy.get(key);
                        JSONArray webChildren = (JSONArray)keyed.get(key);
                        if (webChildren == null || modelChildren.size() != webChildren.size())
                            return false;
                        for (int i = 0; i < modelChildren.size(); i++) {
                            if (!modelChildren.get(i).equals(webChildren.get(i)))
                                return false;
                        }
                    }
                }
            }
        } else if (dge.getDgElement() == null){
            JSONObject viewresponse = (JSONObject)JSONValue.parse(response);
            JSONArray views = (JSONArray)viewresponse.get("views");
            for (Object viewresult: views) {
                if (((JSONObject)viewresult).get("id").equals(view.getID())) {
                    JSONArray childrenViews = (JSONArray)((JSONObject)viewresult).get("childrenViews");
                    if (childrenViews == null)
                        return false;
                    JSONArray modelChildrenViews = (JSONArray)hierarchy.get(view.getID());
                    if (childrenViews.size() != modelChildrenViews.size())
                        return false;
                    for (int i = 0; i < childrenViews.size(); i++) {
                        if (!childrenViews.get(i).equals(modelChildrenViews.get(i)))
                            return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean viewContentsMatch(JSONArray localContains, JSONArray webContains) {
        if (localContains.size() != webContains.size())
            return false;
        for (int i = 0; i < localContains.size(); i++) {
            JSONObject local = (JSONObject)localContains.get(i);
            JSONObject web = (JSONObject)webContains.get(i);
            if (!contentMatch(local, web))
                return false;
        }
        return true;
    }
    
    private boolean contentMatch(JSONObject a, JSONObject b) {
        if (!a.get("type").equals(b.get("type")))
            return false;
        if (a.get("type").equals("Paragraph")) {
            if (!a.get("sourceType").equals(b.get("sourceType"))) {
                return false;
            }
            if (a.get("sourceType").equals("reference")) {
                if (!a.get("source").equals(b.get("source")) || !a.get("sourceProperty").equals(b.get("sourceProperty")))
                    return false;
            } else if (!a.get("text").equals(b.get("text"))) {
                if (((String)a.get("text")).contains("alfresco/service/api/node/content") || 
                        ((String)b.get("text")).contains("alfresco/service/api/node/content"))
                    return true;
                return false;
            }
        } else if (a.get("type").equals("Table")) {
            JSONArray localtable = (JSONArray)a.get("body");
            JSONArray webtable = (JSONArray)b.get("body");
            if (!tableMatch(localtable, webtable))
                return false;
            if (!tableMatch((JSONArray)a.get("header"), (JSONArray)b.get("header")))
                return false;
        } else if (a.get("type").equals("List")) {
            JSONArray alist = (JSONArray)a.get("list");
            JSONArray blist = (JSONArray)b.get("list");
            if (alist.size() != blist.size())
                return false;
            for (int i = 0; i < alist.size(); i++) {
                if (!listMatch((JSONArray)alist.get(i), (JSONArray)blist.get(i)))
                    return false;
            }
        }
        return true;
    }
    
    private boolean tableMatch(JSONArray a, JSONArray b) {
        if (a.size() != b.size())
            return false;
        for (int j = 0; j < a.size(); j++) {
            JSONArray localrow = (JSONArray)a.get(j);
            JSONArray webrow = (JSONArray)b.get(j);
            if (localrow.size() != webrow.size())
                return false;
            for (int k = 0; k < localrow.size(); k++) {
                JSONObject localcell = (JSONObject)localrow.get(k);
                JSONObject webcell = (JSONObject)webrow.get(k);
                if (!listMatch((JSONArray)localcell.get("content"), (JSONArray)webcell.get("content")))
                    return false;
            }
        }
        return true;
    }
    
    private boolean listMatch(JSONArray a, JSONArray b) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!contentMatch((JSONObject)a.get(i), (JSONObject)b.get(i)))
                return false;
        }
        return true;
    }
    
    private Element getStartView() {
        Stereotype conforms  = Utils.getConformsStereotype();
        Stereotype sysml14conforms = Utils.getSysML14ConformsStereotype();//StereotypesHelper.getStereotype(Application.getInstance().getProject(), "SysML1.4.Conforms");

        Element viewpoint = GeneratorUtils.findStereotypedRelationship(view, conforms);
        if (viewpoint == null)
            viewpoint = GeneratorUtils.findStereotypedRelationship(view, sysml14conforms);
       
        if (viewpoint != null && viewpoint instanceof Class) 
            return view;
        Stereotype sysmlview = Utils.getViewStereotype();
        List<Element> expose = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(view,
                DocGen3Profile.queriesStereotype, 1, false, 1);
        if (expose.size() == 1 && StereotypesHelper.hasStereotypeOrDerived(expose.get(0), sysmlview)) {
            return expose.get(0); // substitute another view
        }
        return view;
    }
}
