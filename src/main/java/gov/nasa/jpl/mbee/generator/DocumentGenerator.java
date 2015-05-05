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
package gov.nasa.jpl.mbee.generator;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.model.BulletedList;
import gov.nasa.jpl.mbee.model.CombinedMatrix;
import gov.nasa.jpl.mbee.model.Container;
import gov.nasa.jpl.mbee.model.CustomTable;
import gov.nasa.jpl.mbee.model.DependencyMatrix;
import gov.nasa.jpl.mbee.model.DocGenElement;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.model.GenericTable;
import gov.nasa.jpl.mbee.model.Image;
import gov.nasa.jpl.mbee.model.LibraryMapping;
import gov.nasa.jpl.mbee.model.MissionMapping;
import gov.nasa.jpl.mbee.model.Paragraph;
import gov.nasa.jpl.mbee.model.PropertiesTableByAttributes;
import gov.nasa.jpl.mbee.model.Query;
import gov.nasa.jpl.mbee.model.Section;
import gov.nasa.jpl.mbee.model.TableStructure;
import gov.nasa.jpl.mbee.model.UserScript;
import gov.nasa.jpl.mbee.model.ViewpointConstraint;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ForkNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * <p>
 * Given the document head or a view, generates the document element model
 * classes and structure in gov.nasa.jpl.mbee.model.
 * </p>
 * 
 * @author dlam
 * 
 */
public class DocumentGenerator {

    private GenerationContext context;
    private Element           start;
    private Document          doc;
    private Stereotype        sysmlview = Utils.getViewStereotype();
    private Stereotype        product;
    private Stereotype        conforms  = Utils.getConformsStereotype();
    private Stereotype        ourConforms = Utils.getSysML14ConformsStereotype();//StereotypesHelper.getStereotype(Application.getInstance().getProject(), "SysML1.4.Conforms");
    private Stereotype        md18expose = Utils.get18ExposeStereotype();
    private Stereotype        ourExpose = Utils.getExposeStereotype();
    private boolean hierarchyOnly;
    
    public DocumentGenerator(Element e, DocumentValidator dv, PrintWriter wlog) {
        start = e;
        product = Utils.getProductStereotype();
        doc = new Document();
        context = new GenerationContext(new Stack<List<Object>>(), null, dv, Application.getInstance()
                .getGUILog());
    }

    public DocumentGenerator(Element e, PrintWriter wlog) {
        start = e;
        product = Utils.getProductStereotype();
        doc = new Document();
        context = new GenerationContext(new Stack<List<Object>>(), null, Application.getInstance()
                .getGUILog());
    }

    public Document parseDocument() {
        return this.parseDocument(false, true, false);
    }

    public Document getDocument() {
        return this.doc;
    }
    
    /**
     * singleView: whether to only parse the passed in view recurse: only if
     * singleView is true, whether to process all children views these options
     * are to accommodate normal docgen to docbook xml and view editor export
     * options
     */
    public Document parseDocument(boolean singleView, boolean recurse, boolean hierarchyOnly) {
        this.hierarchyOnly = hierarchyOnly;
        if (StereotypesHelper.hasStereotypeOrDerived(start, sysmlview)) {
            if (start instanceof Package
                    || start instanceof Diagram
                    //|| StereotypesHelper.hasStereotype(start, DocGen3Profile.documentViewStereotype,
                     //       "Document Profile")
                    || GeneratorUtils.findStereotypedRelationship(start, DocGen3Profile.firstStereotype) != null
                    || GeneratorUtils.findStereotypedRelationship(start, DocGen3Profile.nextStereotype) != null
                    || GeneratorUtils.findStereotypedRelationship(start, DocGen3Profile.nosectionStereotype) != null) {
                ViewParser vp = new ViewParser(this, singleView, recurse, doc, start);
                vp.parse();
            } else if (start instanceof Class){
                ProductViewParser vp = new ProductViewParser(this, singleView, recurse, doc, start);
                vp.parse();
            }
        } else if (StereotypesHelper.hasStereotypeOrDerived(start, DocGen3Profile.documentStereotype)
                && start instanceof Activity)
            parseActivityOrStructuredNode(start, doc);
        else {

        }
        docMetadata();
        for (DocGenElement e: doc.getChildren()) {
            if (e instanceof Section)
                ((Section)e).isChapter(true);
        }
        return doc;
    }

    public Section parseView(Element view) {
        Element viewpoint = GeneratorUtils.findStereotypedRelationship(view, conforms);
        if (viewpoint == null)
            viewpoint = GeneratorUtils.findStereotypedRelationship(view, ourConforms);
        Section viewSection = new Section(); // Section is a misnomer, should be
                                             // View
        viewSection.setView(true);

        if (StereotypesHelper.hasStereotype(view, DocGen3Profile.appendixViewStereotype))
            viewSection.isAppendix(true);
        viewSection.setViewpoint(viewpoint);
        
        List<Element> elementImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(
                view, ElementImport.class, 1, 1);
        List<Element> packageImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(
                view, PackageImport.class, 1, 1);
        List<Element> expose = Utils.collectDirectedRelatedElementsByRelationshipStereotype(
                view, ourExpose, 1, false, 1);
        if (md18expose != null)
            expose.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(view, md18expose, 1, false, 1));
        List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                view, DocGen3Profile.oldQueriesStereotype, 1, false, 1);
        if (elementImports == null)
            elementImports = new ArrayList<Element>();
        if (packageImports != null)
            elementImports.addAll(packageImports);
        if (expose != null)
            elementImports.addAll(expose); // all three import/queries
                                           // relationships are
                                           // interpreted the same
        if (queries != null)
            elementImports.addAll(queries); // all three import/queries
                                            // relationships are
                                            // interpreted the same
        if (view instanceof Class) {
            for (TypedElement te: ((Class)view).get_typedElementOfType()) {
                if (te instanceof Property && ((Property)te).getAggregation() == AggregationKindEnum.COMPOSITE) {
                    elementImports.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(te, 
                            ourExpose, 1, false, 1));
                }
            }
        }
        viewSection.setExposes(elementImports);
        
        if (!hierarchyOnly) {
        if (viewpoint != null && viewpoint instanceof Class) { // view conforms
                                                               // to a viewpoint
            if (!(view instanceof Diagram)) { // if it's a diagram, people most
                                              // likely put image query in
                                              // viewpoint already. this is to
                                              // prevent showing duplicate
                                              // documentation
                String viewDoc = ModelHelper.getComment(view);
                if (viewDoc != null) {
                    Paragraph para = new Paragraph(viewDoc);
                    //if ((Boolean)GeneratorUtils.getObjectProperty(view, DocGen3Profile.editableChoosable, "editable", true)) {
                        para.setDgElement(view);
                        para.setFrom(From.DOCUMENTATION);
                    //}
                    viewSection.addElement(para);
                }
            }
            Collection<Behavior> viewpointBehavior = ((Class)viewpoint).getOwnedBehavior();
            Behavior b = null;
            if (viewpointBehavior.size() > 0)
                b = viewpointBehavior.iterator().next();
            else {
                // viewpoint can inherit other viewpoints, if this viewpoint has
                // no behavior, check inherited behaviors
                Class now = (Class)viewpoint;
                while (now != null) {
                    if (!now.getSuperClass().isEmpty()) {
                        now = now.getSuperClass().iterator().next();
                        if (now.getOwnedBehavior().size() > 0) {
                            b = now.getOwnedBehavior().iterator().next();
                            break;
                        }
                    } else {
                        now = null;
                    }
                }
            }
            if (b != null) { // parse and execute viewpoint behavior, giving it
                             // the imported/queried elements
                Boolean addVPElements = (Boolean)GeneratorUtils.getObjectProperty(b,
                        DocGen3Profile.methodStereotype, "includeViewpointElements", false);;
                
                if (elementImports.isEmpty())
                    elementImports.add(view); // if view does not import/query
                                              // anything, give the view element
                                              // itself to the viewpoint
                if (addVPElements) {
                    elementImports.add(viewpoint);
                    elementImports.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                            viewpoint, "AddressedTo", 1, false, 1));
                    elementImports.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                            viewpoint, "Covers", 1, false, 1));
                    elementImports.add(b);
                    elementImports.add(view);
                }
                context.pushTargets(Utils2.asList( Utils.removeDuplicates(elementImports), Object.class));
                if (b instanceof Activity) {
                    parseActivityOrStructuredNode(b, viewSection);
                }
                context.popTargets();
            }
        } else { // view does not conform to a viewpoint, apply default behavior
            if (view instanceof Diagram) { // if a diagram, show diagram and documentation
                Image image = new Image();
                List<Object> images = new ArrayList<Object>();
                images.add(view);
                image.setTargets(images);
                String caption = (String)StereotypesHelper.getStereotypePropertyFirst(view,
                        DocGen3Profile.dgviewStereotype, "caption");
                // Check for old stereotype name for backwards compatibility
                if (caption == null)
                    caption = (String)StereotypesHelper.getStereotypePropertyFirst(view,
                            DocGen3Profile.oldDgviewStereotype, "caption");
                List<String> captions = new ArrayList<String>();
                captions.add(caption);
                image.setCaptions(captions);
                image.setShowCaptions(true);
                viewSection.addElement(image);
            } else { // just show documentation
                String viewDoc = ModelHelper.getComment(view);
                if (viewDoc != null) {
                    Paragraph para = new Paragraph(viewDoc);
                    para.setDgElement(view);
                    para.setFrom(From.DOCUMENTATION);
                    viewSection.addElement(para);
                }
                //if (expose.size() == 1 && expose.get(0) instanceof Diagram) {
                for (Element ex: elementImports) {
                    if (ex instanceof Diagram) {
                        Image image = new Image();
                        List<Object> images = new ArrayList<Object>();
                        images.add(ex);
                        image.setTargets(images);
                        viewSection.addElement(image);
                    }
                }
            }
        }
        }
        viewSection.setDgElement(view);
        viewSection.setId(view.getID());
        viewSection.setTitle(((NamedElement)view).getName());
        return viewSection;
    }

    private void docMetadata() {
        GeneratorUtils.docMetadata(doc, start);
    }

    /**
     * parses activity/structured node - these usually indicate a new context of
     * target elements
     * 
     * @param a
     * @param parent
     * @return the output of parsing the activity
     */
    @SuppressWarnings("unchecked")
    public Object parseActivityOrStructuredNode(Element a, Container parent) {
        if (a == null || parent == null)
            return null;
        Debug.outln("parseActivityOrStructuredNode( " + a.getHumanName() + ", " + a.getID() + ", "
                + parent.getStringIfEmpty() + ")");
        InitialNode in = GeneratorUtils.findInitialNode(a);
        if (in == null)
            return null;
        Collection<ActivityEdge> outs = in.getOutgoing();
        int pushed = 0;
        ActivityNode next2 = in;
        Object lastResults = null;
        Object parseResults = null;
        while (outs != null && outs.size() == 1) {
            parseResults = null;
            ActivityNode next = outs.iterator().next().getTarget();
            Debug.outln("next = " + next.getHumanName() + ", " + next.getID());
            next2 = null;
            boolean evaluatedConstraintsForNext = false;
            if (next instanceof CallBehaviorAction
                    || next instanceof StructuredActivityNode
                    && StereotypesHelper
                            .hasStereotypeOrDerived(next, DocGen3Profile.tableStructureStereotype)) {
                Behavior b = (next instanceof CallBehaviorAction) ? ((CallBehaviorAction)next).getBehavior()
                        : null;
                if (StereotypesHelper.hasStereotypeOrDerived(next, DocGen3Profile.sectionStereotype)
                        || b != null
                        && StereotypesHelper.hasStereotypeOrDerived(b, DocGen3Profile.sectionStereotype)) {
                    parseResults = parseSection((CallBehaviorAction)next, parent);
                    next2 = next;
                } else if (StereotypesHelper.hasStereotypeOrDerived(next, DocGen3Profile.templateStereotype)
                        || b != null
                        && StereotypesHelper.hasStereotypeOrDerived(b, DocGen3Profile.templateStereotype)) {
                    parseResults = parseQuery(next, parent);
                    next2 = next;
                } else if (StereotypesHelper.hasStereotypeOrDerived(next,
                        DocGen3Profile.collectFilterStereotype)
                        || b != null
                        && StereotypesHelper
                                .hasStereotypeOrDerived(b, DocGen3Profile.collectFilterStereotype)) {
                    CollectFilterParser.setContext(context);
                    List<Element> results = CollectFilterParser.startCollectAndFilterSequence(next, null);
                    parseResults = results;
                    this.context.pushTargets( Utils2.asList( results, Object.class ) );
                    pushed++;
                    next2 = context.getCurrentNode();
                    evaluatedConstraintsForNext = true;
                }
            } else if (next instanceof StructuredActivityNode) {
                Boolean loop = (Boolean)GeneratorUtils.getObjectProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "loop", false);
                Boolean ignore = (Boolean)GeneratorUtils.getObjectProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "ignore", false);
                Boolean createSections = (Boolean)GeneratorUtils.getObjectProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "createSections", false);
                Boolean useContextNameAsTitle = (Boolean)GeneratorUtils.getObjectProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "useSectionNameAsTitle", false);
                String titlePrefix = (String)GeneratorUtils.getObjectProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "titlePrefix", "");
                String titleSuffix = (String)GeneratorUtils.getObjectProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "titleSuffix", "");
                List<String> titles = (List<String>)GeneratorUtils.getListProperty(next,
                        DocGen3Profile.structuredQueryStereotype, "titles", null);
                if (titles == null)
                    titles = new ArrayList<String>();

                List<Object> targets = getTargets(next, this.context);
                if (!ignore) {
                    if (loop) {
                        int count = 0;
                        for (Object e: targets) {
                            List<Object> target = new ArrayList<Object>();
                            target.add(e);
                            this.context.pushTargets(target);
                            Container con = parent;
                            if (createSections) {
                                Section sec = new Section();
                                if (titles != null && titles.size() > count)
                                    sec.setTitle(titles.get(count));
                                else if (e instanceof NamedElement)
                                    sec.setTitle(((NamedElement)e).getName());
                                sec.setTitlePrefix(titlePrefix);
                                sec.setTitleSuffix(titleSuffix);
                                sec.setDgElement(next);
                                parent.addElement(sec);
                                con = sec;
                            }
                            parseResults = parseActivityOrStructuredNode(next, con);
                            this.context.popTargets();
                            count++;
                        }
                    } else {
                        this.context.pushTargets(targets);
                        Container con = parent;
                        if (createSections) {
                            Section sec = new Section();
                            if (titles != null && titles.size() > 0)
                                sec.setTitle(titles.get(0));
                            else if (!next.getName().equals(""))
                                sec.setTitle(next.getName());
                            sec.setUseContextNameAsTitle(useContextNameAsTitle);
                            sec.setDgElement(next);
                            sec.setTitlePrefix(titlePrefix);
                            sec.setTitleSuffix(titleSuffix);
                            parent.addElement(sec);
                            con = sec;
                        }
                        parseResults = parseActivityOrStructuredNode(next, con);
                        this.context.popTargets();
                    }
                }
                next2 = next;
            } else if (next instanceof ForkNode
                    && StereotypesHelper.hasStereotype(next, DocGen3Profile.parallel)) {// REVIEW
                                                                                        // --
                                                                                        // hasStereotypeOrDerived()?
                CollectFilterParser.setContext(context);
                List<Element> results = CollectFilterParser.startCollectAndFilterSequence(next, null);
                parseResults = results;
                this.context.pushTargets( Utils2.asList( results, Object.class ) );
                pushed++;
                next2 = context.getCurrentNode();
            }
            if (next2 == null) {
                next2 = next;
            }
            if (parseResults == null)
                parseResults = this.context.peekTargets();
            if (parseResults != null)
                lastResults = parseResults;
            // evaluate constraints on results
            if (!evaluatedConstraintsForNext) {
                DocumentValidator.evaluateConstraints(next, parseResults, context, true, true);
            }
            outs = next2.getOutgoing();
            Debug.outln("outs = " + MoreToString.Helper.toLongString(outs) + " for next2 = "
                    + next2.getHumanName() + ", " + next2.getID());
        }
        while (pushed > 0) {
            this.context.popTargets();
            pushed--;
        }
        return lastResults;
    }

    public static List<Object> getTargets(Object obj, GenerationContext context) {
        ArrayList<Object > list = new ArrayList<Object>();
        if (obj instanceof ActivityNode) {
            list.addAll(getTargets((ActivityNode)obj, context));
        }
        if (obj instanceof Collection) {
            for (Object o: (Collection<?>)obj) {
                list.addAll(getTargets(o, context));
            }
        }
        // REVIEW -- error if obj not one of above
        return list;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getTargets(ActivityNode next, GenerationContext context) {
        // TODO -- REVIEW -- shouldn't this be a list of Objects?!
        List<Object> targets = StereotypesHelper.getStereotypePropertyValue(next,
                DocGen3Profile.templateStereotype, "targets");
        if (targets == null || targets.isEmpty()) {
            List<Element> elements =
                    Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next,
                    DocGen3Profile.queriesStereotype, 1, false, 1);            
            elements.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next,
                    DocGen3Profile.oldQueriesStereotype, 1, false, 1));
            targets = Utils2.asList( elements, Object.class) ;
        }
        if ((targets == null || targets.isEmpty()) && next instanceof CallBehaviorAction
                && ((CallBehaviorAction)next).getBehavior() != null) {
            targets = StereotypesHelper.getStereotypePropertyValue(
                    ((CallBehaviorAction)next).getBehavior(), DocGen3Profile.templateStereotype, "targets");
            if (targets == null || targets.isEmpty()) {
                List<Element> elements =
                        Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                        ((CallBehaviorAction)next).getBehavior(), DocGen3Profile.queriesStereotype, 1, false,
                        1);
                elements.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                        ((CallBehaviorAction)next).getBehavior(), DocGen3Profile.oldQueriesStereotype, 1,
                        false, 1));
                targets = Utils2.asList( elements, Object.class) ;
            }
        }
        if (targets.isEmpty() && !context.targetsEmpty()) {
            targets = context.peekTargets();
        }
        return Utils.removeDuplicates(targets);
    }

    // this is a section made using an activity and should be discouraged since
    // it won't show up on view editor
    private List<Section> parseSection(CallBehaviorAction cba, Container parent) {
        List<Section> sections = new ArrayList<Section>();
        String titlePrefix = (String)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.sectionStereotype,
                "titlePrefix", "");
        String titleSuffix = (String)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.sectionStereotype,
                "titleSuffix", "");
        Boolean useContextNameAsTitle = (Boolean)GeneratorUtils.getObjectProperty(cba,
                DocGen3Profile.sectionStereotype, "useSectionNameAsTitle", false);
        String stringIfEmpty = (String)GeneratorUtils.getObjectProperty(cba,
                DocGen3Profile.sectionStereotype, "stringIfEmpty", "");
        Boolean skipIfEmpty = (Boolean)GeneratorUtils.getObjectProperty(cba,
                DocGen3Profile.sectionStereotype, "skipIfEmpty", false);
        Boolean ignore = (Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.sectionStereotype,
                "ignore", false);
        Boolean loop = (Boolean)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.sectionStereotype,
                "loop", false);
        Boolean isAppendix = false;

        if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.appendixStereotype)
                || (cba.getBehavior() != null && StereotypesHelper.hasStereotype(cba.getBehavior(),
                        DocGen3Profile.appendixStereotype)))
            isAppendix = true;
        String title = (String)GeneratorUtils.getObjectProperty(cba, DocGen3Profile.sectionStereotype,
                "title", "");
        if (title == null || title.equals("")) {
            title = cba.getName();
            if (title.equals("") && cba.getBehavior() != null)
                title = cba.getBehavior().getName();
        }
        if (loop) {
            if (!context.targetsEmpty()) {
                for (Object e: context.peekTargets()) {
                    List<Object> target = new ArrayList<Object>();
                    target.add(e);
                    context.pushTargets(target);
                    Section sec = new Section();
                    sec.isAppendix(isAppendix);
                    sec.setTitlePrefix(titlePrefix);
                    sec.setTitleSuffix(titleSuffix);
                    if (e instanceof NamedElement)
                        sec.setTitle(((NamedElement)e).getName());
                    else
                        sec.setTitle(title);
                    sec.setStringIfEmpty(stringIfEmpty);
                    sec.setSkipIfEmpty(skipIfEmpty);
                    sec.setIgnore(ignore);
                    sec.setUseContextNameAsTitle(useContextNameAsTitle);
                    parent.addElement(sec);
                    sections.add(sec);
                    parseActivityOrStructuredNode(cba.getBehavior(), sec);

                    context.popTargets();
                }
            }
        } else {
            Section sec = new Section();
            sec.isAppendix(isAppendix);
            sec.setTitlePrefix(titlePrefix);
            sec.setTitleSuffix(titleSuffix);
            sec.setTitle(title);
            sec.setStringIfEmpty(stringIfEmpty);
            sec.setSkipIfEmpty(skipIfEmpty);
            sec.setIgnore(ignore);
            sec.setUseContextNameAsTitle(useContextNameAsTitle);
            parent.addElement(sec);
            sections.add(sec);
            parseActivityOrStructuredNode(cba.getBehavior(), sec);
        }
        return sections;
    }

    @SuppressWarnings("unchecked")
    private Object parseQuery(ActivityNode an, Container parent) {
        Object result = null;
        String titlePrefix = (String)GeneratorUtils.getObjectProperty(an, DocGen3Profile.templateStereotype,
                "titlePrefix", "");
        String titleSuffix = (String)GeneratorUtils.getObjectProperty(an, DocGen3Profile.templateStereotype,
                "titleSuffix", "");
        Boolean useContextNameAsTitle = (Boolean)GeneratorUtils.getObjectProperty(an,
                DocGen3Profile.templateStereotype, "useSectionNameAsTitle", false);
        Boolean ignore = (Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.templateStereotype,
                "ignore", false);
        Boolean loop = (Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.templateStereotype,
                "loop", false);
        List<String> titles = (List<String>)GeneratorUtils.getListProperty(an,
                DocGen3Profile.templateStereotype, "titles", new ArrayList<String>());
        boolean structured = false;
        if (StereotypesHelper.hasStereotypeOrDerived(an, DocGen3Profile.structuredQueryStereotype)
                || (an instanceof CallBehaviorAction && ((CallBehaviorAction)an).getBehavior() != null && StereotypesHelper
                        .hasStereotypeOrDerived(((CallBehaviorAction)an).getBehavior(),
                                DocGen3Profile.structuredQueryStereotype)))
            structured = true;
        List<Object> targets = getTargets(an, getContext());
        if (structured && !ignore && an instanceof CallBehaviorAction) {
            Boolean createSections = (Boolean)GeneratorUtils.getObjectProperty(an,
                    DocGen3Profile.structuredQueryStereotype, "createSections", false);
            if (loop) {
                List<Section> sections = new ArrayList<Section>();
                int count = 0;
                for (Object e: targets) {
                    List<Object> target = new ArrayList<Object>();
                    target.add(e);
                    this.context.pushTargets(target);
                    Container con = parent;
                    if (createSections) {
                        Section sec = new Section();
                        if (titles != null && titles.size() > count)
                            sec.setTitle(titles.get(count));
                        else if (e instanceof NamedElement)
                            sec.setTitle(((NamedElement)e).getName());
                        sec.setTitlePrefix(titlePrefix);
                        sec.setTitleSuffix(titleSuffix);
                        sec.setDgElement(an);
                        parent.addElement(sec);
                        sections.add(sec);
                        con = sec;
                    }
                    parseActivityOrStructuredNode(((CallBehaviorAction)an).getBehavior(), con);
                    this.context.popTargets();
                }
                result = sections;
            } else {
                this.context.pushTargets(targets);
                Container con = parent;
                if (createSections) {
                    Section sec = new Section();
                    if (titles.size() > 0)
                        sec.setTitle(titles.get(0));
                    else if (!an.getName().equals(""))
                        sec.setTitle(an.getName());
                    else if (!((CallBehaviorAction)an).getBehavior().getName().equals(""))
                        sec.setTitle(((CallBehaviorAction)an).getBehavior().getName());
                    sec.setUseContextNameAsTitle(useContextNameAsTitle);
                    sec.setDgElement(an);
                    sec.setTitlePrefix(titlePrefix);
                    sec.setTitleSuffix(titleSuffix);
                    parent.addElement(sec);
                    result = sec;
                    con = sec;
                }

                Object res = parseActivityOrStructuredNode(((CallBehaviorAction)an).getBehavior(), con);
                if (result == null)
                    result = res;
                this.context.popTargets();
            }
        } else {
            Query dge = parseTemplate(an);
            if (dge != null) {
                dge.setDgElement(an);
                dge.setTargets(targets);
                dge.setTitles(titles);
                dge.setTitlePrefix(titlePrefix);
                dge.setTitleSuffix(titleSuffix);
                dge.setUseContextNameAsTitle(useContextNameAsTitle);
                dge.setIgnore(ignore);
                dge.setLoop(loop);
                dge.initialize();
                dge.parse();
                parent.addElement(dge);
                result = dge;
            }
        }

        return result;
    }

    /**
     * parses query actions into classes in gov.nasa.jpl.mgss.mbee.docgen.model
     * - creates class representation of the queries There's gotta be a way to
     * make this less ugly... by sweeping the ugliness under multiple rugs!
     * 
     * @param an
     * @return
     */
    private Query parseTemplate(ActivityNode an) {

        Query dge = null;
        if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.imageStereotype)) {
            dge = new Image();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.paragraphStereotype)) {
            dge = new Paragraph(context.getValidator());
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.bulletedListStereotype)) {
            dge = new BulletedList();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.dependencyMatrixStereotype)) {
            dge = new DependencyMatrix();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.genericTableStereotype)) {
            dge = new GenericTable();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.tableStructureStereotype)) {
            // Get all the variables or whatever
            dge = new TableStructure(context.getValidator());
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.combinedMatrixStereotype)) {
            dge = new CombinedMatrix();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.customTableStereotype)) {
            dge = new CustomTable();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.userScriptStereotype, true)) {
            dge = new UserScript();
        } else if (GeneratorUtils.hasStereotypeByString(an,
                DocGen3Profile.propertiesTableByAttributesStereotype)) {
            dge = new PropertiesTableByAttributes();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.missionMappingStereotype)) {
            dge = new MissionMapping();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.libraryChooserStereotype)) {
            dge = new LibraryMapping();
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.viewpointConstraintStereotype)) {
            dge = new ViewpointConstraint(context.getValidator());
        } else if (GeneratorUtils.hasStereotypeByString(an, DocGen3Profile.javaExtensionStereotype, true)) {
            Element e = an;
            if (!StereotypesHelper.hasStereotypeOrDerived(an, DocGen3Profile.javaExtensionStereotype)) {
                if (an instanceof CallBehaviorAction
                        && ((CallBehaviorAction)an).getBehavior() != null
                        && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)an).getBehavior(),
                                DocGen3Profile.javaExtensionStereotype))
                    e = ((CallBehaviorAction)an).getBehavior();
            }
            Stereotype s = StereotypesHelper.checkForDerivedStereotype(e,
                    DocGen3Profile.javaExtensionStereotype);
            String javaClazz = s.getName();
            if (DocGenPlugin.extensionsClassloader != null) {
                try {
                    java.lang.Class<?> clazz = java.lang.Class.forName(javaClazz, true,
                            DocGenPlugin.extensionsClassloader);
                    dge = (Query)clazz.newInstance();
                } catch (Exception e1) {
                    Application.getInstance().getGUILog()
                            .log("[ERROR] Cannot instantiate Java extension class " + javaClazz);
                    e1.printStackTrace();
                }
            }
        }
        return dge;
    }

    public GenerationContext getContext() {
        return context;
    }

    public Stereotype getProductStereotype() {
        return product;
    }

    public Stereotype getView() {
        return sysmlview;
    }

}
