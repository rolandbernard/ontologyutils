package www.ontologyutils.toolbox;

import java.util.*;

/**
 * This class implements a map of sets that can be queried based on subset and
 * superset relationships. This is a useful data structure for some caches.
 */
public class MapOfSets<K extends Comparable<? super K>, V> extends AbstractMap<Set<K>, V> {
    private static class TrieNode<K, V> {
        private Map<K, TrieNode<K, V>> children;
        public int size;
        public V data;

        public void clear() {
            children.clear();
            size = 0;
            data = null;
        }

        public Set<Entry<K, TrieNode<K, V>>> children() {
            if (children != null) {
                return children.entrySet();
            } else {
                return Set.of();
            }
        }

        public void removeChild(final Object key) {
            if (children != null) {
                if (children.size() == 1) {
                    children = null;
                } else {
                    children.remove(key);
                }
            }
        }

        public TrieNode<K, V> getChild(final Object key) {
            if (children != null) {
                return children.get(key);
            } else {
                return null;
            }
        }

        public TrieNode<K, V> getOrCreateChild(final K key) {
            if (children == null) {
                children = new HashMap<>(1);
            }
            if (!children.containsKey(key)) {
                children.put(key, new TrieNode<>());
            }
            return children.get(key);
        }
    }

    private final TrieNode<K, V> root;

    public MapOfSets() {
        root = new TrieNode<>();
    }

    @Override
    public void clear() {
        root.clear();
    }

    @Override
    public V get(final Object key) {
        if (key instanceof Set) {
            final Set<?> set = (Set<?>) key;
            final Iterator<?> sorted = set.stream().sorted().iterator();
            TrieNode<K, V> current = root;
            while (current != null && sorted.hasNext()) {
                current = current.getChild(sorted.next());
            }
            return current != null ? current.data : null;
        } else {
            return null;
        }
    }

    @Override
    public V put(final Set<K> key, final V value) {
        final Iterator<K> sorted = key.stream().sorted().iterator();
        final List<TrieNode<K, V>> path = new ArrayList<>();
        TrieNode<K, V> current = root;
        while (sorted.hasNext()) {
            path.add(current);
            current = current.getOrCreateChild(sorted.next());
        }
        final V oldData = current.data;
        current.data = value;
        if (oldData == null) {
            current.size += 1;
            for (final TrieNode<K, V> node : path) {
                node.size += 1;
            }
        }
        return oldData;
    }

    @Override
    public V remove(final Object key) {
        if (key instanceof Set) {
            final Set<?> set = (Set<?>) key;
            final Iterator<?> sorted = set.stream().sorted().iterator();
            final List<TrieNode<K, V>> path = new ArrayList<>();
            TrieNode<K, V> current = root;
            while (current != null && sorted.hasNext()) {
                path.add(current);
                current = current.getChild(sorted.next());
            }
            if (current != null && current.data != null) {
                final V data = current.data;
                current.size -= 1;
                current.data = null;
                Collections.reverse(path);
                for (final TrieNode<K, V> node : path) {
                    if (current.size == 0) {
                        node.removeChild(key);
                    }
                    node.size -= 1;
                    current = node;
                }
                return data;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return root.size;
    }

    private void entrySetHelper(final TrieNode<K, V> node, final List<K> path, final Set<Entry<Set<K>, V>> into) {
        if (node.data != null) {
            into.add(new SimpleEntry<>(Set.copyOf(path), node.data));
        }
        for (final Entry<K, TrieNode<K, V>> entry : node.children()) {
            path.add(entry.getKey());
            entrySetHelper(entry.getValue(), path, into);
            path.remove(path.size() - 1);
        }
    }

    @Override
    public Set<Entry<Set<K>, V>> entrySet() {
        final Set<Entry<Set<K>, V>> result = new HashSet<>();
        entrySetHelper(root, new ArrayList<>(), result);
        return result;
    }

    private boolean containsSubsetHelper(final TrieNode<K, V> node, final Set<K> key) {
        if (node.data != null) {
            return true;
        } else {
            for (final Entry<K, TrieNode<K, V>> entry : node.children()) {
                if (key.contains(entry.getKey()) && containsSubsetHelper(entry.getValue(), key)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean containsSubset(final Set<K> key) {
        return containsSubsetHelper(root, key);
    }

    private boolean containsSupersetHelper(final TrieNode<K, V> node, final List<K> key, final int depth) {
        if (depth == key.size() && node.data != null) {
            return true;
        } else {
            for (final Entry<K, TrieNode<K, V>> entry : node.children()) {
                final int cmp = depth < key.size() ? entry.getKey().compareTo(key.get(depth)) : -1;
                if (cmp <= 0) {
                    if (containsSupersetHelper(entry.getValue(), key, cmp == 0 ? depth + 1 : depth)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean containsSuperset(final Set<K> key) {
        final List<K> sorted = key.stream().sorted().toList();
        return containsSupersetHelper(root, sorted, 0);
    }

    private void entrySetForSubsetHelper(final TrieNode<K, V> node, final Set<K> key, final List<K> path,
            final Set<Entry<Set<K>, V>> into) {
        if (node.data != null) {
            into.add(new SimpleEntry<>(Set.copyOf(path), node.data));
        }
        for (final Entry<K, TrieNode<K, V>> entry : node.children()) {
            if (key.contains(entry.getKey())) {
                path.add(entry.getKey());
                entrySetForSubsetHelper(entry.getValue(), key, path, into);
                path.remove(path.size() - 1);
            }
        }
    }

    public Set<Entry<Set<K>, V>> entrySetForSubsets(final Set<K> key) {
        final Set<Entry<Set<K>, V>> result = new HashSet<>();
        entrySetForSubsetHelper(root, key, new ArrayList<>(), result);
        return result;
    }

    private void entrySetForSupersetHelper(final TrieNode<K, V> node, final List<K> key, final int depth,
            final List<K> path, final Set<Entry<Set<K>, V>> into) {
        if (depth == key.size() && node.data != null) {
            into.add(new SimpleEntry<>(Set.copyOf(path), node.data));
        }
        for (final Entry<K, TrieNode<K, V>> entry : node.children()) {
            final int cmp = depth < key.size() ? entry.getKey().compareTo(key.get(depth)) : -1;
            if (cmp <= 0) {
                path.add(entry.getKey());
                entrySetForSupersetHelper(entry.getValue(), key, cmp == 0 ? depth + 1 : depth, path, into);
                path.remove(path.size() - 1);
            }
        }
    }

    public Set<Entry<Set<K>, V>> entrySetForSupersets(final Set<K> key) {
        final List<K> sorted = key.stream().sorted().toList();
        final Set<Entry<Set<K>, V>> result = new HashSet<>();
        entrySetForSupersetHelper(root, sorted, 0, new ArrayList<>(), result);
        return result;
    }
}
