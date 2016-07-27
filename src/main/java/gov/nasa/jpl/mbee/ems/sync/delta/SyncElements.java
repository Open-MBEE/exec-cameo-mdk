package gov.nasa.jpl.mbee.ems.sync.delta;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.lib.Changelog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by igomes on 7/25/16.
 */
public class SyncElements {
    public static Map<String, Changelog.ChangeType> JSON_KEY_CHANGE_MAPPING = new LinkedHashMap<>(3);

    static {
        JSON_KEY_CHANGE_MAPPING.put("added", Changelog.ChangeType.CREATED);
        JSON_KEY_CHANGE_MAPPING.put("deleted", Changelog.ChangeType.DELETED);
        JSON_KEY_CHANGE_MAPPING.put("changed", Changelog.ChangeType.UPDATED);
    }

    private static final String CLEAR_SUFFIX = "_clear";
    private static final DateFormat NAME_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss.SSSZ");

    private static String getSyncPackageID(Project project) {
        return project.getPrimaryProject().getProjectID() + "_sync";
    }

    private static Package getSyncPackage(Project project) {
        String folderId = getSyncPackageID(project);
        Element folder = ExportUtility.getElementFromID(folderId);
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
            syncElements.remove(clearedElementName);
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


    private static final Map<Changelog.ChangeType, String> CHANGE_TYPE_KEY_MAP = new HashMap<>(3);

    static {
        // TODO Update keys :'(
        CHANGE_TYPE_KEY_MAP.put(Changelog.ChangeType.CREATED, "added");
        CHANGE_TYPE_KEY_MAP.put(Changelog.ChangeType.UPDATED, "changed");
        CHANGE_TYPE_KEY_MAP.put(Changelog.ChangeType.DELETED, "deleted");
    }

    @SuppressWarnings("unchecked")
    public static JSONObject buildJson(Changelog<String, ?> changelog) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Changelog.ChangeType, String> entry : CHANGE_TYPE_KEY_MAP.entrySet()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(changelog.get(entry.getKey()).keySet());
            jsonObject.put(entry.getValue(), jsonArray);
        }
        return jsonObject;
    }

    public static Changelog<String, Void> buildChangelog(SyncElement syncElement) {
        Changelog<String, Void> changelog = new Changelog<>();
        String comment = ModelHelper.getComment(syncElement.getElement());
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
        Object o;
        try {
            o = jsonParser.parse(comment);
            if (o instanceof JSONObject) {
                jsonObject = (JSONObject) o;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return changelog;
        }
        if (jsonObject != null) {
            for (Map.Entry<Changelog.ChangeType, String> entry : CHANGE_TYPE_KEY_MAP.entrySet()) {
                if ((o = jsonObject.get(entry.getValue())) != null && o instanceof JSONArray) {
                    for (Object o1 : (JSONArray) o) {
                        if (o1 instanceof String) {
                            changelog.addChange((String) o1, null, entry.getKey());
                        }
                    }
                }
            }
        }
        return changelog;
    }
}
