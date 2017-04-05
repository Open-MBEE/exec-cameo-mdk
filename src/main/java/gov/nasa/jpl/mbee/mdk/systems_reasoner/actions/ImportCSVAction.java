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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ImportCSVAction extends SRAction {
    public static final String DEFAULT_ID = "Import from CSV";
    private static String csvSeparator = ",";
    private static Classifier selectedClassifier;
    private static int row = 0;

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
        row = 0;
        JFileChooser choose = new JFileChooser();
        choose.setDialogTitle("Open csv file");
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int retval = choose.showOpenDialog(null);
        if (retval == JFileChooser.APPROVE_OPTION) {
            if (choose.getSelectedFile() != null) {
                File savefile = choose.getSelectedFile();
                try {
                    SessionManager.getInstance().createSession("change");
                    gl.log("Starting CSV import.");
                    CSVReader reader = new CSVReader(new FileReader(savefile), separator.charAt(0));
                    importFromCsv(reader);
                    reader.close();
                    SessionManager.getInstance().closeSession();
                    gl.log("CSV import finished.");
                } catch (IOException ex) {
                    gl.log("CSV import failed:");
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


        List<Element> sortedColumns = new ArrayList<Element>();
        boolean isFirstLine = true;
        boolean lineWasEmpty = true;
        int elementName = -1;
        HashMap<Property, Classifier> previousLinesClasses = new HashMap<>();
        HashMap<Property, Classifier> thisLinesClasses = new HashMap<>();
        while (line != null) {
            previousLinesClasses = thisLinesClasses;
            thisLinesClasses = new HashMap<>();
            if (!isFirstLine) {
                for (int jj = 0; jj < sortedColumns.size(); jj++) {
                    String valueFromCSV = line[jj].trim();
                    Element el = sortedColumns.get(jj);
                    if (el != null) {
                        if (el instanceof Property) {
                            Classifier child = null;
                            Type type = ((Property) el).getType();
                            if (!(type instanceof DataType)) {
                                if (!valueFromCSV.isEmpty()) {
                                    if (type instanceof Classifier) {
                                        child = createNewSubElement(line, jj, (Classifier) type);
                                        thisLinesClasses.put((Property) el, child);
                                    }
                                } else {
                                    thisLinesClasses.put((Property) el, previousLinesClasses.get(el));
                                }
                            }
                            if (!valueFromCSV.isEmpty()) {
                                setPropertyValue(valueFromCSV, (RedefinableElement) el, thisLinesClasses);
                            }
                        }
                    } else {
                        if (jj == elementName) {
                            if (!line[elementName].isEmpty()) {
                                Classifier topClass = (Classifier) CopyPasting.copyPasteElement(selectedClassifier, selectedClassifier.getOwner());
                                topClass.getOwnedMember().clear();
                                topClass.getGeneralization().clear();
                                Utils.createGeneralization(selectedClassifier, topClass);
                                topClass.setName(line[elementName]);
                                thisLinesClasses.put(null, topClass);
                                Application.getInstance().getGUILog().log("Creating new " + selectedClassifier.getName() + " named " + topClass.getName() + " for line " + row + ".");
                            } else {
                                thisLinesClasses.put(null, previousLinesClasses.get(null));
                            }
                        }
                    }
                }

            } else {
                for (int c = 0; c < line.length; c++) {
                    String propertyName = line[c];
                    if (!propertyName.isEmpty()) {
                        if (propertyName.contains(".")) {
                            propertyName = handleSubProperty(sortedColumns, propertyName, selectedClassifier);
                        } else {
                            lineWasEmpty = false;
                            sortedColumns.add(getPropertyFromColumnName(propertyName, selectedClassifier));
                            System.out.println(line);
                        }
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

    private String handleSubProperty(List<Element> sortedColumns, String propertyName, Classifier parent) {
        String[] subprops = propertyName.split("\\.");
        propertyName = propertyName.replace(subprops[0] + ".", "");
        String newTypeName = subprops[0];
        Element property = getPropertyFromColumnName(newTypeName, parent);
        if (property instanceof TypedElement) {
            Type type = ((TypedElement) property).getType();
            if (type instanceof Classifier) {
                if (propertyName.contains(".")) {
                    handleSubProperty(sortedColumns, propertyName, (Classifier) type);
                } else {
                    sortedColumns.add(getPropertyFromColumnName(propertyName, (Classifier) type));
                }
            }
        }
        return propertyName;
    }

    private Classifier createNewSubElement(String[] line, int index, Classifier generalClassifier) {

        Classifier createdClassifier = (Classifier) CopyPasting.copyPasteElement(generalClassifier, generalClassifier.getOwner());
        createdClassifier.getOwnedMember().clear();
        createdClassifier.getGeneralization().clear();
        Utils.createGeneralization(generalClassifier, createdClassifier);
        createdClassifier.setName(line[index]);
        Application.getInstance().getGUILog().log("Creating new " + generalClassifier.getName() + " named " + createdClassifier.getName() + " for line " + row + ".");
        return createdClassifier;
    }

    private void setPropertyValue(String valueFromCSV, RedefinableElement el, HashMap<Property, Classifier> thisLinesClasses) {
        if (el instanceof TypedElement) {
            if (el.getOwner() instanceof Classifier) {
                Classifier owner = findMatchingSubclass((Classifier) el.getOwner(), thisLinesClasses.values());
                if (owner != null) {
                    Property prop = UMLFactory.eINSTANCE.createProperty();
                    prop.setType(((TypedElement) el).getType());
                    prop.getRedefinedElement().add(el);
                    prop.setOwner(owner);
                    prop.setName(el.getName());
                    ValueSpecification vs = null;
                    Classifier linkedElement = null;
                    if (((TypedElement) el).getType() instanceof Classifier) {
                        linkedElement = findMatchingSubclass((Classifier) ((TypedElement) el).getType(), thisLinesClasses.values());
                    }
                    if (linkedElement == null) {
                        if (((Property) el).getType() instanceof DataType) {
                            if (!valueFromCSV.isEmpty()) {
                                try {
                                    if (((Property) el).getType().getID().equals("_16_5_1_12c903cb_1245415335546_479030_4092")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralString();
                                        ((LiteralString) vs).setValue(valueFromCSV);
                                    } else if (((Property) el).getType().getID().equals("_11_5EAPbeta_be00301_1147431819399_50461_1671")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralReal();
                                        ((LiteralReal) vs).setValue(Double.parseDouble(valueFromCSV));
                                    } else if (((Property) el).getType().getID().equals("_16_5_1_12c903cb_1245415335546_39033_4086")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralBoolean();
                                        ((LiteralBoolean) vs).setValue(Boolean.parseBoolean(valueFromCSV));
                                    } else if (((Property) el).getType().getID().equals("_16_5_1_12c903cb_1245415335546_8641_4088")) {
                                        vs = UMLFactory.eINSTANCE.createLiteralInteger();
                                        ((LiteralInteger) vs).setValue(Integer.parseInt(valueFromCSV));
                                    }
                                } catch (NumberFormatException nf) {
                                    Application.getInstance().getGUILog().log("[WARNING] Value in line " + row + " for property " + el.getName() + " not correct.");

                                }
                            }
                        }
                        if (vs != null) {
                            prop.setDefaultValue(vs);
                        }
                    } else {
                        prop.setType(linkedElement);
                    }
                } else {
                    Application.getInstance().getGUILog().log("Property for " + el.getName() + " not created.");

                }
            }
        }

    }

    private Classifier findMatchingSubclass(Classifier general, Collection<Classifier> thisLinesClasses) {
        for (Classifier cl : thisLinesClasses) {
            if (cl.getGeneral().contains(general)) {
                return cl;
            }
        }
        return null;
    }

    /**
     * Selects in order owned elements, inherited elements and then ignores case.
     *
     * @param propertyName
     */
    private Element getPropertyFromColumnName(String propertyName, Classifier classifier) {
        boolean found = false;
        for (Element p : classifier.getOwnedMember()) {
            if (p instanceof NamedElement) {
                if (propertyName.trim().equals(((NamedElement) p).getName())) {
                    return p;

                }
            }
        }
        if (!found) {
            for (Element p : classifier.getInheritedMember()) {
                if (p instanceof NamedElement) {
                    if (propertyName.trim().equals(((NamedElement) p).getName())) {
                        return p;

                    }
                }
            }
        }
        if (!found) {
            for (Element p : classifier.getOwnedMember()) {
                if (p instanceof NamedElement) {
                    if (propertyName.trim().equalsIgnoreCase(((NamedElement) p).getName())) {
                        return p;

                    }
                }
            }
        }
        if (!found) {
            for (Element p : classifier.getInheritedMember()) {
                if (p instanceof NamedElement) {
                    if (propertyName.trim().equalsIgnoreCase(((NamedElement) p).getName())) {
                        return p;
                    }
                }
            }
        }
        if (!found) {
            return null;
        }
        return null;
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