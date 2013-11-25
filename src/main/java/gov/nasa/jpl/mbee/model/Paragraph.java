package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class Paragraph extends Query {

    private String         text;
    private List<Property> stereotypeProperties;
    private From           fromProperty;

    public Paragraph(String t) {
        text = t;
    }

    public Paragraph() {
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

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore())
            return res;
        if (getText() != null) {
            if (forViewEditor || !getText().trim().equals(""))
                res.add(new DBParagraph(getText(), getDgElement(), getFrom()));
        } else if (getTargets() != null) {
            List<Element> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            for (Element e: targets) {
                if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
                    for (Property p: getStereotypeProperties()) {
                        res.addAll(Common.getReferenceAsDocumentElements(Reference.getPropertyReference(e, p)));
                        // List<Object> ob =
                        // Utils.getStereotypePropertyValues(e, p, true);
                        // for (Object o: ob) {
                        // if (o instanceof String)
                        // parent.addElement(new DBParagraph((String)o));
                        // }
                    }
                } else
                    res.add(new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION));
            }
        }
        return res;
    }

    @Override
    public void initialize() {
        String body = (String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.paragraphStereotype,
                "body", null);
        setText(body);
        setStereotypeProperties((List<Property>)GeneratorUtils
                .getListProperty(dgElement, DocGen3Profile.stereotypePropertyChoosable,
                        "stereotypeProperties", new ArrayList<Property>()));
    }

}
