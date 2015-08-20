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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

/**
 * Make an editable table.<br/>
 * <p>
 * Takes a table model consisting of Objects. Objects can be Strings or
 * magicdraw Elements. All rows in the model must have the same number of
 * columns
 * </p>
 * <p>
 * Takes a list of strings for headers. number of headers must equal number of
 * columns
 * </p>
 * <p>
 * Takes a model of boolean indicating which of the objects in the model should
 * be editable. (this has no relation to whether the element itself is actually
 * editable, that will be checked by the table when being edited)
 * </p>
 * <p>
 * Takes a model of PropertyEnum indicating whether the name (or value in the
 * case of magicdraw Property) should be shown/edited
 * </p>
 * <p>
 * The Objects for the table model and headers must be present, editable and
 * propertyEnum can be omitted. By default, if there's no instruction, names
 * will be shown/edited for NamedElements and nothing can be edited
 * </p>
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class EditableTable extends JDialog {

    private List<List<Object>>       model;
    private List<String>             headers;
    private List<List<Boolean>>      editable;
    private List<Boolean>            editableCol;
    private List<PropertyEnum>       whatToShowCol;
    private List<List<PropertyEnum>> whatToShow;
    private JTable                   jtable;
    private int                      precision;
    private static String            csvSeparator = ","; // Keep history of
                                                         // selected separator

    /**
     * 
     * @param title
     *            Title of the table
     * @param m
     *            the table model, Object can be String or magicdraw elements,
     *            all rows must have the same amount of columns
     * @param headers
     *            list of strings, size must match number of columns
     * @param editable
     *            can be null, whether each cell should be editable. (this does
     *            not mean whether the element is actually editable, that'll be
     *            checked when actually being edited)
     * @param e
     *            can be null, what to show/edit for each cell. Only two choices
     *            right now, name for NamedElements or value for Properties.
     * @param precision
     *            can be null, this is applicable to if you want to show values
     *            on properties and they're floating numbers, the precision
     *            indicates how mnay decimal places to show
     */
    public EditableTable(String title, List<List<Object>> m, List<String> headers,
            List<List<Boolean>> editable, List<List<PropertyEnum>> e, Integer precision) {
        super(MDDialogParentProvider.getProvider().getDialogParent());
        this.model = m;
        this.headers = headers;
        this.editable = editable;
        this.whatToShow = e;
        this.setTitle(title);
        if (precision == null)
            this.precision = -1;
        else
            this.precision = precision;
    }

    /**
     * Instead of having to make a n x m table of booleans, if your columns are
     * either editable or not (usually the case), use this to set whether
     * columns are editable. instance the table with null and then use this to
     * set column wide
     * 
     * @param editableCol
     */
    public void setEditableCol(List<Boolean> editableCol) {
        this.editableCol = editableCol;
    }

    /**
     * Instead of having to make a n x m table of what to show, specify by
     * columns instead. you can pass in null when instancing the table and then
     * use this to set column wide
     * 
     * @param whatToShowCol
     */
    public void setWhatToShowCol(List<PropertyEnum> whatToShowCol) {
        this.whatToShowCol = whatToShowCol;
    }

    /**
     * this must be called by your script before your script ends
     */
    public void prepareTable() {
        EditableTableModel tmodel = new EditableTableModel(model, headers, editable, whatToShow, editableCol,
                whatToShowCol, precision);
        jtable = new JTable(tmodel);
        jtable.setDefaultRenderer(String.class, new ChangedTableCellRenderer(tmodel));
        jtable.getTableHeader().setPreferredSize(
                new Dimension(jtable.getColumnModel().getTotalColumnWidth(), 45));
        jtable.getTableHeader().setReorderingAllowed(false);
        jtable.setGridColor(Color.BLACK);
        jtable.setRowHeight(20);
        jtable.setSize(new Dimension(300, 120));
        JScrollPane content = new JScrollPane(jtable);
        this.getContentPane().add(content, BorderLayout.CENTER);

        JPanel csv = new JPanel();
        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(new ExportCSVAction(tmodel));
        csv.add(exportButton);
        JButton importButton = new JButton("Import from CSV");
        importButton.addActionListener(new ImportCSVAction(tmodel));
        csv.add(importButton);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new RefreshAction(tmodel));
        csv.add(refreshButton);
        this.getContentPane().add(csv, BorderLayout.PAGE_END);

    }

    class RefreshAction implements ActionListener {
        EditableTableModel ntable;

        public RefreshAction(EditableTableModel t) {
            ntable = t;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            ntable.fireTableDataChanged();
        }
    }

    class ImportCSVAction implements ActionListener {
        EditableTableModel ntable;

        public ImportCSVAction(EditableTableModel t) {
            ntable = t;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            GUILog gl = Application.getInstance().getGUILog();
            String separator = getSeparator();
            if (separator == null) {
                return;
            }

            JFileChooser choose = new JFileChooser();
            choose.setDialogTitle("Open csv file");
            choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int retval = choose.showOpenDialog(null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                if (choose.getSelectedFile() != null) {
                    File savefile = choose.getSelectedFile();
                    try {
                        SessionManager.getInstance().createSession("change");
                        CSVReader reader = new CSVReader(new FileReader(savefile), separator.charAt(0));
                        importFromCsv(reader);
                        reader.close();
                        SessionManager.getInstance().closeSession();
                        gl.log("import succeeded");
                    } catch (IOException ex) {
                        gl.log("import failed");
                        SessionManager.getInstance().cancelSession();
                        gl.log(ex.getMessage());
                        for (StackTraceElement s: ex.getStackTrace()) {
                            gl.log("\t" + s.toString());
                        }
                    }
                }
            }
        }

        private void importFromCsv(CSVReader reader) throws IOException {
            GUILog gl = Application.getInstance().getGUILog();
            Project prj = Application.getInstance().getProject();
            List<List<PropertyEnum>> what = ntable.getWhatToChange();
            List<PropertyEnum> whatCol = ntable.getWhatToChangeCol();
            String[] line = reader.readNext(); // ignore header
            line = reader.readNext();
            int row = 0;

            boolean positionErrorSeen = false;
            boolean notEditableErrorSeen = false;
            while (line != null) {
                String[] props = line;
                // gl.log("line: " + props.toString());
                int col = 0;
                for (int c = 0; c < props.length; c = c + 2) {
                    if (props[c].equals("")) {
                        col++;
                        continue;
                    }
                    PropertyEnum whatToChange = null;
                    BaseElement e = prj.getElementByID(props[c]);
                    String value = props[c + 1];
                    if (what != null && what.size() > row && what.get(row).size() > col)
                        whatToChange = what.get(row).get(col);
                    if (whatToChange == null && whatCol != null && whatCol.size() > col)
                        whatToChange = whatCol.get(col);
                    whatToChange = whatToChange == null ? PropertyEnum.NAME : whatToChange;
                    Object el = null;
                    if (model.size() > row && model.get(row).size() > col)
                        el = model.get(row).get(col);

                    if (e != null) {
                        if (el instanceof Element && ((Element)el).getID().equals(props[c])) {
                            if (e.isEditable()) {
                                try {
                                    String curValue = (String)ntable.getValueAt(row, col);

                                    if (e instanceof Property && whatToChange == PropertyEnum.VALUE) {
                                        if (UML2ModelUtil.getDefault(((Property)e)) == null
                                                || !UML2ModelUtil.getDefault(((Property)e)).equals(value)) {
                                            if (value.equals("")) {
                                                int ok = JOptionPane.showConfirmDialog(null,
                                                        "You're about to set the value of "
                                                                + ((NamedElement)e).getQualifiedName()
                                                                + " to empty, are you sure?", "Confirm",
                                                        JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);
                                                if (ok == JOptionPane.YES_OPTION) {
                                                    Utils.setPropertyValue((Property)e, value);
                                                    ntable.setElementChanged((Element)e);
                                                }
                                            } else {
                                                // only mark cell as changed if
                                                // value changed
                                                if (!curValue.equals(value)) {
                                                    gl.log("changing table element [" + row + "][" + col
                                                            + "] " + ((NamedElement)e).getQualifiedName()
                                                            + "'s value from {" + curValue + "} to {" + value
                                                            + "}");
                                                    Utils.setPropertyValue((Property)e, value);
                                                    ntable.setElementChanged((Element)e);
                                                }
                                            }
                                        }
                                    } else if (e instanceof NamedElement && whatToChange == PropertyEnum.NAME) {
                                        if (!((NamedElement)e).getName().equals(value)) {
                                            if (value.equals("")) {
                                                int ok = JOptionPane.showConfirmDialog(null,
                                                        "You're about to set the name of "
                                                                + ((NamedElement)e).getQualifiedName()
                                                                + " to empty, are you sure?", "Confirm",
                                                        JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);
                                                if (ok == JOptionPane.YES_OPTION) {
                                                    ((NamedElement)e).setName(value);
                                                    ntable.setElementChanged((Element)e);
                                                }
                                            } else {
                                                // only mark cell as changed if
                                                // value changed
                                                if (!curValue.equals(value)) {
                                                    gl.log("changing table element [" + row + "][" + col
                                                            + "] " + ((NamedElement)e).getQualifiedName()
                                                            + "'s name from {" + curValue + "} to {" + value
                                                            + "}");
                                                    ((NamedElement)e).setName(value);
                                                    ntable.setElementChanged((Element)e);
                                                }

                                            }
                                        }
                                    }

                                } catch (Exception ex) {
                                    gl.log(ex.getMessage());
                                    for (StackTraceElement s: ex.getStackTrace())
                                        gl.log("\t" + s.toString());
                                    ex.printStackTrace();
                                }
                            } else if (e instanceof NamedElement) {
                                if (!notEditableErrorSeen) {
                                    JOptionPane
                                            .showMessageDialog(
                                                    null,
                                                    "Element "
                                                            + ((NamedElement)e).getQualifiedName()
                                                            + " is not editable! (Further similar errors can be seen in the log");
                                    notEditableErrorSeen = true;
                                }
                                gl.log("Element " + ((NamedElement)e).getQualifiedName()
                                        + " is not editable!");
                            }
                        } else {
                            if (!positionErrorSeen) {
                                JOptionPane
                                        .showMessageDialog(
                                                null,
                                                "Element with ID "
                                                        + props[c]
                                                        + " is not in the same position as what's in the table! (See log for further similar errors)");
                                positionErrorSeen = true;
                            }
                            gl.log("Element with ID " + props[c]
                                    + " is not in the same position as what's in the table!");
                            if (el instanceof NamedElement) {
                                gl.log("Table element: " + ((NamedElement)el).getQualifiedName()
                                        + ", Importing element: " + ((NamedElement)e).getQualifiedName());
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Element with ID " + props[c] + " not found!");
                        gl.log("Element with ID " + props[c] + " not found!");
                    }
                    col++;
                }
                line = reader.readNext();
                row++;
            }
        }

    }

    class ExportCSVAction implements ActionListener {
        EditableTableModel ntable;

        public ExportCSVAction(EditableTableModel t) {
            ntable = t;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            GUILog gl = Application.getInstance().getGUILog();
            String separator = getSeparator();
            if (separator == null) {
                return;
            }

            JFileChooser choose = new JFileChooser();
            choose.setDialogTitle("Save to csv...");

            int retval = choose.showSaveDialog(null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                if (choose.getSelectedFile() != null) {
                    File savefile = choose.getSelectedFile();
                    try {
                        CSVWriter csvWriter = new CSVWriter(new FileWriter(savefile), separator.charAt(0));
                        exportToCsv(csvWriter);
                        csvWriter.close();
                        gl.log("export succeeded");
                    } catch (IOException ex) {
                        gl.log("export failed");
                        gl.log(ex.getMessage());
                        for (StackTraceElement s: ex.getStackTrace()) {
                            gl.log("\t" + s.toString());
                        }
                    }
                }
            }
        }

        private void exportToCsv(CSVWriter w) throws IOException {

            List<List<Object>> m = ntable.getModel();
            int rows = ntable.getRowCount();
            int cols = ntable.getColumnCount();
            List<String> blah = new ArrayList<String>();
            for (int i = 0; i < cols; i++) {
                blah.add("");
                blah.add(ntable.getColumnName(i));
            }
            w.writeNext(blah.toArray(new String[0]));

            for (int i = 0; i < rows; i++) {
                List<String> s = new ArrayList<String>();
                for (int j = 0; j < cols; j++) {
                    Object mdo = m.get(i).get(j);
                    if (mdo != null && mdo instanceof Element)
                        s.add(((Element)mdo).getID());
                    else
                        s.add("");
                    String val = (String)ntable.getValueAt(i, j);
                    s.add(val);
                }
                w.writeNext(s.toArray(new String[0]));
            }
        }

    }

    /**
     * this may be called after prepareTable is called, if you want the jtable
     * to mess with
     * 
     * @return
     */
    public JTable getTable() {
        return jtable;
    }

    /**
     * don't call this in your script
     */
    public void showTable() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setSize(300, 120);

        // this.setAlwaysOnTop(true);
        this.pack();
        this.setVisible(true);
    }

    class ChangedTableCellRenderer extends DefaultTableCellRenderer {
        private EditableTableModel table;

        public ChangedTableCellRenderer(EditableTableModel t) {
            super();
            table = t;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
            Object e = table.getObjectAt(row, column);
            Set<Object> changed = table.getChanged();
            if (e != null && changed.contains(e) && (e instanceof Element))
                c.setBackground(Color.yellow);
            else
                c.setBackground(Color.white);
            c.setForeground(Color.black);
            if (e != null && e instanceof Property && ((Property)e).isDerived()) {
                if (changed.contains(e))
                    c.setBackground(Color.lightGray);
                else
                    c.setBackground(Color.gray);
            }
            return c;
        }
    }

    /**
     * Method to bring up input dialog to query user for the delimiter type
     * 
     * @return The character delimiter
     */
    public static String getSeparator() {
        String separator = JOptionPane.showInputDialog("Provide separator (use only a single character).",
                csvSeparator);

        if (separator != null) {
            csvSeparator = separator;
        }

        return separator;
    }

}
