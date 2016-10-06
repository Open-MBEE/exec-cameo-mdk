package gov.nasa.jpl.mbee.mdk.emf;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.annotations.SessionManaged;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonEquivalencePredicate;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by igomes on 9/28/16.
 */
// TODO What about locks? @donbot
public class EMFBulkImporter implements BiFunction<Collection<ObjectNode>, Project, Changelog<String, Pair<Element, ObjectNode>>> {
    private final String sessionName;
    private int sessionCount;

    private Changelog<String, Pair<Element, ObjectNode>> changelog;
    private Map<Pair<Element, ObjectNode>, ImportException> failedJsonObjects;
    private Map<Element, ObjectNode> nonEquivalentElements;

    public EMFBulkImporter(String sessionName) {
        this.sessionName = sessionName;
    }

    @SessionManaged
    @Override
    public Changelog<String, Pair<Element, ObjectNode>> apply(Collection<ObjectNode> objectNodes, Project project) {
        failedJsonObjects = new LinkedHashMap<>(objectNodes.size());
        nonEquivalentElements = new LinkedHashMap<>();

        bulkImport:
        while (!objectNodes.isEmpty()) {
            changelog = new Changelog<>();

            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().cancelSession();
            }
            SessionManager.getInstance().createSession(project, sessionName + " #" + ++sessionCount);

            Iterator<ObjectNode> iterator = objectNodes.iterator();
            while (iterator.hasNext()) {
                ObjectNode objectNode = iterator.next();
                Changelog.Change<Element> change = null;
                ImportException importException = new ImportException(null, objectNode, "Null on first pass");
                try {
                    change = Converters.getJsonToElementConverter().apply(objectNode, project, false);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                    importException = e;
                }
                if (change == null || change.getChanged() == null) {
                    failedJsonObjects.put(new Pair<>(Converters.getIdToElementConverter().apply(objectNode.get(MDKConstants.SYSML_ID_KEY).asText(), project), objectNode), importException);
                    iterator.remove();
                    continue bulkImport;
                }
                //changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), objectNode), change.getType());
            }

            iterator = objectNodes.iterator();
            while (iterator.hasNext()) {
                ObjectNode objectNode = iterator.next();
                Changelog.Change<Element> change = null;
                ImportException importException = new ImportException(null, objectNode, "Null on second pass");
                try {
                    change = Converters.getJsonToElementConverter().apply(objectNode, project, true);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                    importException = e;
                }
                if (change == null || change.getChanged() == null) {
                    failedJsonObjects.put(new Pair<>(Converters.getIdToElementConverter().apply(objectNode.get(MDKConstants.SYSML_ID_KEY).asText(), project), objectNode), importException);
                    iterator.remove();
                    continue bulkImport;
                }
                changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), objectNode), change.getType());
            }

            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, ObjectNode>> entry : changelog.get(changeType).entrySet()) {
                    Element element = entry.getValue().getFirst();
                    ObjectNode objectNode = entry.getValue().getSecond();

                    if (element.isInvalid()) {
                        failedJsonObjects.put(new Pair<>(element, objectNode), new ImportException(element, objectNode, "Element was found to be invalid after importing."));
                        objectNodes.remove(objectNode);
                        continue bulkImport;
                    }
                    ObjectNode sourceObjectNode = Converters.getElementToJsonConverter().apply(element, project);
                    if (!JsonEquivalencePredicate.getInstance().test(sourceObjectNode, objectNode)) {
                        // currently handled as a warning instead of an error
                        nonEquivalentElements.put(element, objectNode);
                    }
                }
            }
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();
            }
            return changelog;
        }
        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().cancelSession();
        }
        return changelog;
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public Changelog<String, Pair<Element, ObjectNode>> getChangelog() {
        return changelog;
    }

    public Map<Pair<Element, ObjectNode>, ImportException> getFailedJsonObjects() {
        return failedJsonObjects;
    }

    public Map<Element, ObjectNode> getNonEquivalentElements() {
        return nonEquivalentElements;
    }
}
