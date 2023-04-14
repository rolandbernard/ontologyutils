package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import org.junit.jupiter.api.Test;

public class MapOfSetsTest {
    @Test
    public void emptyMapHasEmptyEntrySet() {
        final var map = new MapOfSets<Integer, Void>();
        assertEquals(Set.of(), map.entrySet());
    }

    @Test
    public void emptyMapContainsNoKey() {
        final var map = new MapOfSets<Integer, Void>();
        assertFalse(map.containsKey(Set.of()));
        assertFalse(map.containsKey(Set.of(1)));
        assertFalse(map.containsKey(Set.of(2, 3)));
        assertFalse(map.containsKey(Set.of(4, 5, 6)));
    }

    @Test
    public void getForNonContainIsNull() {
        final var map = new MapOfSets<Integer, Void>();
        assertNull(map.get(Set.of()));
        assertNull(map.get(Set.of(1)));
        assertNull(map.get(Set.of(2, 3)));
        assertNull(map.get(Set.of(4, 5, 6)));
    }

    @Test
    public void containsKeyAfterPut() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsKey(Set.of()));
        assertTrue(map.containsKey(Set.of(2)));
        assertTrue(map.containsKey(Set.of(1, 3)));
        assertTrue(map.containsKey(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsNoKeyNotPut() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertFalse(map.containsKey(Set.of(1)));
        assertFalse(map.containsKey(Set.of(2, 3)));
        assertFalse(map.containsKey(Set.of(4, 5, 6)));
    }

    @Test
    public void getReturnsValuesPut() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(0, map.get(Set.of()));
        assertEquals(1, map.get(Set.of(2)));
        assertEquals(2, map.get(Set.of(1, 3)));
        assertEquals(3, map.get(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void getReturnsNullForNotPut() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertNull(map.get(Set.of(1)));
        assertNull(map.get(Set.of(2, 3)));
        assertNull(map.get(Set.of(4, 5, 6)));
    }

    @Test
    public void entrySetAfterPut() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(
                Set.of(new SimpleEntry<>(Set.of(), 0), new SimpleEntry<>(Set.of(2), 1),
                        new SimpleEntry<>(Set.of(1, 3), 2), new SimpleEntry<>(Set.of(1, 4, 5, 6), 3)),
                map.entrySet());
    }

    @Test
    public void containsNoKeyAfterRemove() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        map.remove(Set.of(1, 3));
        assertTrue(map.containsKey(Set.of()));
        assertTrue(map.containsKey(Set.of(2)));
        assertFalse(map.containsKey(Set.of(1, 3)));
        assertTrue(map.containsKey(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void getReturnsNullAfterRemove() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        map.remove(Set.of(1, 3));
        map.remove(Set.of(2));
        map.remove(Set.of());
        assertNull(map.get(Set.of()));
        assertNull(map.get(Set.of(2)));
        assertNull(map.get(Set.of(1, 3)));
        assertEquals(3, map.get(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void sizeReturnsSize() {
        final var map = new MapOfSets<Integer, Integer>();
        assertEquals(0, map.size());
        map.put(Set.of(), 0);
        assertEquals(1, map.size());
        map.put(Set.of(2), 1);
        assertEquals(2, map.size());
        map.put(Set.of(1, 3), 2);
        assertEquals(3, map.size());
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(4, map.size());
        map.remove(Set.of(1, 3));
        assertEquals(3, map.size());
        map.put(Set.of(), 5);
        assertEquals(3, map.size());
    }

    @Test
    public void clearRemovesAllEntries() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(4, map.size());
        map.clear();
        assertEquals(0, map.size());
        assertFalse(map.containsKey(Set.of()));
        assertFalse(map.containsKey(Set.of(2)));
        assertFalse(map.containsKey(Set.of(1, 3)));
        assertFalse(map.containsKey(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSubsetFindsKeys() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsSubset(Set.of(2)));
        assertTrue(map.containsSubset(Set.of(1, 3)));
        assertTrue(map.containsSubset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSubsetNonSubsets() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertFalse(map.containsSubset(Set.of(1)));
        assertFalse(map.containsSubset(Set.of(3)));
        assertFalse(map.containsSubset(Set.of(1, 4, 5)));
        assertFalse(map.containsSubset(Set.of(1, 4, 6)));
        assertFalse(map.containsSubset(Set.of(1, 5, 6)));
        assertFalse(map.containsSubset(Set.of(4, 5, 6)));
        assertFalse(map.containsSubset(Set.of(4, 5)));
        assertFalse(map.containsSubset(Set.of(4, 6)));
        assertFalse(map.containsSubset(Set.of(5, 6)));
        assertFalse(map.containsSubset(Set.of(5)));
        assertFalse(map.containsSubset(Set.of(6)));
        assertFalse(map.containsSubset(Set.of(3, 4, 5, 6)));
        assertFalse(map.containsSubset(Set.of(1, 4, 5, 7)));
        assertFalse(map.containsSubset(Set.of(1, 4, 6, 7)));
    }

    @Test
    public void containsSubsetFindsSubsets() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsSubset(Set.of(2, 7)));
        assertTrue(map.containsSubset(Set.of(3, 2)));
        assertTrue(map.containsSubset(Set.of(1, 5, 6, 4, 8, 9)));
        assertTrue(map.containsSubset(Set.of(3, 5, 4, 7, 1)));
        assertTrue(map.containsSubset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void containsSupersetFindsKeys() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsSuperset(Set.of(2)));
        assertTrue(map.containsSuperset(Set.of(1, 3)));
        assertTrue(map.containsSuperset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSupersetNonSupersets() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertFalse(map.containsSuperset(Set.of(3, 4, 5, 6)));
        assertFalse(map.containsSuperset(Set.of(1, 4, 5, 7)));
        assertFalse(map.containsSuperset(Set.of(1, 4, 6, 7)));
        assertFalse(map.containsSuperset(Set.of(2, 7)));
        assertFalse(map.containsSuperset(Set.of(3, 2)));
        assertFalse(map.containsSuperset(Set.of(1, 5, 6, 4, 8, 9)));
        assertFalse(map.containsSuperset(Set.of(3, 5, 4, 7, 1)));
        assertFalse(map.containsSuperset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void containsSupersetFindsSupersets() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsSuperset(Set.of(1)));
        assertTrue(map.containsSuperset(Set.of(3)));
        assertTrue(map.containsSuperset(Set.of(1, 4, 5)));
        assertTrue(map.containsSuperset(Set.of(1, 4, 6)));
        assertTrue(map.containsSuperset(Set.of(1, 5, 6)));
        assertTrue(map.containsSuperset(Set.of(4, 5, 6)));
        assertTrue(map.containsSuperset(Set.of(4, 5)));
        assertTrue(map.containsSuperset(Set.of(4, 6)));
        assertTrue(map.containsSuperset(Set.of(5, 6)));
        assertTrue(map.containsSuperset(Set.of(5)));
        assertTrue(map.containsSuperset(Set.of(6)));
    }

    @Test
    public void entrySetForSubsets() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(
                Set.of(new SimpleEntry<>(Set.of(), 0), new SimpleEntry<>(Set.of(1, 3), 2),
                        new SimpleEntry<>(Set.of(1, 4, 5, 6), 3)),
                map.entrySetForSubsets(Set.of(1, 4, 5, 6, 3)));
    }

    @Test
    public void entrySetForSupersets() {
        final var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(), 0);
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(
                Set.of(new SimpleEntry<>(Set.of(1, 3), 2),
                        new SimpleEntry<>(Set.of(1, 4, 5, 6), 3)),
                map.entrySetForSupersets(Set.of(1)));
        assertEquals(
                Set.of(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3)),
                map.entrySetForSupersets(Set.of(1, 4)));
    }
}
