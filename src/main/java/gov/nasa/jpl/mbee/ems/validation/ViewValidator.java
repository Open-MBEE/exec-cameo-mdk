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
import gov.nasa.jpl.mbee.ems.validation.actions.Downgrade;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportElementComments;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportHierarchy;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportView;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportElementComments;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportHierarchy;
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
import com.nomagic.task.ProgressStatus;
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
    private ValidationRule productView = new ValidationRule("No longer a document", "no longer a document", ViolationSeverity.WARNING);
    
    private Stereotype productS = Utils.getProductStereotype();
    
    private ValidationSuite modelSuite;
    private ValidationSuite imageSuite;
    private DocumentValidator dv;
    private Element view;
    private boolean recurse;
    private boolean hierarchyOnly;

    public ViewValidator(Element view, boolean recursive, boolean hierarchyOnly) {
        this.view = view;
        this.dv = new DocumentValidator( view );
        if (!hierarchyOnly) {
            suite.addValidationRule(exists);
            suite.addValidationRule(match);
            suite.addValidationRule(comments);
            suite.addValidationRule(projectExist);
            suite.addValidationRule(baselineTag);
            suite.addValidationRule(productView);
        }
        suite.addValidationRule(hierarchy);
        this.recurse = recursive;
        this.hierarchyOnly = hierarchyOnly;
    }
    
    public boolean checkProject() {
      //if (ExportUtility.baselineNotSet)
        //    baselineTag.addViolation(new ValidationRuleViolation(Project.getProject(start).getModel(), "The baseline tag isn't set, baseline check wasn't done."));
        String projectUrl = ExportUtility.getUrlForProject();
        if (projectUrl == null)
            return false;
        String globalUrl = ExportUtility.getUrl();
        globalUrl += "/workspaces/master/elements/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
        String globalResponse = ExportUtility.get(globalUrl, false);
        String url = ExportUtility.getUrlWithWorkspace();
        
        if (globalResponse == null) {
            ValidationRuleViolation v = null;
            if (url.contains("master")) {
                v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The project doesn't exist on the web.");
                v.addAction(new InitializeProjectModel(false));
            } else
                v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The trunk project doesn't exist on the web. Export the trunk first.");
            projectExist.addViolation(v);
            return false;
        }
        String response = ExportUtility.get(projectUrl, false);
        if (response == null || response.contains("Site node is null") || response.contains("Could not find project")) {//tears
            if (url == null)
                return false;
            
            ValidationRuleViolation v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The project exists on the server already under a different site.");
                //v.addAction(new InitializeProjectModel(false));
            projectExist.addViolation(v);
            
            return false;
        }
        if (ProjectUtilities.isElementInAttachedProject(view)) {
            Utils.showPopupMessage("You should not validate or export elements not from this project! Open the right project and do it from there");
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean validate(ProgressStatus ps) {
        dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors(false);
            return false;
        }
        //first run a local generation of the view model to get the current model view structure
        DocumentGenerator dg = new DocumentGenerator(view, dv, null);
        Document dge = dg.parseDocument(true, recurse, hierarchyOnly);
        (new PostProcessor()).process(dge);
        
        DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
        DBAlfrescoVisitor visitor2 = new DBAlfrescoVisitor(recurse);
        if (!hierarchyOnly) {
            dge.accept(visitor);
            DBBook book = visitor.getBook();
            if (book == null)
                return false;
            book.accept(visitor2);
        }
        
        ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
        if (StereotypesHelper.hasStereotypeOrDerived(view, productS) && !recurse) {
            DocumentGenerator dg2 = new DocumentGenerator(view, null, null);
            Document dge2 = dg2.parseDocument(true, true, true);
            (new PostProcessor()).process(dge2);
            dge2.accept(vhv);
        } else
            dge.accept(vhv);

        // this is going to house the elements gotten from web
        JSONObject results = new JSONObject();
        JSONArray resultElements = new JSONArray();
        results.put("elements", resultElements);

        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null)
            return false;

        Element startView = getStartView();
        Map<String, JSONObject> cachedResultElements = new HashMap<String, JSONObject>();
        if (!hierarchyOnly)
            Utils.guilog("[INFO] Validating view(s)");
        else
            Utils.guilog("[INFO] Validating hierarchy");
        Set<String> viewIds = new HashSet<String>();
        if (!hierarchyOnly)
            viewIds = visitor2.getViews().keySet();
        else
            viewIds.add(startView.getID());
        for (String viewid: viewIds) {
            if (ps != null && ps.isCancel())
                break;
            //viewid is a string that's the view's magicdraw id
            if (!recurse && !viewid.equals(startView.getID()))
                continue;
            Element currentView = (Element)Application.getInstance().getProject().getElementByID((String)viewid);

            //check to see if view exists on alfresco
            String existurl = url + "/elements/" + viewid;
            String response = ExportUtility.get(existurl, false);
            //response is the string version of the view json gotten from the web
            if (!ViewEditUtils.isPasswordSet())
                return false;
            if (response == null || !response.contains("contains")) {
                //if the json doesn't contain the "contains" key, that means the view hasn't been exported yet
                ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[EXIST] This view doesn't exist on view editor yet");
                v.addAction(new ExportView(currentView, false, false, "Commit View to MMS"));
                //v.addAction(new ExportView(currentView, false, true, "Commit View with Elements to MMS"));
                //v.addAction(new ExportView(currentView, true, false, "Commit Views to MMS"));
                //v.addAction(new ExportView(currentView, true, true, "Commit View with Elements Hierarchically to MMS"));
                exists.addViolation(v);
            } else {
                //view has been on the web
                JSONObject webView = (JSONObject)((JSONArray)((JSONObject)JSONValue.parse(response)).get("elements")).get(0);
                Object containsObj = ((JSONObject)webView.get("specialization")).get("contains");
                if (containsObj == null) {
                    ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[EXIST] This view doesn't exist on view editor yet");
                    v.addAction(new ExportView(currentView, false, false, "Commit View to MMS"));
                    //v.addAction(new ExportView(currentView, false, true, "Commit View with Elements to MMS"));
                    //v.addAction(new ExportView(currentView, true, false, "Commit Views to MMS"));
                    //v.addAction(new ExportView(currentView, true, true, "Commit View with Elements Hierarchically to MMS"));
                    exists.addViolation(v);
                } else {
                    Boolean editable = (Boolean) webView.get("editable");
                    if (!hierarchyOnly) {
                        String viewElementsUrl = url + "/views/" + viewid + "/elements";
                        JSONArray localElements = (JSONArray)((JSONObject)((JSONObject)visitor2.getViews().get(viewid)).get("specialization")).get("displayedElements");
                        // get the current elements referenced by the view in the current model
                        JSONArray localContains = (JSONArray)((JSONObject)((JSONObject)visitor2.getViews().get(viewid)).get("specialization")).get("contains");
                        // get the current model view structure
    
                        
                        // this is the json object for the view on the web
    
                        // this is the web view structure
                        JSONArray webContains = null;
                        if (containsObj instanceof JSONArray) {
                            webContains = (JSONArray) containsObj;
                        } 
                        if (ps != null && ps.isCancel())
                            break;
                        // quick way to get all element info referenced by view from the web
                        String viewelements = ExportUtility.get(viewElementsUrl, false);
                        if (viewelements == null)
                            continue;
                        JSONObject viewresults = (JSONObject)JSONValue.parse(viewelements);
                        // parse the view elements json from web into JSONObject
                        JSONObject webViewSpec = (JSONObject)webView.get("specialization");
                        boolean matches = viewElementsMatch(localElements, viewresults) && viewContentsMatch(localContains, webContains);
                        if (!matches) {
                            ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[CONTENT] The view editor content is outdated.");
                            if (editable) {
                                v.addAction(new ExportView(currentView, false, false, "Commit View to MMS"));
                                //v.addAction(new ExportView(currentView, false, true, "Commit View with Elements to MMS"));
                                //v.addAction(new ExportView(currentView, true, false, "Commit View recursively to MMS"));
                                //v.addAction(new ExportView(currentView, true, true, "Commit View with Elements recursively to MMS"));
                            }
                            // v.addAction(new ExportHierarchy(currentView));
                            match.addViolation(v);
                        }
                        if (webViewSpec.get("type") instanceof String && ((String)webViewSpec.get("type")).equals("Product") && 
                                !StereotypesHelper.hasStereotypeOrDerived(currentView, Utils.getProductStereotype())) {
                            ValidationRuleViolation v = new ValidationRuleViolation(currentView, "[METACLASS] This is no longer a product/document.");
                            v.addAction(new Downgrade(currentView, webView));
                            productView.addViolation(v);
                        }
                        for (Object reselement: (JSONArray)viewresults.get("elements")) {
                            // add view referenced elements to a cache to later get validated by ModelValidator
                            if (cachedResultElements.containsKey(((JSONObject) reselement).get("sysmlid")))
                                continue;
                            cachedResultElements.put((String)((JSONObject)reselement).get("sysmlid"), (JSONObject) reselement);
                        }
                    }
                    // see if the list of view elements referenced matches and view structures match
                    boolean hierarchyMatches = viewHierarchyMatch(currentView, dge, vhv, (JSONObject)webView.get("specialization")); // this compares the view hierarchy structure
                    
                    if (!hierarchyMatches) {
                        // Update the hierarchy in MagicDraw based on MagicDraw
                        ValidationRuleViolation v = new ValidationRuleViolation( currentView, "[Hierarchy] Document Hierarchy is different");
                        JSONArray view2view = (JSONArray)((JSONObject)webView.get("specialization")).get("view2view");
                        if (editable)
                            v.addAction(new ExportHierarchy(currentView));
                        if (view2view != null) {
                            JSONObject keyed = ExportUtility.keyView2View(view2view);
                            v.addAction(new ImportHierarchy(currentView, vhv.getView2View(), keyed));
                        }
                        hierarchy.addViolation(v);
                    }
                }
            }
        }
        resultElements.addAll(cachedResultElements.values());
        ResultHolder.lastResults = results;
        //elements gotten from web
        if (!hierarchyOnly) {
            Utils.guilog("[INFO] Validating view elements");
            List<Element> starts = new ArrayList<Element>();
            starts.add(view);
            ModelValidator mv = new ModelValidator(starts, results, true, visitor2.getElementSet(), true); //visitor2.getElementSet() has the local model elements
            //do the actual element validations between model and web
            mv.validate(false, ps);
            modelSuite = mv.getSuite();

            Utils.guilog("[INFO] Validating images");
            ImageValidator iv = new ImageValidator(visitor2.getImages());
            //this checks images generated from the local generation against what's on the web based on checksum
            iv.validate();
            imageSuite = iv.getSuite();
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
        Utils.guilog("Showing validations...");
        Utils.displayValidationWindow(vss, "View Web Difference Validation");
    }

    @SuppressWarnings("unchecked")
    private boolean viewElementsMatch(JSONArray viewDisplayedElements, JSONObject veResults) {
        //return true; // workaround for server not giving back the right number
        // of elements
        // this does a "shallow" comparison of what elements are being
        // referenced in the views
        
        Set<String> localElements = new HashSet<String>(viewDisplayedElements); 
        Set<String> webElements = new HashSet<String>(); 
        for (Object o: (JSONArray)veResults.get("elements")) {
            webElements.add((String)((JSONObject)o).get("sysmlid")); 
        }
        if (webElements.containsAll(localElements) && localElements.containsAll(webElements)) 
            return true; 
        return false;
    }

    public static boolean viewHierarchyMatch(Element view, Document dge, ViewHierarchyVisitor vhv, JSONObject spec) {
        JSONObject hierarchy = vhv.getView2View();
        // hierarchy is a mapping from parent views to array of children views
        /*
         * { "parentId": ["firstchildId", "secondChildId", ...], ... }
         */
        if (dge.getDgElement() != null && dge.getDgElement() == view) {//view is a document
                    JSONArray view2view = (JSONArray)spec.get("view2view");
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
        } else if (dge.getDgElement() == null) {
            return true;
            //canonical view children comparison
            /*JSONObject viewresponse = (JSONObject)JSONValue.parse(response);
            JSONArray views = (JSONArray)viewresponse.get("elements");
            for (Object viewresult: views) {
                if (((JSONObject)viewresult).get("sysmlid").equals(view.getID())) {
                    JSONArray childrenViews = (JSONArray)((JSONObject)((JSONObject)viewresult).get("specialization")).get("childrenViews");
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
            }*/
        }
        return true;
    }

    private boolean viewContentsMatch(JSONArray localContains, JSONArray webContains) {
        //this recursively compares the structure of the views between local generation and web
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
        //this compares the paragraph/list/table/section/image objects in the view structure
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
            JSONArray localtable = (JSONArray) a.get("body");
            JSONArray webtable = (JSONArray) b.get("body");
            if (!tableMatch(localtable, webtable))
                return false;
            if (!tableMatch((JSONArray) a.get("header"), (JSONArray) b.get("header")))
                return false;
        } else if (a.get("type").equals("List")) {
            JSONArray alist = (JSONArray)a.get("list");
            JSONArray blist = (JSONArray)b.get("list");
            if (alist.size() != blist.size())
                return false;
            for (int i = 0; i < alist.size(); i++) {
                if (!listMatch((JSONArray) alist.get(i), (JSONArray) blist.get(i)))
                    return false;
            }
        } else if (a.get("type").equals("Section")) {
            if (!a.get("name").equals(b.get("name")))
                return false;
            JSONArray acontains = (JSONArray) a.get("contains");
            JSONArray bcontains = (JSONArray) b.get("contains");
            if (acontains.size() != bcontains.size())
                return false;
            for (int i = 0; i < acontains.size(); i++) {
                JSONObject ao = (JSONObject) acontains.get(i);
                JSONObject bo = (JSONObject) bcontains.get(i);
                if (!contentMatch(ao, bo))
                    return false;
            }
        } else if (a.get("type").equals("Image")) {
            if (!a.get("sysmlid").equals(b.get("sysmlid")))
                return false;
        }
        return true;
    }

    private boolean tableMatch(JSONArray a, JSONArray b) {
        // helper for comparing table structure
        if (a.size() != b.size())
            return false;
        for (int j = 0; j < a.size(); j++) {
            JSONArray localrow = (JSONArray) a.get(j);
            JSONArray webrow = (JSONArray) b.get(j);
            if (localrow.size() != webrow.size())
                return false;
            for (int k = 0; k < localrow.size(); k++) {
                JSONObject localcell = (JSONObject) localrow.get(k);
                JSONObject webcell = (JSONObject) webrow.get(k);
                if (!listMatch((JSONArray) localcell.get("content"), (JSONArray) webcell.get("content")))
                    return false;
            }
        }
        return true;
    }

    private boolean listMatch(JSONArray a, JSONArray b) {
        // helper for comparing list structure
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!contentMatch((JSONObject) a.get(i), (JSONObject) b.get(i)))
                return false;
        }
        return true;
    }

    private Element getStartView() {
        Stereotype conforms = Utils.getConformsStereotype();
        Stereotype sysml14conforms = Utils.getSysML14ConformsStereotype();
        Element viewpoint = GeneratorUtils.findStereotypedRelationship(view, conforms);
        if (viewpoint == null)
            viewpoint = GeneratorUtils.findStereotypedRelationship(view, sysml14conforms);

        if (viewpoint != null && viewpoint instanceof Class)
            return view;
        Stereotype sysmlview = Utils.getViewStereotype();
        List<Element> expose = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                        view, DocGen3Profile.queriesStereotype, 1, false, 1);
        if (expose.size() == 1 && StereotypesHelper.hasStereotypeOrDerived(expose.get(0), sysmlview)) {
            return expose.get(0); // substitute another view
        }
        return view;
    }
}
