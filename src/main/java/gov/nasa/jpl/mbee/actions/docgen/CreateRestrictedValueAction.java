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

import gov.nasa.jpl.mbee.lib.Utils;

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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.impl.ElementsFactory;

public class CreateRestrictedValueAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Property p;
    public static final String actionid = "CreateRestrictedValue";

    public CreateRestrictedValueAction(Property p) {
        super(null, "Create Restricted Value", null, null);
        this.p = p;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
    	SessionManager.getInstance().createSession("instance restricted value");
    	
    	final ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
    	final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
    	
		final Expression expression = ef.createExpressionInstance();
		
		final LiteralString ls = ef.createLiteralStringInstance();
		ls.setValue("RestrictedValue");
		expression.getOperand().add(ls);
		
		final Expression ex = ef.createExpressionInstance();
		final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
		final List<Class<?>> types = new ArrayList<Class<?>>();
		
		// An example of filtering if deemed necessary later on.
		//types.add(ElementValue.class);
		
		final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
		final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
		ElementSelectionDlgFactory.initMultiple(dlg, set, sei, new ArrayList<Object>());
		dlg.setSelectionMode(SelectionMode.MULTIPLE_MODE);
		if (dlg != null) {
			dlg.setVisible(true);
			if (dlg.isOkClicked() && dlg.getSelectedElements() != null && !dlg.getSelectedElements().isEmpty()) {
				for (final BaseElement be : dlg.getSelectedElements()) {
					System.out.println(be.getClass());
					ElementValue ev = null;
					if (be instanceof Element) {
						ev = ef.createElementValueInstance();
						ev.setElement((Element) be);
					}
					// decided not to allow this sort of cross-referencing as it may cause issues or not even be possible in MagicDraw (untested)
					/* else if (be instanceof ElementValue) {
						ev = (ElementValue) be;
					} */
					
					if (ev != null) {
						ex.getOperand().add(ev);
					}
				}
			}
			else {
				SessionManager.getInstance().closeSession();
				return;
			}
		}
		
		final ElementValue ev = ef.createElementValueInstance();
		expression.getOperand().add(ev);
		expression.getOperand().add(ex);
		
		if (!ex.getOperand().isEmpty()) {
			final List<ValueSpecification> options = ex.getOperand();
			final ElementSelectionDlg dlg2 = ElementSelectionDlgFactory.create(dialogParent);
			final List<Class<?>> types2 = new ArrayList<Class<?>>();
			final TypeFilter tf = new TypeFilterImpl(types2) {
				@Override
				public boolean accept(BaseElement baseElement, boolean checkType) {
					return baseElement != null && super.accept(baseElement, checkType) && options != null && options.contains(baseElement);
				}
			};
			final SelectElementInfo sei2 = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
			ElementSelectionDlgFactory.initSingle(dlg2, sei2, new TypeFilterImpl(), tf, new ArrayList<Class<?>>(), null);
			
			Utils.disableSingleSelection(dlg2);
			// Used to disable the tree view in the single selection window as it does not work... at all
			
			if (dlg2 != null) {
				dlg2.setVisible(true);
				if (dlg2.isOkClicked()) {
					if (dlg2.getSelectedElement() instanceof ElementValue) {
						ev.setElement(((ElementValue) dlg2.getSelectedElement()).getElement());
					}
				}
			}
		}
		else {
			SessionManager.getInstance().closeSession();
			return;
		}
		
    	p.setDefaultValue(expression);
    	SessionManager.getInstance().closeSession();
    }
}
