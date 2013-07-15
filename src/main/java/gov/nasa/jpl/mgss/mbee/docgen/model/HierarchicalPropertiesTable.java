package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

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
	

}
