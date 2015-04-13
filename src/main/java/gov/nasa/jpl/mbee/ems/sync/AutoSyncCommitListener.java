package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.UML2MetamodelConstants;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.impl.PropertyNames;
import com.nomagic.uml2.transaction.TransactionCommitListener;
import com.nomagic.uml2.transaction.TransactionManager;

/**
 * This class responds to commits done in the document.
 * 
 * @author jsalcedo
 * 
 */
public class AutoSyncCommitListener implements TransactionCommitListener {
    /**
     * Allow listener to be disabled during imports.
     */
    private boolean disabled = false;
    private TransactionManager tm;
    private boolean auto = false;
    private Map<String, Element> changedElements = new HashMap<String, Element>();
    private Map<String, Element> deletedElements = new HashMap<String, Element>();
    private Map<String, Element> addedElements = new HashMap<String, Element>();
    
    private Set<String> diagramElements = new HashSet<String>();
    
    public AutoSyncCommitListener(boolean auto) {
        this.auto = auto;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public Map<String, Element> getChangedElements() {
        return changedElements;
    }

    public Map<String, Element> getDeletedElements() {
        return deletedElements;
    }

    public Map<String, Element> getAddedElements() {
        return addedElements;
    }
    
    /**
     * Adapter to call handleChangeEvent() from the TransactionCommitListener
     * interface.
     */
    private class TransactionCommitHandler implements Runnable {
        private final Collection<PropertyChangeEvent> events;
        private Map<String, JSONObject> elements = new HashMap<String, JSONObject>();
        private Set<String> deletes = new HashSet<String>();
        TransactionCommitHandler(final Collection<PropertyChangeEvent> events) {
            this.events = events;
        }
        private Set<String> toRemove = new HashSet<String>();
        
        @Override
        public void run() {
            // If the plugin has been disabled,
            // simply return without processing
            // the events.
            //
            if (disabled) //take into account delayed sync?
                return;

            for (PropertyChangeEvent event : events) {

                String strTmp = "NULL";
                if (event != null) {
                    strTmp = event.toString();
                }

                // Get the object (e.g. Element) that
                // contains the change.
                //
                Object source = event.getSource();
                if (source instanceof Element) {

                    String changedPropertyName = event.getPropertyName();
                    if (changedPropertyName == null) {
                        // If the property name is null, this indicates
                        // multiple properties were changed, so
                        // simply continue.
                        //
                        continue;
                    }
                    else {
                        if (event.getNewValue() == null && event.getOldValue() == null)
                            continue;
                        if ((event.getNewValue() == null && event.getOldValue() != null)
                                || (event.getNewValue() != null && event.getOldValue() == null)
                                || (!event.getNewValue().equals(event.getOldValue())))
                            handleChangedProperty((Element) source, changedPropertyName, event.getNewValue(),
                                    event.getOldValue()); 
                    }
                }
            }
            for (String id: toRemove) {
                elements.remove(id);
            }
            if ((!elements.isEmpty() || !deletes.isEmpty()) && auto)
                sendChanges();
        }

        @SuppressWarnings("unchecked")
        private void sendChanges() {
            JSONObject toSend = new JSONObject();
            JSONArray eles = new JSONArray();
            eles.addAll(elements.values());
            toSend.put("elements", eles);
            toSend.put("source", "magicdraw");
            if (!eles.isEmpty()) {
                String url = ExportUtility.getPostElementsUrl();
                if (url != null) {
                    Request r = new Request(url, toSend.toJSONString(), "POST", false, eles.size());
                    OutputQueue.getInstance().offer(r);
                }
            }
            if (!deletes.isEmpty()) {
                String deleteUrl = ExportUtility.getUrlWithWorkspace();
                JSONObject send = new JSONObject();
                JSONArray elements = new JSONArray();
                send.put("elements", elements);
                send.put("source", "magicdraw");
                for (String id: deletes) {
                    JSONObject eo = new JSONObject();
                    eo.put("sysmlid", id);
                    elements.add(eo);
                }
                OutputQueue.getInstance().offer(new Request(deleteUrl + "/elements", send.toJSONString(), "DELETEALL", false, elements.size()));
            }
        }

        private JSONObject getElementObject(Element e) {
            return getElementObject(e, false);
        }
        
        @SuppressWarnings("unchecked")
        private JSONObject getElementObject(Element e, boolean added) {
            JSONObject elementOb = null;
            String elementID = ExportUtility.getElementID(e);
            if (elements.containsKey(elementID)) {
                elementOb = elements.get(elementID);
            } else {
                elementOb = new JSONObject();
                elementOb.put("sysmlid", elementID);
                elements.put(elementID, elementOb);
            }
            if (deletedElements.containsKey(elementID) && !auto)
                deletedElements.remove(elementID);
            if (added && !auto)
                addedElements.put(elementID, e);
            if (!auto)
                changedElements.put(elementID, e);
            return elementOb;
        }

        private boolean isDiagramCreated(Element e) {
            Element cur = e;
            while (cur.getOwner() != null) {
                cur = cur.getOwner();
            }
            if (cur != Application.getInstance().getProject().getModel())
                return true;
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
                if (!ExportUtility.shouldAdd(sourceElement))
                    return;
                elementOb = getElementObject(sourceElement);
                ExportUtility.fillName(sourceElement, elementOb);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if (sourceElement instanceof Comment && 
                    ExportUtility.isElementDocumentation((Comment) sourceElement) && 
                    propertyName.equals(PropertyNames.BODY)) { // doc changed
                Element actual = sourceElement.getOwner();
                if (!ExportUtility.shouldAdd(actual))
                    return;
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
                while (actual instanceof ValueSpecification)
                    actual = actual.getOwner();
                if (!ExportUtility.shouldAdd(actual))
                    return;
                elementOb = getElementObject(actual);
                if (actual instanceof Slot || actual instanceof Property) {
                    JSONObject specialization = ExportUtility.fillPropertySpecialization(actual, null, true);
                    elementOb.put("specialization", specialization);
                } if (actual instanceof Constraint) {
                    JSONObject specialization = ExportUtility.fillConstraintSpecialization((Constraint)actual, null);
                    elementOb.put("specialization", specialization);
                }
                ExportUtility.fillOwner(actual, elementOb);
            }
            // Check if this is a Property or Slot. Need these next two if
            // statement
            // to handle the case where a value is being deleted.
            //
            else if ((sourceElement instanceof Property) && (propertyName.equals(PropertyNames.DEFAULT_VALUE) || propertyName.equals(PropertyNames.TYPE))) {
                JSONObject specialization = ExportUtility.fillPropertySpecialization(sourceElement, null, true);
                elementOb = getElementObject(sourceElement);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if ((sourceElement instanceof Slot) && propertyName.equals(PropertyNames.VALUE) && ExportUtility.shouldAdd(sourceElement)) {
                elementOb = getElementObject(sourceElement);
                JSONObject specialization = ExportUtility.fillPropertySpecialization(sourceElement, null, false);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if ((sourceElement instanceof Constraint) && propertyName.equals(PropertyNames.SPECIFICATION)) {
                elementOb = getElementObject(sourceElement);
                JSONObject specialization = ExportUtility.fillConstraintSpecialization((Constraint)sourceElement, null);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
            }
            else if (propertyName.equals(UML2MetamodelConstants.INSTANCE_CREATED)
                    && ExportUtility.shouldAdd(sourceElement)) {
                if (isDiagramCreated(sourceElement)) {
                    String id = ExportUtility.getElementID(sourceElement);
                    toRemove.add(id);
                    diagramElements.add(id);
                    diagramElements.add(sourceElement.getID());
                } else {
                    elementOb = getElementObject(sourceElement, true);
                    ExportUtility.fillElement(sourceElement, elementOb);
                }
            }
            else if (propertyName.equals(UML2MetamodelConstants.INSTANCE_DELETED)
                    && ExportUtility.shouldAdd(sourceElement)) {
                elementID = ExportUtility.getElementID(sourceElement);
                if (elementID == null)
                    return; //this happens when slot is deleted ARGHHHH
                elements.remove(elementID);
                
                if (diagramElements.contains(elementID) || diagramElements.contains(sourceElement.getID()))
                    return; //prevent unneeded deletes
                
                deletes.add(elementID);
                changedElements.remove(elementID);
                addedElements.remove(elementID);
                if (!auto)
                    deletedElements.put(elementID, sourceElement);
            }
            else if (sourceElement instanceof DirectedRelationship && 
                    (propertyName.equals(PropertyNames.SUPPLIER) || propertyName.equals(PropertyNames.CLIENT))) {
                // This event represents a move of a relationship from
                // one element (A) to another element (B). Process only
                // the events associated with the element B.
                //
                if ((newValue != null) && (oldValue == null)) {
                    JSONObject specialization = ExportUtility.fillDirectedRelationshipSpecialization((DirectedRelationship)sourceElement, null);
                    elementOb = getElementObject(sourceElement);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(sourceElement, elementOb);
                }
            }
            else if ((sourceElement instanceof Generalization)
                    && ((propertyName.equals(PropertyNames.SPECIFIC)) || (propertyName.equals(PropertyNames.GENERAL)))) {
                if ((newValue != null) && (oldValue == null)) {
                    JSONObject specialization = ExportUtility.fillDirectedRelationshipSpecialization((DirectedRelationship)sourceElement, null);
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
            } else if (sourceElement instanceof ConnectorEnd && propertyName.equals(PropertyNames.ROLE)) {
                Connector conn = ((ConnectorEnd)sourceElement).get_connectorOfEnd();
                elementOb = getElementObject(conn);
                JSONObject specialization = ExportUtility.fillConnectorSpecialization(conn, null);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(conn, elementOb);
            } else if (sourceElement instanceof Association && propertyName.equals(PropertyNames.OWNED_END)) {
                elementOb = getElementObject(sourceElement);
                JSONObject specialization = ExportUtility.fillAssociationSpecialization((Association)sourceElement, null);
                elementOb.put("specialization", specialization);
                ExportUtility.fillOwner(sourceElement, elementOb);
            } else if (sourceElement instanceof Property && propertyName.equals(PropertyNames.AGGREGATION)) {
                Association a = ((Property)sourceElement).getAssociation();
                if (a != null) {
                    elementOb = getElementObject(a);
                    JSONObject specialization = ExportUtility.fillAssociationSpecialization(a, null);
                    elementOb.put("specialization", specialization);
                    ExportUtility.fillOwner(a, elementOb);
                }
            }
        }
    }

    public void disable() {
        disabled = true;
    }

    public void enable() {
        disabled = false;
    }

    public TransactionManager getTm() {
        return tm;
    }

    public void setTm(TransactionManager tm) {
        this.tm = tm;
    }

    @Override
    public Runnable transactionCommited(Collection<PropertyChangeEvent> events) {
        return new TransactionCommitHandler(events);
    }
}
