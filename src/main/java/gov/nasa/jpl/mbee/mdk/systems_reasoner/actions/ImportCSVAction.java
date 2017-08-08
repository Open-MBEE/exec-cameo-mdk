package gov.nasa.jpl.mbee.mdk.systems_reasoner.actions;

import au.com.bytecode.opencsv.CSVReader;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.SelectionMode;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.validation.SRValidationSuite;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ImportCSVAction extends SRAction {
    public static final String DEFAULT_ID = "Import from CSV";
    private static String csvSeparator = ",";
    private static Classifier selectedClassifier;
    private static int row = 0;
    private static Namespace container;
    private String literalBoolean = "_16_5_1_12c903cb_1245415335546_39033_4086";
    private String literalInteger = "_16_5_1_12c903cb_1245415335546_8641_4088";
    private String literalReal = "_11_5EAPbeta_be00301_1147431819399_50461_1671";
    private String literalString = "_16_5_1_12c903cb_1245415335546_479030_4092";

    public ImportCSVAction(Classifier classifier) {
        super(DEFAULT_ID);
        this.selectedClassifier = classifier;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        final List<java.lang.Class<?>> types = new ArrayList<>();
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);
        types.add(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class);
        types.add(Model.class);


        GUILog gl = Application.getInstance().getGUILog();
        String separator = getSeparator();
        if (separator == null) {
            return;
        }
        row = 0;
        JFileChooser choose = new JFileChooser();
        choose.setDialogTitle("Open CSV file");
        choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
        HashSet<Classifier> createdElements = new HashSet<>();
        int retval = choose.showOpenDialog(null);
        if (retval == JFileChooser.APPROVE_OPTION) {
            if (choose.getSelectedFile() != null) {
                File savefile = choose.getSelectedFile();
                try {

                    final Frame dialogParent = MDDialogParentProvider.getProvider().getDialogParent();
                    final ElementSelectionDlg dlg = ElementSelectionDlgFactory.create(dialogParent);
                    dlg.setTitle("Select container for generated elements:");
                    final SelectElementTypes set = new SelectElementTypes(null, types, null, null);
                    final SelectElementInfo sei = new SelectElementInfo(true, false, Application.getInstance().getProject().getModel().getOwner(), true);
                    ElementSelectionDlgFactory.initSingle(dlg, set, sei, selectedClassifier.getOwner());
                    dlg.setSelectionMode(SelectionMode.SINGLE_MODE);
                    dlg.setVisible(true);
                    if (dlg.isOkClicked() && dlg.getSelectedElement() != null && dlg.getSelectedElement() instanceof Namespace) {
                        container = (Namespace) dlg.getSelectedElement();
                        SessionManager.getInstance().createSession("change");
                        gl.log("[INFO] Starting CSV import.");
                        CSVReader reader = new CSVReader(new FileReader(savefile), separator.charAt(0));
                        importFromCsv(reader, createdElements);
                        checkForRedefinedElements(createdElements);
                        reader.close();
                        SessionManager.getInstance().closeSession();
                        checkForAssociationInheritance(createdElements);
                        gl.log("[INFO] CSV import finished.");
                    }
                } catch (IOException ex) {
                    gl.log("[ERROR] CSV import failed. Reason: " + ex.getMessage());
                    SessionManager.getInstance().cancelSession();
                    ex.printStackTrace();
                    for (StackTraceElement s : ex.getStackTrace()) {
                        gl.log("\t" + s.toString());
                    }
                }
            }
        }
    }


    private void checkForAssociationInheritance(HashSet<Classifier> createdElements) {

        for (Classifier element : createdElements) {
            for (Classifier general : element.getGeneral()) {
                SRValidationSuite.checkAssociationsForInheritance(element, general);
                ValidationRule ele = SRValidationSuite.getAssociationInheritanceRule();
                for (ValidationRuleViolation violation : ele.getViolations()) {
                    NMAction action = violation.getActions().get(0);
                    action.actionPerformed(null);
                }
            }
        }

    }

    private void checkForRedefinedElements(HashSet<Classifier> createdElements) {
        for (Classifier ns : createdElements) {
            for (NamedElement mem : ns.getInheritedMember()) {
                if (mem instanceof RedefinableElement) {
                    boolean redefined = false;
                    for (NamedElement om : ns.getOwnedMember()) {
                        if (om instanceof RedefinableElement) {
                            if (SRValidationSuite.doesEventuallyRedefine(((RedefinableElement) om), (RedefinableElement) mem)) {
                                redefined = true;
                            }
                        }
                    }
                    if (!redefined) {
                        SetOrCreateRedefinableElementAction action = new SetOrCreateRedefinableElementAction(ns, (RedefinableElement) mem, false);
                        action.run();
                    }
                }
            }
        }
    }

    private void importFromCsv(CSVReader reader, HashSet<Classifier> createdElements) throws IOException {
        GUILog gl = Application.getInstance().getGUILog();
        Project project = Application.getInstance().getProject();

        String[] line = reader.readNext(); // ignore header

        String selectedClassifierName = selectedClassifier.getName();
        List<Element> sortedColumns = new ArrayList<Element>();
        boolean isFirstLine = true;
        boolean lineWasEmpty = true;
        int elementName = -1;
        HashMap<Property, Classifier> previousLinesClasses = new HashMap<>();
        HashMap<Property, Classifier> thisLinesClasses = new HashMap<>();
        while (line != null) {
            if (!emptyLine(line)) {
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
                                    }
                                    else {
                                        thisLinesClasses.put((Property) el, previousLinesClasses.get(el));
                                    }
                                }
                                if (!valueFromCSV.isEmpty()) {
                                    setPropertyValue(valueFromCSV, (RedefinableElement) el, thisLinesClasses);
                                }
                            }
                        }
                        else {
                            if (sortedColumns.get(jj) == null) {
                                if (!line[jj].isEmpty()) {
                                    Classifier topClass = (Classifier) CopyPasting.copyPasteElement(selectedClassifier, container, true);
                                    if (topClass == null) {
                                        continue;
                                    }
                                    topClass.getOwnedMember().clear();
                                    topClass.getGeneralization().clear();
                                    Utils.createGeneralization(selectedClassifier, topClass);
                                    topClass.setName(line[jj]);
                                    thisLinesClasses.put(null, topClass);
                                    Application.getInstance().getGUILog().log("[INFO] Creating new " + selectedClassifier.getName() + " named " + topClass.getName() + " for line " + row + ".");
                                }
                                else {
                                    if (jj == elementName) {
                                        thisLinesClasses.put(null, previousLinesClasses.get(null));
                                    }
                                }
                            }
                        }
                    }

                }
                else {
                    for (int c = 0; c < line.length; c++) {
                        String propertyName = line[c];
                        if (!propertyName.isEmpty()) {
                            if (propertyName.contains(".")) {
                                propertyName = handleSubProperty(sortedColumns, propertyName, selectedClassifier);
                            }
                            else {
                                lineWasEmpty = false;
                                sortedColumns.add(getPropertyFromColumnName(propertyName, selectedClassifier));
                                if (propertyName.equals(selectedClassifierName)) {
                                    elementName = c;
                                }
                            }
                        }
                        else {
                            if (elementName == -1) {// only set it the first time.
                                elementName = c;
                            }
                            sortedColumns.add(null);
                        }
                    }
                }
                if (!lineWasEmpty) {
                    isFirstLine = false;
                }
                createdElements.addAll(thisLinesClasses.values());

            }
            line = reader.readNext();
            row++;

        }
    }

    private boolean emptyLine(String[] line) {
        for (int i = 0; i < line.length; i++) {
            String s = line[i];
            if (!s.trim().isEmpty()) {
                return false;
            }
        }
        return true;
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
                }
                else {
                    sortedColumns.add(getPropertyFromColumnName(propertyName, (Classifier) type));
                }
            }
        }
        return propertyName;
    }

    private Classifier createNewSubElement(String[] line, int index, Classifier generalClassifier) {

        Classifier createdClassifier = (Classifier) CopyPasting.copyPasteElement(generalClassifier, container, true);
        if (createdClassifier == null) {
            return createdClassifier;
        }
        createdClassifier.getOwnedMember().clear();
        createdClassifier.getGeneralization().clear();
        Utils.createGeneralization(generalClassifier, createdClassifier);
        createdClassifier.setName(line[index]);
        Application.getInstance().getGUILog().log("[INFO] Creating new " + generalClassifier.getName() + " named " + createdClassifier.getName() + " for line " + row + ".");
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
                                    if (((Property) el).getType().getID().equals(literalString)) {
                                        vs = UMLFactory.eINSTANCE.createLiteralString();
                                        ((LiteralString) vs).setValue(valueFromCSV);
                                    }
                                    else {
                                        if (((Property) el).getType().getID().equals(literalReal)) {
                                            double val = Double.parseDouble(valueFromCSV);
                                            vs = UMLFactory.eINSTANCE.createLiteralReal();
                                            ((LiteralReal) vs).setValue(val);
                                        }
                                        else {
                                            if (((Property) el).getType().getID().equals(literalBoolean)) {
                                                vs = UMLFactory.eINSTANCE.createLiteralBoolean();
                                                ((LiteralBoolean) vs).setValue(Boolean.parseBoolean(valueFromCSV));
                                            }
                                            else {
                                                if (((Property) el).getType().getID().equals(literalInteger)) {
                                                    int val = Integer.parseInt(valueFromCSV);
                                                    vs = UMLFactory.eINSTANCE.createLiteralInteger();
                                                    ((LiteralInteger) vs).setValue(val);
                                                }
                                            }
                                        }
                                    }
                                } catch (NumberFormatException nf) {
                                    Application.getInstance().getGUILog().log("[WARNING] Value in line " + row + " for property " + el.getName() + " not correct. Reason: " + nf.getMessage());

                                }
                            }
                        }
                        if (vs != null) {
                            prop.setDefaultValue(vs);
                        }
                    }
                    else {
                        prop.setType(linkedElement);
                        if (el instanceof Property) {
                            if (((Property) el).getAssociation() != null) {
                                SetOrCreateRedefinableElementAction.createInheritingAssociation((Property) el, owner, prop);
                            }
                        }
                    }
                }
                else {
                    Application.getInstance().getGUILog().log("[WARNING] Property for " + el.getName() + " not created.");

                }
            }
        }

    }

    private Classifier findMatchingSubclass(Classifier general, Collection<Classifier> thisLinesClasses) {
        for (Classifier cl : thisLinesClasses) {
            if (cl != null) {
                if (cl.getGeneral().contains(general)) {
                    return cl;
                }
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
        if (separator == null) {
            separator = csvSeparator;
        }
        return separator;
    }
}