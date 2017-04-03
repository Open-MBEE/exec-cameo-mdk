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

package gov.nasa.jpl.mbee.mdk.api;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to mimic the interaction of a user with the validation results window
 *
 * @author Aaron Black (ablack)
 */

public class MDKValidationWindow {

    protected List<ValidationSuite> vss;
    protected List<List<ValidationRuleViolation>> pooledViolations;

    // these constants correspond to the index in the subarray where the violation type or action class names are stored
    private final static int VIOLATION_RULE_NAME = 0;
    private final static int COMMIT_ACTION = 1;
    private final static int ACCEPT_ACTION = 2;

    private final static int INITIALIZATION_RULE = 0;
    private final static int EQUIVALENCE_RULE = 1;

    // the text indicating the validation rule name is stored in slot 0 of the subarray
    // the text matching the name of the class that handles the commit action is stored in slot 1
    // the text matching the name of the class that handles the accept action is stored in slot 2
    public static final String[][] VALIDATION_RULE_OPTIONS = {
            {"Project Existence", "CommitProjectAction", ""},
            {"Element Equivalence", "CommitClientElementAction", "AcceptClientElementAction"}
    };

    /**
     * Constructor. This will sort the validation rules results into a format expected by a user who sorted their
     * validation result window based on message (for batch processing)
     *
     * @param vs validationSuite returned from a validation action
     */
    public MDKValidationWindow(ValidationSuite vs) {
        this.vss = new ArrayList<>();
        vss.add(vs);
        initializeWindow();
    }

    /**
     * Constructor. This will sort the validation rules results into a format expected by a user who sorted their
     * validation result window based on message (for batch processing)
     *
     * @param vss List of validationSuites returned from a set of validation actions
     */
    public MDKValidationWindow(List<ValidationSuite> vss) {
        this.vss = vss;
        initializeWindow();
    }

    /**
     * Helper method that does the work of sorting the violations stored in the ValidationSuite(s)
     */
    private void initializeWindow() {
        pooledViolations = new ArrayList<>();
        for (int i = 0; i <= VALIDATION_RULE_OPTIONS.length; i++) {
            pooledViolations.add(new ArrayList<>());
        }

        // sort the ValidationRuleViolations into the appropriate list in this class
        // this used to be a lot more complicated :-(
        for (ValidationSuite vs : vss) {
            if (vs != null && vs.hasErrors()) {
                for (ValidationRule vr : vs.getValidationRules()) {
                    if (vr.getViolations() == null || vr.getViolations().isEmpty()) {
                        continue;
                    }
                    try {
                        pooledViolations.get(lookupListIndex(vr.getName())).addAll(vr.getViolations());
                    } catch (UnsupportedOperationException e) {
                        Application.getInstance().getGUILog().log("[ERROR] Unable to store violations for rule "
                                + vr.getName());
                    }
                }
            }
        }
    }

    /**
     * Returns the ValidationSuite(s) stored in the MDKValidationWindow object
     *
     * @return the ValidationSuite(s) stored in object
     */
    public List<ValidationSuite> getValidations() {
        return vss;
    }

    /**
     * Display method to see sorted violations stored in object of specified type
     *
     * @param type Type of violation to list
     * @return Number of violations of specified type
     * @throws Exception Unsupported violation type
     */
    public int listPooledViolations(String type) {
        try {
            int index = lookupListIndex(type);
            System.out.println("There are " + pooledViolations.get(index).size()
                    + " ValidationRuleViolations in the " + type + " pool");
            for (ValidationRuleViolation vrv : pooledViolations.get(index)) {
                System.out.println("  " + (vrv.getElement() != null ? vrv.getElement().getHumanName() : "null")
                        + " : " + vrv.getComment());
            }
            return pooledViolations.get(index).size();
        } catch (UnsupportedOperationException e) {
            System.out.println(type + " is not a supported violation result.");
        }
        return 0;
    }

    /**
     * Display method to see sorted violations stored in object of all types
     *
     * @return Number of violations of all types
     */
    public int listPooledViolations() {
        int numViolations = 0;
        for (String[] s : VALIDATION_RULE_OPTIONS) {
            try {
                numViolations += listPooledViolations(s[VIOLATION_RULE_NAME]);
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
     */
    public List<ValidationRuleViolation> getPooledValidations(String type) {
        return pooledViolations.get(lookupListIndex(type));
    }

    /**
     * Determines which list contains the validation rule of the specified type.
     *
     * @param type String of the type to look for. Expected format: '[type]'
     * @return index of the list of validation results of the specified type
     */
    private int lookupListIndex(String type) throws IllegalArgumentException {
        for (int index = 0; index < VALIDATION_RULE_OPTIONS.length; index++) {
            if (VALIDATION_RULE_OPTIONS[index][VIOLATION_RULE_NAME].equalsIgnoreCase(type)) {
                return index;
            }
        }
        throw new IllegalArgumentException(type + " is not a supported violation rule.");
    }

    private String getVRVElementID(ValidationRuleViolation vrv) {
        String id = vrv.getComment();
        Element vrve = vrv.getElement();
        if (id.indexOf('`') > 0) {
            id = id.substring(id.indexOf('`') + 1, id.lastIndexOf('`'));
            return id;
        }
        else if (vrve != null) {
            return Converters.getElementToIdConverter().apply(vrve);
        }
        return "";
    }

    /************************************************************************
     *
     * Import / Export Methods
     *
     ************************************************************************/

    /**
     * Processes validation rule violations that have been stored in the MDKValidationWindow object
     *
     * @param violationRuleName the type of violation to be accepted
     * @param commit            will commit to MMS if true, will accept from MMS is false
     * @param targets           limits processing of violations to only those elements that are
     *                          contained in the collection. if null, does not limit processing.
     *                          ** COLLECTION IS MODIFIED DURING FUNCTION EXECUTION **
     *                          exception will be thrown if specified with targetIDs.
     * @param targetIDs         limits processing of violations to only those elements whose IDs are
     *                          contained in the collection. if null, does not limit processing.
     *                          ** COLLECTION IS MODIFIED DURING FUNCTION EXECUTION **
     *                          exception will be thrown if specified with targets.
     * @throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException
     */
    private void processValidationResults(String violationRuleName, Collection<Element> targets,
                                          Collection<String> targetIDs, boolean commit)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalStateException, IllegalAccessException {
        if (targets != null && targetIDs != null) {
            throw new IllegalStateException("Both element target lists specified.");
        }

        MagicDrawHelper.generalMessage((commit ? "Commit" : "Accept") + "ing " + (targets != null ? "selected " : "") + "instances of " + violationRuleName + " violations.");

        int ruleIndex;
        try {
            ruleIndex = lookupListIndex(violationRuleName);
        } catch (IllegalArgumentException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + violationRuleName + " is not a supported violation rule.");
            return;
        }
        String className = VALIDATION_RULE_OPTIONS[ruleIndex][(commit ? COMMIT_ACTION : ACCEPT_ACTION)];

        // if nothing to do, say so and return
        if (pooledViolations.get(ruleIndex).isEmpty()) {
            MagicDrawHelper.generalMessage("[INFO] There are no instances of " + violationRuleName + " to " + (commit ? "commit" : "accept") + ".");
            return;
        }
        else if (className.isEmpty()) {
            MagicDrawHelper.generalMessage("[INFO] There is no " + (commit ? "commit" : "accept") + " action for instances of " + violationRuleName + " to process.");
            return;
        }

        // get the appropriate violation list
        List<ValidationRuleViolation> violationList = pooledViolations.get(ruleIndex);

        // there is only one initialization violation with a commit action, so check for it.
        // if it's not an actionable initialization violation, there is nothing to do so return.
        // TODO fix project initialization checks
        if (violationRuleName.equals(VALIDATION_RULE_OPTIONS[INITIALIZATION_RULE][VIOLATION_RULE_NAME])) {
//            if (!commit || !violationList.get(0).getComment().equals(ManualSyncRunner.INITIALIZE_PROJECT_COMMENT)) {
            return;
//            }
        }

        // find the index of the relevant action type within the vrv; throw exception if it's not found
        int actionIndex = 0;
        while (actionIndex < violationList.get(0).getActions().size()
                && !(violationList.get(0).getActions().get(actionIndex).getClass().getSimpleName().equals(className))) {
            actionIndex++;
        }
        if (actionIndex >= violationList.get(0).getActions().size()
                || !(violationList.get(0).getActions().get(actionIndex).getClass().getSimpleName().equals(className))) {
            throw new ClassNotFoundException("Unable to find " + className + " for violation type " + violationRuleName);
        }

        // use reflection to get methods for getAnnotaiton and execute from the selected object
        java.lang.reflect.Method getAnnotation =
                violationList.get(0).getActions().get(actionIndex).getClass().getMethod("getAnnotation");
        java.lang.reflect.Method execute =
                violationList.get(0).getActions().get(actionIndex).getClass().getMethod("execute", Collection.class);

        // get annotations from the nmaction objects by invoking the getAnnotation method on them
        Collection<Annotation> annos = new LinkedList<>();
        for (ValidationRuleViolation vrv : violationList) {
            if ((targets == null || targets.remove(vrv.getElement()))
                    && (targetIDs == null || targetIDs.remove(getVRVElementID(vrv)))) {
                Annotation anno = (Annotation) getAnnotation.invoke(vrv.getActions().get(actionIndex));
                annos.add(anno);
                System.out.println("  " + (commit ? "Committed " : "Accepted ")
                        + (vrv.getElement() != null ? vrv.getElement().getHumanName() : "null") + " : " + vrv.getComment());
            }
        }

        // disable popups, invoke execute on one of the nmactions, re-enable popups
        Utils.forceDialogReturnFalse();
        execute.invoke(violationList.get(0).getActions().get(actionIndex), annos);
        Utils.resetForcedReturns();
    }

    /**************************************************************************
     *
     * Public methods for validation window processing
     *
     **************************************************************************/

    /**
     * Accepts the MMS version into MD for all violation types
     */
    public void acceptAllMMSChangesIntoMD() {
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[EQUIVALENCE_RULE][VIOLATION_RULE_NAME], null, null, false);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        }
    }

    /**
     * Accepts the MMS version into MD for the all violation types, if the associated element is in the collection targets
     *
     * @param targets the collection of elements whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     */
    public Collection<Element> acceptSpecificMMSChangesIntoMD(Collection<Element> targets) {
        Collection<Element> notFound = new ArrayList<>();
        notFound.addAll(targets);
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[EQUIVALENCE_RULE][VIOLATION_RULE_NAME], notFound, null, false);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        }
        return notFound;
    }

    /**
     * Commits the MD version to MMS for the specified violation type, if the associated element id is in the targetIDs collection
     *
     * @param violationType the type of violation to be accepted
     * @param targetIDs     the collection of element IDs whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     */
    public Collection<String> acceptSpecificMDChangesToMMSByID(String violationType, Collection<String> targetIDs) {
        Collection<String> notFound = new ArrayList<>();
        notFound.addAll(targetIDs);
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[EQUIVALENCE_RULE][VIOLATION_RULE_NAME], null, notFound, false);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        }
        return notFound;
    }

    /**
     * Commits the MD version to MMS for all violation types
     */
    public void commitAllMDChangesToMMS() {
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[EQUIVALENCE_RULE][VIOLATION_RULE_NAME], null, null, true);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
    }

    /**
     * Commits the MD version to MMS for the all violation types, if the associated element is in the targets collection
     *
     * @param targets the collection of elements whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     * @throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
     */
    public Collection<Element> commitSpecificMDChangesToMMS(Collection<Element> targets) {
        Collection<Element> notFound = new ArrayList<>();
        notFound.addAll(targets);
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[EQUIVALENCE_RULE][VIOLATION_RULE_NAME], notFound, null, true);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        }
        return notFound;
    }

    /**
     * Commits the MD version to MMS for the specified violation type, if the associated element id is in the targetIDs collection
     *
     * @param violationType the type of violation to be accepted
     * @param targetIDs     the collection of element IDs whose validations are to be processed
     * @return returns elements in the target collection that could not be processed / that did not have violations
     */
    public Collection<String> commitSpecificMDChangesToMMSByID(String violationType, Collection<String> targetIDs) {
        Collection<String> notFound = new ArrayList<>();
        notFound.addAll(targetIDs);
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[EQUIVALENCE_RULE][VIOLATION_RULE_NAME], null, notFound, true);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        }
        return notFound;
    }

    public void initializeProject() {
        try {
            processValidationResults(VALIDATION_RULE_OPTIONS[INITIALIZATION_RULE][VIOLATION_RULE_NAME], null, null, true);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | IllegalStateException e) {
            MagicDrawHelper.generalMessage("[ERROR]" + e.getMessage());
        }
    }

    /********************************************************************************
     *
     * Investigation methods
     *
     ********************************************************************************/

    /**
     * Searches through the indicated list of pooled violations for elements in the passed collection
     *
     * @param validationType Type of validation to search through
     * @param targets        Elements to confirm the presence of in the validations
     * @return Returns The subset of the elements that were not found in the specified validation type pool
     */
    public Collection<Element> confirmElementViolation(String validationType, Collection<Element> targets) {
        Collection<Element> remainingElements = new ArrayList<>();
        remainingElements.addAll(targets);
        int index = lookupListIndex(validationType);
        for (ValidationRuleViolation vrv : pooledViolations.get(index)) {
            if (remainingElements.remove(vrv.getElement())) {
                if (remainingElements.isEmpty()) {
                    return remainingElements;
                }
            }
        }
        return remainingElements;
    }

    /**
     * Searches through the indicated list of pooled violations for the passed element IDs
     *
     * @param validationType Type of validation to search through
     * @param targetIDs      Collection of strings to confirm the presence of in the validations
     * @return Returns the subset of the element IDss that were not found in the indicated pool
     */
    public Collection<String> confirmElementViolationByID(String validationType, Collection<String> targetIDs) {
        Collection<String> remainingElements = new ArrayList<>();
        remainingElements.addAll(targetIDs);
        int index = lookupListIndex(validationType);
        for (ValidationRuleViolation vrv : pooledViolations.get(index)) {
            if (remainingElements.remove(getVRVElementID(vrv))) {
                if (remainingElements.isEmpty()) {
                    return remainingElements;
                }
            }
        }
        return remainingElements;
    }

    /********************************************************************************
     *
     * Deprecated methods
     *
     ********************************************************************************/

    // @Deprecated
    /*
     * Non-reflection method for getting annotations and executing actions. Saved only in case it's instructive for
     * something later on. Should not be made public or called.
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
