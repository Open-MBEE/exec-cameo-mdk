package gov.nasa.jpl.mbee.mdk.docgen.table;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class EditableTableModel extends AbstractTableModel {

    private List<List<Object>> model;
    private List<String> headers;
    private List<List<Boolean>> editable;
    private List<List<PropertyEnum>> whatToShow;
    private List<Boolean> editableCol;
    private List<PropertyEnum> whatToShowCol;
    private GUILog gl;
    private Set<Object> changed;
    private int precision;

    public EditableTableModel(List<List<Object>> m, List<String> headers, List<List<Boolean>> editable,
                              List<List<PropertyEnum>> e, List<Boolean> editableCol, List<PropertyEnum> whatToShowCol,
                              int precision) {
        this.model = m;
        this.headers = headers;
        gl = Application.getInstance().getGUILog();
        this.editable = editable;
        this.changed = new HashSet<Object>();
        whatToShow = e;
        this.precision = precision;
        this.editableCol = editableCol;
        this.whatToShowCol = whatToShowCol;
    }

    public List<List<Object>> getModel() {
        return model;
    }

    @Override
    public int getColumnCount() {
        return headers.size();
    }

    @Override
    public int getRowCount() {
        return model.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object element = model.get(row).get(col);
        PropertyEnum what = null;
        if (whatToShow != null && whatToShow.size() > row && whatToShow.get(row).size() > col) {
            what = whatToShow.get(row).get(col);
        }
        else if (whatToShowCol != null && whatToShowCol.size() > col) {
            what = whatToShowCol.get(col);
        }
        if (element instanceof String) {
            return element;
        }
        if (element instanceof Property && what == PropertyEnum.VALUE) {
            return Utils.floatTruncate(UML2ModelUtil.getDefault((Property) element), precision);
        }
        if (element instanceof Slot && what == PropertyEnum.VALUE) {
            String s = Utils.slotValueToString((Slot) element);
            return Utils.floatTruncate(s, precision);
        }
        if (element instanceof Element && what == PropertyEnum.DOC) {
            return ModelHelper.getComment((Element) element);
        }
        if (element instanceof NamedElement) {
            return ((NamedElement) element).getName();
        }
        if (element != null) {
            return element.toString();
        }
        return "n/a";
    }

    @Override
    public java.lang.Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (editable != null && editable.size() > row && editable.get(row).size() > col) {
            return editable.get(row).get(col);
        }
        if (editableCol != null && editableCol.size() > col) {
            return editableCol.get(col);
        }
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Object element = model.get(row).get(col);
        PropertyEnum what = PropertyEnum.NAME;
        if (whatToShow != null && whatToShow.size() > row && whatToShow.get(row).size() > col) {
            what = whatToShow.get(row).get(col);
        }
        else if (whatToShowCol != null && whatToShowCol.size() > col) {
            what = whatToShowCol.get(col);
        }
        if (element instanceof String || element == null) {
            return;
        }
        if (element instanceof Element && !((Element) element).isEditable()) {
            JOptionPane.showMessageDialog(null, "Element " + ((NamedElement) element).getQualifiedName()
                    + " is not editable!");
            gl.log("Element " + ((NamedElement) element).getQualifiedName() + " is not editable!");
            return;
        }
        Project project = Application.getInstance().getProject();
        try {
            SessionManager.getInstance().createSession(project, "change");
            if (element instanceof Property && what == PropertyEnum.VALUE) {
                if (value instanceof String) {
                    Utils.setPropertyValue((Property) element, (String) value);
                }
            }
            else if (element instanceof Slot && what == PropertyEnum.VALUE) {
                if (value instanceof String) {
                    Utils.setSlotValue((Slot) element, value);
                }
            }
            else if (element instanceof Element && what == PropertyEnum.DOC) {
                ModelHelper.setComment((Element) element, value.toString());
            }
            else if (element instanceof NamedElement) {
                if (value instanceof String) {
                    ((NamedElement) element).setName((String) value);
                }
            }
            changed.add(element);
            this.fireTableCellUpdated(row, col);
            SessionManager.getInstance().closeSession(project);
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession(project);
            gl.log(ex.getMessage());
            for (StackTraceElement s : ex.getStackTrace()) {
                gl.log(s.toString());
            }
            ex.printStackTrace();
        }
    }

    @Override
    public String getColumnName(int c) {
        return headers.get(c);
    }

    public Set<Object> getChanged() {
        return changed;
    }

    public void setModel(List<List<Object>> model) {
        this.model = model;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setEditable(List<List<Boolean>> editable) {
        this.editable = editable;
    }

    public void setElementChanged(Element e) {
        changed.add(e);
    }

    public List<List<PropertyEnum>> getWhatToChange() {
        return whatToShow;
    }

    public List<PropertyEnum> getWhatToChangeCol() {
        return whatToShowCol;
    }

    public Object getObjectAt(int row, int col) {
        return this.model.get(row).get(col);
    }

    public PropertyEnum getWhatToChangeAt(int row, int col) {
        PropertyEnum what = null;
        if (whatToShow != null && whatToShow.size() > row && whatToShow.get(row).size() > col) {
            what = whatToShow.get(row).get(col);
        }
        else if (whatToShowCol != null && whatToShowCol.size() > col) {
            what = whatToShowCol.get(col);
        }
        return what;
    }
}
