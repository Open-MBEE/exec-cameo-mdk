package gov.nasa.jpl.mbee.mdk.api.incubating.convert;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.emf.EMFExporter;
import gov.nasa.jpl.mbee.mdk.emf.EMFImporter;
import org.apache.commons.lang.math.NumberUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Converters {

    private static BiFunction<Element, Project, ObjectNode> ELEMENT_TO_JSON_CONVERTER;
    private static JsonToElementFunction JSON_TO_ELEMENT_CONVERTER;
    private static Function<Element, String> ELEMENT_TO_ID_CONVERTER;
    private static BiFunction<String, Project, Element> ID_TO_ELEMENT_CONVERTER;
    private static Function<Element, String> ELEMENT_TO_HUMAN_NAME_CONVERTER;
    private static Function<Project, String> PROJECT_TO_ID_CONVERTER;
    private static Function<IProject, String> IPROJECT_TO_ID_CONVERTER;

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
                if (id.equals(project.getID()) || id.equals(project.getPrimaryProject().getProjectID())) {
                    return null;
                }
                BaseElement baseElement = project.getElementByID(id);
                if (baseElement instanceof Element) {
                    return (Element) baseElement;
                }
                if (id.endsWith(MDKConstants.PRIMARY_MODEL_ID_SUFFIX)) {
                    String projectId = id.substring(0, id.length() - MDKConstants.PRIMARY_MODEL_ID_SUFFIX.length());
                    if (projectId.equals(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))) {
                        return project.getPrimaryModel();
                    }
                }
                if (id.endsWith(MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX)) {
                    String stereotypedElementId = id.substring(0, id.length() - MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX.length());
                    Element stereotypedElement = ID_TO_ELEMENT_CONVERTER.apply(stereotypedElementId, project);
                    if (stereotypedElement != null) {
                        return stereotypedElement.getAppliedStereotypeInstance();
                    }
                }
                if (id.contains(MDKConstants.SLOT_VALUE_ID_SEPARATOR)) {
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
                /*if (id.endsWith(MDKConstants.TIME_EXPRESSION_ID_SUFFIX)) {
                    String timeEventId = id.substring(0, id.length() - MDKConstants.TIME_EXPRESSION_ID_SUFFIX.length());
                    Element timeEvent = ID_TO_ELEMENT_CONVERTER.apply(timeEventId, project);
                    if (timeEvent != null && timeEvent instanceof TimeEvent) {
                        return ((TimeEvent) timeEvent).getWhen();
                    }
                }*/
                if (id.contains(MDKConstants.SLOT_ID_SEPARATOR) && !id.contains(MDKConstants.SLOT_VALUE_ID_SEPARATOR)) {
                    String[] sections = id.split(MDKConstants.SLOT_ID_SEPARATOR);
                    if (sections.length < 2) {
                        return null;
                    }
                    Element owningInstance = Converters.getIdToElementConverter().apply(sections[0], project);
                    Element definingFeature = Converters.getIdToElementConverter().apply(sections[1], project);
                    if (!(owningInstance instanceof InstanceSpecification) || !(definingFeature instanceof StructuralFeature)) {
                        return null;
                    }
                    return ((InstanceSpecification) owningInstance).getSlot().stream().filter(slot -> definingFeature.equals(slot.getDefiningFeature())).findAny().orElse(null);
                }
                return null;
            };
        }
        return ID_TO_ELEMENT_CONVERTER;
    }

    public static Function<Element, String> getElementToHumanNameConverter() {
        if (ELEMENT_TO_HUMAN_NAME_CONVERTER == null) {
            ELEMENT_TO_HUMAN_NAME_CONVERTER = element -> {
                if (element == null) {
                    return null;
                }
                return element.getHumanName() + " (" + Converters.getElementToIdConverter().apply(element) + ")";
            };
        }
        return ELEMENT_TO_HUMAN_NAME_CONVERTER;
    }

    public static Function<Project, String> getProjectToIdConverter() {
        // this returns the primary project ID intentionally, as that is the tracked id in mms
        if (PROJECT_TO_ID_CONVERTER == null) {
            PROJECT_TO_ID_CONVERTER = (project) -> {
                if (project == null) {
                    return null;
                }
                return project.getPrimaryProject().getProjectID();
            };
        }
        return PROJECT_TO_ID_CONVERTER;
    }

    public static Function<IProject, String> getIProjectToIdConverter() {
        if (IPROJECT_TO_ID_CONVERTER == null) {
            IPROJECT_TO_ID_CONVERTER = (iProject) -> {
                if (iProject == null) {
                    return null;
                }
                return iProject.getProjectID();
            };
        }
        return IPROJECT_TO_ID_CONVERTER;
    }
}
