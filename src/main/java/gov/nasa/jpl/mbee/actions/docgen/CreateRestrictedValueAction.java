/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.actions.docgen;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilter;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilterImpl;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.impl.ElementsFactory;

public class CreateRestrictedValueAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private ArrayList<Property> props;
    public static final String actionid = "CreateRestrictedValue";
    
    public static final int[] TABBED_PANE_INDICES = { 1, 0, 0, 0, 1, 0, 0, 1, 1 };
    // final JTabbedPane jtp = ((JTabbedPane) ((Container) ((Container) ((Container) ((Container) ((Container) ((Container) ((Container) ((Container) dlg2.getContentPane().getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[1]);
    
    public CreateRestrictedValueAction(ArrayList<Property> ps) {
    	super(null, "Create Restricted Value", null, null);
    	this.props = ps;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
    	
    	// vars
    	
    	SessionManager.getInstance().createSession("instance restricted value");
    	final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
    	final ElementsFactory elementsFact = Application.getInstance().getProject().getElementsFactory();
    	
    	// build first window
    	
		final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
		final List<Class<?>> types = new ArrayList<Class<?>>(); // ??
		final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
		final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initMultiple(dlg, set, sei, new ArrayList<Object>());
		dlg.setSelectionMode(SelectionMode.MULTIPLE_MODE);
		
		// get selections
		final ArrayList<BaseElement> baseElems = new ArrayList<BaseElement>();
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElements() != null && !dlg.getSelectedElements().isEmpty()) {
				for (final BaseElement be: dlg.getSelectedElements()) {
					baseElems.add(be);
					System.out.println("base element");
				}
			} else {
				SessionManager.getInstance().closeSession();
				return;
			}
		}
		
		if (baseElems.isEmpty()) {
			SessionManager.getInstance().closeSession();
			return;
		}
		
		// build second window
		final ElementSelectionDlg dlg2 = ElementSelectionDlgFactory.create(dialogParent);
		final List<Class<?>> types2 = new ArrayList<Class<?>>();
		final TypeFilter tf = new TypeFilterImpl(types2) {
			@Override
			public boolean accept(BaseElement baseElement, boolean checkType) {
				return baseElement != null && super.accept(baseElement, checkType) && baseElems != null && baseElems.contains(baseElement);
			}
		};
		final SelectElementInfo sei2 = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initSingle(dlg2, sei2, new TypeFilterImpl(), tf, new ArrayList<Class<?>>(), null);
		
		// Used to disable the tree view in the single selection window as it does not work... at all
		Container c = dlg2.getContentPane();
		for (final int i : TABBED_PANE_INDICES) {
			if (c.getComponents().length <= i || !(c.getComponents()[i] instanceof Container)) {
				break;
			}
			c = (Container) c.getComponents()[i];
		}
		if (c instanceof JTabbedPane) {
			final JTabbedPane jtp = (JTabbedPane) c;
			if (jtp.getTabCount() >= 2) {
				jtp.setSelectedIndex(1);
				jtp.setEnabledAt(0, false);
			}
		}
		
		Element selectedElement = null;
		if (dlg2 != null) {
			dlg2.setVisible(true);
			if (dlg2.isOkClicked() && dlg2.getSelectedElement() instanceof Element) {
				selectedElement = (Element) dlg2.getSelectedElement();
			}
		}

		// loop me
    	for (Property prop: props) {
		
    		// vars
			final Expression expression = elementsFact.createExpressionInstance();
			final LiteralString ls = elementsFact.createLiteralStringInstance();
			final Expression ex = elementsFact.createExpressionInstance();
			final ElementValue ev = elementsFact.createElementValueInstance();
			
			// set literalString
			ls.setValue("RestrictedValue");
			
			// add base elements to ex
			for (final BaseElement be: baseElems) {
				if (be instanceof Element) {
					final ElementValue subEv = elementsFact.createElementValueInstance();
					subEv.setElement((Element) be);
					ex.getOperand().add(subEv);
				}
			}
			
			//System.out.println(selectedElemVal);
			
			// if any default is selected, set ev to that element
			if (selectedElement != null) {
				ev.setElement(selectedElement);
			}
			
			// add attribs to the main expression
			expression.getOperand().add(ls);
			expression.getOperand().add(ev);
			expression.getOperand().add(ex);
			
			prop.setDefaultValue(expression);
    	}
    	
    	// close session
    	SessionManager.getInstance().closeSession();
    }
}
