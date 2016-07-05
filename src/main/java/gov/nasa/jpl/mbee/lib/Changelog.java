package gov.nasa.jpl.mbee.lib;

import gov.nasa.jpl.mbee.lib.function.BiPredicate;

import java.util.*;

/**
 * Created by igomes on 6/30/16.
 */
public class Changelog<K, V> extends HashMap<Changelog.ChangeType, Map<K, V>> {

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

    public Changelog<K, V> and(Changelog<K, V> secondChangelog) {
        Changelog<K, V> combinedChangelog = new Changelog<>();
        for (ChangeType changeType : ChangeType.values()) {
            Map<K, V> map = combinedChangelog.get(changeType);
            map.putAll(this.get(changeType));
            map.putAll(secondChangelog.get(changeType));
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
