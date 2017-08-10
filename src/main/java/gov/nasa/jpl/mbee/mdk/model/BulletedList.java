package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class BulletedList extends Table {
    private boolean orderedList;
    private boolean showTargets;
    private boolean showStereotypePropertyNames;

    public BulletedList() {
        orderedList = false;
        showTargets = true;
        showStereotypePropertyNames = true;
        setSortElementsByName(false);
    }

    public void setOrderedList(boolean b) {
        orderedList = b;
    }

    public void setShowTargets(boolean b) {
        showTargets = b;
    }

    public void setShowStereotypePropertyNames(boolean b) {
        showStereotypePropertyNames = b;
    }

    public boolean isOrderedList() {
        return orderedList;
    }

    public boolean isShowTargets() {
        return showTargets;
    }

    public boolean isShowStereotypePropertyNames() {
        return showStereotypePropertyNames;
    }

    public void addStereotypeProperties(DBHasContent parent, Element e, Property p) {
        Common.addReferenceToDBHasContent(Reference.getPropertyReference(e, p), parent, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        Boolean showTargets = (Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.bulletedListStereotype, "showTargets", DocGenProfile.PROFILE_NAME, false);
        Boolean showSPN = (Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.bulletedListStereotype, "showStereotypePropertyNames", DocGenProfile.PROFILE_NAME, false);
        Boolean ordered = (Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.bulletedListStereotype, "orderedList", DocGenProfile.PROFILE_NAME, false);
        setShowTargets(showTargets);
        setShowStereotypePropertyNames(showSPN);
        setOrderedList(ordered);
        setIncludeDoc((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.documentationChoosable, "includeDoc", DocGenProfile.PROFILE_NAME, false));
        setStereotypeProperties((List<Property>) GeneratorUtils.getStereotypePropertyValue(dgElement, DocGenProfile.stereotypePropertyChoosable,
                        "stereotypeProperties", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ignore) {
            return res;
        }
        if (targets != null && !targets.isEmpty()) {
            DBList l = new DBList();
            res.add(l);
            l.setOrdered(isOrderedList());
            List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            if (isShowTargets() || isIncludeDoc()) {

                for (Object o : targets) {
                    DBListItem li = new DBListItem();
                    l.addElement(li);
                    if (!(o instanceof Element)) {
                        li.addElement(new DBText(o));
                        continue;
                    }
                    Element e = (Element) o;
                    if (isShowTargets() && e instanceof NamedElement) {
                        li.addElement(new DBParagraph(((NamedElement) e).getName(), e, From.NAME));
                    }
                    if (isIncludeDoc() && (e instanceof Element) && (!ModelHelper.getComment(e).isEmpty() || forViewEditor)) {
                        li.addElement(new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION));
                    }
                    if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
                        if (isShowStereotypePropertyNames()) {
                            DBList l2 = new DBList();
                            l2.setOrdered(isOrderedList());
                            li.addElement(l2);
                            for (Property p : getStereotypeProperties()) {
                                DBListItem li2 = new DBListItem();
                                l2.addElement(li2);
                                li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
                                DBList l3 = new DBList();
                                l3.setOrdered(isOrderedList());
                                li2.addElement(l3);
                                addStereotypeProperties(l3, e, p);
                            }
                        }
                        else {
                            DBList l2 = new DBList();
                            l2.setOrdered(isOrderedList());
                            li.addElement(l2);
                            for (Property p : getStereotypeProperties()) {
                                addStereotypeProperties(l2, e, p);
                            }
                        }
                    }
                }
            }
            else {
                for (Object o : targets) {
                    if (!(o instanceof Element)) {
                        continue;
                    }
                    Element e = (Element) o;
                    if (getStereotypeProperties() != null && !getStereotypeProperties().isEmpty()) {
                        if (isShowStereotypePropertyNames()) {
                            for (Property p : getStereotypeProperties()) {
                                DBListItem li2 = new DBListItem();
                                li2.addElement(new DBParagraph(p.getName(), p, From.NAME));
                                l.addElement(li2);
                                DBList l3 = new DBList();
                                li2.addElement(l3);
                                l3.setOrdered(isOrderedList());
                                addStereotypeProperties(l3, e, p);
                            }
                        }
                        else {
                            for (Property p : getStereotypeProperties()) {
                                addStereotypeProperties(l, e, p);
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
}
