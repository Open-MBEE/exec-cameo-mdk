package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Created by igomes on 6/30/16.
 */
public class Changelog<K, V> extends HashMap<Changelog.ChangeType, Map<K, V>> implements Cloneable {

    private boolean shouldLogChanges;

    public boolean shouldLogChanges() {
        return shouldLogChanges;
    }

    public void setShouldLogChanges(boolean shouldLogChanges) {
        this.shouldLogChanges = shouldLogChanges;
    }

    // ConcurrentHashMap was decided against as it doesn't accept null values (or keys) and while we could fill it with junk we don't expect
    // too much concurrency with Changelogs, so just syncing it shouldn't be an issue.
    public Map<K, V> createMap() {
        return Collections.synchronizedMap(new LinkedHashMap<K, V>());
    }

    @Override
    public Map<K, V> get(Object key) {
        Map<K, V> map = super.get(key);
        if (map == null && key instanceof ChangeType) {
            super.put((ChangeType) key, map = createMap());
        }
        return map;
    }

    @Override
    public Changelog<K, V> clone() {
        Changelog<K, V> clonedChangelog = new Changelog<>(); // lgtm [java/empty-container]
        for (ChangeType changeType : ChangeType.values()) {
            Map<K, V> map = clonedChangelog.get(changeType);
            for (Map.Entry<K, V> entry : get(changeType).entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return clonedChangelog;
    }

    public Changelog<K, V> and(Changelog<K, V> secondChangelog) {
        Changelog<K, V> combinedChangelog = this.clone();
        for (ChangeType changeType : ChangeType.values()) {
            for (Map.Entry<K, V> entry : secondChangelog.get(changeType).entrySet()) {
                combinedChangelog.addChange(entry.getKey(), entry.getValue(), changeType);
            }
        }
        return combinedChangelog;
    }

    public <W> Changelog<K, V> and(Changelog<K, W> secondChangelog, BiFunction<K, W, V> converter) {
        Changelog<K, V> combinedChangelog = this.clone();
        for (ChangeType changeType : ChangeType.values()) {
            for (Map.Entry<K, W> entry : secondChangelog.get(changeType).entrySet()) {
                combinedChangelog.addChange(entry.getKey(), converter.apply(entry.getKey(), entry.getValue()), changeType);
            }
        }
        return combinedChangelog;
    }

    public <W> void findConflicts(Changelog<K, W> changelog, BiPredicate<Change<V>, Change<W>> conflictCondition, Map<K, Pair<Change<V>, Change<W>>> conflictedChanges, Map<K, Pair<Change<V>, Change<W>>> unconflictedChanges) {
        Set<K> keySet = new HashSet<>();
        keySet.addAll(this.flattenedKeyset());
        keySet.addAll(changelog.flattenedKeyset());

        for (K key : keySet) {
            Change<V> vChange = null;
            Change<W> wChange = null;
            for (ChangeType changeType : ChangeType.values()) {
                // must use containsKey instead of get, because null is an acceptable value in this paradigm
                if (vChange == null && this.get(changeType).containsKey(key)) {
                    vChange = new Change<>(this.get(changeType).get(key), changeType);
                }
                if (wChange == null && changelog.get(changeType).containsKey(key)) {
                    wChange = new Change<>(changelog.get(changeType).get(key), changeType);
                }
            }
            (conflictCondition.test(vChange, wChange) ? conflictedChanges : unconflictedChanges).put(key, new Pair<>(vChange, wChange));
        }
    }

    // it is desired that all ChangeTypes are mutually exclusive
    // enforces that a change can only exist under a single ChangetType at any point
    public void addChange(K k, V v, ChangeType changeType) {
        switch (changeType) {
            case CREATED:
                Map<K, V> deletedElements = get(Changelog.ChangeType.DELETED);
                if (deletedElements.containsKey(k)) {
                    deletedElements.remove(k);
                    if (shouldLogChanges) {
                       String message = "Undeleted: " + k + " - " + (v instanceof NamedElement ? " " + ((NamedElement) v).getName() : "<>");
                       System.out.println(message);
                       Application.getInstance().getGUILog().log(message);
                    }
                }
                else {
                    get(Changelog.ChangeType.CREATED).put(k, v);
                    get(Changelog.ChangeType.UPDATED).remove(k);
                    if (shouldLogChanges) {
                        String message = "Created: " + k + " - " + (v instanceof NamedElement ? " " + ((NamedElement) v).getName() : "<>");
                        System.out.println(message);
                        Application.getInstance().getGUILog().log(message);
                    }
                }
                break;
            case DELETED:
                Map<K, V> createdChanges = get(Changelog.ChangeType.CREATED);
                // minimizes extraneous deletions
                if (createdChanges.containsKey(k)) {
                    createdChanges.remove(k);
                    if (shouldLogChanges) {
                        String message = "Unadded: " + k + " - " + (v instanceof NamedElement ? ((NamedElement) v).getName() : "<>");
                        System.out.println(message);
                        Application.getInstance().getGUILog().log(message);
                    }
                }
                else {
                    get(Changelog.ChangeType.UPDATED).remove(k);
                    get(Changelog.ChangeType.DELETED).put(k, v);
                    if (shouldLogChanges) {
                        String message = "Deleted: " + k + " - " + (v instanceof NamedElement ? ((NamedElement) v).getName() : "<>");
                        System.out.println(message);
                        Application.getInstance().getGUILog().log(message);
                    }
                }
                break;
            case UPDATED:
                if (!get(Changelog.ChangeType.CREATED).containsKey(k) && !get(Changelog.ChangeType.DELETED).containsKey(k)) {
                    get(Changelog.ChangeType.UPDATED).put(k, v);
                    if (shouldLogChanges) {
                        String message = "Updated: " + k + " - " + (v instanceof NamedElement ? " " + ((NamedElement) v).getName() : "<>");
                        System.out.println(message);
                        Application.getInstance().getGUILog().log(message);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unhandled ChangeType for putting.");

        }
    }

    public boolean isEmpty() {
        for (ChangeType changeType : ChangeType.values()) {
            if (!get(changeType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int flattenedSize() {
        int size = 0;
        for (ChangeType changeType : ChangeType.values()) {
            size += get(changeType).size();
        }
        return size;
    }

    public Set<K> flattenedKeyset() {
        Set<K> keySet = new LinkedHashSet<>();
        for (ChangeType changeType : ChangeType.values()) {
            keySet.addAll(get(changeType).keySet());
        }
        return keySet;
    }

    public enum ChangeType {
        CREATED,
        UPDATED,
        DELETED
    }

    public static class Change<C> {
        private final C changed;
        private final ChangeType type;

        public Change(C changed, ChangeType type) {
            this.changed = changed;
            this.type = type;
        }

        public C getChanged() {
            return changed;
        }

        public ChangeType getType() {
            return type;
        }
    }
}
