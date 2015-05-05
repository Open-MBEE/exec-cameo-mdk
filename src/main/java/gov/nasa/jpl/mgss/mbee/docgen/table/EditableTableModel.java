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
package gov.nasa.jpl.mgss.mbee.docgen.table;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

@SuppressWarnings("serial")
public class EditableTableModel extends AbstractTableModel {

    private List<List<Object>>       model;
    private List<String>             headers;
    private List<List<Boolean>>      editable;
    private List<List<PropertyEnum>> whatToShow;
    private List<Boolean>            editableCol;
    private List<PropertyEnum>       whatToShowCol;
    private GUILog                   gl;
    private Set<Object>              changed;
    private int                      precision;

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
        if (whatToShow != null && whatToShow.size() > row && whatToShow.get(row).size() > col)
            what = whatToShow.get(row).get(col);
        else if (whatToShowCol != null && whatToShowCol.size() > col)
            what = whatToShowCol.get(col);
        if (element instanceof String)
            return element;
        if (element instanceof Property && what == PropertyEnum.VALUE)
            return Utils.floatTruncate(UML2ModelUtil.getDefault((Property)element), precision);
        if (element instanceof Slot && what == PropertyEnum.VALUE) {
            String s = Utils.slotValueToString((Slot)element);
            return Utils.floatTruncate(s, precision);
        }
        if (element instanceof Element && what == PropertyEnum.DOC)
            return ModelHelper.getComment((Element)element);
        if (element instanceof NamedElement)
            return ((NamedElement)element).getName();
        if (element != null)
            return element.toString();
        return "n/a";
    }

    @Override
    public java.lang.Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (editable != null && editable.size() > row && editable.get(row).size() > col)
            return editable.get(row).get(col);
        if (editableCol != null && editableCol.size() > col)
            return editableCol.get(col);
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Object element = model.get(row).get(col);
        PropertyEnum what = PropertyEnum.NAME;
        if (whatToShow != null && whatToShow.size() > row && whatToShow.get(row).size() > col)
            what = whatToShow.get(row).get(col);
        else if (whatToShowCol != null && whatToShowCol.size() > col)
            what = whatToShowCol.get(col);
        if (element instanceof String || element == null)
            return;
        if (element instanceof Element && !((Element)element).isEditable()) {
            JOptionPane.showMessageDialog(null, "Element " + ((NamedElement)element).getQualifiedName()
                    + " is not editable!");
            gl.log("Element " + ((NamedElement)element).getQualifiedName() + " is not editable!");
            return;
        }
        try {
            SessionManager.getInstance().createSession("change");
            if (element instanceof Property && what == PropertyEnum.VALUE) {
                if (value instanceof String)
                    Utils.setPropertyValue((Property)element, (String)value);
            } else if (element instanceof Slot && what == PropertyEnum.VALUE) {
                if (value instanceof String)
                    Utils.setSlotValue((Slot)element, (String)value);
            } else if (element instanceof Element && what == PropertyEnum.DOC) {
                ModelHelper.setComment((Element)element, value.toString());
            } else if (element instanceof NamedElement) {
                if (value instanceof String)
                    ((NamedElement)element).setName((String)value);
            }
            changed.add(element);
            this.fireTableCellUpdated(row, col);
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            gl.log(ex.getMessage());
            for (StackTraceElement s: ex.getStackTrace()) {
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
        if (whatToShow != null && whatToShow.size() > row && whatToShow.get(row).size() > col)
            what = whatToShow.get(row).get(col);
        else if (whatToShowCol != null && whatToShowCol.size() > col)
            what = whatToShowCol.get(col);
        return what;
    }
}
