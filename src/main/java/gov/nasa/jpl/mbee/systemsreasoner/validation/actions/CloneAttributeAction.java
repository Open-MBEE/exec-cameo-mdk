package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.eclipse.jdt.core.dom.Modifier;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.emf.ValueHolder;
import com.nomagic.magicdraw.emf.impl.ValueHolderImpl;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.impl.ElementsFactory;

import org.eclipse.emf.common.util.AbstractEnumerator;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;

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
		try {
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
					final List<Field> fields = getModifiableFields(property);
					clone = Application.getInstance().getProject().getElementsFactory().createPropertyInstance();
					String id = clone.getID();
					System.out.println("IDDDDDD: " + id);
					try {
						//for (final Field field : fields) {
						for (int i = 0; i < fields.size(); i++) {
							final Field field = fields.get(i);
							//if (i % 2 == 1 || i < fields.size() / 2 || field.getName().toLowerCase().contains("holder"))
							//if (field.getName().toLowerCase().contains("holder"))
							//	continue;
							field.setAccessible(true);
							final Object o = field.get(property);
							if (field.getName().toLowerCase().contains("holder") && ValueHolder.class.isAssignableFrom(field.getType())) {
								final ValueHolder vh = (ValueHolder) o;
								final ValueHolder clonedVh = new ValueHolderImpl(vh.getSetFeatureIDs().length);
								
								final List<Field> vhFields = getModifiableFields(vh);
								byte[] clonedMIndexes = null;
								Object[] clonedMValues = null;
								for (final Field vhField : vhFields) {
									System.out.println("vhField: " + vhField.getName() + "::" + vhField.getType());
									if (clonedMIndexes != null && clonedMValues != null) {
										break;
									}
									else if (vhField.getName().equals("mIndexes") && vhField.getType().isArray()) {
										//System.out.println("GETTING HERE 1");
										vhField.setAccessible(true);
										final byte[] mIndexes = (byte[]) vhField.get(vh);
										clonedMIndexes = Arrays.copyOf(mIndexes, mIndexes.length);
										vhField.set(clonedVh, clonedMIndexes);
									}
									else if (vhField.getName().equals("mValues") && vhField.getType().isArray()) {
										System.out.println("GETTING HERE 3");
										vhField.setAccessible(true);
										//System.out.println("asdfasdf" + vhField.get(clone).toString());
										final Object[] mValues = (Object[]) vhField.get(vh);
										clonedMValues = new Object[mValues.length];
										for (int j = 0; j < clonedMValues.length; j++) {
											clonedMValues[j] = hardClone(mValues[j]);
										}
										vhField.set(clonedVh, clonedMValues);
									}
								}
								
								if (clonedMIndexes != null && clonedMValues != null) {
									System.out.println("Indexes");
									for (int j = 0; j < clonedMIndexes.length; j++) {
										System.out.println(j + ": " + clonedMIndexes[j]);
										//System.out.println(clonedMIndexes[j] + " -> " + clonedMValues[j].getClass().getCanonicalName() + "-" + clonedMValues[j]);
										
										//if (clonedMIndexes[j] == 0) {
										//	clonedMValues[j] = null;
										//}
									}
									System.out.println("Values");
									for (int j = 0; j < clonedMValues.length; j++) {
										System.out.println(j + ": " + clonedMValues[j] + " ... " + clonedMValues[j].getClass().getCanonicalName());
									}
									System.out.println("Old ID: " + clonedMValues[0].getClass().getCanonicalName() + "::" + clonedMValues[0]);
									//clonedMValues[0] = Integer.toString(new Random().nextInt(10000000));
									clonedMValues[0] = id;
									System.out.println("New ID: " + clonedMValues[0]);
									field.set(clone, clonedVh);
								}
								
								/*for (final int idx : vh.getSetFeatureIDs()) {
									System.out.println(idx + " -> " + vh.getValue(idx));
									if (idx != 0) {
										clonedVh.setValue(idx, vh.getValue(idx));
									}
								}
								for (final int idx : clonedVh.getSetFeatureIDs()) {
									System.out.println("iii " + idx + " -> " + clonedVh.getValue(idx));
								}*/
								//field.set(clone, clonedVh);
								/*Application.getInstance().getProject().getElementsFactory()
								final ValueHolder vh = 
								final Object o = field.get(property);
								System.out.println("--- O ---");
								for (final Field f : getModifiableFields(o)) {
									System.out.println(f.getDeclaringClass().getCanonicalName() + "::" + f.getName() + " -> " + f.getType().getCanonicalName());
								}
								System.out.println("--- /O ---");*/
							}
							else if (o != null && Map.class.isAssignableFrom(o.getClass())) {
								final Map map = ((Map) o);
								final Map newMap = map.getClass().newInstance();
								newMap.putAll(map);
								field.set(clone, map);
								System.out.println("MAP -> " + field.getDeclaringClass().getCanonicalName() + "::" + field.getName() + " -> " + field.getType().getCanonicalName() + " ... " + o);
							}
							else {
								//field.set(clone, o);
								System.out.println(field.getDeclaringClass().getCanonicalName() + "::" + field.getName() + " -> " + field.getType().getCanonicalName() + " ... " + o);
							}
						}
					} catch (ReflectiveOperationException ignored) {
						clone = null;
						ignored.printStackTrace();
					}
				}
			}
			System.out.println("CLONED PROPERTY: " + clone);
			if (clone == null) {
				return;
			}
			clazz.getAttribute().add(clone);
			//clazz.getAttribute().add(Application.getInstance().getProject().getElementsFactory().createPropertyInstance());
			//clazz.getAttribute().add(Application.getInstance().getProject().getElementsFactory().createPropertyInstance());
				//clazz.getAttribute().add(property.clone());
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		SessionManager.getInstance().closeSession();
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
	
	public Object hardClone(Object o) {
		if (o == null) {
			return o;
		}
		if (o instanceof Boolean) {
			return new Boolean((Boolean) o);
		}
		else if (o instanceof String) {
			return new String((String) o);
		}
		else if (o instanceof AbstractEnumerator) {
			try {
				Object value = o.getClass().getMethod("getValue").invoke(o);
				return o.getClass().getMethod("get", int.class).invoke(null, value);
			} catch (ReflectiveOperationException roe) {
				// Needed methods probably don't exist. I wish NoMagic implemented a common interface we could use instead. :(
				roe.printStackTrace();
				return o;
			}
		}
		else if (o instanceof Serializable) {
			System.out.println("Serializable hallelujah!");
			try {
				return SerializationUtils.clone((Serializable) o);
			} catch (SerializationException se) {
				// incorrect implementation of serializable on NoMagic's part >.> (not default constructor)
				se.printStackTrace();
				return o;
			}
		}
		else {
			System.out.println("UH OH! Couldn't clone!: " + o + " :: " + o.getClass());
			try {
				for (final Field f : getModifiableFields(o.getClass())) {
					f.setAccessible(true);
					System.out.println("-> " + f.getName() + "::" + f.getType().getCanonicalName());
				}
			} catch (Exception roe) {
				roe.printStackTrace();
			}
			return o;
		}
	}

}
