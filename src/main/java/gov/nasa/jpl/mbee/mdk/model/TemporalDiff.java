package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils.AvailableAttribute;
import gov.nasa.jpl.mbee.mdk.util.Utils2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemporalDiff extends Table {
    private String baseVersionTime;
    private String compareToTime;
    private AvailableAttribute attributeToCompare;
    private String tagAttr;
    private String baseBranchName;
    private String compareToBranchName;

    public TemporalDiff() {
        setSortElementsByName(false);
    }

    public void addStereotypeProperties(DBHasContent parent, Element e, Property p) {
        Common.addReferenceToDBHasContent(Reference.getPropertyReference(e, p), parent, this);
    }

    @Override
    public void initialize() {

        Object attr = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "desiredAttribute", DocGenProfile.PROFILE_NAME, null);
        if (attr instanceof EnumerationLiteral) {
            attributeToCompare = Utils.AvailableAttribute.valueOf(((EnumerationLiteral) attr).getName());
        }
        else {
            attributeToCompare = Utils.AvailableAttribute.valueOf("Documentation");
        }
        if (attributeToCompare == Utils.AvailableAttribute.Documentation) {
            tagAttr = "doc";
        }
        else if (attributeToCompare == Utils.AvailableAttribute.Name) {
            tagAttr = "name";
        }
        else {
            tagAttr = "val";
        }
        baseVersionTime = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "baseVersionTime", DocGenProfile.PROFILE_NAME, null);
        compareToTime = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "compareToTime", DocGenProfile.PROFILE_NAME, "latest");
        baseBranchName = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "baseBranch", DocGenProfile.PROFILE_NAME, null);
        compareToBranchName = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "compareToBranch", DocGenProfile.PROFILE_NAME, null);
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date baseVersionDate = null;
        Date compareToDate = null;
        if (null != baseVersionTime && !baseVersionTime.isEmpty() && !baseVersionTime.equalsIgnoreCase("latest")) {
            baseVersionDate = parseDate(baseVersionTime);
        }
        if (null != compareToTime && !compareToTime.isEmpty() && !compareToTime.equalsIgnoreCase("latest")) {
            compareToDate = parseDate(compareToTime);
        }

        List<Object> list = getTargets(); // This is not the right list of objects so far?
        if (forViewEditor) {
            // for every target.
            DBParagraph retval = new DBParagraph();
            StringBuffer tag = new StringBuffer();
            for (Object e : list) {
                if (e instanceof Element) {
                    tag.append("<mms-diff-attr mms-eid=\"");
                    tag.append(Converters.getElementToIdConverter().apply((Element) e) + "\"");
                    tag.append(" mms-attr=\"" + tagAttr + "\" mms-version-one=\"");
                    if (baseVersionTime == null) {
                        tag.append("latest");
                    }
                    else if (baseVersionDate == null) {
                        tag.append(baseVersionTime);
                    }
                    else {
                        tag.append(sdf.format(baseVersionDate));
                    }
                    tag.append("\" mms-version-two=\"");
                    if (compareToTime == null) {
                        tag.append("latest");
                    }
                    else if (compareToDate == null) {
                        tag.append(compareToTime);
                    }
                    else {
                        tag.append(sdf.format(compareToDate));
                    }
                    if (baseBranchName != null) {
                        tag.append("\" mms-ws-one=\"");
                        tag.append(baseBranchName);
                    }
                    if (compareToBranchName != null) {
                        tag.append("\" mms-ws-two=\"");
                        tag.append(compareToBranchName);
                    }
                    tag.append("\"></mms-diff-attr>");
                }
            }
            retval.setText(tag); // concatenate the elements
            // System.out.println(tag);
            res.add(retval);
            return res;
        }
        else {
            for (Object e : list) {
                if (e instanceof Element) {
                    if (compareToTime == null) {
                        Object v = Utils.getElementAttribute((Element) e, attributeToCompare);
                        if (!Utils2.isNullOrEmpty(v)) {
                            if (v instanceof String) {
                                // System.out.println(v);
                            }
                        }
                    }
                    else {
                        //TODO @scopecreep throw new MethodNotSupportedException("");
                    }
                    if (baseVersionTime == null) {
                        Object v = Utils.getElementAttribute((Element) e, attributeToCompare);
                        if (!Utils2.isNullOrEmpty(v)) {
                            if (v instanceof String) {
                                // System.out.println(v);
                            }
                        }
                    }
                    else {
                        //TODO @scopecreep throw new MethodNotSupportedException("");
                    }
                }
                // diff the elements
                DBParagraph retval = new DBParagraph();
                retval.setText("</diffResult>this will be the results.</diffResults>"); // concatenate the elements
                res.add(retval);
            }
            return res;
        }
    }

    public String getBaseVersionTime() {
        return baseVersionTime;
    }

    public void setBaseVersionTime(String baseVersionTime) {
        this.baseVersionTime = baseVersionTime;
    }

    public String getCompareToTime() {
        return compareToTime;
    }

    public void setCompareToTime(String compareToTime) {
        this.compareToTime = compareToTime;
    }

    public AvailableAttribute getAttributeToCompare() {
        return attributeToCompare;
    }

    public void setAttributeToCompare(AvailableAttribute attributeToCompare) {
        this.attributeToCompare = attributeToCompare;
    }

    private Date parseDate(String candidate) {
        List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
        knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm"));
        knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd HH:mm"));
        knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss"));
        knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy'T'HH"));
        knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd"));
        knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy"));
        knownPatterns.add(new SimpleDateFormat("yyyyMMdd"));
        knownPatterns.add(new SimpleDateFormat("MMM/dd/yyyy"));
        for (SimpleDateFormat pattern : knownPatterns) {
            try {
                // Take a try
                return new Date(pattern.parse(candidate).getTime());

            } catch (ParseException pe) {
                // Loop on
            }
        }
        System.err.println("No known Date format found: " + candidate);
        return null;
    }
}
