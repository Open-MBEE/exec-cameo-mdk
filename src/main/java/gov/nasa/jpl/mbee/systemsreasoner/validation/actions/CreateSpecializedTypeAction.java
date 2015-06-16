package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;

public class CreateSpecializedTypeAction extends GenericRuleViolationAction {
	

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_NAME = "Create Specialized Classifier";
	
	private Property property;
	private Classifier parent;
	private boolean redefineAttributes;
	private String name;
	
	public CreateSpecializedTypeAction(final Property property, final Classifier parent) {
		this(property, parent, true, DEFAULT_NAME);
	}
	
	public CreateSpecializedTypeAction(final Property property, final Classifier parent, final boolean redefineAttributes, final String name) {
		super(name, name, null, null);
		this.property = property;
		this.parent = parent;
		this.redefineAttributes = redefineAttributes;
		this.name = name;
	}
	
	public static final void createSpecializedType(final Property property, final Classifier parent, final boolean redefineAttributes) {
		if (property.getType() instanceof Classifier && !(property.getType() instanceof Property)) {
			final Classifier special = (Classifier) CopyPasting.copyPasteElement(property.getType(), parent);
			special.getOwnedMember().clear();
			SpecializeClassifierAction.specialize(special, (Classifier) property.getType());
			property.setType(special);
			if (redefineAttributes) {
				for (final NamedElement ne : special.getInheritedMember()) {
					if (ne instanceof Property && ne instanceof RedefinableElement && !((RedefinableElement) ne).isLeaf()) {
						RedefineAttributeAction.redefineAttribute(special, (RedefinableElement) ne, true, true);
					}
				}
			}
		}
	}

	@Override
	public void run() {
		CreateSpecializedTypeAction.createSpecializedType(property, parent, redefineAttributes);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "create specialized classifier";
	}
}
