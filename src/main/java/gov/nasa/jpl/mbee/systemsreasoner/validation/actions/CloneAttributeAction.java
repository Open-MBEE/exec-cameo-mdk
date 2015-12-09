package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.emf.ValueHolder;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import org.eclipse.emf.common.util.AbstractEnumerator;
import org.eclipse.emf.common.util.BasicEList;

public class CloneAttributeAction extends MDAction {
	
	/**
	 * Original reflection based approach. Completely defunct. Reference only.
	 */
	private static final long serialVersionUID = 1L;
	
	private Class clazz;
	private Property property;
	private Property clonedProperty;

	public CloneAttributeAction(final Class clazz, final Property property) {
		super("CreateProperty", "Create Property", null, null);
		this.clazz = clazz;
		this.property = property;
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		SessionManager.getInstance().createSession("clone attribute");
		clonedProperty = (Property) clone(property, false);
		if (clonedProperty != null) {
			for (final Stereotype stereotype : StereotypesHelper.getStereotypes(clonedProperty)) {
				stereotype.setOwner(clonedProperty);
			}
			clazz.getAttribute().add(clonedProperty);
		}
		SessionManager.getInstance().closeSession();
	}
	
	public Field getModifiableField(final Object o, String fieldName) {
		java.lang.Class clazz = o.getClass();
		while (clazz != null) {
			for (final Field field : clazz.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && field.getName().equals(fieldName))
					return field;
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}
	
	public List<Field> getModifiableFields(final Object o) {
		final List<Field> fields = new ArrayList<Field>();
		java.lang.Class clazz = o.getClass();
		while (clazz != null) {
			for (final Field field : clazz.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()))
					fields.add(field);
			}
			clazz = clazz.getSuperclass();
		}
		return fields;
	}
	
	public List<java.lang.Class> getInterfacesRecursively(final Object o) {
		final List<java.lang.Class> interfaces = new ArrayList<java.lang.Class>();
		java.lang.Class clazz = o.getClass();
		while (clazz != null) {
			for (final java.lang.Class c : o.getClass().getInterfaces()) {
				interfaces.add(c);
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}
	
	public <R> R clone(R r) {
		return clone(r, true);
	}
	
	@SuppressWarnings("unchecked")
	public <R> R clone(R r, final boolean checkForRecursion) {
		if (r == null) {
			return r;
		}
		if (checkForRecursion && r.equals(property)) {
			System.out.println("Returning cloned property");
			return (R) clonedProperty;
		}
		System.out.println("clone: " + r.getClass());
		/* if (o instanceof Cloneable) {
			try {
				return o.clone();
			} catch (CloneNotSupportedException ignored) {
				//e1.printStackTrace();
			}
		} */ // Clone is broken for everything in MD we've tried :(
		/*if (r instanceof Serializable) {
			try {
				return (R) SerializationUtils.clone((Serializable) r);
			} catch (SerializationException se) {
				//se.printStackTrace();
			}
		}*/
		if (r instanceof Element) {
			System.out.println("ELEMENT! " + r.getClass().getCanonicalName());
			return (R) cloneElement((Element) r);
		}
		else if (r instanceof Boolean) {
			return (R) new Boolean((Boolean) r);
		}
		else if (r instanceof String) {
			return (R) new String((String) r);
		}
		else if (r instanceof int[]) {
			return (R) Arrays.copyOf((int[]) r, ((int[]) r).length);
		}
		else if (r instanceof Object[]) {
			final Object[] clone = new Object[((Object[]) r).length];
			for (int i = 0; i < clone.length; i++) {
				clone[i] = clone(((Object[]) r)[i]);
				System.out.println("Object[]: " + clone[i]);
			}
			return (R) clone;
		}
		else if (r instanceof AbstractEnumerator) {
			try {
				Object value = r.getClass().getMethod("getValue").invoke(r);
				return (R) r.getClass().getMethod("get", int.class).invoke(null, value);
			} catch (ReflectiveOperationException roe) {
				// Needed methods probably don't exist. I wish NoMagic implemented a common interface we could use instead. :(
				roe.printStackTrace();
				return r;
			}
		}
		else if (r instanceof Map) {
			return (R) cloneMap((Map<Object, Object>) r);
		}
		else if (r instanceof BasicEList) {
			return (R) cloneBasicEList((BasicEList<Object>) r);
		}
		else if (r instanceof Collection) {
			return (R) cloneCollection((Collection<Object>) r);
		}
		else {
			System.out.println("UH OH! Couldn't clone!: " + r + " :: " + r.getClass());
			try {
				for (final Field f : getModifiableFields(r.getClass())) {
					f.setAccessible(true);
					System.out.println("-> " + f.getName() + "::" + f.getType().getCanonicalName());
				}
				for (final java.lang.Class interfacee : r.getClass().getInterfaces()) {
					System.out.println("--- " + interfacee.getCanonicalName());
				}
			} catch (Exception roe) {
				roe.printStackTrace();
			}
			return r;
		}
	}
	
	public <R extends Element> R cloneElement(final R element) {
		System.out.println("Cloning " + element + " -> " + element.getClass().getCanonicalName());
		final ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
		R clone = null;
		try {
			final Method[] createInstanceMethods = ef.getClass().getMethods();
			//System.out.println("FOO" + ef.getClass().getInterfaces().length);
			for (final java.lang.Class interfacee : element.getClass().getInterfaces()) {
				System.out.println(interfacee.getSimpleName());
				for (final Method m : createInstanceMethods) {
					//System.out.println("> " + m.getName());
					if (m.getName().equals("create" + interfacee.getSimpleName() + "Instance")) {
						//System.out.println("GOT HERE");
						clone = (R) m.invoke(ef);
						break;
					}
				}
				if (clone != null) {
					break;
				}
			}
			//clone = (R) ef.getClass().getMethod("create" + element.getClass().getName() + "Instance").invoke(null);
		} catch (ReflectiveOperationException roe) {
			//System.out.println("create" + element.getClass().getName() + "Instance");
			roe.printStackTrace();
		}
		if (clone == null) {
			System.out.println("Failed to clone element. Backing up to reference; potentially unsafe.");
			return clone;
		}
		
		final String id = clone.getID();
		final Field mHolderField = getModifiableField(element, "mHolder");
		if (mHolderField != null && ValueHolder.class.isAssignableFrom(mHolderField.getType())) {
			try {
				mHolderField.setAccessible(true);
				final ValueHolder mHolder = (ValueHolder) mHolderField.get(element);
				final ValueHolder clonedMHolder = (ValueHolder) mHolderField.get(clone);
				
				Field mIndexesField, mValuesField;
				byte[] mIndexes = null;
				Object[] mValues = null;
				
				if ((mIndexesField = getModifiableField(mHolder, "mIndexes")) != null) {
					mIndexesField.setAccessible(true);
					mIndexes = (byte[]) mIndexesField.get(mHolder);
					mIndexesField.set(clonedMHolder, clone(mIndexes));
				}
				if ((mValuesField = getModifiableField(mHolder, "mValues")) != null) {
					//System.out.println("ASDFSDFSDFSDFSDFSDFBOO");
					mValuesField.setAccessible(true);
					mValues = (Object[]) mValuesField.get(mHolder);
					/*if (false && element instanceof Stereotype) {
						for (final Object o : mValues) {
							System.out.println("STEREOTYPE: " + o + " -> " + o.getClass().getCanonicalName());
						}
						return (R) ef.createStereotypeInstance();
					}
					else {*/
					
					final Object[] clonedMValues = clone(mValues);
					//System.out.println(clonedMValues[0]);
					clonedMValues[0] = id;
					//System.out.println(clonedMValues[0]);
					mValuesField.set(clonedMHolder, clonedMValues);
					
					//}
					
				}
			} catch (ReflectiveOperationException roe) {
				roe.printStackTrace();
			}
		}
		return clone;
	}
	
	public <R extends Map<Object, Object>> R cloneMap(final R map) {
		R clone = null;
		try {
			//clone = (R) map.getClass().newInstance();
			final Constructor defaultConstructor = map.getClass().getConstructor();
			if (defaultConstructor == null) {
				System.out.println("NO DEFAULT CONSTRUCTOR?!?!?!");
				return map;
			}
			defaultConstructor.setAccessible(true);
			clone = (R) defaultConstructor.newInstance();
			for (Entry<Object, Object> entry : map.entrySet()) {
				clone.put(entry.getKey(), clone(entry.getValue()));
			}
			//clone.putAll(map);
		} catch (ReflectiveOperationException roe) {
			roe.printStackTrace();
			// Almost all implementations of the Map interface has a default constructor. We never expect this exception.
			return map;
		}
		return clone;
	}
	
	public <R extends Collection<Object>> R cloneCollection(final R collection) {
		R clone = null;
		try {
			final Constructor defaultConstructor = collection.getClass().getConstructor();
			if (defaultConstructor == null) {
				System.out.println("NO DEFAULT CONSTRUCTOR?!?!?!");
				return collection;
			}
			defaultConstructor.setAccessible(true);
			clone = (R) defaultConstructor.newInstance();
			for (Object o : collection) {
				clone.add(clone(o));
			}
		} catch (ReflectiveOperationException roe) {
			roe.printStackTrace();
			// Almost all implementations of the List interface has a default constructor. We never expect this exception.
			System.out.println("DUMP CONSTRUCTORS");
			for (final Constructor constructor : collection.getClass().getConstructors()) {
				System.out.println("Constructor: " + constructor);
			}
			return collection;
		}
		return clone;
	}
	
	public <R extends BasicEList<Object>> BasicEList<Object> cloneBasicEList(final R bel) {
		System.out.println("BESEL");
		final BasicEList<Object> clone = new BasicEList<Object>();
		for (Object o : bel) {
			if (o != null && o.equals(property)) {
				clone.add(property);
			}
			else {
				System.out.println("BESEL " + o + " :: " + o.getClass());
				clone.add(clone(o));
			}
		}
		return clone;
	}

}
