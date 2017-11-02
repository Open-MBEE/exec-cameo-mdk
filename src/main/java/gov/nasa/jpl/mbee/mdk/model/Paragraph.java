package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.From;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.generator.GenerationContext;
import gov.nasa.jpl.mbee.mdk.util.*;
import gov.nasa.jpl.mbee.mdk.util.Utils.AvailableAttribute;

import java.util.*;

public class Paragraph extends Query {

    private String text;
    private List<Property> stereotypeProperties;
    private From fromProperty;

    private DocumentValidator validator = null;
    private boolean tryOcl = false;
    private boolean iterate = true;
    private AvailableAttribute attribute = null; // this is redundant with fromProperty

    public InitialNode bnode;
    public ActivityNode activityNode;
    public GenerationContext context = null;

    public GenerationContext makeContext() {
        ActivityNode n = null;
        if (bnode != null && bnode.getOutgoing().iterator().hasNext()) { // should
            // check
            // next
            // node
            // is
            // collect/filter
            // node
            n = bnode.getOutgoing().iterator().next().getTarget();
        }
        Stack<List<Object>> in = new Stack<List<Object>>();
        // in.add( targets );
        context = new GenerationContext(in, n, getValidator(), Application.getInstance().getGUILog());
        return context;
    }


    public Paragraph(String t) {
        text = t;
    }

    public Paragraph() {
    }

    public Paragraph(DocumentValidator dv) {
        this.validator = dv;
    }

    public void setText(String t) {
        text = t;
    }

    public String getText() {
        return text;
    }

    public void setStereotypeProperties(List<Property> p) {
        stereotypeProperties = p;
    }

    public List<Property> getStereotypeProperties() {
        return stereotypeProperties;
    }

    public void setFrom(From f) {
        fromProperty = f;
    }

    public From getFrom() {
        return fromProperty;
    }

    public DocumentValidator getValidator() {
        return validator;
    }

//    /* (non-Javadoc)
//     * @see gov.nasa.jpl.mbee.mdk.model.Query#parse()
//     */
//    @Override
//    public void parse() {
//        super.parse();
//    }

    protected void addOclParagraph(List<DocumentElement> res,
                                   Object oclExpression, Object context) {
        addOclParagraph(res, oclExpression, context, new HashSet<Object>());
    }

    protected void addOclParagraph(List<DocumentElement> res,
                                   Object oclExpression, Object context,
                                   HashSet<Object> seen) {
        // check for infinite recursion
        if (seen.contains(oclExpression)) {
            return;
        }
        seen.add(oclExpression);

        if (oclExpression instanceof Collection) {
            Collection<?> oclColl = (Collection<?>) oclExpression;
            for (Object ocl : oclColl) {
                addOclParagraph(res, ocl, context, seen);
            }
            return;
        }
        Debug.outln("addOclParagraph(" + res + ", \"" + oclExpression
                + "\", " + context + ")" + " class(" + context.getClass() + ")");
        Object result =
                DocumentValidator.evaluate(oclExpression, context,
                        getValidator(), true);
        Debug.outln("ocl result = " + result);

//        if ( result instanceof Collection && ((Collection<?>)result).size() == 1 ) {
//            result = ( (Collection< ? >)result ).iterator().next();
//        }

        addAttributeParagraphs(res, result);
    }

    public void addAttributeParagraphs(List<DocumentElement> res, Object result) {
        if (result instanceof Element && getFrom() != null) {
            Element e = (Element) result;
            Object v = Utils.getElementAttribute(e, attribute);
            if (!Utils2.isNullOrEmpty(v)) {
                Object o;
                DBParagraph paragraph = new DBParagraph(v, e, getFrom());
                if (getDgElement() != null && (o = StereotypesHelper.getStereotypePropertyFirst(getDgElement(), DocGenProfile.editableChoosable, "editable")) instanceof Boolean) {
                    paragraph.setEditable((Boolean) o);
                }
                res.add(paragraph);
            }
        }
        else if (!Utils2.isNullOrEmpty(result)) {
            if (result instanceof Collection) {
                // Get the attribute for each element in the result list and
                // create a paragraph for each.
                // TODO -- REVIEW -- Do we want to make this a DBList so that we
                // can distinguish nested collections as subparagraphs?
                for (Object o : (Collection<?>) result) {
                    addAttributeParagraphs(res, o);
                }
            }
            else {
                if (!Utils2.isNullOrEmpty(result)) {
                    Object o;
                    DBParagraph paragraph = new DBParagraph(result);
                    if (getDgElement() != null && (o = StereotypesHelper.getStereotypePropertyFirst(getDgElement(), DocGenProfile.editableChoosable, "editable")) instanceof Boolean) {
                        paragraph.setEditable((Boolean) o);
                    }
                    res.add(paragraph);
                }
            }
        }
    }

    /**
     * Create DocBook paragraph(s) for this Paragraph.
     *
     * @param forViewEditor
     * @param outputDir
     * @return Return one or more DBParagraphs for docgen or the view editor
     * based on properties of the Paragraph UML stereotype.
     * <p>
     * <code>
     * O=tryOcl && T=gotText && R=gotTargets && S=gotStereotypeProperties && D=don't care <br><br>
     * <p>
     * 1 &nbsp;D && !T && !R &&  D: return nothing <br>
     * 2     !O && !T &&  R && !S: return a paragraph of documentation for each target <br>
     * 3     !O && !T &&  R &&  S: return a paragraph for each target-property pair  <br>
     * 4     !O &&  T &&  D &&  D: return a paragraph of the text, tied to the "body" slot of dgElement <br>
     * <br>
     * 5 &nbsp;O && !T &&  R && !S: return a paragraph of the evaluation of the documentation of each target as OCL on dgElement <br>
     * 6 &nbsp;O && !T &&  R &&  S: return a paragraph of the evaluation of each target-property as OCL on dgElement <br>
     * 7 &nbsp;O &&  T && !R &&  D: return a paragraph of the evaluation of the text as OCL on dgElement <br>
     * 8 &nbsp;O &&  T &&  R && !S: return a paragraph of the evaluation of the text as OCL on each target <br>
     * 9 &nbsp;O &&  T &&  R &&  S: return a paragraph of the evaluation of the text as OCL on each target-property pair <br>
     * </code>
     * <p>
     * @see gov.nasa.jpl.mbee.mdk.model.Query#visit(boolean, java.lang.String)
     */
    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        Debug.outln("visit(" + forViewEditor + ", " + outputDir + ")");
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        List<Reference> refs = new ArrayList<Reference>();
        if (getIgnore()) {
            return res;
        }
        boolean gotText = getText() != null;// && !getText().isEmpty();
        boolean gotTargets = getTargets() != null && !getTargets().isEmpty();
        boolean gotStereotypeProperties =
                !Utils2.isNullOrEmpty(getStereotypeProperties());
        boolean allTargetsAreProperties = false;
        Debug.outln("gotText = " + gotText + ", " + getText());
        Debug.outln("gotTargets = " + gotTargets + ", " + MoreToString.Helper.toLongString(getTargets()));
        Debug.outln("gotStereotypeProperties = " + gotStereotypeProperties + ", " + getStereotypeProperties());
        Debug.outln("desiredAttribute = " + attribute);
        if (gotText && !tryOcl) { // ignoring targets -- should be none -- REVIEW
            Debug.outln("case 4");
            // case 4: return a paragraph of the text, tied to the "body" slot
            // of dgElement or the documentation of the dgElement if dgElement
            // is something other than a Paragraph
            if (forViewEditor || !getText().trim().isEmpty()) {
                //GeneratorUtils.getObjectProperty( getDgElement(), DocGenProfile.paragraphStereotype, "body", null );
                // TODO @donbot find a way to remove this getProject() call
                Stereotype paragraphStereotype = Utils.getStereotype(Application.getInstance().getProject(), DocGenProfile.paragraphStereotype);
                Slot s = Utils.getSlot(getDgElement(), Utils.getStereotypePropertyByName(paragraphStereotype, "body"));
                //StereotypesHelper.getSlot( getDgElement(), , arg2, arg3 )
                DBParagraph paragraph;
                if (s != null) {
                    paragraph = new DBParagraph(getText(), s, From.DVALUE);
                }
                else { // dgElement is not a Paragraph
                    if (getDgElement() != null && getFrom() != null) {
                        paragraph = new DBParagraph(getText(), getDgElement(), getFrom());
                    }
                    else if (getDgElement() != null) { // getFrom() must be null
                        paragraph = new DBParagraph(getText(), getDgElement(), From.DOCUMENTATION);
                    }
                    else {
                        paragraph = new DBParagraph(getText());
                    }
                }
                Object o;
                if (getDgElement() != null && (o = StereotypesHelper.getStereotypePropertyFirst(getDgElement(), DocGenProfile.editableChoosable, "editable")) instanceof Boolean) {
                    paragraph.setEditable((Boolean) o);
                }
                res.add(paragraph);
            } //else {
            //res.add(new DBParagraph(getText()));
            //}
        }
        else if (gotText && !gotTargets) { // tryOcl must be true
            Debug.outln("case 7");
            // case 7: return a paragraph of the evaluation of the text as OCL on dgElement 
            addOclParagraph(res, getText(), new ArrayList<Object>());
        }
        else if (gotTargets) {
            // In case 5, we get the OCL from the targets; if the targets are
            // Properties, then we look for the OCL in their values; otherwise,
            // we use the documentation as OCL.
            allTargetsAreProperties = true;
            for (Object o : targets) {
                if (o != null && !(o instanceof Property) && !(o instanceof Slot) && !(o instanceof Constraint)) {
                    allTargetsAreProperties = false;
                    break;
                }

            }
            // Build up a list of References before generating DBParagraphs.
            for (Object o : targets) {
                Element e = null;
                if (o instanceof Element) {
                    e = (Element) o;
                }
                else if (!tryOcl) {
                    continue;
                }
                Reference ref = null;
                if (gotStereotypeProperties) {
                    // for cases 3, 6, and 9
                    Debug.outln("case 3, 6, or 9, target=" + o);
                    for (Property p : getStereotypeProperties()) {
                        ref = Reference.getPropertyReference(e, p);
                        refs.add(ref);
                    }
                }
                else {
                    if (tryOcl && gotText) {
                        Debug.outln("case 8, target=" + Utils.getName(o));
                        // for case 8
                        ref = new Reference(o);
                    }
                    else {
                        Debug.outln("case 2 or 5");
                        // for cases 2 and 5
                        //Object ocl = allTargetsAreProperties ? : ModelHelper.getComment( e );
                        if (allTargetsAreProperties && tryOcl) {
                            Object v = Utils.getElementAttribute(e, AvailableAttribute.Value);
                            ref = new Reference(e, From.DVALUE, v);
                        }
                        else {
                            if (attribute != null) {
                                ref = new Reference(e, fromProperty, Utils.getElementAttribute(e, attribute));
                            }
                            else {
                                ref = new Reference(e, From.DOCUMENTATION, ModelHelper.getComment(e));
                            }
                        }
                    }
                    refs.add(ref);
                }
            }
            if (tryOcl && !iterate && gotText) {
                Debug.outln("case 8 or 9 a");
                // for cases 8 & 9 when !iterate
                // apply text as OCL to the collection as a whole
                ArrayList<Object> results = new ArrayList<Object>();
                for (Reference r : refs) {
                    results.add(r.getResult());
                }
                addOclParagraph(res, getText(), results);
            }
            else {
                if (!iterate) {
                    Debug.error(false, "The iterate property should be true when not using OCL or when the OCL is in the targets instead of the body: " + dgElement);
                    // REVIEW -- create a validation violation instead?
                    // getValidator().addViolationIfUnique( rule, element, comment, reported ); // no public rule to reference!
                }
                // creating paragraph for each reference
                for (Reference r : refs) {
                    if (!tryOcl) { // gotText is false
                        Debug.outln("case 2 or 3, ref=" + r);
                        // cases 2 & 3: return a paragraph for each
                        // target-property pair (3) or for each target's
                        // documentation (2)
                        res.addAll(Common.getReferenceAsDocumentElements(r, this));
//                        res.add( new DBParagraph( r.getLegacyResult(),
//                                                  r.getElement(), r.getFrom() ) );
                    }
                    else {
                        if (gotText) {
                            Debug.outln("case 8 or 9, ref=" + r);
                            // cases 8 & 9: return a paragraph of the evaluation
                            // of the text as OCL on each target-property pair (9)
                            // or on each target (8)
                            addOclParagraph(res, getText(), r.getResult());
                        }
                        else {
                            Debug.outln("case 5 or 6, ref=" + r);
                            // cases 5 & 6: add a paragraph of the evaluation of
                            // the value of each target-property (6) or of each target's
                            // documentation (5) as OCL on dgElement
                            addOclParagraph(res, r.getResult(), new ArrayList<Object>());
                        }
                    }
                }
            }
        } // else case 1: gotText and gotTarget are both false, so return nothing 

        Debug.outln("visit(" + forViewEditor + ", \"" + outputDir + ") returning " + res);
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        String body = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.paragraphStereotype,
                "body", DocGenProfile.PROFILE_NAME, null);
        setText(body);
        Object doOcl = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.paragraphStereotype,
                "evaluateOcl", DocGenProfile.PROFILE_NAME, null);
        if (doOcl != null) {
            tryOcl = Utils.isTrue(doOcl, true);
        }
        Object iter = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.paragraphStereotype,
                "iterate", DocGenProfile.PROFILE_NAME, null);
        if (iter != null) {
            iterate = Utils.isTrue(iter, false); // TODO -- use this!
        }

        Object attr = GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.attributeChoosable, "desiredAttribute", DocGenProfile.PROFILE_NAME, null);
        if (attr instanceof EnumerationLiteral) {
            attribute = Utils.AvailableAttribute.valueOf(((EnumerationLiteral) attr).getName());
            if (attribute != null) {
                setFrom(Utils.getFromAttribute(attribute));
            }
        }

        setStereotypeProperties((List<Property>) GeneratorUtils.getStereotypePropertyValue(dgElement, DocGenProfile.stereotypePropertyChoosable,
                        "stereotypeProperties", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
    }


}
