package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ForkNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.From;
import gov.nasa.jpl.mbee.mdk.model.*;
import gov.nasa.jpl.mbee.mdk.util.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * <p>
 * Given the document head or a view, generates the document element model
 * classes and structure in gov.nasa.jpl.mbee.mdk.model.
 * </p>
 *
 * @author dlam
 */
public class DocumentGenerator {

    private GenerationContext context;
    private Project project;
    private Element start;
    private Document doc;
    private Stereotype sysmlview,
            product,
            conforms,
            ourConforms,
            md18expose,
            ourExpose;
    private boolean hierarchyOnly;
    private boolean addViewDoc = true; //whether to add default view doc

    public DocumentGenerator(Element e, PrintWriter wlog) {
        this(e, null, wlog, true);
//        start = e;
//        product = Utils.getProductStereotype();
//        doc = new Document();
//        context = new GenerationContext(new Stack<>(), null, Application.getInstance().getGUILog());
    }

    public DocumentGenerator(Element e, DocumentValidator dv, PrintWriter wlog) {
        this(e, dv, wlog, true);
    }

    public DocumentGenerator(Element e, DocumentValidator dv, PrintWriter wlog, boolean addViewDoc) {
        this.start = e;
        this.project = Project.getProject(e);
        this.product = Utils.getProductStereotype(project);
        this.sysmlview = Utils.getViewStereotype(project);
        this.conforms = Utils.getConformsStereotype(project);
        this.ourConforms = Utils.getSysML14ConformsStereotype(project);//StereotypesHelper.getStereotype(Application.getInstance().getProject(), "SysML1.4.Conforms");
        this.md18expose = Utils.get18ExposeStereotype(project);
        this.ourExpose = Utils.getExposeStereotype(project);
        this.doc = new Document();
        this.context = new GenerationContext(new Stack<>(), null, dv, Application.getInstance().getGUILog());
        this.addViewDoc = addViewDoc;
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
                    //|| StereotypesHelper.hasStereotype(start, DocGenProfile.documentViewStereotype,
                    //       "Document Profile")
                    || GeneratorUtils.findStereotypedRelationship(start, DocGenProfile.firstStereotype) != null
                    || GeneratorUtils.findStereotypedRelationship(start, DocGenProfile.nextStereotype) != null
                    || GeneratorUtils.findStereotypedRelationship(start, DocGenProfile.nosectionStereotype) != null) {
                ViewParser vp = new ViewParser(this, singleView, recurse, doc, start);
                vp.parse();
            }
            else if (start instanceof Class) {
                ProductViewParser vp = new ProductViewParser(this, singleView, recurse, doc, start);
                vp.parse();
            }
        }
        else if (StereotypesHelper.hasStereotypeOrDerived(start, DocGenProfile.documentStereotype)
                && start instanceof Activity) {
            parseActivityOrStructuredNode(start, doc);
        }
        else {

        }
        docMetadata();
        for (DocGenElement e : doc.getChildren()) {
            if (e instanceof Section) {
                ((Section) e).isChapter(true);
            }
        }
        return doc;
    }

    public Section parseView(Element view) {
        Element viewpoint = GeneratorUtils.findStereotypedRelationship(view, conforms);
        if (viewpoint == null) {
            viewpoint = GeneratorUtils.findStereotypedRelationship(view, ourConforms);
        }
        Section viewSection = new Section(); // Section is a misnomer, should be
        // View
        viewSection.setView(true);

        if (StereotypesHelper.hasStereotype(view, DocGenProfile.appendixViewStereotype)) {
            viewSection.isAppendix(true);
        }
        viewSection.setViewpoint(viewpoint);

        List<Element> elementImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(
                view, ElementImport.class, 1, 1);
        List<Element> packageImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(
                view, PackageImport.class, 1, 1);
        List<Element> expose = Utils.collectDirectedRelatedElementsByRelationshipStereotype(
                view, ourExpose, 1, false, 1);
        if (md18expose != null) {
            expose.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(view, md18expose, 1, false, 1));
        }
        List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                view, DocGenProfile.oldQueriesStereotype, 1, false, 1);
        if (elementImports == null) {
            elementImports = new ArrayList<Element>();
        }
        if (packageImports != null) {
            elementImports.addAll(packageImports);
        }
        if (expose != null) {
            elementImports.addAll(expose); // all three import/queries
        }
        // relationships are
        // interpreted the same
        if (queries != null) {
            elementImports.addAll(queries); // all three import/queries
        }
        // relationships are
        // interpreted the same
        if (view instanceof Class) {
            for (TypedElement te : ((Class) view).get_typedElementOfType()) {
                if (te instanceof Property && ((Property) te).getAggregation() == AggregationKindEnum.COMPOSITE) {
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
                    if (viewDoc != null && addViewDoc) {
                        Paragraph para = new Paragraph(viewDoc);
                        //if ((Boolean)GeneratorUtils.getObjectProperty(view, DocGenProfile.editableChoosable, "editable", true)) {
                        para.setDgElement(view);
                        para.setFrom(From.DOCUMENTATION);
                        //}
                        viewSection.addElement(para);
                    }
                }
                Collection<Behavior> viewpointBehavior = ((Class) viewpoint).getOwnedBehavior();
                Behavior b = ((Class) viewpoint).getClassifierBehavior();
                if (b == null && viewpointBehavior.size() > 0) {
                    b = viewpointBehavior.iterator().next();
                }
                if (b == null) {
                    // viewpoint can inherit other viewpoints, if this viewpoint has
                    // no behavior, check inherited behaviors
                    Class now = (Class) viewpoint;
                    while (now != null) {
                        if (!now.getSuperClass().isEmpty()) {
                            now = now.getSuperClass().iterator().next();
                            b = now.getClassifierBehavior();
                            if (b != null) {
                                break;
                            }
                            if (now.getOwnedBehavior().size() > 0) {
                                b = now.getOwnedBehavior().iterator().next();
                                break;
                            }
                        }
                        else {
                            now = null;
                        }
                    }
                }
                if (b != null) { // parse and execute viewpoint behavior, giving it
                    // the imported/queried elements
                    Boolean addVPElements = (Boolean) GeneratorUtils.getObjectProperty(b,
                            DocGenProfile.methodStereotype, "includeViewpointElements", false);

                    if (elementImports.isEmpty()) {
                        elementImports.add(view); // if view does not import/query
                    }
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
                    context.pushTargets(Utils2.asList(Utils.removeDuplicates(elementImports), Object.class));
                    if (b instanceof Activity) {
                        parseActivityOrStructuredNode(b, viewSection);
                    }
                    context.popTargets();
                }
            }
            else { // view does not conform to a viewpoint, apply default behavior
                if (view instanceof Diagram) { // if a diagram, show diagram and documentation
                    Image image = new Image();
                    List<Object> images = new ArrayList<Object>();
                    images.add(view);
                    image.setTargets(images);
                    String caption = (String) StereotypesHelper.getStereotypePropertyFirst(view,
                            DocGenProfile.dgviewStereotype, "caption");
                    // Check for old stereotype name for backwards compatibility
                    if (caption == null) {
                        caption = (String) StereotypesHelper.getStereotypePropertyFirst(view,
                                DocGenProfile.oldDgviewStereotype, "caption");
                    }
                    List<String> captions = new ArrayList<String>();
                    captions.add(caption);
                    image.setCaptions(captions);
                    image.setShowCaptions(true);
                    viewSection.addElement(image);
                }
                else { // just show documentation
                    String viewDoc = ModelHelper.getComment(view);
                    if (viewDoc != null && addViewDoc) {
                        Paragraph para = new Paragraph(viewDoc);
                        para.setDgElement(view);
                        para.setFrom(From.DOCUMENTATION);
                        viewSection.addElement(para);
                    }
                    //if (expose.size() == 1 && expose.get(0) instanceof Diagram) {
                    for (Element ex : elementImports) {
                        if (ex instanceof Diagram) {
                            DiagramType diagramType = Application.getInstance().getProject().getDiagram((Diagram) ex).getDiagramType();
                            if (diagramType.isTypeOf(DiagramType.GENERIC_TABLE) || diagramType.isTypeOf(DiagramType.DEPENDENCY_MATRIX) || diagramType.getType().equals(GenericTable.INSTANCE_TABLE) || diagramType.getType().equals(GenericTable.VERIFY_REQUIREMENTS_MATRIX) || diagramType.getType().equals(GenericTable.ALLOCATION_MATRIX) || diagramType.getType().equals(GenericTable.SATISFY_REQUIREMENTS_MATRIX) || diagramType.getType().equals(GenericTable.REQUIREMENTS_TABLE)) {
                                GenericTable gt = new GenericTable();
                                List<Object> tables = new ArrayList<Object>();
                                tables.add(ex);
                                gt.setTargets(tables);
                                viewSection.addElement(gt);
                            }
                            else {
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
        }
        viewSection.setDgElement(view);
        viewSection.setId(Converters.getElementToIdConverter().apply(view));
        viewSection.setTitle(((NamedElement) view).getName());
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
        if (a == null || parent == null) {
            return null;
        }
        Debug.outln("parseActivityOrStructuredNode( " + a.getHumanName() + ", " + Converters.getElementToIdConverter().apply(a) + ", "
                + parent.getStringIfEmpty() + ")");
        InitialNode in = GeneratorUtils.findInitialNode(a);
        if (in == null) {
            return null;
        }
        Collection<ActivityEdge> outs = in.getOutgoing();
        int pushed = 0;
        ActivityNode next2 = in;
        Object lastResults = null;
        Object parseResults = null;
        while (outs != null && outs.size() == 1) {
            parseResults = null;
            ActivityNode next = outs.iterator().next().getTarget();
            Debug.outln("next = " + next.getHumanName() + ", " + Converters.getElementToIdConverter().apply(next));
            next2 = null;
            boolean evaluatedConstraintsForNext = false;
            if (next instanceof CallBehaviorAction
                    || next instanceof StructuredActivityNode
                    && (StereotypesHelper.hasStereotypeOrDerived(next, DocGenProfile.tableStructureStereotype)
                        ||
                        StereotypesHelper.hasStereotypeOrDerived(next, DocGenProfile.plotStereotype)
                        )) {
                Behavior b = (next instanceof CallBehaviorAction) ? ((CallBehaviorAction) next).getBehavior()
                        : null;
                if (StereotypesHelper.hasStereotypeOrDerived(next, DocGenProfile.sectionStereotype)
                        || b != null
                        && StereotypesHelper.hasStereotypeOrDerived(b, DocGenProfile.sectionStereotype)) {
                    parseResults = parseSection((CallBehaviorAction) next, parent);
                    next2 = next;
                }
                else if (StereotypesHelper.hasStereotypeOrDerived(next, DocGenProfile.templateStereotype)
                        || b != null
                        && StereotypesHelper.hasStereotypeOrDerived(b, DocGenProfile.templateStereotype)) {
                    parseResults = parseQuery(next, parent);
                    next2 = next;
                }
                else if (StereotypesHelper.hasStereotypeOrDerived(next,
                        DocGenProfile.collectFilterStereotype)
                        || b != null
                        && StereotypesHelper
                        .hasStereotypeOrDerived(b, DocGenProfile.collectFilterStereotype)) {
                    CollectFilterParser.setContext(context);
                    List<Element> results = CollectFilterParser.startCollectAndFilterSequence(next, null);
                    parseResults = results;
                    this.context.pushTargets(Utils2.asList(results, Object.class));
                    pushed++;
                    next2 = context.getCurrentNode();
                    evaluatedConstraintsForNext = true;
                }
            }
            else if (next instanceof StructuredActivityNode) {
                Boolean loop = (Boolean) GeneratorUtils.getObjectProperty(next,
                        DocGenProfile.structuredQueryStereotype, "loop", false);
                Boolean ignore = (Boolean) GeneratorUtils.getObjectProperty(next,
                        DocGenProfile.structuredQueryStereotype, "ignore", false);
                Boolean createSections = (Boolean) GeneratorUtils.getObjectProperty(next,
                        DocGenProfile.structuredQueryStereotype, "createSections", false);
                Boolean useContextNameAsTitle = (Boolean) GeneratorUtils.getObjectProperty(next,
                        DocGenProfile.structuredQueryStereotype, "useSectionNameAsTitle", false);
                String titlePrefix = (String) GeneratorUtils.getObjectProperty(next,
                        DocGenProfile.structuredQueryStereotype, "titlePrefix", "");
                String titleSuffix = (String) GeneratorUtils.getObjectProperty(next,
                        DocGenProfile.structuredQueryStereotype, "titleSuffix", "");
                List<String> titles = (List<String>) GeneratorUtils.getListProperty(next,
                        DocGenProfile.structuredQueryStereotype, "titles", null);
                if (titles == null) {
                    titles = new ArrayList<String>();
                }

                List<Object> targets = getTargets(next, this.context);
                if (!ignore) {
                    if (loop) {
                        int count = 0;
                        for (Object e : targets) {
                            List<Object> target = new ArrayList<Object>();
                            target.add(e);
                            this.context.pushTargets(target);
                            Container con = parent;
                            if (createSections) {
                                Section sec = new Section();
                                if (titles != null && titles.size() > count) {
                                    sec.setTitle(titles.get(count));
                                }
                                else if (e instanceof NamedElement) {
                                    sec.setTitle(((NamedElement) e).getName());
                                }
                                sec.setTitlePrefix(titlePrefix);
                                sec.setTitleSuffix(titleSuffix);
                                sec.setDgElement(next);
                                if (e instanceof Element) {
                                    sec.setLoopElement((Element) e);
                                }
                                parent.addElement(sec);
                                con = sec;
                            }
                            parseResults = parseActivityOrStructuredNode(next, con);
                            this.context.popTargets();
                            count++;
                        }
                    }
                    else {
                        this.context.pushTargets(targets);
                        Container con = parent;
                        if (createSections) {
                            Section sec = new Section();
                            if (titles != null && titles.size() > 0) {
                                sec.setTitle(titles.get(0));
                            }
                            else if (!next.getName().isEmpty()) {
                                sec.setTitle(next.getName());
                            }
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
            }
            else if (next instanceof ForkNode
                    && StereotypesHelper.hasStereotype(next, DocGenProfile.parallel)) {// REVIEW
                // --
                // hasStereotypeOrDerived()?
                CollectFilterParser.setContext(context);
                List<Element> results = CollectFilterParser.startCollectAndFilterSequence(next, null);
                parseResults = results;
                this.context.pushTargets(Utils2.asList(results, Object.class));
                pushed++;
                next2 = context.getCurrentNode();
            }
            if (next2 == null) {
                next2 = next;
            }
            if (parseResults == null) {
                parseResults = this.context.peekTargets();
            }
            if (parseResults != null) {
                lastResults = parseResults;
            }
            // evaluate constraints on results
            if (!evaluatedConstraintsForNext) {
                DocumentValidator.evaluateConstraints(next, parseResults, context, true, true);
            }
            outs = next2.getOutgoing();
            Debug.outln("outs = " + MoreToString.Helper.toLongString(outs) + " for next2 = "
                    + next2.getHumanName() + ", " + Converters.getElementToIdConverter().apply(next2));
        }
        while (pushed > 0) {
            this.context.popTargets();
            pushed--;
        }
        return lastResults;
    }

    public static List<Object> getTargets(Object obj, GenerationContext context) {
        ArrayList<Object> list = new ArrayList<Object>();
        if (obj instanceof ActivityNode) {
            list.addAll(getTargets((ActivityNode) obj, context));
        }
        if (obj instanceof Collection) {
            for (Object o : (Collection<?>) obj) {
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
                DocGenProfile.templateStereotype, "targets");
        if (targets == null || targets.isEmpty()) {
            List<Element> elements =
                    Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next,
                            DocGenProfile.queriesStereotype, 1, false, 1);
            elements.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next,
                    DocGenProfile.oldQueriesStereotype, 1, false, 1));
            targets = Utils2.asList(elements, Object.class);
        }
        if ((targets == null || targets.isEmpty()) && next instanceof CallBehaviorAction
                && ((CallBehaviorAction) next).getBehavior() != null) {
            targets = StereotypesHelper.getStereotypePropertyValue(
                    ((CallBehaviorAction) next).getBehavior(), DocGenProfile.templateStereotype, "targets");
            if (targets == null || targets.isEmpty()) {
                List<Element> elements =
                        Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                                ((CallBehaviorAction) next).getBehavior(), DocGenProfile.queriesStereotype, 1, false,
                                1);
                elements.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                        ((CallBehaviorAction) next).getBehavior(), DocGenProfile.oldQueriesStereotype, 1,
                        false, 1));
                targets = Utils2.asList(elements, Object.class);
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
        String titlePrefix = (String) GeneratorUtils.getObjectProperty(cba, DocGenProfile.sectionStereotype,
                "titlePrefix", "");
        String titleSuffix = (String) GeneratorUtils.getObjectProperty(cba, DocGenProfile.sectionStereotype,
                "titleSuffix", "");
        Boolean useContextNameAsTitle = (Boolean) GeneratorUtils.getObjectProperty(cba,
                DocGenProfile.sectionStereotype, "useSectionNameAsTitle", false);
        String stringIfEmpty = (String) GeneratorUtils.getObjectProperty(cba,
                DocGenProfile.sectionStereotype, "stringIfEmpty", "");
        Boolean skipIfEmpty = (Boolean) GeneratorUtils.getObjectProperty(cba,
                DocGenProfile.sectionStereotype, "skipIfEmpty", false);
        Boolean ignore = (Boolean) GeneratorUtils.getObjectProperty(cba, DocGenProfile.sectionStereotype,
                "ignore", false);
        Boolean loop = (Boolean) GeneratorUtils.getObjectProperty(cba, DocGenProfile.sectionStereotype,
                "loop", false);
        Boolean isAppendix = false;

        if (StereotypesHelper.hasStereotype(cba, DocGenProfile.appendixStereotype)
                || (cba.getBehavior() != null && StereotypesHelper.hasStereotype(cba.getBehavior(),
                DocGenProfile.appendixStereotype))) {
            isAppendix = true;
        }
        String title = (String) GeneratorUtils.getObjectProperty(cba, DocGenProfile.sectionStereotype,
                "title", "");
        if (title == null || title.isEmpty()) {
            title = cba.getName();
            if (title.isEmpty() && cba.getBehavior() != null) {
                title = cba.getBehavior().getName();
            }
        }
        if (loop) {
            if (!context.targetsEmpty()) {
                for (Object e : context.peekTargets()) {
                    List<Object> target = new ArrayList<Object>();
                    target.add(e);
                    context.pushTargets(target);
                    Section sec = new Section();
                    sec.isAppendix(isAppendix);
                    sec.setTitlePrefix(titlePrefix);
                    sec.setTitleSuffix(titleSuffix);
                    if (e instanceof NamedElement) {
                        sec.setTitle(((NamedElement) e).getName());
                    }
                    else {
                        sec.setTitle(title);
                    }
                    sec.setStringIfEmpty(stringIfEmpty);
                    sec.setSkipIfEmpty(skipIfEmpty);
                    sec.setIgnore(ignore);
                    sec.setUseContextNameAsTitle(useContextNameAsTitle);
                    parent.addElement(sec);
                    if (e instanceof Element) {
                        sec.setLoopElement((Element) e);
                    }
                    sections.add(sec);
                    parseActivityOrStructuredNode(cba.getBehavior(), sec);

                    context.popTargets();
                }
            }
        }
        else {
            Section sec = new Section();
            sec.isAppendix(isAppendix);
            sec.setTitlePrefix(titlePrefix);
            sec.setTitleSuffix(titleSuffix);
            sec.setTitle(title);
            sec.setStringIfEmpty(stringIfEmpty);
            sec.setSkipIfEmpty(skipIfEmpty);
            sec.setIgnore(ignore);
            sec.setDgElement(cba);
            sec.setUseContextNameAsTitle(useContextNameAsTitle);
            parent.addElement(sec);
            sections.add(sec);
            parseActivityOrStructuredNode(cba.getBehavior(), sec);
        }
        return sections;
    }

    @SuppressWarnings("unchecked")
    public Object parseQuery(ActivityNode an, Container parent) {
        Object result = null;
        String titlePrefix = (String) GeneratorUtils.getObjectProperty(an, DocGenProfile.templateStereotype,
                "titlePrefix", "");
        String titleSuffix = (String) GeneratorUtils.getObjectProperty(an, DocGenProfile.templateStereotype,
                "titleSuffix", "");
        Boolean useContextNameAsTitle = (Boolean) GeneratorUtils.getObjectProperty(an,
                DocGenProfile.templateStereotype, "useSectionNameAsTitle", false);
        Boolean ignore = (Boolean) GeneratorUtils.getObjectProperty(an, DocGenProfile.templateStereotype,
                "ignore", false);
        Boolean loop = (Boolean) GeneratorUtils.getObjectProperty(an, DocGenProfile.templateStereotype,
                "loop", false);
        List<String> titles = (List<String>) GeneratorUtils.getListProperty(an,
                DocGenProfile.templateStereotype, "titles", new ArrayList<String>());
        boolean structured = false;
        if (StereotypesHelper.hasStereotypeOrDerived(an, DocGenProfile.structuredQueryStereotype)
                || (an instanceof CallBehaviorAction && ((CallBehaviorAction) an).getBehavior() != null && StereotypesHelper
                .hasStereotypeOrDerived(((CallBehaviorAction) an).getBehavior(),
                        DocGenProfile.structuredQueryStereotype))) {
            structured = true;
        }
        List<Object> targets = getTargets(an, getContext());
        if (structured && !ignore && an instanceof CallBehaviorAction) {
            Boolean createSections = (Boolean) GeneratorUtils.getObjectProperty(an,
                    DocGenProfile.structuredQueryStereotype, "createSections", false);
            if (loop) {
                List<Section> sections = new ArrayList<Section>();
                int count = 0;
                for (Object e : targets) {
                    List<Object> target = new ArrayList<Object>();
                    target.add(e);
                    this.context.pushTargets(target);
                    Container con = parent;
                    if (createSections) {
                        Section sec = new Section();
                        if (titles != null && titles.size() > count) {
                            sec.setTitle(titles.get(count));
                        }
                        else if (e instanceof NamedElement) {
                            sec.setTitle(((NamedElement) e).getName());
                        }
                        sec.setTitlePrefix(titlePrefix);
                        sec.setTitleSuffix(titleSuffix);
                        sec.setDgElement(an);
                        parent.addElement(sec);
                        sections.add(sec);
                        con = sec;
                    }
                    parseActivityOrStructuredNode(((CallBehaviorAction) an).getBehavior(), con);
                    this.context.popTargets();
                }
                result = sections;
            }
            else {
                this.context.pushTargets(targets);
                Container con = parent;
                if (createSections) {
                    Section sec = new Section();
                    if (titles.size() > 0) {
                        sec.setTitle(titles.get(0));
                    }
                    else if (!an.getName().isEmpty()) {
                        sec.setTitle(an.getName());
                    }
                    else if (!((CallBehaviorAction) an).getBehavior().getName().isEmpty()) {
                        sec.setTitle(((CallBehaviorAction) an).getBehavior().getName());
                    }
                    sec.setUseContextNameAsTitle(useContextNameAsTitle);
                    sec.setDgElement(an);
                    sec.setTitlePrefix(titlePrefix);
                    sec.setTitleSuffix(titleSuffix);
                    parent.addElement(sec);
                    result = sec;
                    con = sec;
                }

                Object res = parseActivityOrStructuredNode(((CallBehaviorAction) an).getBehavior(), con);
                if (result == null) {
                    result = res;
                }
                this.context.popTargets();
            }
        }
        else {
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
     * parses query actions into classes in gov.nasa.jpl.mbee.mdk.docgen.model
     * - creates class representation of the queries There's gotta be a way to
     * make this less ugly... by sweeping the ugliness under multiple rugs!
     *
     * @param an
     * @return
     */
    private Query parseTemplate(ActivityNode an) {

        Query dge = null;
        if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.imageStereotype)) {
            dge = new Image();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.paragraphStereotype)) {
            dge = new Paragraph(context.getValidator());
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.bulletedListStereotype)) {
            dge = new BulletedList();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.dependencyMatrixStereotype)) {
            dge = new DependencyMatrix();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.genericTableStereotype)) {
            dge = new GenericTable();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.tableStructureStereotype)) {
            // Get all the variables or whatever
            dge = new TableStructure(context.getValidator());
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.combinedMatrixStereotype)) {
            dge = new CombinedMatrix();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.customTableStereotype)) {
            dge = new CustomTable();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.userScriptStereotype, true)) {
            dge = new UserScript();
        }
        else if (GeneratorUtils.hasStereotypeByString(an,
                DocGenProfile.propertiesTableByAttributesStereotype)) {
            dge = new PropertiesTableByAttributes();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.viewpointConstraintStereotype)) {
            dge = new ViewpointConstraint(context.getValidator());
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.temporalDiffStereotype)) {
            dge = new TemporalDiff();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.tomsawyerDiagramStereotype)) {
            dge = new TomSawyerDiagram();

        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.javaExtensionStereotype, true)) {
            Element e = an;
            if (!StereotypesHelper.hasStereotypeOrDerived(an, DocGenProfile.javaExtensionStereotype)) {
                if (an instanceof CallBehaviorAction
                        && ((CallBehaviorAction) an).getBehavior() != null
                        && StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction) an).getBehavior(),
                        DocGenProfile.javaExtensionStereotype)) {
                    e = ((CallBehaviorAction) an).getBehavior();
                }
            }
            Stereotype s = StereotypesHelper.checkForDerivedStereotype(e,
                    DocGenProfile.javaExtensionStereotype);
            String javaClazz = s.getName();
            if (MDKPlugin.extensionsClassloader != null) {
                try {
                    java.lang.Class<?> clazz = java.lang.Class.forName(javaClazz, true,
                            MDKPlugin.extensionsClassloader);
                    dge = (Query) clazz.newInstance();
                } catch (Exception e1) {
                    Application.getInstance().getGUILog()
                            .log("[ERROR] Cannot instantiate Java extension class " + javaClazz);
                    e1.printStackTrace();
                }
            }
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.simulateStereotype, true)) {
            dge = new Simulate();
        }
        else if (GeneratorUtils.hasStereotypeByString(an, DocGenProfile.plotStereotype, true)) {
            dge = new Plot(context.getValidator());
        }
        return dge;
    }

    public GenerationContext getContext() {
        return context;
    }

    public void setContext(final GenerationContext context) {
        this.context = context;
    }


    public Stereotype getProductStereotype() {
        return product;
    }

    public Stereotype getView() {
        return sysmlview;
    }

}
