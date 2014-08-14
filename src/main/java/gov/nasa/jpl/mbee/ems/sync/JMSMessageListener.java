package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.PropertyValueType;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

public class JMSMessageListener implements MessageListener {

	private Project project;

	public JMSMessageListener(Project project) {
		this.project = project;
	}

	@Override
	public void onMessage(Message msg) {
		try {
			// Take the incoming message and parse it into a
			// JSONObject.
			//
			TextMessage message = (TextMessage) msg;
			JSONObject ob = (JSONObject) JSONValue.parse(message.getText());

			// Changed element are encapsulated in the "workspace2"
			// JSONObject.
			//
			JSONObject ws2 = (JSONObject) ob.get("workspace2");

			// Retrieve the changed elements: each type of change (updated,
			// added, moved, deleted)
			// will be returned as an JSONArray.
			//
			final JSONArray updated = (JSONArray) ws2.get("updatedElements");
			final JSONArray added = (JSONArray) ws2.get("addedElements");
			final JSONArray deleted = (JSONArray) ws2.get("deletedElements");
			final JSONArray moved = (JSONArray) ws2.get("movedElements");

			Runnable runnable = new Runnable() {
				public void run() {
					Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
					AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances
							.get("AutoSyncCommitListener");

					// Disable the listener so we do not react to the
					// changes we are importing from MMS.
					//
					if (listener != null)
						listener.disable();

					SessionManager sm = SessionManager.getInstance();
					sm.createSession("mms sync change");
					try {
						// Loop through each specified element.
						//
						for (Object element : updated) {
							makeChange((JSONObject) element);
						}
						for (Object element : added) {
							addElement((JSONObject) element);
						}
						for (Object element : deleted) {
							deleteElement((JSONObject) element);
						}
						for (Object element : moved) {
							moveElement((JSONObject) element);
						}
						sm.closeSession();
					}
					catch (Exception e) {
						sm.cancelSession();
					}

					// Once we've completed make all the
					// changes, enable the listener.
					//
					if (listener != null)
						listener.enable();
				}

				private void makeChange(JSONObject ob) {
					Element changedElement = ExportUtility.getElementFromID((String) (ob).get("sysmlid"));
					if (changedElement == null) {
						Application.getInstance().getGUILog().log("element not found from mms sync change");
						return;
					}
					else if (!changedElement.isEditable()) {
						Application.getInstance().getGUILog()
								.log("[ERROR] " + changedElement.getID() + " is not editable!");
						return;
					}

					String newName = (String) (ob).get("name");
					if (changedElement instanceof NamedElement && newName != null
							&& !((NamedElement) changedElement).getName().equals(newName)) {
						((NamedElement) changedElement).setName(newName);
					}

					String tmpComment = (String) (ob).get("documentation");
					if (tmpComment != null)
						ModelHelper.setComment(changedElement, Utils.addHtmlWrapper(tmpComment));

					if ((changedElement instanceof Property) || (changedElement instanceof Slot)) {
						ValueSpecification newVal = null;
						JSONArray vals = (JSONArray) ((JSONObject) (ob).get("specialization")).get("value");
						// Check if this is a slot. If so, process
						// the associated values; otherwise continue
						// to process the Property element using only the
						// first value in the array.
						//
						JSONObject specialization = (JSONObject) ob.get("specialization");
						Boolean isSlot = (Boolean) (specialization).get("isSlot");
						if ((isSlot != null) && (isSlot == true)) {

							if ((vals == null || vals.isEmpty())) {
								((Slot) changedElement).getValue().clear();
							}
							else {
								((Slot) changedElement).getValue().clear();
								for (Object value : vals) {
									newVal = this.getNewValue((JSONObject) value);
									((Slot) changedElement).getValue().add(newVal);
								}
							}
						}
						else {
							newVal = getNewValue((JSONObject) vals.get(0));
							((Property) changedElement).setDefaultValue(newVal);
						}

					}
				}

				private void addElement(JSONObject ob) {
					String elementName = "";
					String ownerName = "";
					String documentation = "";
					String sysmlID = "";
					Element owner = null;
					JSONObject specialization = new JSONObject();
					ElementsFactory elemFactory = Application.getInstance().getProject().getElementsFactory();

					project.getCounter().setCanResetIDForObject(true);

					// For all new elements the should be the following fields
					// should be present: name, owner, and documentation
					//
					elementName = (String) (ob).get("name");
					ownerName = (String) (ob).get("owner");
					documentation = Utils.addHtmlWrapper((String) (ob).get("documentation"));
					sysmlID = (String) (ob).get("sysmlid");

					if ((ownerName == null) || (ownerName.isEmpty())) {
						Application.getInstance().getGUILog().log("Owner not specified for mms sync add");
						return;
					}
					owner = ExportUtility.getElementFromID(ownerName);
					if (owner == null) {
						Application.getInstance().getGUILog().log("Owner not found for mms sync add");
						return;
					}

					specialization = (JSONObject) ob.get("specialization");
					if (specialization == null)
						return;

					String valueType = (String) specialization.get("type");

					// String valueType = (String) ob.get("type");

					if (valueType.equalsIgnoreCase("element")) {
						Class newElement = elemFactory.createClassInstance();
						newElement.setName((String) (ob).get("name"));
						newElement.setOwner(owner);
						newElement.setID(sysmlID);
						ModelHelper.setComment(newElement, documentation);
					}
					else if (valueType.equalsIgnoreCase("view")) {
						Class view = elemFactory.createClassInstance();
						Stereotype sysmlView = Utils.getViewClassStereotype();

						view.setOwner(owner);
						view.setName(elementName);
						view.setID(sysmlID);
						ModelHelper.setComment(view, documentation);
						StereotypesHelper.addStereotype(view, sysmlView);
					}
					else if (valueType.equalsIgnoreCase("Property")) {
						ValueSpecification newVal = null;
						JSONArray vals = (JSONArray) ((JSONObject) (ob).get("specialization")).get("value");

						// Check if this is a slot. If so, process
						// the associated values; otherwise continue
						// to process the Property element using only the
						// first value in the array.
						//
						Boolean isSlot = (Boolean) (ob).get("isSlot");
						if ((isSlot != null) && (isSlot == true)) {
							Slot newSlot = elemFactory.createSlotInstance();
							newSlot.setID(sysmlID);
							newSlot.setOwner(owner);

							if ((vals == null || vals.isEmpty())) {
								newSlot.getValue().clear();
							}
							else {
								newSlot.getValue().clear();
								for (Object value : vals) {
									newVal = getNewValue((JSONObject) value);
									newSlot.getValue().add(newVal);
								}
							}
						}
						else {
							Property newProperty = elemFactory.createPropertyInstance();
							newProperty.setName(elementName);
							newProperty.setOwner(owner);
							newProperty.setID(sysmlID);
							ModelHelper.setComment(newProperty, documentation);

							if (vals == null || vals.isEmpty()) {
								newProperty.setDefaultValue(null);
							}
							else {
								newVal = getNewValue((JSONObject) vals.get(0));
								newProperty.setDefaultValue(newVal);
							}

						}
					}
					else if ((valueType.equalsIgnoreCase("Dependency")) || (valueType.equalsIgnoreCase("Conforms"))
							|| (valueType.equalsIgnoreCase("Expose"))
							|| (valueType.equalsIgnoreCase("DirectedRelationship"))) {
						Dependency newDependency = elemFactory.createDependencyInstance();
						newDependency.setName(elementName);
						newDependency.setOwner(owner);
						newDependency.setID(sysmlID);
						ModelHelper.setComment(newDependency, documentation);

						specialization = (JSONObject) ob.get("specialization");
						String sourceId = (String) specialization.get("source");
						String targetId = (String) specialization.get("target");

						Element source = (Element) Application.getInstance().getProject().getElementByID(sourceId);
						Element target = (Element) Application.getInstance().getProject().getElementByID(targetId);

						ModelHelper.setSupplierElement(newDependency, target);
						ModelHelper.setClientElement(newDependency, source);

					}
					else if (valueType.equalsIgnoreCase("Generalization")) {
						Generalization newGeneralization = elemFactory.createGeneralizationInstance();

						newGeneralization.setOwner(owner);
						newGeneralization.setID(sysmlID);
						ModelHelper.setComment(newGeneralization, documentation);

						specialization = (JSONObject) ob.get("specialization");
						String sourceId = (String) specialization.get("source");
						String targetId = (String) specialization.get("target");
						Element source = (Element) Application.getInstance().getProject().getElementByID(sourceId);
						Element target = (Element) Application.getInstance().getProject().getElementByID(targetId);

						newGeneralization.setGeneral((Classifier) target);
						newGeneralization.setSpecific((Classifier) source);
					}
					else if (valueType.equalsIgnoreCase("Package")) {
						Package newPackage = elemFactory.createPackageInstance();

						newPackage.setName(elementName);
						newPackage.setOwner(owner);
						newPackage.setID(sysmlID);
						ModelHelper.setComment(newPackage, documentation);
					}
				}

				private ValueSpecification getNewValue(JSONObject ob) {
					ValueSpecification newValue = null;
					ElementsFactory elemFactory = Application.getInstance().getProject().getElementsFactory();
					String valueType = (String) ob.get("type");
					PropertyValueType propValueType = PropertyValueType.valueOf(valueType);

					switch (propValueType) {
					case LiteralString:
						newValue = elemFactory.createLiteralStringInstance();
						((LiteralString) newValue).setValue(Utils.addHtmlWrapper((String) ob.get("string")));
						break;
					case LiteralInteger:
						newValue = elemFactory.createLiteralIntegerInstance();
						((LiteralInteger) newValue).setValue(((Long) ob.get("integer")).intValue());
						break;
					case LiteralBoolean:
						newValue = elemFactory.createLiteralBooleanInstance();
						((LiteralBoolean) newValue).setValue((Boolean) ob.get("boolean"));
						break;
					case LiteralUnlimitedNatural:
						newValue = elemFactory.createLiteralUnlimitedNaturalInstance();
						((LiteralUnlimitedNatural) newValue).setValue(((Long) ob.get("naturalValue")).intValue());
						break;
					case LiteralReal:
						Double value;
						if (ob.get("double") instanceof Long)
							value = Double.parseDouble(((Long) ob.get("double")).toString());
						else
							value = (Double) ob.get("double");

						newValue = elemFactory.createLiteralRealInstance();
						((LiteralReal) newValue).setValue(value);
						break;
					case ElementValue:
						Element find = ExportUtility.getElementFromID((String) ob.get("element"));
						if (find == null) {
							Application.getInstance().getGUILog()
									.log("Element with id " + ob.get("element") + " not found!");
							break;
						}
						newValue = elemFactory.createElementValueInstance();
						((ElementValue) newValue).setElement(find);
						break;
					case InstanceValue:
						Element findInst = ExportUtility.getElementFromID((String) ob.get("instance"));
						if (findInst == null) {
							Application.getInstance().getGUILog()
									.log("Element with id " + ob.get("instance") + " not found!");
							break;
						}
						if (!(findInst instanceof InstanceSpecification)) {
							Application
									.getInstance()
									.getGUILog()
									.log("Element with id " + ob.get("instance")
											+ " is not an instance spec, cannot be put into an InstanceValue.");
							break;
						}
						newValue = elemFactory.createInstanceValueInstance();
						((InstanceValue) newValue).setInstance((InstanceSpecification) findInst);
					default:
						Debug.error("Bad PropertyValueType: " + valueType);
					}

					return newValue;
				}

				private void deleteElement(JSONObject ob) {
					Element changedElement = ExportUtility.getElementFromID((String) (ob).get("sysmlid"));
					if (changedElement == null) {
						Application.getInstance().getGUILog().log("element not found from mms sync delete");
						return;
					}
					// modelelementsmanager util functions.
					//
					project.removeElementByID(changedElement);
				}

				private void moveElement(JSONObject ob) {
					Element changedElement = ExportUtility.getElementFromID((String) (ob).get("sysmlid"));
					if (changedElement == null) {
						Application.getInstance().getGUILog().log("element not found from mms sync move");
						return;
					}

					String newOwnerName = (String) (ob).get("owner");
					if ((newOwnerName == null) || (newOwnerName.isEmpty())) {
						Application.getInstance().getGUILog().log("Owner not specified for mms sync move");
						return;
					}
					Element newOwnerElement = ExportUtility.getElementFromID(newOwnerName);
					if (newOwnerElement == null) {
						Application.getInstance().getGUILog().log("Owner not found for mms sync move");
						return;

					}
					changedElement.setOwner(newOwnerElement);

				}
			};
			project.getRepository().invokeAfterTransaction(runnable);

		}
		catch (Exception e) {

		}
	}
}
