package gov.nasa.jpl.mbee.ems.sync.common;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.UML2MetamodelConstants;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.impl.PropertyNames;
import com.nomagic.uml2.transaction.TransactionCommitListener;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class responds to commits done in the document.
 *
 * @author jsalcedo
 * @author igomes
 */
public class CommonSyncTransactionCommitListener implements TransactionCommitListener {
    private static final List<String> IGNORED_PROPERTY_CHANGE_EVENT_NAMES = Arrays.asList(
            PropertyNames.PACKAGED_ELEMENT,
            UML2MetamodelConstants.ID,
            PropertyNames.NESTED_CLASSIFIER
    );
    /**
     * Allow listener to be disabled during imports.
     */
    private boolean disabled = false;
    private Changelog<String, Element> inMemoryLocalChangelog = new Changelog<>();

    {
        inMemoryLocalChangelog.setShouldLogChanges(true);
    }

    public synchronized boolean isDisabled() {
        return disabled;
    }

    public synchronized void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Changelog<String, Element> getInMemoryLocalChangelog() {
        return inMemoryLocalChangelog;
    }

    @Override
    public Runnable transactionCommited(Collection<PropertyChangeEvent> events) {
        if (isDisabled() || !MDKOptionsGroup.getMDKOptions().isCommitListener()) {
            return null;
        }
        return new TransactionCommitHandler(events, Application.getInstance().getProject().getModel());
    }

    /**
     * Adapter to call handleChangeEvent() from the TransactionCommitListener
     * interface.
     */
    private class TransactionCommitHandler implements Runnable {
        private final Collection<PropertyChangeEvent> events;
        private final Model model;

        TransactionCommitHandler(final Collection<PropertyChangeEvent> events, Model model) {
            this.events = events;
            this.model = model;
        }

        @Override
        public void run() {
            try {
                for (PropertyChangeEvent event : events) {
                    Object source = event.getSource();
                    if (!(source instanceof Element) || ProjectUtilities.isElementInAttachedProject((Element) source)) {
                        continue;
                    }
                    Element sourceElement = (Element) source;
                    System.out.println(event.getPropertyName() + ": " + sourceElement.getID() + " - " + (sourceElement instanceof NamedElement ? ((NamedElement) sourceElement).getName() : "<>"));
                    String changedPropertyName = event.getPropertyName();
                    if (changedPropertyName == null || changedPropertyName.startsWith("_") || IGNORED_PROPERTY_CHANGE_EVENT_NAMES.contains(changedPropertyName)) {
                        continue;
                    }
                    if ((event.getNewValue() == null && event.getOldValue() == null) || (event.getNewValue() != null && event.getNewValue().equals(event.getOldValue()))) {
                        continue;
                    }
                    System.out.println("1");

                    if (!changedPropertyName.equals(UML2MetamodelConstants.INSTANCE_DELETED)) {
                        Element root = sourceElement;
                        while (root.getOwner() != null) {
                            root = root.getOwner();
                        }
                        if (!root.equals(model)) {
                            continue;
                        }
                    }
                    System.out.println("2");

                    // START PRE-PROCESSING
                    Element e;
                    if (sourceElement instanceof Comment && ExportUtility.isElementDocumentation((Comment) sourceElement) && changedPropertyName.equals(PropertyNames.BODY)) {
                        sourceElement = sourceElement.getOwner();
                    }
                    else if ((sourceElement instanceof ValueSpecification) && (changedPropertyName.equals(PropertyNames.VALUE)) ||
                            (sourceElement instanceof OpaqueExpression) && (changedPropertyName.equals(PropertyNames.BODY)) ||
                            (sourceElement instanceof Expression) && (changedPropertyName.equals(PropertyNames.OPERAND))) {
                        // Need to find the actual element that needs to be sent (most likely a Property or Slot that's the closest owner of this element)
                        sourceElement = sourceElement.getOwner();
                        // There may be multiple ValueSpecification changes so go up the chain of owners until we find the actual owner that should be submitted
                        while (sourceElement instanceof ValueSpecification) {
                            sourceElement = sourceElement.getOwner();
                        }
                    }

                    if (sourceElement instanceof Constraint && (e = ExportUtility.getViewFromConstraint((Constraint) sourceElement)) != null) {
                        sourceElement = e;
                    }
                    // END PRE-PROCESSING

                    if (!ExportUtility.shouldAdd(sourceElement)) {
                        continue;
                    }
                    System.out.println("3");
                    String elementID = ExportUtility.getElementID(sourceElement);
                    if (elementID == null) {
                        continue;
                    }
                    System.out.println("4");

                    Changelog.ChangeType changeType = Changelog.ChangeType.UPDATED;
                    switch (changedPropertyName) {
                        case UML2MetamodelConstants.INSTANCE_DELETED:
                            changeType = Changelog.ChangeType.DELETED;
                            break;
                        case UML2MetamodelConstants.INSTANCE_CREATED:
                            changeType = Changelog.ChangeType.CREATED;
                            break;
                    }
                    inMemoryLocalChangelog.addChange(elementID, sourceElement, changeType);
                }
            } catch (Exception e) {
                Application.getInstance().getGUILog().log("CommonSyncTransactionCommitListener had an unexpected error.");
                Utils.printException(e);
                throw e;
            }
        }

        /*private boolean isDiagramCreated(Element e) {
            Element cur = e;
            while (cur.getOwner() != null) {
                cur = cur.getOwner();
            }
            if (cur != Application.getInstance().getProject().getModel()) {
                return true;
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        private void handleChangedProperty(Element sourceElement, String propertyName, Object newValue, Object oldValue) {
            JSONObject elementOb = null;
            String elementID = null;
            ArrayList<String> moveKeywords = new ArrayList<String>();
            // Create a list of the 'owning' property names.
            //
            moveKeywords.add(PropertyNames.OWNING_ASSOCIATION);
            moveKeywords.add(PropertyNames.OWNING_CONSTRAINT);
            moveKeywords.add(PropertyNames.OWNING_ELEMENT);
            moveKeywords.add(PropertyNames.OWNING_EXPRESSION);
            moveKeywords.add(PropertyNames.OWNING_INSTANCE);
            moveKeywords.add(PropertyNames.OWNING_INSTANCE_SPEC);
            moveKeywords.add(PropertyNames.OWNING_LOWER);
            moveKeywords.add(PropertyNames.OWNING_PACKAGE);
            moveKeywords.add(PropertyNames.OWNING_PARAMETER);
            moveKeywords.add(PropertyNames.OWNING_PROPERTY);
            moveKeywords.add(PropertyNames.OWNING_SIGNAL);
            moveKeywords.add(PropertyNames.OWNING_SLOT);
            moveKeywords.add(PropertyNames.OWNING_STATE);
            moveKeywords.add(PropertyNames.OWNING_TEMPLATE_PARAMETER);
            moveKeywords.add(PropertyNames.OWNING_TRANSITION);
            moveKeywords.add(PropertyNames.OWNING_UPPER);
            moveKeywords.add(PropertyNames._U_M_L_CLASS);
            moveKeywords.add(PropertyNames.OWNER);

            //
            // Examine property name to determine how to
            // process the change.
            //
            if (propertyName.equals(PropertyNames.NAME)) {
                if (!ExportUtility.shouldAdd(sourceElement)) {
                    return;
                }
                elementOb = getElementObject(sourceElement);
                ExportUtility.fillName(sourceElement, elementOb);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if (sourceElement instanceof Comment &&
                    ExportUtility.isElementDocumentation((Comment) sourceElement) &&
                    propertyName.equals(PropertyNames.BODY)) { // doc changed
                Element actual = sourceElement.getOwner();
                if (!ExportUtility.shouldAdd(actual)) {
                    return;
                }
                elementOb = getElementObject(actual);
                ExportUtility.fillDoc(actual, elementOb);
                ExportUtility.fillOwner(actual, elementOb);
            }
            else if ((sourceElement instanceof ValueSpecification) && (propertyName.equals(PropertyNames.VALUE)) ||
                    (sourceElement instanceof OpaqueExpression) && (propertyName.equals(PropertyNames.BODY)) ||
                    (sourceElement instanceof Expression) && (propertyName.equals(PropertyNames.OPERAND))) {
                //
                // Need to find the actual element that needs to be sent (most
                // likely a Property or Slot that's the closest owner of this
                // value spec).
                Element actual = sourceElement.getOwner();

                // There may multiple ValueSpecification changes
                // so go up the chain of owners until we find
                // the actual owner (Element) that has the changes.
                //
                while (actual instanceof ValueSpecification) {
                    actual = actual.getOwner();
                }
                if (!ExportUtility.shouldAdd(actual)) {
                    if (actual instanceof Constraint && ExportUtility.isViewConstraint((Constraint) actual) ||
                            actual instanceof Slot && ((Slot) actual).getDefiningFeature() != null && ((Slot) actual).getDefiningFeature().getID().equals("_18_0_2_407019f_1433361787467_278914_14410")) {
                        Element viewOb = actual.getOwner();
                        if (actual instanceof Slot) {
                            viewOb = viewOb.getOwner();
                        }
                        elementOb = getElementObject(viewOb);
                        JSONObject specialization = ExportUtility.fillViewContent(viewOb, null);
                        elementOb.put("specialization", specialization);
                        ExportUtility.fillOwner(viewOb, elementOb);
                    }
                    return;
                }
                elementOb = getElementObject(actual);
                if (actual instanceof Slot || actual instanceof Property) {
                    JSONObject specialization = ExportUtility.fillPropertySpecialization(actual, null, true, true);
                    elementOb.put("specialization", specialization);
                    if (actual instanceof Slot && actual.getOwner() != null) { //catch instanceSpec if it wasn't caught before
                        elementOb = getElementObject(actual.getOwner(), false);
                        ExportUtility.fillElement(actual.getOwner(), elementOb);
                    }
                }
                else if (actual instanceof Constraint) {
                    JSONObject specialization = ExportUtility.fillConstraintSpecialization((Constraint) actual, null);
                    elementOb.put("specialization", specialization);
                }
                else if (actual instanceof InstanceSpecification) {
                    JSONObject specialization = ExportUtility.fillInstanceSpecificationSpecialization((InstanceSpecification) actual, null);
                    elementOb.put("specialization", specialization);
                }
                ExportUtility.fillOwner(actual, elementOb);
            }
            // Check if this is a Property or Slot. Need these next two if
            // statement
            // to handle the case where a value is being deleted.
            //
            else if ((sourceElement instanceof Property) && (propertyName.equals(PropertyNames.DEFAULT_VALUE) || propertyName.equals(PropertyNames.TYPE) ||
                    propertyName.equals(PropertyNames.LOWER_VALUE) || propertyName.equals(PropertyNames.UPPER_VALUE) || propertyName.equals("multiplicity") ||
                    propertyName.equals(PropertyNames.REDEFINED_PROPERTY))) {
                JSONObject specialization = ExportUtility.fillPropertySpecialization(sourceElement, null, true, true);
                elementOb = getElementObject(sourceElement);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if ((sourceElement instanceof Slot) && propertyName.equals(PropertyNames.VALUE) && ExportUtility.shouldAdd(sourceElement)) {
                elementOb = getElementObject(sourceElement);
                JSONObject specialization = ExportUtility.fillPropertySpecialization(sourceElement, null, true, true);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
                if (sourceElement instanceof Slot && sourceElement.getOwner() != null) { //catch instanceSpec if it wasn't caught before
                    elementOb = getElementObject(sourceElement.getOwner(), false);
                    ExportUtility.fillElement(sourceElement.getOwner(), elementOb);
                }
            }
            else if ((sourceElement instanceof Class) && propertyName.equals(PropertyNames.OWNED_ATTRIBUTE)) {
                elementOb = getElementObject(sourceElement);
                ExportUtility.fillOwnedAttribute(sourceElement, elementOb);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if ((sourceElement instanceof Constraint) && propertyName.equals(PropertyNames.SPECIFICATION)) {
                if (ExportUtility.isViewConstraint((Constraint) sourceElement)) {
                    Element viewOb = sourceElement.getOwner();
                    elementOb = getElementObject(viewOb);
                    JSONObject specialization = ExportUtility.fillViewContent(viewOb, null);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(viewOb, elementOb);
                }
                else {
                    elementOb = getElementObject(sourceElement);
                    JSONObject specialization = ExportUtility.fillConstraintSpecialization((Constraint) sourceElement, null);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(sourceElement, elementOb);
                }
            }
            else if (propertyName.equals(UML2MetamodelConstants.INSTANCE_CREATED) && ExportUtility.shouldAdd(sourceElement)) {
                if (isDiagramCreated(sourceElement)) {
                    String id = ExportUtility.getElementID(sourceElement);
                    toRemove.add(id);
                    diagramElements.add(id);
                    diagramElements.add(sourceElement.getID());
                }
                else {
                    elementOb = getElementObject(sourceElement, true);
                    ExportUtility.fillElement(sourceElement, elementOb);
                    if (sourceElement instanceof Slot && sourceElement.getOwner() != null) { //catch instanceSpec if it wasn't caught before
                        elementOb = getElementObject(sourceElement.getOwner(), false);
                        ExportUtility.fillElement(sourceElement.getOwner(), elementOb);
                    }
                }
            }
            else if (propertyName.equals(UML2MetamodelConstants.INSTANCE_DELETED)) {
                elementID = ExportUtility.getElementID(sourceElement);
                if (elementID == null) {
                    return; //this happens when slot is deleted ARGHHHH
                }
                elements.remove(elementID);

                if (diagramElements.contains(elementID) || diagramElements.contains(sourceElement.getID()) || (!ExportUtility.shouldAdd(sourceElement) && !(sourceElement instanceof InstanceSpecification))) {
                    return; //prevent unneeded deletes (instance specs don't have enough info at this point to determine if they're unneeded delete or not, so just delete them)
                }

                deletes.add(elementID);
                locallyChangedElementsInMemory.remove(elementID);
                locallyAddedElementsInMemory.remove(elementID);
                if (!auto) {
                    locallyDeletedElementsInMemory.add(elementID);
                }
            }
            else if (propertyName.equals(UML2MetamodelConstants.BEFORE_DELETE) && (sourceElement instanceof Slot)) {
                //this is useless
                elementID = ExportUtility.getElementID(sourceElement);
                if (elementID == null) {
                    return;
                }
                elements.remove(elementID);
                if (diagramElements.contains(elementID) || diagramElements.contains(sourceElement.getID())) {
                    return; //prevent unneeded deletes
                }
                deletes.add(elementID);
                locallyChangedElementsInMemory.remove(elementID);
                locallyAddedElementsInMemory.remove(elementID);
                if (!auto) {
                    locallyDeletedElementsInMemory.add(elementID);
                }
            }
            else if (sourceElement instanceof DirectedRelationship &&
                    (propertyName.equals(PropertyNames.SUPPLIER) || propertyName.equals(PropertyNames.CLIENT))) {
                // This event represents a move of a relationship from
                // one element (A) to another element (B). Process only
                // the events associated with the element B.
                //
                if ((newValue != null) && (oldValue == null)) {
                    JSONObject specialization = ExportUtility.fillDirectedRelationshipSpecialization((DirectedRelationship) sourceElement, null);
                    elementOb = getElementObject(sourceElement);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(sourceElement, elementOb);
                }
            }
            else if ((sourceElement instanceof Generalization)
                    && ((propertyName.equals(PropertyNames.SPECIFIC)) || (propertyName.equals(PropertyNames.GENERAL)))) {
                if ((newValue != null) && (oldValue == null)) {
                    JSONObject specialization = ExportUtility.fillDirectedRelationshipSpecialization((DirectedRelationship) sourceElement, null);
                    elementOb = getElementObject(sourceElement);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(sourceElement, elementOb);
                }
            }
            else if ((moveKeywords.contains(propertyName)) && ExportUtility.shouldAdd(sourceElement)) {
                // This code handle moving an element (not a relationship)
                // from one class to another.
                elementOb = getElementObject(sourceElement);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if (sourceElement instanceof ConnectorEnd && propertyName.equals(PropertyNames.ROLE)) {
                Connector conn = ((ConnectorEnd) sourceElement).get_connectorOfEnd();
                if (conn == null) {
                    return;
                }
                elementOb = getElementObject(conn);
                JSONObject specialization = ExportUtility.fillConnectorSpecialization(conn, null);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(conn, elementOb);
            }
            else if (sourceElement instanceof Association && propertyName.equals(PropertyNames.OWNED_END)) {
                elementOb = getElementObject(sourceElement);
                JSONObject specialization = ExportUtility.fillAssociationSpecialization((Association) sourceElement, null);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if (sourceElement instanceof Property && propertyName.equals(PropertyNames.AGGREGATION)) {
                //Association a = ((Property)sourceElement).getAssociation();
                //if (a != null) {
                elementOb = getElementObject(sourceElement);
                JSONObject specialization = ExportUtility.fillPropertySpecialization((Property) sourceElement, null, false, false);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
                //}
            }
            else if (sourceElement instanceof InstanceSpecification && ExportUtility.shouldAdd(sourceElement) && (propertyName.equals(PropertyNames.SPECIFICATION) || propertyName.equals(PropertyNames.CLASSIFIER))) {
                if (isDiagramCreated(sourceElement)) {
                    String id = ExportUtility.getElementID(sourceElement);
                    toRemove.add(id);
                    diagramElements.add(id);
                    diagramElements.add(sourceElement.getID());
                }
                else {
                    elementOb = getElementObject(sourceElement);
                    JSONObject specialization = ExportUtility.fillInstanceSpecificationSpecialization((InstanceSpecification) sourceElement, null);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(sourceElement, elementOb);
                }
            }
            else if (propertyName.equals("APPLIED_STEREOTYPES") && ExportUtility.shouldAdd(sourceElement)) {
                if (isDiagramCreated(sourceElement)) {
                    String id = ExportUtility.getElementID(sourceElement);
                    toRemove.add(id);
                    diagramElements.add(id);
                    diagramElements.add(sourceElement.getID());
                }
                else {
                    // this triggers on creation or modification of an applied stereotype
                    // APPLIED_STEREOTYPE_INSTANCE and STEREOTYPED_ELEMENT occur on create
                    elementID = ExportUtility.getElementID(sourceElement);
                    if (elementID == null) {
                        return;
                    }
                    elementOb = getElementObject(sourceElement);
                    ExportUtility.fillMetatype(sourceElement, elementOb);
                    ExportUtility.fillOwner(sourceElement, elementOb);
                    locallyChangedElementsInMemory.put(elementID, sourceElement);
                }
            }
        }*/
    }
}
