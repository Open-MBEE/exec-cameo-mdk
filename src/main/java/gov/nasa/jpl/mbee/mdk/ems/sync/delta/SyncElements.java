package gov.nasa.jpl.mbee.mdk.ems.sync.delta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by igomes on 7/25/16.
 */

//@donbot update json simple to jackson
public class SyncElements {
    private static final String CLEAR_SUFFIX = "_clear";
    private static final DateFormat NAME_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSSZ");

    private static String getSyncPackageID(Project project) {
        return Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + MDKConstants.SYNC_SYSML_ID_SUFFIX;
    }

    public static Package getSyncPackage(Project project) {
        String folderId = getSyncPackageID(project);
        Element folder = Converters.getIdToElementConverter().apply(folderId, project);
        return folder instanceof Package ? (Package) folder : null;
    }

    public static Collection<SyncElement> getAllOfType(Project project, SyncElement.Type type) {
        Package syncPackage = getSyncPackage(project);
        if (syncPackage == null) {
            return Collections.emptySet();
        }
        Collection<PackageableElement> packagedElements = syncPackage.getPackagedElement();
        Map<String, SyncElement> syncElements = new HashMap<>(packagedElements.size());
        List<NamedElement> clearElements = new ArrayList<>(packagedElements.size());
        for (Element element : packagedElements) {
            if (!(element instanceof NamedElement)) {
                continue;
            }
            String name = ((NamedElement) element).getName();
            if (!name.startsWith(type.getPrefix())) {
                continue;
            }
            if (name.endsWith(CLEAR_SUFFIX)) {
                clearElements.add((NamedElement) element);
                continue;
            }
            syncElements.put(name, new SyncElement((NamedElement) element, type));
        }
        for (NamedElement clearElement : clearElements) {
            String name = clearElement.getName();
            syncElements.remove(name.substring(0, name.length() - CLEAR_SUFFIX.length()));
        }
        return syncElements.values();
    }

    public static SyncElement setByType(Project project, SyncElement.Type type, String comment) {
        Package syncPackage = getSyncPackage(project);
        if (syncPackage == null) {
            project.getCounter().setCanResetIDForObject(true);
            syncPackage = project.getElementsFactory().createPackageInstance();
            syncPackage.setOwner(project.getModel());
            syncPackage.setName("__MMSSync__");
            syncPackage.setID(getSyncPackageID(project));
        }
        Collection<PackageableElement> packagedElements = syncPackage.getPackagedElement();
        Map<String, SyncElement> syncElements = new HashMap<>(packagedElements.size());
        List<NamedElement> clearElements = new ArrayList<>(packagedElements.size());
        for (Element element : packagedElements) {
            if (!(element instanceof NamedElement)) {
                continue;
            }
            String name = ((NamedElement) element).getName();
            if (!name.startsWith(type.getPrefix())) {
                continue;
            }
            if (name.endsWith(CLEAR_SUFFIX)) {
                clearElements.add((NamedElement) element);
                continue;
            }
            syncElements.put(name, new SyncElement((NamedElement) element, type));
        }

        // DELETE ALREADY CLEARED BLOCKS AND THEIR CLEAR BLOCKS (AND DANGLING CLEAR BLOCKS)

        for (NamedElement clearElement : clearElements) {
            if (!clearElement.isEditable()) {
                continue;
            }
            String name = clearElement.getName();
            String clearedElementName = name.substring(0, name.length() - CLEAR_SUFFIX.length());
            SyncElement clearedSyncElement = syncElements.get(clearedElementName);
            if (clearedSyncElement == null) {
                // delete dangling clear blocks
                try {
                    ModelElementsManager.getInstance().removeElement(clearElement);
                } catch (ReadOnlyElementException e) {
                    e.printStackTrace();
                }
                continue;
            }
            syncElements.remove(clearedElementName);
            if (!clearedSyncElement.getElement().isEditable()) {
                continue;
            }
            try {
                ModelElementsManager.getInstance().removeElement(clearedSyncElement.getElement());
                ModelElementsManager.getInstance().removeElement(clearElement);
            } catch (ReadOnlyElementException e) {
                e.printStackTrace();
            }
        }

        // PERSIST CONTENT

        boolean isReusing = false;
        NamedElement newSyncElementElement = null;
        for (SyncElement syncElement : syncElements.values()) {
            if (syncElement.getElement().isEditable()) {
                newSyncElementElement = syncElement.getElement();
                isReusing = true;
                break;
            }
        }
        if (newSyncElementElement == null) {
            newSyncElementElement = project.getElementsFactory().createClassInstance();
            newSyncElementElement.setOwner(syncPackage);
        }
        newSyncElementElement.setName(type.getPrefix() + "_" + NAME_DATE_FORMAT.format(new Date()));
        ModelHelper.setComment(newSyncElementElement, comment);

        // DELETE/CLEAR ALL OLD SYNC ELEMENTS OF SAME TYPE AND DANGLING CLEAR BLOCKS

        for (SyncElement syncElement : syncElements.values()) {
            // so it doesn't delete the one that we just updated
            if (isReusing && syncElement.getElement() == newSyncElementElement) {
                continue;
            }
            if (syncElement.getElement().isEditable()) {
                try {
                    ModelElementsManager.getInstance().removeElement(syncElement.getElement());
                    continue;
                } catch (ReadOnlyElementException e) {
                    e.printStackTrace();
                }
            }
            NamedElement newClearElement = project.getElementsFactory().createClassInstance();
            newClearElement.setName(syncElement.getElement().getName() + CLEAR_SUFFIX);
            newClearElement.setOwner(syncPackage);
        }

        return new SyncElement(newSyncElementElement, type);
    }

    @SuppressWarnings("unchecked")
    public static ObjectNode buildJson(Changelog<String, ?> changelog) {
        ObjectNode objectNode = JacksonUtils.getObjectMapper().createObjectNode();
        for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
            ArrayNode arrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            changelog.get(changeType).keySet().forEach(arrayNode::add);
            objectNode.set(changeType.name().toLowerCase(), arrayNode);
        }
        return objectNode;
    }

    public static Changelog<String, Void> buildChangelog(SyncElement syncElement) {
        String comment = ModelHelper.getComment(syncElement.getElement());
        try {
            JsonNode jsonNode = JacksonUtils.getObjectMapper().readTree(comment);
            if (jsonNode != null && jsonNode.isObject()) {
                return buildChangelog((ObjectNode) jsonNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Changelog<>();
    }

    public static Changelog<String, Void> buildChangelog(ObjectNode objectNode) {
        Changelog<String, Void> changelog = new Changelog<>();
        for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
            JsonNode jsonNode = objectNode.get(changeType.name().toLowerCase());
            if (jsonNode == null || !jsonNode.isArray()) {
                continue;
            }
            for (JsonNode jsonNode1 : jsonNode) {
                if (jsonNode1 == null || !jsonNode1.isTextual()) {
                    continue;
                }
                changelog.addChange(jsonNode1.asText(), null, changeType);
            }
        }
        return changelog;
    }

    public static List<Element> lockSyncFolder(Project project) {
        if (!ProjectUtilities.isFromEsiServer(project.getPrimaryProject())) {
            return Collections.emptyList();
        }
        String folderId = Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + MDKConstants.SYNC_SYSML_ID_SUFFIX;
        Element folder = Converters.getIdToElementConverter().apply(folderId, project);
        if (folder == null) {
            return Collections.emptyList();
        }
        ILockProjectService lockService = EsiUtils.getLockService(project);
        if (lockService == null) {
            return Collections.emptyList();
        }
        Collection<Element> ownedElements = folder.getOwnedElement();
        List<Element> lockedElements = new ArrayList<>(ownedElements.size());
        for (Element element : folder.getOwnedElement()) {
            if (element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class && !lockService.isLocked(element) && lockService.canBeLocked(element)) {
                lockedElements.add(element);
            }
        }
        if (!lockService.lockElements(lockedElements, false, null)) {
            return Collections.emptyList();
        }
        return lockedElements;
    }
}
