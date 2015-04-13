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

import gov.nasa.jpl.mbee.actions.ComponentToClassRefactorWithIDAction;
import gov.nasa.jpl.mbee.actions.PublishDocWebAction;
import gov.nasa.jpl.mbee.actions.ClassToComponentRefactorWithIDAction;
import gov.nasa.jpl.mbee.actions.ViewViewCommentsAction;
import gov.nasa.jpl.mbee.actions.docgen.GenerateDocumentAction;
import gov.nasa.jpl.mbee.actions.docgen.InstanceViewpointAction;
import gov.nasa.jpl.mbee.actions.docgen.MigrateToClassViewAction;
import gov.nasa.jpl.mbee.actions.docgen.NumberAssociationAction;
import gov.nasa.jpl.mbee.actions.docgen.NumberDependencyAction;
import gov.nasa.jpl.mbee.actions.docgen.RunUserScriptAction;
import gov.nasa.jpl.mbee.actions.docgen.RunUserValidationScriptAction;
import gov.nasa.jpl.mbee.actions.docgen.ValidateDocument3Action;
import gov.nasa.jpl.mbee.actions.docgen.ValidateOldDocgen;
import gov.nasa.jpl.mbee.actions.docgen.ValidateViewStructureAction;
import gov.nasa.jpl.mbee.actions.docgen.ViewDocument3Action;
import gov.nasa.jpl.mbee.actions.ems.ExportAllDocuments;
import gov.nasa.jpl.mbee.actions.ems.ExportModelAction;
import gov.nasa.jpl.mbee.actions.ems.ExportViewAction;
import gov.nasa.jpl.mbee.actions.ems.InitializeProjectAction;
import gov.nasa.jpl.mbee.actions.ems.ValidateHierarchyAction;
import gov.nasa.jpl.mbee.actions.ems.ValidateModelAction;
import gov.nasa.jpl.mbee.actions.ems.ValidateViewAction;
import gov.nasa.jpl.mbee.actions.ems.ValidateViewRecursiveAction;
import gov.nasa.jpl.mbee.actions.vieweditor.SynchronizeViewAction;
import gov.nasa.jpl.mbee.actions.vieweditor.SynchronizeViewRecursiveAction;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.model.CollectActionsVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.UserScript;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.browser.Tree;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DocGenConfigurator implements BrowserContextAMConfigurator, DiagramContextAMConfigurator {

    private Set<ActionsManager> viewQueryCalled = new HashSet<ActionsManager>();
    
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
        List<Element> elements = new ArrayList<Element>();
        for (Node node: browser.getSelectedNodes()) {
            if (node == null)
                continue;;
            Object ob = node.getUserObject();
            if (!(ob instanceof Element))
                continue;
            elements.add((Element)ob);
        }
        addElementActions(manager, (Element)o, elements);
    }

    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram,
            PresentationElement[] selected, PresentationElement requestor) {
        if ( repainting() ) return;
        if (requestor != null) {
            Collection<Element> es = new ArrayList<Element>();
            for (PresentationElement pe: selected) {
                if (pe.getElement() != null)
                    es.add(pe.getElement());
            }
            Element e = requestor.getElement();
            if (e != null)
                addElementActions(manager, e, es);
        } else {
            addDiagramActions(manager, diagram);
        }
    }

    public static boolean repainting() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String lastMethod = "";
        for ( StackTraceElement traceElem : trace ) {
            if ( traceElem.getClassName().contains( "MainFrame") && ( traceElem.getMethodName().equals( "paint" ) || lastMethod.equals( "paintImmediately" ) ) ) {
                //Debug.outln( "@@@ repainting() = true" );
                return true;
            }
            if ( traceElem.getClassName().endsWith( "RepaintManager") && traceElem.getMethodName().equals( "paint" ) ) {
                //Debug.outln( "@@@ repainting() = true" );
                return true;
            }
            //Debug.outln( "class name = " + traceElem.getClassName() + ", method name = " + traceElem.getMethodName() + ", last method name = " + lastMethod );
            lastMethod = traceElem.getMethodName();
        }
        //Debug.outln( "@@@ repainting() = false:" );
        //Debug.outln( MoreToString.Helper.toString( trace ) );
        return false;
    }

    private void addElementActions(ActionsManager manager, Element e, Collection<Element> es) {
        Project prj = Project.getProject(e);
        if (prj == null)
            return;
        Stereotype sysmlview = Utils.getViewStereotype();
        Stereotype sysmlviewpoint = Utils.getViewpointStereotype();
        Stereotype documentView = Utils.getProductStereotype();
        if (e == null)
            return;

        // top-level context menu: Refactor With ID
        ActionsCategory refactorWithIDActionCat = myCategory(manager, "Refactor With ID", "Refactor With ID");       
        if(e instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class & !(e instanceof com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component)){
            if (manager.getActionFor(ClassToComponentRefactorWithIDAction.actionid) == null)
                refactorWithIDActionCat.addAction(new ClassToComponentRefactorWithIDAction(e));   
        }
        if(e instanceof com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component){
            if (manager.getActionFor(ComponentToClassRefactorWithIDAction.actionid) == null)
                refactorWithIDActionCat.addAction(new ComponentToClassRefactorWithIDAction(e));   
        }
        //manager.addCategory(refactorWithIDActionCat);

        if (ViewEditUtils.isPasswordSet()) {
            ActionsCategory modelLoad = myCategory(manager, "AlfrescoModel", "MMS");
            ActionsCategory models = getCategory(manager, "MMSModel", "MMSModel", modelLoad);
            if (MDUtils.isDeveloperMode()) {
                if (manager.getActionFor(ExportModelAction.actionid) == null)
                    models.addAction(new ExportModelAction(e));
                if (e instanceof Model && manager.getActionFor(InitializeProjectAction.actionid) == null)
                    models.addAction(new InitializeProjectAction());
            }
            if (manager.getActionFor(ValidateModelAction.actionid) == null)
                models.addAction(new ValidateModelAction(es, (Application.getInstance().getProject().getModel() == e) ? "Validate Models": "Validate Models"));
            if (e instanceof Package) {
                if (manager.getActionFor(ExportAllDocuments.actionid) == null)
                    models.addAction(new ExportAllDocuments(e));
            }
        }
        
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
            us.setTargets( Utils2.asList( targets, Object.class ) );
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
            us.setTargets( Utils2.asList( targets, Object.class ) );
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
                boolean added = addViewQueryActions(manager, category, (NamedElement)e);
                if (added)
                    manager.addCategory(0, category);
            }
            if (ViewEditUtils.isPasswordSet()) {
                ActionsCategory modelLoad2 = myCategory(manager, "AlfrescoModel", "MMS");
                ActionsCategory views = getCategory(manager, "MMSView", "MMSView", modelLoad2);

                NMAction action = manager.getActionFor(ValidateViewAction.actionid);
                if (action == null)
                    views.addAction(new ValidateViewAction(e));
                action = manager.getActionFor(ValidateViewRecursiveAction.actionid);
                if (action == null)
                    views.addAction(new ValidateViewRecursiveAction(e));
                if (StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
                    action = manager.getActionFor(ValidateHierarchyAction.actionid);
                    if (action == null)
                        modelLoad2.addAction(new ValidateHierarchyAction(e));
                }
                ActionsCategory viewsC = getCategory(manager, "MMSViewC", "MMSViewC", modelLoad2);

                action = manager.getActionFor("ExportView");
                if (action == null)
                    viewsC.addAction(new ExportViewAction(e, false));
                action = manager.getActionFor("ExportViewRecursive");
                if (action == null)
                    viewsC.addAction(new ExportViewAction(e, true));
            }
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

        if (e == Application.getInstance().getProject().getModel()) {
            NMAction act = null;
            ActionsCategory c = myCategory(manager, "DocGen", "DocGen");
            // DefaultPropertyResourceProvider pp = new
            // DefaultPropertyResourceProvider();
            act = manager.getActionFor(ValidateOldDocgen.actionid);
            if (act == null)
                c.addAction(new ValidateOldDocgen());
        }
        
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
            if (act == null && e instanceof Classifier)
                c.addAction(new ValidateViewStructureAction(e));

            act = manager.getActionFor(ViewDocument3Action.actionid);
            if (act == null)
                c.addAction(new ViewDocument3Action(e));

            act = manager.getActionFor(GenerateDocumentAction.actionid);
            if (act == null)
                c.addAction(new GenerateDocumentAction(e));

            if (StereotypesHelper.hasStereotypeOrDerived(e, documentView)) {
                //act = manager.getActionFor(PublishDocWebAction.actionid); 
                //if (act == null) 
                 //   c.addAction(new PublishDocWebAction((NamedElement)e));
                if (e instanceof Package) {
                    act = manager.getActionFor(NumberDependencyAction.actionid);
                    if (act == null)
                        c.addAction(new NumberDependencyAction(e));
                    act = manager.getActionFor(MigrateToClassViewAction.actionid);
                    if (act == null)
                        c.addAction(new MigrateToClassViewAction(e));
                }
                if (e instanceof Class) {
                    act = manager.getActionFor(NumberAssociationAction.actionid);
                    if (act == null)
                        c.addAction(new NumberAssociationAction((Class)e));
                }
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
 /*   private void addEditableViewActions(ActionsCategory parent, NamedElement e) {
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
*/
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
    
    private ActionsCategory getCategory(ActionsManager manager, String id, String name, ActionsCategory parent) {
        ActionsCategory category = (ActionsCategory)manager.getActionFor(id);
        if (category == null) {
            category = new MDActionsCategory(id, name);
            category.setNested(false);
            parent.addAction(category);
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
    private boolean addViewQueryActions(ActionsManager manager, ActionsCategory parent, NamedElement e) {
        if (viewQueryCalled.contains(manager))
            return false;
        DocumentGenerator dg = new DocumentGenerator(e, null, null);
        Document dge = dg.parseDocument(true, false, false);
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
        viewQueryCalled.clear();
        viewQueryCalled.add(manager);
        return added;
    }

}
