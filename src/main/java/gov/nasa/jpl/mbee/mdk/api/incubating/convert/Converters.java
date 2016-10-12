package gov.nasa.jpl.mbee.mdk.api.incubating.convert;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.emf.EMFExporter;
import gov.nasa.jpl.mbee.mdk.emf.EMFImporter;
import org.apache.commons.lang.math.NumberUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by igomes on 9/15/16.
 */
// TODO Lombok? @donbot
public class Converters {

    private static BiFunction<Element, Project, ObjectNode> ELEMENT_TO_JSON_CONVERTER;
    private static JsonToElementFunction JSON_TO_ELEMENT_CONVERTER;
    private static Function<Element, String> ELEMENT_TO_ID_CONVERTER;
    private static BiFunction<String, Project, Element> ID_TO_ELEMENT_CONVERTER;

    public static BiFunction<Element, Project, ObjectNode> getElementToJsonConverter() {
        if (ELEMENT_TO_JSON_CONVERTER == null) {
            ELEMENT_TO_JSON_CONVERTER = new EMFExporter();
        }
        return ELEMENT_TO_JSON_CONVERTER;
    }

    public static JsonToElementFunction getJsonToElementConverter() {
        if (JSON_TO_ELEMENT_CONVERTER == null) {
            JSON_TO_ELEMENT_CONVERTER = new EMFImporter();
        }
        return JSON_TO_ELEMENT_CONVERTER;
    }

    public static Function<Element, String> getElementToIdConverter() {
        if (ELEMENT_TO_ID_CONVERTER == null) {
            ELEMENT_TO_ID_CONVERTER = EMFExporter::getEID;
        }
        return ELEMENT_TO_ID_CONVERTER;
    }

    public static BiFunction<String, Project, Element> getIdToElementConverter() {
        if (ID_TO_ELEMENT_CONVERTER == null) {
            ID_TO_ELEMENT_CONVERTER = (id, project) -> {
                if (id == null) {
                    return null;
                }
                if (id.equals(project.getPrimaryProject().getProjectID())) {
                    return null;
                }
                BaseElement baseElement = project.getElementByID(id);
                if (baseElement == null && id.endsWith(MDKConstants.PRIMARY_MODEL_ID_SUFFIX)) {
                    String projectId = id.substring(0, id.length() - MDKConstants.PRIMARY_MODEL_ID_SUFFIX.length());
                    if (projectId.equals(project.getPrimaryProject().getProjectID())) {
                        return project.getPrimaryModel();
                    }
                }
                if (baseElement == null && id.endsWith(MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX)) {
                    String stereotypedElementId = id.substring(0, id.length() - MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX.length());
                    Element stereotypedElement = ID_TO_ELEMENT_CONVERTER.apply(stereotypedElementId, project);
                    if (stereotypedElement != null) {
                        return stereotypedElement.getAppliedStereotypeInstance();
                    }
                }
                if (baseElement == null && id.contains(MDKConstants.SLOT_VALUE_ID_SEPARATOR)) {
                    String[] sections = id.split(MDKConstants.SLOT_VALUE_ID_SEPARATOR);
                    Element element = Converters.getIdToElementConverter().apply(sections[0], project);
                    if (element == null || !(element instanceof Slot)) {
                        return null;
                    }
                    Slot owningSlot = (Slot) element;
                    String[] subSections = sections[1].split("-");
                    if (subSections.length != 2) {
                        return null;
                    }
                    if (!NumberUtils.isDigits(subSections[0])) {
                        return null;
                    }
                    int index;
                    try {
                        index = Integer.parseInt(subSections[0]);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                    if (index < 0 || index >= owningSlot.getValue().size()) {
                        return null;
                    }
                    ValueSpecification value = owningSlot.getValue().get(index);
                    if (!value.eClass().getName().toLowerCase().equals(subSections[1])) {
                        return null;
                    }
                    return value;
                }
                /*if (baseElement == null && id.endsWith(MDKConstants.TIME_EXPRESSION_ID_SUFFIX)) {
                    String timeEventId = id.substring(0, id.length() - MDKConstants.TIME_EXPRESSION_ID_SUFFIX.length());
                    Element timeEvent = ID_TO_ELEMENT_CONVERTER.apply(timeEventId, project);
                    if (timeEvent != null && timeEvent instanceof TimeEvent) {
                        return ((TimeEvent) timeEvent).getWhen();
                    }
                }*/
                if (baseElement == null && id.contains(MDKConstants.SLOT_ID_SEPARATOR) && !id.contains(MDKConstants.SLOT_VALUE_ID_SEPARATOR)) {
                    String[] sections = id.split(MDKConstants.SLOT_ID_SEPARATOR);
                    Element owningInstance = Converters.getIdToElementConverter().apply(sections[0], project);
                    Element definingFeature = Converters.getIdToElementConverter().apply(sections[1], project);
                    if (!(owningInstance instanceof InstanceSpecification) || !(definingFeature instanceof StructuralFeature)) {
                        return null;
                    }
                    return ((InstanceSpecification) owningInstance).getSlot().stream().filter(slot -> definingFeature.equals(slot.getDefiningFeature())).findAny().orElse(null);
                }
                return baseElement instanceof Element ? (Element) baseElement : null;
            };
        }
        return ID_TO_ELEMENT_CONVERTER;
    }
}
