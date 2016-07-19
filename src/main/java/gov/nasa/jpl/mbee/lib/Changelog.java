package gov.nasa.jpl.mbee.lib;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.UML2MetamodelConstants;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.lib.function.BiPredicate;

import java.util.*;

/**
 * Created by igomes on 6/30/16.
 */
public class Changelog<K, V> extends HashMap<Changelog.ChangeType, Map<K, V>> implements Cloneable {

    // ConcurrentHashMap was decided against as it doesn't accept null values (or keys) and while we could fill it with junk we don't expect
    // too much concurrency with Changelogs, so just syncing it shouldn't be an issue.
    public Map<K, V> getNewMap() {
        return Collections.synchronizedMap(new HashMap<K, V>());
    }

    @Override
    public Map<K, V> get(Object key) {
        Map<K, V> map = super.get(key);
        if (map == null && key instanceof ChangeType) {
            super.put((ChangeType) key, map = getNewMap());
        }
        return map;
    }

    @Override
    public Changelog<K, V> clone() {
        Changelog<K, V> clonedChangelog = new Changelog<>();
        for (ChangeType changeType : ChangeType.values()) {
            for (Map.Entry<K, V> entry : get(changeType).entrySet()) {
                clonedChangelog.get(changeType).put(entry.getKey(), entry.getValue());
            }
        }
        return clonedChangelog;
    }

    public Changelog<K, V> and(Changelog<K, V> secondChangelog) {
        Changelog<K, V> combinedChangelog = new Changelog<>();
        for (ChangeType changeType : ChangeType.values()) {
            Map<K, V> map = combinedChangelog.get(changeType);
            //map.putAll(this.get(changeType));
            //map.putAll(secondChangelog.get(changeType));
            for (Map.Entry<K, V> entry : this.get(changeType).entrySet()) {
                combinedChangelog.addChange(entry.getKey(), entry.getValue(), changeType);
            }
            for (Map.Entry<K, V> entry : secondChangelog.get(changeType).entrySet()) {
                combinedChangelog.addChange(entry.getKey(), entry.getValue(), changeType);
            }
        }
        return combinedChangelog;
    }

    public <W> Map<K, Pair<Set<Change<V>>, Set<Change<W>>>> conflicts(Changelog<K, W> changelog, BiPredicate<Set<Change<V>>, Set<Change<W>>> conflictCondition, Map<K, Pair<Set<Change<V>>, Set<Change<W>>>> conflicts) {
        Set<K> keySet = new HashSet<>();
        for (ChangeType changeType : ChangeType.values()) {
            keySet.addAll(this.get(changeType).keySet());
            keySet.addAll(changelog.get(changeType).keySet());
        }

        V v;
        W w;
        for (K key : keySet) {
            Set<Change<V>> vChanges = new HashSet<>();
            Set<Change<W>> wChanges = new HashSet<>();
            for (ChangeType changeType : ChangeType.values()) {
                if ((v = this.get(changeType).get(key)) != null) {
                    vChanges.add(new Change<>(v, changeType));
                }
                if ((w = changelog.get(changeType).get(key)) != null) {
                    wChanges.add(new Change<>(w, changeType));
                }
            }
            if (conflictCondition.test(vChanges, wChanges)) {
                conflicts.put(key, new Pair<>(vChanges, wChanges));
            }
        }
        return conflicts;
    }

    public void addChange(K k, V v, ChangeType changeType) {
        switch (changeType) {
            case CREATED:
                Map<K, V> deletedElements = get(Changelog.ChangeType.DELETED);
                if (deletedElements.containsKey(k)) {
                    deletedElements.remove(k);
                    Application.getInstance().getGUILog().log("Undeleted: " + k + " - " + (v instanceof NamedElement ? " " + ((NamedElement) v).getName() : "<>"));
                }
                else {
                    get(Changelog.ChangeType.CREATED).put(k, v);
                    get(Changelog.ChangeType.UPDATED).remove(k);
                    Application.getInstance().getGUILog().log("Created: " + k + " - " + (v instanceof NamedElement ? " " + ((NamedElement) v).getName() : "<>"));
                }
                break;
            case DELETED:
                Map<K, V> createdChanges = get(Changelog.ChangeType.CREATED);
                // minimizes extraneous deletions
                if (createdChanges.containsKey(k)) {
                    createdChanges.remove(k);
                    Application.getInstance().getGUILog().log("Unadded: " + k + " - " + (v instanceof NamedElement ? ((NamedElement) v).getName() : "<>"));
                }
                else {
                    get(Changelog.ChangeType.UPDATED).remove(k);
                    get(Changelog.ChangeType.DELETED).put(k, v);
                    Application.getInstance().getGUILog().log("Deleted: " + k + " - " + (v instanceof NamedElement ? ((NamedElement) v).getName() : "<>"));
                }
                break;
            case UPDATED:
                if (!get(Changelog.ChangeType.CREATED).containsKey(k) && !get(Changelog.ChangeType.DELETED).containsKey(k)) {
                    get(Changelog.ChangeType.UPDATED).put(k, v);
                    Application.getInstance().getGUILog().log("Updated: " + k + " - " + (v instanceof NamedElement ? " " + ((NamedElement) v).getName() : "<>"));
                }
                break;
            default:
                throw new IllegalArgumentException("Unhandled ChangeType for putting.");

        }
    }

    public enum ChangeType {
        CREATED,
        UPDATED,
        DELETED
    }

    public class Change<C> extends Pair<C, ChangeType> {

        public Change(C c, ChangeType changeType) {
            super(c, changeType);
        }
    }
}
