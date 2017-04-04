package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import au.com.bytecode.opencsv.CSVReader;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImportCSVAction extends SRAction {
    public static final String DEFAULT_ID = "Import from CSV";
    private static String csvSeparator = ",";
    private static Classifier selectedClassifier;

    public ImportCSVAction(Classifier classifier) {
        super(DEFAULT_ID);
        this.selectedClassifier = classifier;
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
                    for (StackTraceElement s : ex.getStackTrace()) {
                        gl.log("\t" + s.toString());
                    }
                }
            }
        }
    }

    private void importFromCsv(CSVReader reader) throws IOException {
        GUILog gl = Application.getInstance().getGUILog();
        Project project = Application.getInstance().getProject();

        String[] line = reader.readNext(); // ignore header
        //line = reader.readNext();
        int row = 0;

        List<Element> sortedColumns = new ArrayList<Element>();
        boolean isFirstLine = true;
        boolean lineWasEmpty = true;
        int elementName = -1;
        boolean notEditableErrorSeen = false;
        String[] propertyLine = line;
        while (line != null) {
            String[] props = line;
            // gl.log("line: " + props.toString());
            if (!isFirstLine) {
                Classifier cl = (Classifier) CopyPasting.copyPasteElement(selectedClassifier, selectedClassifier.getOwner());
                cl.getOwnedMember().clear();
                cl.getGeneralization().clear();
                Utils.createGeneralization(selectedClassifier, cl);
                if (elementName != -1) {
                    if (!props[elementName].isEmpty()) {
                        cl.setName(props[elementName]);
                    }
                }
                Application.getInstance().getGUILog().log("Creating new Element for line " + row);
                for (int jj = 0; jj < sortedColumns.size(); jj++) {
                    Element el = sortedColumns.get(jj);
                    if (el != null) {
                        if (el instanceof Property) {
                            Property prop = UMLFactory.eINSTANCE.createProperty();
                            prop.getRedefinedElement().add((RedefinableElement) el);
                            prop.setOwner(cl);
                            prop.setName(((Property) el).getName());
                            LiteralSpecification vs = null;
                            if (((Property) el).getType() instanceof DataType) {
                                if (!props[jj].isEmpty()) {
                                    if (((Property) el).getType().getID().equals("_16_5_1_12c903cb_1245415335546_479030_4092")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralString();
                                        ((LiteralString) vs).setValue(props[jj]);
                                    } else if (((Property) el).getType().getID().equals("_11_5EAPbeta_be00301_1147431819399_50461_1671")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralReal();
                                        ((LiteralReal) vs).setValue(Double.parseDouble(props[jj]));
                                    } else if (((Property) el).getType().getID().equals("_16_5_1_12c903cb_1245415335546_39033_4086")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralBoolean();
                                        ((LiteralBoolean) vs).setValue(Boolean.parseBoolean(props[jj]));
                                    } else if (((Property) el).getType().getID().equals("_16_5_1_12c903cb_1245415335546_8641_4088")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralInteger();
                                        ((LiteralInteger) vs).setValue(Integer.parseInt(props[jj]));
                                    }
                                }
                            } else {

                            }
                            if (vs != null) {
                                prop.setDefaultValue(vs);
                            }
                        }
                    }
                }
            } else {
                for (int c = 0; c < props.length; c++) {
                    String propertyName = props[c];
                    if (propertyName.contains(".")) {
                        String[] subprops = propertyName.split("//.");
                        propertyName = subprops[0];
                    }
                    if (!propertyName.isEmpty()) {
                        lineWasEmpty = false;
                        getPropertyFromColumnName(sortedColumns, propertyName);
                        System.out.println(line);

                    } else {
                        elementName = c;
                        sortedColumns.add(null);
                    }
                }
            }
            if (!lineWasEmpty) {
                isFirstLine = false;
            }
            line = reader.readNext();
            row++;
        }
    }

    /**
     * Selects in order owned elements, inherited elements and then ignores case.
     *
     * @param sortedColumns
     * @param propertyName
     */
    private void getPropertyFromColumnName(List<Element> sortedColumns, String propertyName) {
        boolean found = false;
        for (Element p : selectedClassifier.getOwnedMember()) {
            if (p instanceof NamedElement) {
                if (propertyName.trim().equals(((NamedElement) p).getName())) {
                    sortedColumns.add(p);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            for (Element p : selectedClassifier.getInheritedMember()) {
                if (p instanceof NamedElement) {
                    if (propertyName.trim().equals(((NamedElement) p).getName())) {
                        sortedColumns.add(p);
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!found) {
            for (Element p : selectedClassifier.getOwnedMember()) {
                if (p instanceof NamedElement) {
                    if (propertyName.trim().equalsIgnoreCase(((NamedElement) p).getName())) {
                        sortedColumns.add(p);
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!found) {
            for (Element p : selectedClassifier.getInheritedMember()) {
                if (p instanceof NamedElement) {
                    if (propertyName.trim().equalsIgnoreCase(((NamedElement) p).getName())) {
                        sortedColumns.add(p);
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!found) {
            sortedColumns.add(null);
        }
    }

    /**
     * Method to bring up input dialog to query user for the delimiter type
     *
     * @return The character delimiter
     */
    private static String getSeparator() {
        String separator = null;
//       separator = JOptionPane.showInputDialog("Provide separator (use only a single character).",
//                csvSeparator);
        if (separator == null) {
            separator = csvSeparator;
        }
        return separator;
    }
}