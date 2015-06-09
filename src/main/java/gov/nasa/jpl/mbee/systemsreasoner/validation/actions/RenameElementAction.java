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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import org.eclipse.emf.common.util.AbstractEnumerator;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;

public class RenameElementAction extends MDAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private NamedElement source, target;

	public RenameElementAction(final NamedElement source, final NamedElement target, final String title) {
		super(title, title, null, null);
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		SessionManager.getInstance().createSession("rename element");
		target.setName(source.getName());
		SessionManager.getInstance().closeSession();
	}
}
