package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import org.junit.jupiter.api.Test;

public class MapOfSetsTest {
    @Test
    public void emptyMapHasEmptyEntrySet() {
        var map = new MapOfSets<Integer, Void>();
        assertEquals(Set.of(), map.entrySet());
    }

    @Test
    public void emptyMapContainsNoKey() {
        var map = new MapOfSets<Integer, Void>();
        assertFalse(map.containsKey(Set.of()));
        assertFalse(map.containsKey(Set.of(1)));
        assertFalse(map.containsKey(Set.of(2, 3)));
        assertFalse(map.containsKey(Set.of(4, 5, 6)));
    }

    @Test
    public void getForNonContainIsNull() {
        var map = new MapOfSets<Integer, Void>();
        assertNull(map.get(Set.of()));
        assertNull(map.get(Set.of(1)));
        assertNull(map.get(Set.of(2, 3)));
        assertNull(map.get(Set.of(4, 5, 6)));
    }

    @Test
    public void containsKeyAfterPut() {
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsSubset(Set.of(2)));
        assertTrue(map.containsSubset(Set.of(1, 3)));
        assertTrue(map.containsSubset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSubsetNonSubsets() {
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
    public void containsDisjointNonDisjoint() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertFalse(map.containsDisjoint(Set.of(1, 2)));
        assertFalse(map.containsDisjoint(Set.of(3, 2, 5)));
        assertFalse(map.containsDisjoint(Set.of(1, 2, 5)));
        assertFalse(map.containsDisjoint(Set.of(2, 4, 3)));
        assertFalse(map.containsDisjoint(Set.of(2, 3, 6)));
        assertFalse(map.containsDisjoint(Set.of(2, 3, 6)));
    }

    @Test
    public void containsDisjointFindsDisjoint() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsDisjoint(Set.of(2, 5)));
        assertTrue(map.containsDisjoint(Set.of(3, 2)));
        assertTrue(map.containsDisjoint(Set.of(1, 5, 6, 4, 8, 9)));
        assertTrue(map.containsDisjoint(Set.of(3, 5, 4, 7, 1)));
        assertTrue(map.containsDisjoint(Set.of(1, 5, 4, 6, 3)));
        assertTrue(map.containsDisjoint(Set.of(2, 4, 5, 6)));
        assertTrue(map.containsDisjoint(Set.of(2, 3)));
        assertTrue(map.containsDisjoint(Set.of(1, 3, 4, 5, 6)));
    }

    @Test
    public void containsSupersetFindsKeys() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertTrue(map.containsSuperset(Set.of(2)));
        assertTrue(map.containsSuperset(Set.of(1, 3)));
        assertTrue(map.containsSuperset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSupersetNonSupersets() {
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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
        var map = new MapOfSets<Integer, Integer>();
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

    @Test
    public void getSubsetFindsKeys() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getSubset(Set.of(2)));
        assertEquals(new SimpleEntry<>(Set.of(1, 3), 2), map.getSubset(Set.of(1, 3)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSubset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void getSubsetNonSubsets() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertNull(map.getSubset(Set.of(1)));
        assertNull(map.getSubset(Set.of(3)));
        assertNull(map.getSubset(Set.of(1, 4, 5)));
        assertNull(map.getSubset(Set.of(1, 4, 6)));
        assertNull(map.getSubset(Set.of(1, 5, 6)));
        assertNull(map.getSubset(Set.of(4, 5, 6)));
        assertNull(map.getSubset(Set.of(4, 5)));
        assertNull(map.getSubset(Set.of(4, 6)));
        assertNull(map.getSubset(Set.of(5, 6)));
        assertNull(map.getSubset(Set.of(5)));
        assertNull(map.getSubset(Set.of(6)));
        assertNull(map.getSubset(Set.of(3, 4, 5, 6)));
        assertNull(map.getSubset(Set.of(1, 4, 5, 7)));
        assertNull(map.getSubset(Set.of(1, 4, 6, 7)));
    }

    @Test
    public void getSubsetFindsSubsets() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getSubset(Set.of(2, 7)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSubset(Set.of(1, 5, 6, 4, 8, 9)));
        assertEquals(new SimpleEntry<>(Set.of(1, 3), 2), map.getSubset(Set.of(3, 5, 4, 7, 1)));
        assertNotNull(map.getSubset(Set.of(3, 2)));
        assertNotNull(map.getSubset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void getDisjointNonDisjoint() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertNull(map.getDisjoint(Set.of(1, 2)));
        assertNull(map.getDisjoint(Set.of(3, 2, 5)));
        assertNull(map.getDisjoint(Set.of(1, 2, 5)));
        assertNull(map.getDisjoint(Set.of(2, 4, 3)));
        assertNull(map.getDisjoint(Set.of(2, 3, 6)));
        assertNull(map.getDisjoint(Set.of(2, 3, 6)));
    }

    @Test
    public void getDisjointFindsDisjoint() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(new SimpleEntry<>(Set.of(1, 3), 2), map.getDisjoint(Set.of(2, 5)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getDisjoint(Set.of(3, 2)));
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getDisjoint(Set.of(1, 5, 6, 4, 8, 9)));
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getDisjoint(Set.of(3, 5, 4, 7, 1)));
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getDisjoint(Set.of(1, 5, 4, 6, 3)));
        assertEquals(new SimpleEntry<>(Set.of(1, 3), 2), map.getDisjoint(Set.of(2, 4, 5, 6)));
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getDisjoint(Set.of(1, 3, 4, 5, 6)));
        assertNotNull(map.getDisjoint(Set.of(2)));
        assertNotNull(map.getDisjoint(Set.of(3)));
        assertNotNull(map.getDisjoint(Set.of(4, 5, 6)));
    }

    @Test
    public void getSupersetFindsKeys() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(new SimpleEntry<>(Set.of(2), 1), map.getSuperset(Set.of(2)));
        assertEquals(new SimpleEntry<>(Set.of(1, 3), 2), map.getSuperset(Set.of(1, 3)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void getSupersetNonSupersets() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertNull(map.getSuperset(Set.of(3, 4, 5, 6)));
        assertNull(map.getSuperset(Set.of(1, 4, 5, 7)));
        assertNull(map.getSuperset(Set.of(1, 4, 6, 7)));
        assertNull(map.getSuperset(Set.of(2, 7)));
        assertNull(map.getSuperset(Set.of(3, 2)));
        assertNull(map.getSuperset(Set.of(1, 5, 6, 4, 8, 9)));
        assertNull(map.getSuperset(Set.of(3, 5, 4, 7, 1)));
        assertNull(map.getSuperset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void getSupersetFindsSupersets() {
        var map = new MapOfSets<Integer, Integer>();
        map.put(Set.of(2), 1);
        map.put(Set.of(1, 3), 2);
        map.put(Set.of(1, 4, 5, 6), 3);
        assertEquals(new SimpleEntry<>(Set.of(1, 3), 2), map.getSuperset(Set.of(3)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(1, 4, 5)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(1, 4, 6)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(1, 5, 6)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(4, 5, 6)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(4, 5)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(4, 6)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(5, 6)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(5)));
        assertEquals(new SimpleEntry<>(Set.of(1, 4, 5, 6), 3), map.getSuperset(Set.of(6)));
        assertNotNull(map.getSuperset(Set.of(1)));
    }
}
