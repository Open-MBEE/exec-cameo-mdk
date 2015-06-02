package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.commons.lang.SerializationUtils;
import org.eclipse.jdt.core.dom.Modifier;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.impl.ElementsFactory;

public class CloneAttributeAction extends MDAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Class clazz;
	private Property property;

	public CloneAttributeAction(final Class clazz, final Property property) {
		super("CreateProperty", "Create Property", null, null);
		this.clazz = clazz;
		this.property = property;
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		SessionManager.getInstance().createSession("clone attribute");
		Property clone = null;
		if (property instanceof Cloneable) {
			try {
				clone = (Property) property.clone();
			} catch (CloneNotSupportedException ignored) {
				//e1.printStackTrace();
			}
		}
		if (clone == null) {
			if (property instanceof Serializable) {
				clone = (Property) SerializationUtils.clone((Serializable) property);
			}
			else {
				try {
					clone = Application.getInstance().getProject().getElementsFactory().createPropertyInstance();
					java.lang.Class c = property.getClass();
					while (c != null) {
						for (final Field field : c.getDeclaredFields()) {
							if (!Modifier.isStatic(field.getModifiers())) {
								field.setAccessible(true);
								field.set(clone, field.get(property));
								System.out.println(field.getDeclaringClass().getCanonicalName() + "::" + field.getName());
							}
						}
						c = c.getSuperclass();
					}
				} catch (ReflectiveOperationException ignored) {
					ignored.printStackTrace();
				}
			}
		}
		System.out.println("CLONED PROPERTY: " + clone);
		if (clone == null) {
			return;
		}
		clazz.getAttribute().add(clone);
			//clazz.getAttribute().add(property.clone());
		SessionManager.getInstance().closeSession();
	}

}
