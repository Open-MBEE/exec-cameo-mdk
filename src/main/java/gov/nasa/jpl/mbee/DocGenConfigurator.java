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
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.actions.ViewViewCommentsAction;
import gov.nasa.jpl.mbee.actions.alfresco.EMSLogoutAction;
import gov.nasa.jpl.mbee.actions.alfresco.ExportModelAction;
import gov.nasa.jpl.mbee.actions.alfresco.InitializeProjectAction;
import gov.nasa.jpl.mbee.actions.alfresco.ValidateModelAction;
import gov.nasa.jpl.mbee.actions.alfresco.ValidateViewAction;
import gov.nasa.jpl.mbee.actions.alfresco.ValidateViewRecursiveAction;
import gov.nasa.jpl.mbee.actions.docgen.GenerateDocumentAction;
import gov.nasa.jpl.mbee.actions.docgen.InstanceViewpointAction;
import gov.nasa.jpl.mbee.actions.docgen.NumberDependencyAction;
import gov.nasa.jpl.mbee.actions.docgen.RunUserScriptAction;
import gov.nasa.jpl.mbee.actions.docgen.RunUserValidationScriptAction;
import gov.nasa.jpl.mbee.actions.docgen.ValidateDocument3Action;
import gov.nasa.jpl.mbee.actions.docgen.ValidateViewStructureAction;
import gov.nasa.jpl.mbee.actions.docgen.ViewDocument3Action;
import gov.nasa.jpl.mbee.actions.vieweditor.DeleteDocumentAction;
import gov.nasa.jpl.mbee.actions.vieweditor.DeleteProjectAction;
import gov.nasa.jpl.mbee.actions.vieweditor.DeleteVolumeAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ExportViewAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ExportViewCommentsAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ExportViewHierarchyAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ExportViewRecursiveAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ImportViewAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ImportViewCommentsAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ImportViewDryAction;
import gov.nasa.jpl.mbee.actions.vieweditor.ImportViewRecursiveAction;
import gov.nasa.jpl.mbee.actions.vieweditor.OrganizeDocumentAction;
import gov.nasa.jpl.mbee.actions.vieweditor.OrganizeViewEditorAction;
import gov.nasa.jpl.mbee.actions.vieweditor.SynchronizeViewAction;
import gov.nasa.jpl.mbee.actions.vieweditor.SynchronizeViewRecursiveAction;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.CollectActionsVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.UserScript;

import java.util.List;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DocGenConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator {

    @Override
    public int getPriority() {
        return ConfiguratorWithPriority.MEDIUM_PRIORITY;
    }

    @Override
    public void configure(ActionsManager manager, Tree browser) {
        Node no = browser.getSelectedNode();
        if (no == null)
            return;
        Object o = no.getUserObject();
        if (!(o instanceof Element))
            return;
        addElementActions(manager, (Element)o);
    }

    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram,
            PresentationElement[] selected, PresentationElement requestor) {
        if (requestor != null) {
            Element e = requestor.getElement();
            addElementActions(manager, e);
        } else {
            addDiagramActions(manager, diagram);
        }
    }

    private void addElementActions(ActionsManager manager, Element e) {
        Project prj = Project.getProject(e);
        if (prj == null)
            return;
        Stereotype sysmlview = Utils.getViewStereotype();
        Stereotype sysmlviewpoint = Utils.getViewpointStereotype();
        Stereotype documentView = StereotypesHelper.getStereotype(prj, DocGen3Profile.documentViewStereotype,
                "Document Profile");
        if (e == null)
            return;
        
        ActionsCategory modelLoad = myCategory(manager, "AlfrescoModel", "MMS");
        if (manager.getActionFor(ExportModelAction.actionid) == null)
            modelLoad.addAction(new ExportModelAction(e));
        if (manager.getActionFor(ValidateModelAction.actionid) == null && !(e instanceof Model))
            modelLoad.addAction(new ValidateModelAction(e));
        if (e instanceof Model && manager.getActionFor(InitializeProjectAction.actionid) == null)
            modelLoad.addAction(new InitializeProjectAction());
        if (manager.getActionFor(EMSLogoutAction.actionid) == null)
            modelLoad.addAction(new EMSLogoutAction());
        
        // add menus in reverse order since they are inserted at top
        // View Interaction menu
        if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.validationScriptStereotype)) {
            ActionsCategory c = myCategory(manager, "ViewInteraction", "View Interaction");
            UserScript us = new UserScript();
            us.setDgElement(e);
            List<Element> targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.queriesStereotype, 1, false, 1);
            targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.oldQueriesStereotype, 1, false, 1));
            us.setTargets(targets);
            if (manager.getActionFor(RunUserValidationScriptAction.actionid) == null)
                c.addAction(new RunUserValidationScriptAction(us, true));
        } else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.userScriptStereotype)) {
            ActionsCategory c = myCategory(manager, "ViewInteraction", "View Interaction");
            UserScript us = new UserScript();
            us.setDgElement(e);
            List<Element> targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.queriesStereotype, 1, false, 1);
            targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    DocGen3Profile.oldQueriesStereotype, 1, false, 1));
            us.setTargets(targets);
            if (manager.getActionFor(RunUserScriptAction.actionid) == null)
                c.addAction(new RunUserScriptAction(us, true));
        }
        if (StereotypesHelper.hasStereotypeOrDerived(e, sysmlview)) {
            // There may be no view query actions to add, in which case we need
            // to avoid adding an empty menu category, so the category is
            // removed in this case.
            ActionsCategory category = (ActionsCategory)manager.getActionFor("ViewInteraction");
            if (category == null) {
                category = new MDActionsCategory("ViewInteraction", "View Interaction");
                category.setNested(true);
                boolean added = addViewQueryActions(category, (NamedElement)e);
                if (added)
                    manager.addCategory(0, category);
            }
        
            ActionsCategory modelLoad2 = myCategory(manager, "AlfrescoModel", "MMS");
            NMAction action = manager.getActionFor(ValidateViewAction.actionid);
            if (action == null)
                modelLoad2.addAction(new ValidateViewAction(e));
            action = manager.getActionFor(ValidateViewRecursiveAction.actionid);
            if (action == null)
                modelLoad2.addAction(new ValidateViewRecursiveAction(e));
            
            //ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            //action = manager.getActionFor(ExportViewAction.actionid);
            //if (action == null)
                //addEditableViewActions(c, (NamedElement)e);
        }
        /*if (StereotypesHelper.hasStereotype(e, ViewEditorProfile.project)) { // REVIEW
                                                                         // --
                                                                         // hasStereotypeOrDerived()?
            ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            NMAction act = manager.getActionFor(OrganizeViewEditorAction.actionid);
            if (act == null)
                c.addAction(new OrganizeViewEditorAction(e));
            act = manager.getActionFor(DeleteProjectAction.actionid);
            if (act == null)
                c.addAction(new DeleteProjectAction(e));
            act = manager.getActionFor(EMSLogoutAction.actionid);
            if (act == null)
                c.addAction(new EMSLogoutAction());
        }
        if (StereotypesHelper.hasStereotype(e, ViewEditorProfile.volume)) { // REVIEW
                                                                        // --
                                                                        // hasStereotypeOrDerived()?
            ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            NMAction act = manager.getActionFor(DeleteVolumeAction.actionid);
            if (act == null)
                c.addAction(new DeleteVolumeAction(e));
            act = manager.getActionFor(EMSLogoutAction.actionid);
            if (act == null)
                c.addAction(new EMSLogoutAction());
        }
        if (StereotypesHelper.hasStereotype(e, ViewEditorProfile.document)
                || StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
            ActionsCategory c = myCategory(manager, "ViewEditor", "View Editor");
            NMAction act = manager.getActionFor(DeleteDocumentAction.actionid);
            if (act == null)
                c.addAction(new DeleteDocumentAction(e));
            if (StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
                act = manager.getActionFor(OrganizeDocumentAction.actionid);
                if (act == null)
                    c.addAction(new OrganizeDocumentAction(e));
            }
        }*/

        // DocGen menu
        if ((e instanceof Activity && StereotypesHelper.hasStereotypeOrDerived(e,
                DocGen3Profile.documentStereotype)) || StereotypesHelper.hasStereotypeOrDerived(e, sysmlview)) {
            NMAction act = null;
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            // DefaultPropertyResourceProvider pp = new
            // DefaultPropertyResourceProvider();
            act = manager.getActionFor(ValidateDocument3Action.actionid);
            if (act == null)
                c.addAction(new ValidateDocument3Action(e));

            act = manager.getActionFor(ValidateViewStructureAction.actionid);
            if (act == null)
                c.addAction(new ValidateViewStructureAction(e));

            act = manager.getActionFor(ViewDocument3Action.actionid);
            if (act == null)
                c.addAction(new ViewDocument3Action(e));

            act = manager.getActionFor(GenerateDocumentAction.actionid);
            if (act == null)
                c.addAction(new GenerateDocumentAction(e));

            if (StereotypesHelper.hasStereotype(e, documentView)) {
                /*
                 * act = manager.getActionFor(PublishDocWebAction.actionid); if
                 * (act == null) c.addAction(new
                 * PublishDocWebAction((NamedElement)e));
                 */
                act = manager.getActionFor(NumberDependencyAction.actionid);
                if (act == null)
                    c.addAction(new NumberDependencyAction(e));

            }
            /*
             * if (e instanceof Activity &&
             * StereotypesHelper.hasStereotypeOrDerived(e,
             * DocGen3Profile.documentStereotype)) { act =
             * manager.getActionFor(PublishDocWebAction.actionid); if (act ==
             * null) c.addAction(new PublishDocWebAction((NamedElement)e)); }
             */
        }

        if (StereotypesHelper.hasStereotypeOrDerived(e, sysmlviewpoint)) {
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            NMAction act = manager.getActionFor(InstanceViewpointAction.actionid);
            if (act == null)
                c.addAction(new InstanceViewpointAction(e));
        }
        // if ( ( e instanceof Activity &&
        // StereotypesHelper.hasStereotypeOrDerived( e,
        // DocGen3Profile.documentStereotype ) ) ||
        // StereotypesHelper.hasStereotypeOrDerived( e, sysmlview ) ) {
        // ActionsCategory c = myCategory( manager, "DocGen", "DocGen" );
        // NMAction act = manager.getActionFor( "DocGenComments" );
        // if ( act == null ) addCommentActions( c, (NamedElement)e );
        // }
    }

    private void addDiagramActions(ActionsManager manager, DiagramPresentationElement diagram) {
        if (diagram == null)
            return;
        Element element = diagram.getActualElement();
        if (element == null)
            return;
        Element owner = element.getOwner();
        if (owner == null || !(owner instanceof NamedElement))
            return;
        // //this add actions for syncing to docweb comments
        // if (StereotypesHelper.hasStereotypeOrDerived(owner,
        // DocGen3Profile.documentViewStereotype)) {
        // ActionsCategory category = myCategory(manager, "DocGen", "DocGen");
        // NMAction action = manager.getActionFor("DocGenComments");
        // if (action == null)
        // addCommentActions(category, (NamedElement) owner);
        // }
    }

    /**
     * add actions related to view editor (this includes view comments)
     * 
     * @param parent
     * @param e
     */
    private void addEditableViewActions(ActionsCategory parent, NamedElement e) {
        ActionsCategory c = parent; // new ActionsCategory("EditableView",
                                    // "Editable View");
        c.addAction(new ImportViewDryAction(e));
        c.addAction(new ExportViewAction(e));
        c.addAction(new ExportViewHierarchyAction(e));
        c.addAction(new ImportViewAction(e));
        c.addAction(new SynchronizeViewAction(e));
        c.addAction(new ExportViewCommentsAction(e));
        c.addAction(new ImportViewCommentsAction(e));
        c.addAction(new ViewViewCommentsAction(e));
        ActionsCategory a = new MDActionsCategory("AdvanceEditor", "ModelLoad");
        a.setNested(true);
        a.addAction(new ImportViewRecursiveAction(e));
        a.addAction(new ExportViewRecursiveAction(e));
        a.addAction(new SynchronizeViewRecursiveAction(e));
        c.addAction(a);
        // c.setNested(true);
        // synchronized (this) { // saw a concurrency error at some point
        // parent.addAction(c);
        // parent.getCategories().add(c);
        // }
    }

    /**
     * Gets the specified category, creates it if necessary.
     * 
     * @param manager
     * @param id
     * @param name
     * @return category with given id/name
     */
    private ActionsCategory myCategory(ActionsManager manager, String id, String name) {
        ActionsCategory category = (ActionsCategory)manager.getActionFor(id); // getCategory(id);
        if (category == null) {
            category = new MDActionsCategory(id, name);
            category.setNested(true);
            manager.addCategory(0, category);
        }
        return category;
    }

    /**
     * this should be used to add actions that're possible when user right
     * clicks on a view<br/>
     * it parses the single view, gets any document model that'll result in
     * running script, editable table, validation rule
     * 
     * @param parent
     * @param e
     */
    private boolean addViewQueryActions(ActionsCategory parent, NamedElement e) {
        DocumentGenerator dg = new DocumentGenerator(e, null, null);
        Document dge = dg.parseDocument(true, false);
        CollectActionsVisitor cav = new CollectActionsVisitor();
        dge.accept(cav);

        boolean added = false;
        if (cav.getActions().size() > 0) {
            for (MDAction a: cav.getActions()) {
                parent.addAction(a);
            }
            added = true;
        }
        parent.setNested(true);
        return added;
    }

}
