package gov.nasa.jpl.mbee.mdk.emf;

import com.fasterxml.jackson.databind.JsonNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.annotations.SessionManaged;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonEquivalencePredicate;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by igomes on 9/28/16.
 */
// TODO What about locks? @donbot
public class EMFBulkImporter implements BiFunction<List<JSONObject>, Project, Changelog<String, Pair<Element, JSONObject>>> {
    private final String sessionName;
    private int sessionCount;

    private Changelog<String, Pair<Element, JSONObject>> changelog;
    private Map<Pair<Element, JSONObject>, ImportException> failedJsonObjects;
    private Map<Element, JSONObject> nonEquivalentElements;

    public EMFBulkImporter(String sessionName) {
        this.sessionName = sessionName;
    }

    @SessionManaged
    @Override
    public Changelog<String, Pair<Element, JSONObject>> apply(List<JSONObject> jsonObjects, Project project) {
        failedJsonObjects = new LinkedHashMap<>(jsonObjects.size());
        nonEquivalentElements = new LinkedHashMap<>();

        bulkImport:
        while (!jsonObjects.isEmpty()) {
            changelog = new Changelog<>();

            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().cancelSession();
            }
            SessionManager.getInstance().createSession(project, sessionName + " #" + ++sessionCount);

            Iterator<JSONObject> iterator = jsonObjects.iterator();
            while (iterator.hasNext()) {
                JSONObject jsonObject = iterator.next();
                Changelog.Change<Element> change = null;
                ImportException importException = null;
                try {
                    change = Converters.getJsonToElementConverter().apply(jsonObject, project, false);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                    importException = e;
                }
                if (change == null || change.getChanged() == null) {
                    failedJsonObjects.put(new Pair<>(Converters.getIdToElementConverter().apply((String) jsonObject.get(MDKConstants.SYSML_ID_KEY), project), jsonObject), importException);
                    iterator.remove();
                    continue bulkImport;
                }
                changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), jsonObject), change.getType());
            }

            iterator = jsonObjects.iterator();
            while (iterator.hasNext()) {
                JSONObject jsonObject = iterator.next();
                Changelog.Change<Element> change = null;
                ImportException importException = null;
                try {
                    change = Converters.getJsonToElementConverter().apply(jsonObject, project, true);
                } catch (ImportException e) {
                    if (MDUtils.isDeveloperMode()) {
                        e.printStackTrace();
                    }
                    importException = e;
                }
                if (change == null || change.getChanged() == null) {
                    failedJsonObjects.put(new Pair<>(Converters.getIdToElementConverter().apply((String) jsonObject.get(MDKConstants.SYSML_ID_KEY), project), jsonObject), importException);
                    iterator.remove();
                    continue bulkImport;
                }
                changelog.addChange(Converters.getElementToIdConverter().apply(change.getChanged()), new Pair<>(change.getChanged(), jsonObject), change.getType());
            }

            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, JSONObject>> entry : changelog.get(changeType).entrySet()) {
                    Element element = entry.getValue().getFirst();
                    JSONObject jsonObject = entry.getValue().getSecond();

                    if (element.isInvalid()) {
                        failedJsonObjects.put(new Pair<>(element, jsonObject), new ImportException(element, jsonObject, "Element was found to be invalid after importing."));
                        jsonObjects.remove(jsonObject);
                        continue bulkImport;
                    }
                    JSONObject sourceJsonObject = Converters.getElementToJsonConverter().apply(element, project);
                    try {
                        JsonNode sourceJsonNode = sourceJsonObject != null ? JacksonUtils.getObjectMapper().readTree(sourceJsonObject.toJSONString()) : null;
                        JsonNode targetJsonNode = jsonObject != null ? JacksonUtils.getObjectMapper().readTree(jsonObject.toJSONString()) : null;
                        if (!JsonEquivalencePredicate.getInstance().test(sourceJsonNode, targetJsonNode)) {
                            // currently handled as a warning instead of an error
                            nonEquivalentElements.put(element, jsonObject);
                        }
                    } catch (IOException e) {
                        if (MDUtils.isDeveloperMode()) {
                            e.printStackTrace();
                        }
                        failedJsonObjects.put(new Pair<>(element, jsonObject), new ImportException(element, jsonObject, "Unexpected JSON serialization error", e));
                    }
                }
            }
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();
            }
            return changelog;
        }
        return changelog;
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public Changelog<String, Pair<Element, JSONObject>> getChangelog() {
        return changelog;
    }

    public Map<Pair<Element, JSONObject>, ImportException> getFailedJsonObjects() {
        return failedJsonObjects;
    }

    public Map<Element, JSONObject> getNonEquivalentElements() {
        return nonEquivalentElements;
    }
}
