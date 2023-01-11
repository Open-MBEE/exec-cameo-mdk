package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
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
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.SysMLExtensions;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.ViewViewpointValidator;
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
            conform,
            expose;
    private boolean hierarchyOnly;
    private boolean addViewDoc = true; //whether to add default view doc

    private SysMLExtensions profile;

    public DocumentGenerator(Element e, PrintWriter wlog) {
        this(e, null, wlog, true);
    }

    public DocumentGenerator(Element e, ViewViewpointValidator dv, PrintWriter wlog) {
        this(e, dv, wlog, true);
    }

    public DocumentGenerator(Element e, ViewViewpointValidator dv, PrintWriter wlog, boolean addViewDoc) {
        this.start = e;
        this.project = Project.getProject(e);
        this.profile = SysMLExtensions.getInstance(e);
        this.product = this.profile.product().getStereotype();
        this.sysmlview = SysMLProfile.getInstance(e).view().getStereotype();
        this.conform = this.profile.conforms().getStereotype();
        this.expose = SysMLProfile.getInstance(e).expose().getStereotype();
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
            if (start instanceof Class) {
                ProductViewParser vp = new ProductViewParser(this, singleView, recurse, doc, start);
                vp.parse();
            }
        }
        else if (this.profile.document().is(start)
                && start instanceof Activity) {
            parseActivityOrStructuredNode(start, doc);
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
        Element viewpoint = GeneratorUtils.findStereotypedRelationship(view, conform);
        Section viewSection = new Section(); // Section is a misnomer, should be
        // View
        viewSection.setView(true);
        if (this.profile.appendixView().is(view)) {
            viewSection.isAppendix(true);
        }
        viewSection.setViewpoint(viewpoint);

        List<Element> elementImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(
                view, ElementImport.class, 1, 1);
        List<Element> packageImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(
                view, PackageImport.class, 1, 1);
        List<Element> exposed = Utils.collectDirectedRelatedElementsByRelationshipStereotype(
                view, this.expose, 1, true, 1);
        if (elementImports == null) {
            elementImports = new ArrayList<Element>();
        }
        if (packageImports != null) {
            elementImports.addAll(packageImports);
        }
        if (exposed != null) {
            elementImports.addAll(exposed); // all three import/queries
        }
        // relationships are
        // interpreted the same
        if (view instanceof Class) {
            for (TypedElement te : ((Class) view).get_typedElementOfType()) {
                if (te instanceof Property && ((Property) te).getAggregation() == AggregationKindEnum.COMPOSITE) {
                    elementImports.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(te,
                            expose, 1, true, 1));
                }
            }
        }
        viewSection.setExposes(elementImports);

        if (!hierarchyOnly) {
            if (viewpoint != null && viewpoint instanceof Classifier) { // view conform
                // to a viewpoint
                if (!(view instanceof Diagram)) { // if it's a diagram, people most
                    // likely put image query in
                    // viewpoint already. this is to
                    // prevent showing duplicate
                    // documentation
                    String viewDoc = ModelHelper.getComment(view);
                    if (viewDoc != null && addViewDoc) {
                        Paragraph para = new Paragraph(viewDoc);
                        //if ((Boolean)GeneratorUtils.getStereotypePropertyFirst(view, DocGenProfile.editableChoosable, "editable", true)) {
                        para.setDgElement(view);
                        para.setFrom(From.DOCUMENTATION);
                        //}
                        viewSection.addElement(para);
                    }
                }
                Behavior behavior = GeneratorUtils.getViewpointMethod((Classifier) viewpoint, project);
                if (behavior != null) { // parse and execute viewpoint behavior, giving it
                    // the imported/queried elements
                    Boolean addVPElements = (Boolean) GeneratorUtils.getStereotypePropertyFirst(behavior,
                            profile.method().getIncludeViewpointElementsProperty(), false);

                    if (elementImports.isEmpty()) {
                        elementImports.add(view); // if view does not import/query
                    }
                    // anything, give the view element
                    // itself to the viewpoint
                    if (addVPElements) {
                        elementImports.add(viewpoint);
                        elementImports.add(behavior);
                        elementImports.add(view);
                    }
                    context.pushTargets(Utils2.asList(Utils.removeDuplicates(elementImports), Object.class));
                    if (behavior instanceof Activity) {
                        parseActivityOrStructuredNode(behavior, viewSection);
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
                    image.setProfile(this.profile);
                    String caption = this.profile.view().getCaption(view);
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
                                gt.setProfile(this.profile);
                                viewSection.addElement(gt);
                            }
                            else {
                                Image image = new Image();
                                List<Object> images = new ArrayList<Object>();
                                images.add(ex);
                                image.setTargets(images);
                                image.setProfile(this.profile);
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
            if (next instanceof CallBehaviorAction || next instanceof StructuredActivityNode && (profile.tableStructure().is(next) || profile.plot().is(next))) {
                Behavior b = (next instanceof CallBehaviorAction) ? ((CallBehaviorAction) next).getBehavior()
                        : null;
                if (this.profile.dynamicView().is(next)
                        || b != null
                        && this.profile.dynamicView().is(b)) {
                    parseResults = parseSection((CallBehaviorAction) next, parent);
                    next2 = next;
                }
                else if (this.profile.collectOrFilter().is(next)
                        || b != null
                        && this.profile.collectOrFilter().is(b)) {
                    CollectFilterParser.setContext(context);
                    CollectFilterParser.setProfile(profile);
                    List<Element> results = CollectFilterParser.startCollectAndFilterSequence(next, null);
                    parseResults = results;
                    this.context.pushTargets(Utils2.asList(results, Object.class));
                    pushed++;
                    next2 = context.getCurrentNode();
                    evaluatedConstraintsForNext = true;
                }
                else if (profile.formattingAndDisplayTemplate().is(next)
                        || b != null) {
                    parseResults = parseQuery(next, parent);
                    next2 = next;
                }
            }
            else if (next instanceof StructuredActivityNode) {
                SysMLExtensions.StructuredQueryStereotype s = profile.structuredQuery();
                Boolean loop = (Boolean) GeneratorUtils.getStereotypePropertyFirst(next,
                        s.getLoopProperty(), false);
                Boolean ignore = (Boolean) GeneratorUtils.getStereotypePropertyFirst(next,
                        s.getIgnoreProperty(), false);
                Boolean createSections = (Boolean) GeneratorUtils.getStereotypePropertyFirst(next,
                        s.getCreateSectionsProperty(), false);
                Boolean useContextNameAsTitle = (Boolean) GeneratorUtils.getStereotypePropertyFirst(next,
                        s.getUseSectionNameAsTitleProperty(), false);
                String titlePrefix = (String) GeneratorUtils.getStereotypePropertyFirst(next,
                        s.getTitlePrefixProperty(), "");
                String titleSuffix = (String) GeneratorUtils.getStereotypePropertyFirst(next,
                        s.getTitleSuffixProperty(), "");
                List<String> titles = (List<String>) GeneratorUtils.getStereotypePropertyValue(next,
                        s.getTitlesProperty(), null);
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
                    && this.profile.parallel().is(next)) {
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
                ViewViewpointValidator.evaluateConstraints(next, parseResults, context, true, true);
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
        List<Object> targets = Utils2.asList(SysMLExtensions.getInstance(next).formattingAndDisplayTemplate().getTargets(next), Object.class);
        if (targets.isEmpty()) {
            List<Element> elements =
                    Utils.collectDirectedRelatedElementsByRelationshipStereotype(next,
                            SysMLProfile.getInstance(next).expose().getStereotype(), 1, true, 1);
            targets = Utils2.asList(elements, Object.class);
        }
        if ((targets == null || targets.isEmpty()) && next instanceof CallBehaviorAction
                && ((CallBehaviorAction) next).getBehavior() != null) {
            targets = Utils2.asList(SysMLExtensions.getInstance(next).formattingAndDisplayTemplate().getTargets(
                    ((CallBehaviorAction) next).getBehavior()), Object.class);
            if (targets == null || targets.isEmpty()) {
                List<Element> elements =
                        Utils.collectDirectedRelatedElementsByRelationshipStereotype(
                                ((CallBehaviorAction) next).getBehavior(), SysMLProfile.getInstance(next).expose().getStereotype(), 1, true,
                                1);
                targets = Utils2.asList(elements, Object.class);
            }
        }
        if (targets == null || targets.isEmpty() && !context.targetsEmpty()) {
            targets = context.peekTargets();
        }
        return Utils.removeDuplicates(targets);
    }

    // this is a section made using an activity and should be discouraged since
    // it won't show up on view editor
    private List<Section> parseSection(CallBehaviorAction cba, Container parent) {
        List<Section> sections = new ArrayList<Section>();
        SysMLExtensions.DynamicViewStereotype s = this.profile.dynamicView();
        String titlePrefix = (String) GeneratorUtils.getStereotypePropertyFirst(cba, s.getTitlePrefixProperty(), "");
        String titleSuffix = (String) GeneratorUtils.getStereotypePropertyFirst(cba, s.getTitleSuffixProperty(), "");
        Boolean useContextNameAsTitle = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                s.getUseSectionNameAsTitleProperty(), false);
        String stringIfEmpty = (String) GeneratorUtils.getStereotypePropertyFirst(cba,
                s.getStringIfEmptyProperty(), "");
        Boolean skipIfEmpty = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba,
                s.getSkipIfEmptyProperty(), false);
        Boolean ignore = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, s.getIgnoreProperty(), false);
        Boolean loop = (Boolean) GeneratorUtils.getStereotypePropertyFirst(cba, s.getLoopProperty(), false);
        String title = (String) GeneratorUtils.getStereotypePropertyFirst(cba, s.getTitleProperty(), "");
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
        SysMLExtensions.FormattingAndDisplayTemplateStereotype s = profile.formattingAndDisplayTemplate();
        String titlePrefix = (String) GeneratorUtils.getStereotypePropertyFirst(an, s.getTitlePrefixProperty(), "");
        String titleSuffix = (String) GeneratorUtils.getStereotypePropertyFirst(an, s.getTitleSuffixProperty(), "");
        Boolean useContextNameAsTitle = (Boolean) GeneratorUtils.getStereotypePropertyFirst(an,
                s.getUseSectionNameAsTitleProperty(), false);
        Boolean ignore = (Boolean) GeneratorUtils.getStereotypePropertyFirst(an, s.getIgnoreProperty(), false);
        Boolean loop = (Boolean) GeneratorUtils.getStereotypePropertyFirst(an, s.getLoopProperty(), false);
        List<String> titles = (List<String>) GeneratorUtils.getStereotypePropertyValue(an,
                s.getTitlesProperty(), new ArrayList<String>());
        boolean structured = false;
        if (profile.structuredQuery().is(an)
                || (an instanceof CallBehaviorAction && ((CallBehaviorAction) an).getBehavior() != null
                && profile.structuredQuery().is(((CallBehaviorAction) an).getBehavior()))) {
            structured = true;
        }
        List<Object> targets = getTargets(an, getContext());
        if (structured && !ignore && an instanceof CallBehaviorAction) {
            Boolean createSections = (Boolean) GeneratorUtils.getStereotypePropertyFirst(an,
                    profile.structuredQuery().getCreateSectionsProperty(), false);
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
                        if (titles.size() > count) {
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
        if (GeneratorUtils.hasStereotype(an, profile.image().getStereotype())) {
            dge = new Image();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.paragraph().getStereotype())) {
            dge = new Paragraph(context.getValidator());
        }
        else if (GeneratorUtils.hasStereotype(an, profile.bulletedList().getStereotype())) {
            dge = new BulletedList();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.genericTable().getStereotype())) {
            dge = new GenericTable();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.tableStructure().getStereotype())) {
            // Get all the variables or whatever
            dge = new TableStructure(context.getValidator());
        }
        else if (GeneratorUtils.hasStereotype(an, profile.userScript().getStereotype(), true)) {
            dge = new UserScript();
        }
        else if (GeneratorUtils.hasStereotype(an,
                profile.propertiesTableByAttributes().getStereotype())) {
            dge = new PropertiesTableByAttributes();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.temporalDiff().getStereotype())) {
            dge = new TemporalDiff();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.tomSawyerDiagram().getStereotype())) {
            dge = new TomSawyerDiagram();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.javaExtension().getStereotype(), true)) {
            Element e = an;
            if (!profile.javaExtension().is(an)) {
                if (an instanceof CallBehaviorAction
                        && ((CallBehaviorAction) an).getBehavior() != null
                        && profile.javaExtension().is(((CallBehaviorAction) an).getBehavior())) {
                    e = ((CallBehaviorAction) an).getBehavior();
                }
            }
            Stereotype s = StereotypesHelper.checkForDerivedStereotype(e,
                    profile.javaExtension().getStereotype());
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
        else if (GeneratorUtils.hasStereotype(an, profile.simulate().getStereotype(), true)) {
            dge = new Simulate();
        }
        else if (GeneratorUtils.hasStereotype(an, profile.plot().getStereotype(), true)) {
            dge = new Plot(context.getValidator());
        }
        else if (an instanceof CallBehaviorAction && ((CallBehaviorAction) an).getBehavior() != null) {
            dge = new BehaviorQuery((CallBehaviorAction) an);
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
