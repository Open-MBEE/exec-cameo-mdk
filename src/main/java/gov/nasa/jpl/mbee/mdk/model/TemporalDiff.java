package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.util.Utils.AvailableAttribute;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

public class TemporalDiff extends Table {
    private String baseCommitId;
    private String compareCommitId;
    private String tagAttr;
    private String baseRefId;
    private String compareRefId;

    public TemporalDiff() {
        setSortElementsByName(false);
    }

    @Override
    public void initialize() {
        Object attr = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.attributeChoosable, "desiredAttribute", DocGenProfile.PROFILE_NAME, null);
        AvailableAttribute attributeToCompare;
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
        baseRefId = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "baseRefId", DocGenProfile.PROFILE_NAME, null);
        baseCommitId = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "baseCommitId", DocGenProfile.PROFILE_NAME, null);
        compareRefId = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "compareRefId", DocGenProfile.PROFILE_NAME, null);
        compareCommitId = (String) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.temporalDiffStereotype, "compareCommitId", DocGenProfile.PROFILE_NAME, "latest");
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> results = new ArrayList<>();
        List<Object> targets = getTargets();
        if (forViewEditor) {
            for (Object target : targets) {
                if (target instanceof Element) {
                    StringBuffer buffer = new StringBuffer();
                    DBParagraph paragraph = new DBParagraph();
                    // <mms-diff-attr mms-element-one-id="" (mms-element-two-id="")  mms-attr="name|doc|val" (mms-project-one-id="" mms-project-two-id="" mms-ref-one-id="" mms-ref-two-id="" mms-commit-one-id="" mms-commit-two-id="")></mms-diff-attr>
                    buffer
                            .append("<mms-diff-attr")
                            .append(" ")
                            .append("mms-attr=")
                            .append("\"")
                            .append(StringEscapeUtils.escapeXml(tagAttr))
                            .append("\"");
                    buffer
                            .append(" ")
                            .append("mms-base-element-id=")
                            .append("\"")
                            .append(StringEscapeUtils.escapeXml(Converters.getElementToIdConverter().apply((Element) target)))
                            .append("\"");
                    if (baseRefId != null) {
                        buffer
                                .append(" ")
                                .append("mms-base-ref-id=")
                                .append("\"")
                                .append(StringEscapeUtils.escapeXml(baseRefId))
                                .append("\"");
                    }
                    if (baseCommitId != null) {
                        buffer
                                .append(" ")
                                .append("mms-base-commit-id=")
                                .append("\"")
                                .append(StringEscapeUtils.escapeXml(baseCommitId))
                                .append("\"");
                    }
                    buffer
                            .append(" ")
                            .append("mms-compare-element-id=")
                            .append("\"")
                            .append(StringEscapeUtils.escapeXml(Converters.getElementToIdConverter().apply((Element) target)))
                            .append("\"");
                    if (compareRefId != null) {
                        buffer
                                .append(" ")
                                .append("mms-compare-ref-id=")
                                .append("\"")
                                .append(StringEscapeUtils.escapeXml(compareRefId))
                                .append("\"");
                    }
                    if (compareCommitId != null) {
                        buffer
                                .append(" ")
                                .append("mms-compare-commit-id=")
                                .append("\"")
                                .append(StringEscapeUtils.escapeXml(compareCommitId))
                                .append("\"");
                    }
                    buffer
                            .append(">")
                            .append("</mms-diff-attr>");

                    paragraph.setText(buffer);
                    results.add(paragraph);
                }
            }
        }
        // TODO Implement diff for local DocGen
        return results;
    }
}
