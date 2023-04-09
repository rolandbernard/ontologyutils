package www.ontologyutils.toolbox;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class implements a set of sets that can be queried based on subset and
 * superset relationships. This is a simple convenience wrapper around
 * MapOfSets.
 */
public class SetOfSets<K extends Comparable<? super K>> extends AbstractSet<Set<K>> {
    private final MapOfSets<K, Boolean> map;

    public SetOfSets() {
        map = new MapOfSets<>();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(final Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean add(final Set<K> key) {
        return map.put(key, true) == null;
    }

    @Override
    public boolean remove(final Object key) {
        return map.remove(key) != null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Iterator<Set<K>> iterator() {
        return map.entrySet().stream().map(entry -> entry.getKey()).iterator();
    }

    public boolean containsSubset(final Set<K> key) {
        return map.containsSubset(key);
    }

    public boolean containsSuperset(final Set<K> key) {
        return map.containsSuperset(key);
    }

    public Stream<Set<K>> subsets(final Set<K> key) {
        return map.entrySetForSubsets(key).stream().map(entry -> entry.getKey());
    }

    public Stream<Set<K>> supersets(final Set<K> key) {
        return map.entrySetForSupersets(key).stream().map(entry -> entry.getKey());
    }
}
