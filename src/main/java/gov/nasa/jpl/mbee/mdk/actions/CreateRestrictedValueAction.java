package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.*;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.impl.ElementsFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.Class;
import java.util.ArrayList;
import java.util.List;

public class CreateRestrictedValueAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private ArrayList<Property> props;
    public static final String DEFAULT_ID = "CreateRestrictedValue";

    public static final int[] TABBED_PANE_INDICES = {1, 0, 0, 0, 1, 0, 0, 1, 1};
    // final JTabbedPane jtp = ((JTabbedPane) ((Container) ((Container) ((Container) ((Container) ((Container) ((Container) ((Container) ((Container) dlg2.getContentPane().getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[0]).getComponents()[0]).getComponents()[1]).getComponents()[1]);

    public CreateRestrictedValueAction(ArrayList<Property> ps) {
        super(null, "Create Restricted Value", null, null);
        this.props = ps;
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        // vars

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
        dlg.setVisible(true);
        if (dlg.isOkClicked()) {
            baseElems.addAll(dlg.getSelectedElements());
        }
        else {
            return;
        }

        if (baseElems.isEmpty()) {
            Application.getInstance().getGUILog().log("No elements selected for restricted value.");
            return;
        }

        // build second window
        final ElementSelectionDlg dlg2 = ElementSelectionDlgFactory.create(dialogParent);
        final List<Class<?>> types2 = new ArrayList<Class<?>>();
        final TypeFilter tf = new TypeFilterImpl(types2) {
            @Override
            public boolean accept(BaseElement baseElement, boolean checkType) {
                return baseElement != null && super.accept(baseElement, checkType) && baseElems.contains(baseElement);
            }
        };
        final SelectElementInfo sei2 = new SelectElementInfo(true, false, Application.getInstance().getProject().getPrimaryModel(), true);
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

        SessionManager.getInstance().createSession("instance restricted value");
        for (Property prop : props) {
            if (!prop.isEditable()) {
                Application.getInstance().getGUILog().log(prop.getQualifiedName() + " is not editable. Skipped creating restricted value.");
                continue;
            }
            // vars
            final Expression expression = elementsFact.createExpressionInstance();
            final LiteralString ls = elementsFact.createLiteralStringInstance();
            final Expression ex = elementsFact.createExpressionInstance();
            final ElementValue ev = elementsFact.createElementValueInstance();

            // set literalString
            ls.setValue("RestrictedValue");

            // add base elements to ex
            for (final BaseElement be : baseElems) {
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
