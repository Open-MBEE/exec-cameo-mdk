package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.jmi.UML2MetamodelConstants;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
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

	public AutoSyncCommitListener() {

	}

	/**
	 * Adapter to call handleChangeEvent() from the TransactionCommitListener
	 * interface.
	 */
	private class TransactionCommitHandler implements Runnable {
		private final Collection<PropertyChangeEvent> events;
		private Map<String, JSONObject> elements = new HashMap<String, JSONObject>();

		TransactionCommitHandler(final Collection<PropertyChangeEvent> events) {
			this.events = events;
		}

		@Override
		public void run() {
			// If the plugin has been disabled,
			// simply return without processing
			// the events.
			//
			if (disabled)
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
						// there multliple properties were changed, so
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
			if (!elements.isEmpty())
				sendChanges();

		}

		private void sendChanges() {
			JSONObject toSend = new JSONObject();
			JSONArray eles = new JSONArray();
			eles.addAll(elements.values());
			toSend.put("elements", eles);
			String url = ExportUtility.getPostElementsUrl();
			if (url != null)
				ExportUtility.send(url, toSend.toJSONString());
		}

		@SuppressWarnings("unchecked")
		private void handleChangedProperty(Element sourceElement, String propertyName, Object newValue, Object oldValue) {
			JSONObject elementOb = null;
			String elementID = null;
			//
			// Examine property name to determine how to
			// process the change.
			//
			if (propertyName.equals(PropertyNames.NAME)) {
				elementID = ExportUtility.getElementID(sourceElement);
				if (elements.containsKey(elementID)) {
					elementOb = elements.get(elementID);
				}
				else {
					elementOb = new JSONObject();
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}
				elementOb.put("name", newValue);
			}
			else if (sourceElement instanceof Comment && ExportUtility.isElementDocumentation((Comment) sourceElement)
					&& propertyName.equals(PropertyNames.BODY)) { // doc changed

				Element actual = sourceElement.getOwner();
				if (elements.containsKey(ExportUtility.getElementID(actual))) {
					elementOb = elements.get(ExportUtility.getElementID(sourceElement));
				}
				else {
					elementOb = new JSONObject();
					elementOb.put("sysmlid", ExportUtility.getElementID(actual));
					elements.put(ExportUtility.getElementID(actual), elementOb);
				}
				elementOb.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(actual)));
			}
			else if ((sourceElement instanceof ValueSpecification) && (propertyName.equals(PropertyNames.VALUE))) {
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

				// If we found the appropriate owner,
				// get its element id.
				if (actual != null)
					elementID = ExportUtility.getElementID(actual);
				else
					elementID = ExportUtility.getElementID(sourceElement);

				JSONObject specialization = new JSONObject();
				if (actual instanceof Property) {

					specialization.put("type", "Property");
					specialization.put("isDerived", ((Property) actual).isDerived());
					specialization.put("isSlot", false);
					ValueSpecification vs = ((Property) actual).getDefaultValue();
					JSONArray singleElementSpecVsArray = new JSONArray();

					if (vs != null) {
						// Create a new JSONObject and a new JSONArray. Fill in
						// the values to the new JSONObject and then insert
						// that JSONObject into the array (NOTE: there will
						// be single element in this array). Finally, insert
						// the array into the specialization element as the
						// value of the "value" property.
						//

						JSONObject newElement = new JSONObject();
						ExportUtility.fillValueSpecification(vs, newElement, null, null);
						singleElementSpecVsArray.add(newElement);
					}
					specialization.put("value", singleElementSpecVsArray);
				}
				else if (actual instanceof Slot) {
					specialization.put("type", "Property");

					if (((Slot) actual).getDefiningFeature().getID()
							.equals("_17_0_2_3_e9f034d_1375396269655_665865_29411"))
						specialization.put("stylesaver", true);

					List<ValueSpecification> vsl = ((Slot) actual).getValue();
					JSONArray specVsArray = new JSONArray();
					if (vsl != null && vsl.size() > 0) {
						for (ValueSpecification vs : vsl) {
							JSONObject newElement = new JSONObject();
							ExportUtility.fillValueSpecification(vs, newElement, null, null);
							specVsArray.add(newElement);
						}
					}
					specialization.put("value", specVsArray);
				}
				else
					return;

				// If the element is already in the elements Map,
				// retrieve it and then update it; otherwise
				// create a new JSONObject object, update it
				// and store it in the elements Map structure
				//
				if (elements.containsKey(elementID)) {
					elementOb = elements.get(elementID);
					elementOb.put("specialization", specialization);
				}
				else {
					elementOb = new JSONObject();
					elementOb.put("sysmlid", ExportUtility.getElementID(actual));
					elementOb.put("specialization", specialization);
					elements.put(ExportUtility.getElementID(actual), elementOb);
				}
			}
			// Check if this is a Property or Slot. Need these next two if
			// statement
			// to handle the case where a value is being deleted.
			//
			else if ((sourceElement instanceof Property) && propertyName.equals(PropertyNames.DEFAULT_VALUE)) {
				elementID = ExportUtility.getElementID(sourceElement);
				ValueSpecification vs = ((Property) sourceElement).getDefaultValue();
				if (vs != null) {
					JSONObject jsonObj = new JSONObject();
					JSONArray value = new JSONArray();
					JSONObject specialization = new JSONObject();

					elementOb = new JSONObject();

					specialization.put("value", value);
					specialization.put("type", "Property");

					ExportUtility.fillValueSpecification(vs, jsonObj, null, null);
					value.add(jsonObj);

					elementOb.put("specialization", specialization);
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}
			}
			else if ((sourceElement instanceof Slot) && propertyName.equals(PropertyNames.VALUE)) {
				JSONObject specialization = new JSONObject();
				JSONArray value = new JSONArray();
				List<ValueSpecification> vsl = ((Slot) sourceElement).getValue();
				elementOb = new JSONObject();
				elementID = ExportUtility.getElementID(sourceElement);

				if (vsl != null && vsl.size() > 0) {
					specialization.put("value", value);
					specialization.put("type", "Property");
					for (ValueSpecification vs : vsl) {
						JSONObject jsonObj = new JSONObject();
						ExportUtility.fillValueSpecification(vs, jsonObj, null, null);
						value.add(jsonObj);
					}

					elementOb.put("specialization", specialization);
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}
			}
			else if (propertyName.equals(UML2MetamodelConstants.INSTANCE_CREATED)
					&& ExportUtility.shouldAdd(sourceElement)) {

				elementID = ExportUtility.getElementID(sourceElement);

				if (elements.containsKey(elementID)) {
					elementOb = elements.get(elementID);
				}
				else {
					elementOb = new JSONObject();
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}
				ExportUtility.fillElement(sourceElement, elementOb, null, null);
			}
			else if (propertyName.equals(UML2MetamodelConstants.INSTANCE_DELETED)) {
				elementID = ExportUtility.getElementID(sourceElement);

				if (elements.containsKey(elementID))
					elements.remove(elementID);
			}
			else if (propertyName.equals(PropertyNames.SUPPLIER)) {
				// This event represents a move of a relationship from
				// one element (A) to another element (B). Process only
				// the events associated with the element B.
				//
				if ((newValue != null) && (oldValue == null)) {
					JSONObject specialization = new JSONObject();
					elementOb = new JSONObject();
					elementID = ExportUtility.getElementID(sourceElement);
					Element client = ModelHelper.getClientElement(sourceElement);
					Element supplier = ModelHelper.getSupplierElement(sourceElement);
					specialization.put("source", client.getID());
					specialization.put("target", supplier.getID());

					elementOb.put("specialization", specialization);
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}

			}
			else if (propertyName.equals(PropertyNames.CLIENT)) {
				// This event represents a move of a directed relationship
				// from one element (A) to another element (B). Process
				// only the events associated with the element B.
				//
				if ((newValue != null) && (oldValue == null)) {
					JSONObject specialization = new JSONObject();
					elementOb = new JSONObject();
					elementID = ExportUtility.getElementID(sourceElement);
					Element client = ModelHelper.getClientElement(sourceElement);
					Element supplier = ModelHelper.getSupplierElement(sourceElement);
					specialization.put("source", client.getID());
					specialization.put("target", supplier.getID());

					elementOb.put("specialization", specialization);
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}
			}
			else if (propertyName.equals(PropertyNames.GENERALIZATION)) {
				if ((newValue != null) && (oldValue == null)) {
					JSONObject specialization = new JSONObject();
					elementOb = new JSONObject();
					elementID = ExportUtility.getElementID(sourceElement);
					boolean isConform = StereotypesHelper.hasStereotypeOrDerived(sourceElement,
							DocGen3Profile.conformStereotype);

					if (isConform)
						specialization.put("type", "Conform");
					else if (StereotypesHelper.hasStereotypeOrDerived(sourceElement, DocGen3Profile.queriesStereotype))
						specialization.put("type", "Expose");
					else
						specialization.put("type", "Generalization");

					elementOb.put("specialization", specialization);
					elementOb.put("sysmlid", elementID);
					elements.put(elementID, elementOb);
				}
				else if (propertyName.equals(PropertyNames.CLIENT_DEPENDENCY)) {
					if ((newValue != null) && (oldValue == null)) {
						JSONObject specialization = new JSONObject();
						elementOb = new JSONObject();
						elementID = ExportUtility.getElementID(sourceElement);
						boolean isConform = StereotypesHelper.hasStereotypeOrDerived(sourceElement,
								DocGen3Profile.conformStereotype);

						if (isConform)
							specialization.put("type", "Conform");
						else if (StereotypesHelper.hasStereotypeOrDerived(sourceElement,
								DocGen3Profile.queriesStereotype))
							specialization.put("type", "Expose");
						else
							specialization.put("type", "Generalization");

						elementOb.put("specialization", specialization);
						elementOb.put("sysmlid", elementID);
						elements.put(elementID, elementOb);
					}
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
