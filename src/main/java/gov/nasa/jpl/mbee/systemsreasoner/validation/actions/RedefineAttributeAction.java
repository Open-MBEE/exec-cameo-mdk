package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Namespace;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;

public class RedefineAttributeAction extends GenericRuleViolationAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_NAME = "Redefine Attribute";
	
	private Classifier clazz;
	private RedefinableElement re;
	private boolean createSpecializedType;
	private String name;

	public RedefineAttributeAction(final Classifier clazz, final RedefinableElement re) {
		this(clazz, re, false, DEFAULT_NAME);
	}
	
	public RedefineAttributeAction(final Classifier clazz, final RedefinableElement re, final boolean createSpecializedType, final String name) {
		super(name, name, null, null);
		this.clazz = clazz;
		this.re = re;
		this.createSpecializedType = createSpecializedType;
		this.name = name;
	}
	
	public static void redefineAttribute(final Classifier clazz, final RedefinableElement re, final boolean createSpecializedType, final boolean doLog) {
		if (re.isLeaf() && doLog) {
			Application.getInstance().getGUILog().log(re.getQualifiedName() + " is a leaf. Cannot redefine further.");
		}
		
		RedefinableElement redefinedElement = null;
		for (final Property p : clazz.getAttribute()) {
			if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().contains(re)) {
				redefinedElement = (RedefinableElement) p;
				break;
			}
		}
		if (redefinedElement == null) {
			redefinedElement = (RedefinableElement) CopyPasting.copyPasteElement(re, clazz);
			if (redefinedElement instanceof Namespace) {
				((Namespace) redefinedElement).getOwnedMember().clear();
			}
			redefinedElement.getRedefinedElement().add((RedefinableElement) re);
			if (createSpecializedType && redefinedElement instanceof Property && redefinedElement instanceof TypedElement && ((TypedElement) redefinedElement).getType() != null) {
				CreateSpecializedTypeAction.createSpecializedType((Property) redefinedElement, clazz, true);
			}
		}
		else if (doLog) {
			Application.getInstance().getGUILog().log(re.getQualifiedName() + " has already been redefined in " + clazz.getQualifiedName() + ".");
		}
	}

	@Override
	public void run() {
		redefineAttribute(clazz, re, createSpecializedType, true);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "redefine attribute";
	}
}
