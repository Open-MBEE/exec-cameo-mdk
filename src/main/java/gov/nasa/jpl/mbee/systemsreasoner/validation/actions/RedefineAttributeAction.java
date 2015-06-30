package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.MultiplicityElement;
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
	
	public static void redefineAttribute(final Classifier clazz, final RedefinableElement re, final boolean createSpecializedType) {
		redefineAttribute(clazz, re, createSpecializedType, new ArrayList<Property>());
	}
	
	public static void redefineAttribute(final Classifier clazz, final RedefinableElement re, final boolean createSpecializedType, final List<Property> traveled) {
		if (re.isLeaf()) {
			Application.getInstance().getGUILog().log(re.getQualifiedName() + " is a leaf. Cannot redefine further.");
		}
		
		if (!clazz.isEditable()) {
			Application.getInstance().getGUILog().log(clazz.getQualifiedName() + " is not editable. Skipping redefinition.");
			return;
		}
		
		RedefinableElement redefinedElement = null;
		for (final Property p : clazz.getAttribute()) {
			if (p instanceof RedefinableElement && ((RedefinableElement) p).getRedefinedElement().contains(re)) {
				redefinedElement = (RedefinableElement) p;
				break;
			}
		}
		if (redefinedElement == null) {
			int multiplicity = 1;
			if (re instanceof MultiplicityElement) {
				final int upper = ((MultiplicityElement) re).getUpper();
				if (upper > 0) {
					multiplicity = upper;
				}
				else {
					final String multiplicityString = ModelHelper.getMultiplicity((MultiplicityElement) re);
					if (multiplicityString != null && !multiplicityString.isEmpty()) {
						final String response = JOptionPane.showInputDialog("Specify multiplicity for " + clazz.getQualifiedName() + "::" + re.getName() + ".", 1);
						try {
							multiplicity = Integer.parseInt(response);
						} catch (NumberFormatException nfe) {
							Application.getInstance().getGUILog().log("Non-numeric multiplicity specified for " + clazz.getQualifiedName() + "::" + re.getName() + ". Skipping redefinition.");
							return;
						}
					}
				}
				//System.out.println("UPPER: " + upper);
			}
			for (int i = 0; i < multiplicity; i++) {
				redefinedElement = (RedefinableElement) CopyPasting.copyPasteElement(re, clazz, multiplicity > 1);
				if (redefinedElement instanceof Namespace) {
					((Namespace) redefinedElement).getOwnedMember().clear();
				}
				redefinedElement.getRedefinedElement().add((RedefinableElement) re);
				if (createSpecializedType && redefinedElement instanceof Property && redefinedElement instanceof TypedElement && ((TypedElement) redefinedElement).getType() != null) {
					CreateSpecializedTypeAction.createSpecializedType((Property) redefinedElement, clazz, true, traveled);
				}
			}
		}
		else {
			Application.getInstance().getGUILog().log(re.getQualifiedName() + " has already been redefined in " + clazz.getQualifiedName() + ".");
		}
	}

	@Override
	public void run() {
		redefineAttribute(clazz, re, createSpecializedType);
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
