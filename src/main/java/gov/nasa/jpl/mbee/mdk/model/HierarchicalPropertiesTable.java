package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class HierarchicalPropertiesTable extends Table {
    protected int floatingPrecision;
    protected int maxDepth;
    protected List<String> topIncludeTypeName;
    protected List<String> topExcludeTypeName;
    protected List<Stereotype> topIncludeStereotype;
    protected List<Stereotype> topExcludeStereotype;
    protected List<String> topIncludeName;
    protected List<String> topExcludeName;
    protected int topAssociationType;
    protected List<String> topOrder;
    protected boolean showType;
    protected boolean includeInherited;

    public boolean isIncludeInherited() {
        return includeInherited;
    }

    public void setIncludeInherited(boolean includeInherited) {
        this.includeInherited = includeInherited;
    }

    public void setFloatingPrecision(int floatingPrecision) {
        this.floatingPrecision = floatingPrecision;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setTopIncludeTypeName(List<String> topIncludeTypeName) {
        this.topIncludeTypeName = topIncludeTypeName;
    }

    public void setTopExcludeTypeName(List<String> topExcludeTypeName) {
        this.topExcludeTypeName = topExcludeTypeName;
    }

    public void setTopIncludeStereotype(List<Stereotype> topIncludeStereotype) {
        this.topIncludeStereotype = topIncludeStereotype;
    }

    public void setTopExcludeStereotype(List<Stereotype> topExcludeStereotype) {
        this.topExcludeStereotype = topExcludeStereotype;
    }

    public void setTopIncludeName(List<String> topIncludeName) {
        this.topIncludeName = topIncludeName;
    }

    public void setTopExcludeName(List<String> topExcludeName) {
        this.topExcludeName = topExcludeName;
    }

    public void setTopAssociationType(int topAssociationType) {
        this.topAssociationType = topAssociationType;
    }

    public void setTopOrder(List<String> topOrder) {
        this.topOrder = topOrder;
    }

    public void setShowType(boolean showType) {
        this.showType = showType;
    }

    public int getFloatingPrecision() {
        return floatingPrecision;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public List<String> getTopIncludeTypeName() {
        return topIncludeTypeName;
    }

    public List<String> getTopExcludeTypeName() {
        return topExcludeTypeName;
    }

    public List<Stereotype> getTopIncludeStereotype() {
        return topIncludeStereotype;
    }

    public List<Stereotype> getTopExcludeStereotype() {
        return topExcludeStereotype;
    }

    public List<String> getTopIncludeName() {
        return topIncludeName;
    }

    public List<String> getTopExcludeName() {
        return topExcludeName;
    }

    public int getTopAssociationType() {
        return topAssociationType;
    }

    public List<String> getTopOrder() {
        return topOrder;
    }

    public boolean isShowType() {
        return showType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        Integer maxDepth = (Integer) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "maxDepth", DocGenProfile.PROFILE_NAME, 0);
        List<String> topIncludeTypeName = DocGenUtils
                .getElementNames((Collection<NamedElement>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                        DocGenProfile.hierarchicalPropertiesTableStereotype, "topIncludeTypeName",
                        DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        List<String> topExcludeTypeName = DocGenUtils
                .getElementNames((Collection<NamedElement>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                        DocGenProfile.hierarchicalPropertiesTableStereotype, "topExcludeTypeName",
                        DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        List<Stereotype> topIncludeStereotype = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "topIncludeStereotype",
                DocGenProfile.PROFILE_NAME, new ArrayList<Stereotype>());
        List<Stereotype> topExcludeStereotype = (List<Stereotype>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "topExcludeStereotype",
                DocGenProfile.PROFILE_NAME, new ArrayList<Stereotype>());
        List<String> topIncludeName = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.hierarchicalPropertiesTableStereotype,
                        "topIncludeName", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        List<String> topExcludeName = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.hierarchicalPropertiesTableStereotype,
                        "topExcludeName", DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        Integer topAssociationType = (Integer) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.hierarchicalPropertiesTableStereotype, "topAssociationType", DocGenProfile.PROFILE_NAME, 0);
        List<String> topOrder = DocGenUtils.getElementNames((Collection<NamedElement>) GeneratorUtils
                .getStereotypePropertyValue(dgElement, DocGenProfile.hierarchicalPropertiesTableStereotype, "topOrder",
                        DocGenProfile.PROFILE_NAME, new ArrayList<Property>()));
        if (!topIncludeName.isEmpty() && topOrder.isEmpty()) {
            topOrder = topIncludeName;
        }

        setFloatingPrecision((Integer) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.precisionChoosable, "floatingPrecision", DocGenProfile.PROFILE_NAME, -1));
        setMaxDepth(maxDepth);
        setTopIncludeTypeName(topIncludeTypeName);
        setTopExcludeTypeName(topExcludeTypeName);
        setTopIncludeStereotype(topIncludeStereotype);
        setTopExcludeStereotype(topExcludeStereotype);
        setTopIncludeName(topIncludeName);
        setTopExcludeName(topExcludeName);
        setTopAssociationType(topAssociationType);
        setTopOrder(topOrder);
        setIncludeInherited((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                DocGenProfile.inheritedChoosable, "includeInherited", DocGenProfile.PROFILE_NAME, false));
    }
}
