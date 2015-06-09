package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.eclipse.jdt.core.dom.Modifier;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.emf.ValueHolder;
import com.nomagic.magicdraw.emf.impl.BasicEStoreEList;
import com.nomagic.magicdraw.emf.impl.MDEStoreEObjectImpl;
import com.nomagic.magicdraw.emf.impl.ValueHolderImpl;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import org.eclipse.emf.common.util.AbstractEnumerator;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;

public class RedefineAttributeAction extends MDAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Class clazz;
	private RedefinableElement re;

	public RedefineAttributeAction(final Class clazz, final RedefinableElement re) {
		super("RedefineAttribute", "Redefine Attribute", null, null);
		this.clazz = clazz;
		this.re = re;
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (((RedefinableElement) re).isLeaf()) {
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
			SessionManager.getInstance().createSession("redefine attribute");
			redefinedElement = (RedefinableElement) CopyPasting.copyPasteElement(re, clazz);
			redefinedElement.getRedefinedElement().add((RedefinableElement) re);
			SessionManager.getInstance().closeSession();
		}
		else {
			Application.getInstance().getGUILog().log(re.getQualifiedName() + " has already been redefined in " + clazz.getQualifiedName() + ".");
		}
	}
}
