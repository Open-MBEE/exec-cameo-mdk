/*******************************************************************************
 * Copyright (c) <2016>, California Institute of Technology ("Caltech").  
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

package gov.nasa.jpl.mbee.api;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.validation.actions.*;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

/**
 * Class to mimic the interaction of a user with the validation results window
 * 
 * @author Aaron Black (ablack)
 *
 */

public class MDKValidationWindow {

    protected List<ValidationSuite> vss;
    protected List<List<ValidationRuleViolation>> pooledViolations;

    // these constants correspond to the index in the subarray where the violation type or action class names are stored
    private final static int RULE_VIOLATION_TYPE = 0;
    private final static int COMMIT_ACTION = 1;
    private final static int ACCEPT_ACTION = 2;

    // these constants are used to indicate the number of operations before [EXIST] and after [EXIST ON MMS]
    // these may contain listed validations, but NEVER contain any violations we would want to process with process all
    private static final int PRE_OPERATIONS = 1;
    private static final int POST_OPERATIONS = 6;

    // the text indicating its displayed violation is stored in slot 0 of the subarray
    // note that these are stored in upper case letters, and any passed text is converted to upper case before comparison
    // these are stored in recommended order of operation, to simplify the exportALL and acceptALL functions
    // new validation results should be added between [EXIST] and [EXIST ON MMS], in order or processing.
    // adding new validation results outside of this area will require an adjustment to the PREOPERATION and POSTOPERATION
    // constants, so that non-processable functions do not get processed during the export/accept all methods
    // the text matching the name of the class that handles the commit action is stored in slot 1
    // the text matching the name of the class that handles the accept action is stored in slot 2
    // note that for [EXIST on MMS] the accept action is to create in MD and the commit action is to delete on MMS
    public static final String[][] VALIDATION_OPTIONS = {
            // intiialization messages do not start with a [MESSAGE] in the validationSuite
            // they use INITIALIZATION here for clarity in output
            { "INITIALIZATION", "InitializeProjectModel", "" }, { "[EXIST]", "ExportElement", "DeleteMagicDrawElement" }, { "[INSTANCE]", "ExportInstanceSpec", "ImportInstanceSpec" },
            { "[VIEW CONSTRAINT]", "ExportViewConstraint", "ImportViewConstraint" }, { "[NAME]", "ExportName", "ImportName" }, { "[DOC]", "ExportDoc", "ImportDoc" }, { "[ATTRIBUTE]", "ExportOwnedAttribute", "ImportOwnedAttribute" },
            { "[OWNER]", "ExportOwner", "FixModelOwner" }, { "[PROP]", "ExportProperty", "ImportProperty" }, { "[FEATURE]", "ExportProperty", "" }, { "[SITE CHAR]", "ExportSite", "" }, { "[REL]", "ExportRel", "ImportRel" },
            { "[VALUE]", "ExportValue", "ImportValue" }, { "[CONNECTOR]", "ExportConnector", "ImportConnector" }, { "[CONSTRAINT]", "ExportConstraint", "ImportConstraint" }, { "[ASSOC]", "ExportAssociation", "ImportAssociation" },
            { "[METATYPE]", "ExportMetatypes", "" }, { "[HIERARCHY]", "ExportHierarchy", "ImportHierarchy" }, { "[IMAGE]", "ExportImage", "" }, { "[EXIST ON MMS]", "DeleteAlfrescoElement", "CreateMagicDrawElement" }, { "[CREATED]", "", "" },
            { "[CREATE FAILED]", "", "" }, { "[UPDATED]", "", "" }, { "[LOCAL FAILED]", "", "" }, { "[DELETED]", "", "" }, { "[DELETE FAILED]", "", "" } };

    /**
     * Constructor. This will sort the validation rules results into a format expected by a user who sorted their validation result window based on message (for batch processing)
     * 
     * @param vs
     *            validationSuite returned from a validation action
     */
    public MDKValidationWindow(ValidationSuite vs) {
        this.vss = new ArrayList<ValidationSuite>();
        vss.add(vs);
        initializeWindow();
    }

    /**
     * Constructor. This will sort the validation rules results into a format expected by a user who sorted their validation result window based on message (for batch processing)
     * 
     * @param vss
     *            List of validationSuites returned from a set of validation actions
     */
    public MDKValidationWindow(List<ValidationSuite> vss) {
        this.vss = vss;
        initializeWindow();
    }

    /**
     * Helper method that does the work of sorting the violations stored in the ValidationSuite(s)
     */
    private void initializeWindow() {
        this.pooledViolations = new ArrayList<List<ValidationRuleViolation>>();
        for (int i = 0; i <= VALIDATION_OPTIONS.length; i++) {
            this.pooledViolations.add(new ArrayList<ValidationRuleViolation>());
        }

        // sort the ValidationRuleViolations into the appropriate list in this class
        for (ValidationSuite vs : vss) {
            if (vs != null && vs.hasErrors()) {
                for (ValidationRule vr : vs.getValidationRules()) {
                    if (vr.getViolations() == null || vr.getViolations().size() == 0)
                        continue;
                    for (ValidationRuleViolation vrv : vr.getViolations()) {
                        String s = vrv.getComment();
                        s = s.substring(0, s.indexOf(']') + 1);
                        if (lookupListIndex(s) != -1)
                            pooledViolations.get(lookupListIndex(s)).add(vrv);
                    }
                }
            }
        }
    }

    /**
     * Returns the ValidationSuite(s) stored in object
     * 
     * @return the ValidationSuite(s) stored in object
     * 
     */
    public List<ValidationSuite> getValidations() {
        return vss;
    }

    /**
     * Display method to see sorted violations stored in object of specified type
     * 
     * @param type
     *            Type of violation to list
     * 
     * @throws Exception
     *            Unsupported violation type
     * 
     * @return Number of violations of specified type
     */
    public int listPooledViolations(String type) throws UnsupportedOperationException {
        type = standardize(type);
        int index;
        index = lookupListIndex(type);
        if (index == -1)
            throw new UnsupportedOperationException(type + " is not a supported violation result.");
        System.out.println("There are " + pooledViolations.get(index).size() + " ValidationRuleViolatons in the " + type + " pool");
        for (ValidationRuleViolation vrv : pooledViolations.get(index)) {
            System.out.println("  " + (vrv.getElement() != null ? vrv.getElement().getHumanName() : "null") + " : " + vrv.getComment());
        }
        return pooledViolations.get(index).size();
    }

    /**
     * Display method to see sorted violations stored in object of all types
     * 
     * @return Number of violations of all types
     */
    public int listPooledViolations() {
        int numViolations = 0;
        for (String[] s : VALIDATION_OPTIONS) {
            try {
                numViolations += listPooledViolations(s[RULE_VIOLATION_TYPE]);
            } catch (Exception e) { 
                // do nothing, not a user problem if one of the listed types should be updated 
            }
        }
        return numViolations;
    }

    /**
     * Returns the List of ValidationRuleViolations stored in object for the
     * specified type
     * 
     * @return
     * 
     */
    public List<ValidationRuleViolation> getPooledValidations(String type) {
        type = standardize(type);
        return pooledViolations.get(lookupListIndex(type));
    }

    /**
     * Determines which list contains the validation rule of the specified type.
     * 
     * @param type
     *            String of the type to look for. Expected format: '[type]'
     * @return index of the list of validation results of the specified type
     */
    private int lookupListIndex(String type) {
        type = standardize(type);
        for (int index = 0; index < VALIDATION_OPTIONS.length; index++) {
            if (VALIDATION_OPTIONS[index][RULE_VIOLATION_TYPE].equalsIgnoreCase(type))
                return index;
        }
        return -1;
    }

    /**
     * Helper function that standardizes any validation rule type, to prevent dumb errors
     * 
     * @param s
     * @return
     */
    private String standardize(String s) {
        if (s.equals("") || s.equals("INITIALIZATION"))
            return "INITIALIZATION";
        s = s.toUpperCase();
        if (s.charAt(0) != '[' && s.charAt(s.length() - 1) != ']')
            s = "[" + s + "]";
        return s;
    }

    /************************************************************************
     * 
     * Import / Export Methods
     * 
     ************************************************************************/

    /**
     * Accepts the MMS version into MD for the specified violation type
     * 
     * @param violationType
     *            the type of violation to be accepted
     * @param commit
     *            will commit to MMS if true, will accept from MMS is false
     * @throws Exception
     */
    private Collection<Element> processValidationResults(String violationType, Collection<Element> targets, boolean commit) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        violationType = standardize(violationType);
        MagicDrawHelper.generalMessage((commit ? "Commit" : "Accept") + "ing " + (targets != null ? "selected " : "") + "instances of " + violationType + " violations.");

        int ruleIndex = lookupListIndex(violationType);
        String className = VALIDATION_OPTIONS[ruleIndex][(commit ? COMMIT_ACTION : ACCEPT_ACTION)];

        // if nothing to do, say so and return
        if (pooledViolations.get(ruleIndex).isEmpty()) {
            MagicDrawHelper.generalMessage("[INFO] There are no instances of " + violationType + " to " + (commit ? "commit" : "accept") + ".");
            return targets;
        } else if (className.equals("")) {
            MagicDrawHelper.generalMessage("[INFO] There is no " + (commit ? "commit" : "accept") + " action for instances of " + violationType + " to process.");
            return targets;
        }

        // get the appropriate violation list
        listPooledViolations(violationType);
        List<ValidationRuleViolation> violationList = pooledViolations.get(ruleIndex);

        // there are not accept initialization actions, not any commit actions except for one with a specific message
        // if it's not an actionable initialization violation, there is nothing to do so return
        if (violationType.equals("INITIALIZATION")) {
            if (!commit || !violationList.get(0).getComment().equals("The project doesn't exist on the web.")) {
                return targets;
            }
        }

        // find the index of the relevant action type; throw exception if it's not found
        int actionIndex = 0;
        while (actionIndex < violationList.get(0).getActions().size() && !(violationList.get(0).getActions().get(actionIndex).getClass().getSimpleName().equals(className)))
            actionIndex++;
        if (actionIndex >= violationList.get(0).getActions().size() || !(violationList.get(0).getActions().get(actionIndex).getClass().getSimpleName().equals(className)))
            throw new ClassNotFoundException("Unable to find " + className + " for violation type " + violationType);

        // project initialization is specialized and might not include a nmaction to run
        if (violationType.equals("INITIALIZATION")) {
            for (ValidationRuleViolation vrv : violationList) {
                // disable popups, invoke execute on one of the nmactions, re-enable popups
                Utils.forceDialogReturnFalse();
                ((InitializeProjectModel) vrv.getActions().get(actionIndex)).actionPerformed(new ActionEvent(new JButton(), 5, ""));
            }
        }

        // hierarchies don't export nicely using the standard method
        else if (violationType.equals("[HIERARCHY]")) {

            // type cast the action class appropriately, and then pass it a dummy action event to trigger
            for (ValidationRuleViolation vrv : violationList) {
                if (targets == null || targets.remove(vrv.getElement())) {
                    if (commit) {
                        vrv.getActions().get(actionIndex).actionPerformed(new ActionEvent(new JButton(), 5, ""));
                    } else {
                        vrv.getActions().get(actionIndex).actionPerformed(new ActionEvent(new JButton(), 5, ""));
                    }
                }
            }
        }

        // for everything else, which runs nicely
        else {

            // use reflection to get methods for getAnnotaiton and execute from the selected object
            // using full path for Method and Class to avoid confusion with MD objects
            java.lang.reflect.Method getAnnotation = violationList.get(0).getActions().get(actionIndex).getClass().getMethod("getAnnotation");
            java.lang.reflect.Method execute = violationList.get(0).getActions().get(actionIndex).getClass().getMethod("execute", new java.lang.Class[] { Collection.class });

            // get annotations from the nmaction objects by invoking the getAnnotation method on them
            Collection<Annotation> annos = new LinkedList<Annotation>();
            for (ValidationRuleViolation vrv : violationList) {
                if (targets == null || targets.remove(vrv.getElement())) {
                    Annotation anno = (Annotation) getAnnotation.invoke(vrv.getActions().get(actionIndex));
                    annos.add(anno);
                }
            }

            // disable popups, invoke execute on one of the nmactions, re-enable popups
            Utils.forceDialogReturnFalse();
            execute.invoke(violationList.get(0).getActions().get(actionIndex), annos);
        }

        return targets;
    }

    /**************************************************************************
     * 
     * Helper methods to process all violation types
     * 
     **************************************************************************/

    /**
     * Process all commits of the MD version to MMS for all violation types for elements in the targets collection
     * 
     * @param targets
     *            Collection of elements whose validations should be processed; if null all will be processed
     * @throws Exception
     */
    private Collection<Element> processAllMDChangesToMMS(Collection<Element> targets) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults(VALIDATION_OPTIONS[0 + PRE_OPERATIONS][RULE_VIOLATION_TYPE], targets, true);
        for (int i = 1 + PRE_OPERATIONS; i < VALIDATION_OPTIONS.length - 1 - POST_OPERATIONS; i++)
            processValidationResults(VALIDATION_OPTIONS[i][RULE_VIOLATION_TYPE], targets, true);
        processValidationResults(VALIDATION_OPTIONS[VALIDATION_OPTIONS.length - 1 - POST_OPERATIONS][RULE_VIOLATION_TYPE], targets, true);
        return targets;
    }

    /**
     * Processes all accepts of the MMS version into MD for all violation types for elements in the targets collection
     * 
     * @param targets
     *            Collection of elements whose validations should be processed; if null all will be processed
     * @throws Exception
     */
    private Collection<Element> processAllMMSChangesIntoMD(Collection<Element> targets) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults(VALIDATION_OPTIONS[VALIDATION_OPTIONS.length - 1 - POST_OPERATIONS][RULE_VIOLATION_TYPE], targets, false);
        for (int i = 1 + PRE_OPERATIONS; i < VALIDATION_OPTIONS.length - 1 - POST_OPERATIONS; i++)
            processValidationResults(VALIDATION_OPTIONS[i][RULE_VIOLATION_TYPE], targets, false);
        processValidationResults(VALIDATION_OPTIONS[PRE_OPERATIONS][RULE_VIOLATION_TYPE], targets, false);
        return targets;
    }

    /**************************************************************************
     * 
     * Public methods for validation window processing
     * 
     **************************************************************************/

    /**
     * Accepts the MMS version into MD for all violation types
     *
     * @throws Exception
     */
    public void acceptAllMMSChangesIntoMD() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        processAllMMSChangesIntoMD(null);
    }

    /**
     * Accepts the MMS version into MD for the specified violation type
     * 
     * @param violationType
     *            the type of violation to be accepted
     * @throws Exception
     */
    public void acceptMMSChangesIntoMD(String violationType) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults(violationType, null, false);
    }

    /**
     * Commits the MD version to MMS for the all violation types, if the associated element is in the targets collection
     * 
     * @param targets
     *            the collection of elements whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     * @throws Exception
     */
    public Collection<Element> acceptSpecificMMSChangesIntoMD(Collection<Element> targets) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return processAllMMSChangesIntoMD(targets);
    }

    /**
     * Accepts the MMS version into MD for the specified violation type, if the associated element is in the targets collection
     * 
     * @param violationType
     *            the type of violation to be accepted
     * @param targets
     *            the collection of elements whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     * @throws Exception
     */
    public Collection<Element> acceptSpecificTypeMDChangesToMMS(String violationType, Collection<Element> targets) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return processValidationResults(violationType, targets, false);
    }

    /**
     * Commits the MD version to MMS for all violation types
     * 
     * @throws Exception
     */
    public void commitAllMDChangesToMMS() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        processAllMDChangesToMMS(null);
    }

    /**
     * Commits the MD version to MMS for the specified violation type
     * 
     * @param violationType
     *            the type of violation to be accepted
     * @throws Exception
     */
    public void commitMDChangesToMMS(String violationType) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults(violationType, null, true);
    }

    /**
     * Commits the MD version to MMS for the all violation types, if the associated element is in the targets collection
     * 
     * @param targets
     *            the collection of elements whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     * @throws Exception
     */
    public Collection<Element> commitSpecificMDChangesToMMS(Collection<Element> targets) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return processAllMDChangesToMMS(targets);
    }

    /**
     * Commits the MD version to MMS for the specified violation type, if the associated element is in the targets collection
     * 
     * @param violationType
     *            the type of violation to be accepted
     * @param targets
     *            the collection of elements whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     * @throws Exception
     */
    public Collection<Element> commitSpecificTypeMDChangesToMMS(String violationType, Collection<Element> targets) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return processValidationResults(violationType, targets, true);
    }

    /**
     * Creates all MMS elements that were not found in MD (accepts their existence from MMS)
     * 
     * @throws Exception
     */
    public void createAllMMSElementsNotFoundInMD() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults("[EXIST on MMS]", null, false);
    }

    /**
     * Deletes all MD elements that were not found in MMS (accepts their non-existence from MMS)
     * 
     * @throws Exception
     */
    public void deleteAllMDElementsNotFoundOnMMS() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults("[EXIST]", null, false);
    }

    /**
     * Deletes all MMS elements that were not found in MD (commits their non-existence to MMS)
     * 
     * @throws Exception
     */
    public void deleteAllMMSElementsNotFoundInMD() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults("[EXIST on MMS]", null, true);
    }

    /**
     * Exports all MD elements that were not found in MMS (commits their existence to MMS)
     * 
     * @throws Exception
     */
    public void exportAllMDElementsToMMS() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        processValidationResults("[EXIST]", null, true);
    }

    /********************************************************************************
     * 
     * Investigation methods
     * 
     ********************************************************************************/

    /**
     * Searches through the indicated list of pooled violations for elements in the passed collection
     * 
     * @param targets
     *            Elements to confirm the presence of in the validaitons
     * @param validationType
     *            Type of validation to search through
     * @return Returns The subset of the elements that were not found in the specified validation type pool
     */
    public Collection<Element> confirmElementValidationTypeResult(Collection<Element> targets, String validationType) {
        Collection<Element> notFound = new ArrayList<Element>();
        notFound.addAll(targets);
        validationType = standardize(validationType);
        int index = lookupListIndex(validationType);
        for (ValidationRuleViolation vrv : pooledViolations.get(index)) {
            if (notFound.remove(vrv.getElement())) {
                if (notFound.isEmpty())
                    return notFound;
            }
        }
        return notFound;
    }

    /**
     * Searches through the all pooled violations for elements in the passed collection
     * 
     * @param targets
     *            Elements to confirm the presence of in the validaitons
     * @return Returns The subset of the elements that were not found in the pools
     */
    public Collection<Element> confirmElementValidationResult(Collection<Element> targets) {
        Collection<Element> notFound = new ArrayList<Element>();
        notFound.addAll(targets);
        for (String[] sar : VALIDATION_OPTIONS) {
            notFound = confirmElementValidationTypeResult(notFound, sar[RULE_VIOLATION_TYPE]);
            if (notFound.isEmpty()) {
                return notFound;
            }
        }
        return notFound;
    }

    /**
     * Searches through the indicated list of pooled violations for the passed element
     * 
     * @param target
     *            Element to confirm the presence of in the validaitons
     * @return Returns true if the element was found in any validation pool, false otherwise
     */
    public boolean confirmElementValidationResult(Element target) {
        Collection<Element> notFound = new ArrayList<Element>();
        notFound.add(target);
        return confirmElementValidationResult(notFound).isEmpty();
    }

    /**
     * Searches through the indicated list of pooled violations for the passed element
     * 
     * @param target
     *            Element to confirm the presence of in the validaitons
     * @param validationType
     *            Type of validation to search through
     * @return Returns true if the element was found in any validation pool, false otherwise
     */
    public boolean confirmElementValidationTypeResult(Element target, String validationType) {
        Collection<Element> notFound = new ArrayList<Element>();
        notFound.add(target);
        return confirmElementValidationTypeResult(notFound, validationType).isEmpty();
    }

    /********************************************************************************
     * 
     * Deprecated methods
     * 
     ********************************************************************************/

    // @Deprecated
    /*
     * Non-reflection method for getting annotations and executing actions. Saved only in case it's instructive for something later on. Should not be made public or called.
     * 
     * Exports all [ATTRIBUTE] validation results to MD
     * 
     * @throws Exception
     */
    // private void exportAllValidatedAttribute() {
    // // get the approrpiate violation list, throw exception if empty
    // String type = "[ATTRIBUTE]";
    // List<ValidationRuleViolation> importList = pooledViolations.get(lookupListIndex(type));
    // if (importList.isEmpty())
    // throw new Exception("No " + type + " validations to export.");
    // listPooledViolations(type);
    //
    // // find the index of the relevant action type, else throw exception
    // // replace this class with the appropriate action class if necessary
    // int index = 0;
    // while (index < importList.get(0).getActions().size()
    // && !(importList.get(0).getActions().get(index) instanceof ExportOwnedAttribute))
    // index++;
    // if (!(importList.get(0).getActions().get(index) instanceof ExportOwnedAttribute))
    // throw new Exception("Unable to find indicated NMAction class type.");
    //
    // // get action object, for cleaner code
    // ExportOwnedAttribute exportAttribute = (ExportOwnedAttribute) importList.get(0).getActions().get(index);
    //
    // // get annotations from the nmaction
    // Collection<Annotation> annos = new LinkedList<>();
    // for (ValidationRuleViolation vrv : importList) {
    // annos.add(((ExportOwnedAttribute) vrv.getActions().get(index)).getAnnotation());
    // }
    //
    // // disable popups, export elements, re-enable popups
    // Utils.forceDialogReturnFalse(true);
    // exportAttribute.execute(annos);
    // Utils.forceDialogReturnFalse(false);
    // }

}
