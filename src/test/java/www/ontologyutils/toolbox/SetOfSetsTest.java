package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

public class SetOfSetsTest {
    @Test
    public void emptySetHasEmpty() {
        var set = new SetOfSets<Integer>();
        assertEquals(List.of(), Utils.toList(set.stream()));
    }

    @Test
    public void emptySetContainsNoKey() {
        var set = new SetOfSets<Integer>();
        assertFalse(set.contains(Set.of()));
        assertFalse(set.contains(Set.of(1)));
        assertFalse(set.contains(Set.of(2, 3)));
        assertFalse(set.contains(Set.of(4, 5, 6)));
    }

    @Test
    public void containsAfterAdd() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.contains(Set.of()));
        assertTrue(set.contains(Set.of(2)));
        assertTrue(set.contains(Set.of(1, 3)));
        assertTrue(set.contains(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsNoKeyNotAdded() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertFalse(set.contains(Set.of(1)));
        assertFalse(set.contains(Set.of(2, 3)));
        assertFalse(set.contains(Set.of(4, 5, 6)));
    }

    @Test
    public void streamAfterAdd() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(
                Set.of(Set.of(), Set.of(2), Set.of(1, 3), Set.of(1, 4, 5, 6)),
                Utils.toSet(set.stream()));
    }

    @Test
    public void containsNoKeyAfterRemove() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        set.remove(Set.of(1, 3));
        assertTrue(set.contains(Set.of()));
        assertTrue(set.contains(Set.of(2)));
        assertFalse(set.contains(Set.of(1, 3)));
        assertTrue(set.contains(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void sizeReturnsSize() {
        var set = new SetOfSets<Integer>();
        assertEquals(0, set.size());
        set.add(Set.of());
        assertEquals(1, set.size());
        set.add(Set.of(2));
        assertEquals(2, set.size());
        set.add(Set.of(1, 3));
        assertEquals(3, set.size());
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(4, set.size());
        set.remove(Set.of(1, 3));
        assertEquals(3, set.size());
        set.add(Set.of());
        assertEquals(3, set.size());
    }

    @Test
    public void clearRemovesAllEntries() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(4, set.size());
        set.clear();
        assertEquals(0, set.size());
        assertFalse(set.contains(Set.of()));
        assertFalse(set.contains(Set.of(2)));
        assertFalse(set.contains(Set.of(1, 3)));
        assertFalse(set.contains(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSubsetFindsKeys() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsSubset(Set.of(2)));
        assertTrue(set.containsSubset(Set.of(1, 3)));
        assertTrue(set.containsSubset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSubsetNonSubsets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertFalse(set.containsSubset(Set.of(1)));
        assertFalse(set.containsSubset(Set.of(3)));
        assertFalse(set.containsSubset(Set.of(1, 4, 5)));
        assertFalse(set.containsSubset(Set.of(1, 4, 6)));
        assertFalse(set.containsSubset(Set.of(1, 5, 6)));
        assertFalse(set.containsSubset(Set.of(4, 5, 6)));
        assertFalse(set.containsSubset(Set.of(4, 5)));
        assertFalse(set.containsSubset(Set.of(4, 6)));
        assertFalse(set.containsSubset(Set.of(5, 6)));
        assertFalse(set.containsSubset(Set.of(5)));
        assertFalse(set.containsSubset(Set.of(6)));
        assertFalse(set.containsSubset(Set.of(3, 4, 5, 6)));
        assertFalse(set.containsSubset(Set.of(1, 4, 5, 7)));
        assertFalse(set.containsSubset(Set.of(1, 4, 6, 7)));
    }

    @Test
    public void containsSubsetFindsSubsets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsSubset(Set.of(2, 7)));
        assertTrue(set.containsSubset(Set.of(3, 2)));
        assertTrue(set.containsSubset(Set.of(1, 5, 6, 4, 8, 9)));
        assertTrue(set.containsSubset(Set.of(3, 5, 4, 7, 1)));
        assertTrue(set.containsSubset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void containsDisjointNonDisjoint() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertFalse(set.containsDisjoint(Set.of(1, 2)));
        assertFalse(set.containsDisjoint(Set.of(3, 2, 5)));
        assertFalse(set.containsDisjoint(Set.of(1, 2, 5)));
        assertFalse(set.containsDisjoint(Set.of(2, 4, 3)));
        assertFalse(set.containsDisjoint(Set.of(2, 3, 6)));
        assertFalse(set.containsDisjoint(Set.of(2, 3, 6)));
    }

    @Test
    public void containsDisjointFindsDisjoint() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsDisjoint(Set.of(2, 5)));
        assertTrue(set.containsDisjoint(Set.of(3, 2)));
        assertTrue(set.containsDisjoint(Set.of(1, 5, 6, 4, 8, 9)));
        assertTrue(set.containsDisjoint(Set.of(3, 5, 4, 7, 1)));
        assertTrue(set.containsDisjoint(Set.of(1, 5, 4, 6, 3)));
        assertTrue(set.containsDisjoint(Set.of(2, 4, 5, 6)));
        assertTrue(set.containsDisjoint(Set.of(2, 3)));
        assertTrue(set.containsDisjoint(Set.of(1, 3, 4, 5, 6)));
    }

    @Test
    public void containsSupersetFindsKeys() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsSuperset(Set.of(2)));
        assertTrue(set.containsSuperset(Set.of(1, 3)));
        assertTrue(set.containsSuperset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSupersetNonSupersets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertFalse(set.containsSuperset(Set.of(3, 4, 5, 6)));
        assertFalse(set.containsSuperset(Set.of(1, 4, 5, 7)));
        assertFalse(set.containsSuperset(Set.of(1, 4, 6, 7)));
        assertFalse(set.containsSuperset(Set.of(2, 7)));
        assertFalse(set.containsSuperset(Set.of(3, 2)));
        assertFalse(set.containsSuperset(Set.of(1, 5, 6, 4, 8, 9)));
        assertFalse(set.containsSuperset(Set.of(3, 5, 4, 7, 1)));
        assertFalse(set.containsSuperset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void containsSupersetFindsSupersets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsSuperset(Set.of(1)));
        assertTrue(set.containsSuperset(Set.of(3)));
        assertTrue(set.containsSuperset(Set.of(1, 4, 5)));
        assertTrue(set.containsSuperset(Set.of(1, 4, 6)));
        assertTrue(set.containsSuperset(Set.of(1, 5, 6)));
        assertTrue(set.containsSuperset(Set.of(4, 5, 6)));
        assertTrue(set.containsSuperset(Set.of(4, 5)));
        assertTrue(set.containsSuperset(Set.of(4, 6)));
        assertTrue(set.containsSuperset(Set.of(5, 6)));
        assertTrue(set.containsSuperset(Set.of(5)));
        assertTrue(set.containsSuperset(Set.of(6)));
    }

    @Test
    public void subsets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(
                Set.of(Set.of(), Set.of(1, 3), Set.of(1, 4, 5, 6)),
                Utils.toSet(set.subsets(Set.of(1, 4, 5, 6, 3))));
    }

    @Test
    public void entrySetForSupersets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(
                Set.of(Set.of(1, 3), Set.of(1, 4, 5, 6)),
                Utils.toSet(set.supersets(Set.of(1))));
        assertEquals(
                Set.of(Set.of(1, 4, 5, 6)),
                Utils.toSet(set.supersets(Set.of(1, 4))));
    }

    @Test
    public void getSubsetFindsKeys() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(Set.of(2), set.getSubset(Set.of(2)));
        assertEquals(Set.of(1, 3), set.getSubset(Set.of(1, 3)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSubset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void getSubsetNonSubsets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertNull(set.getSubset(Set.of(1)));
        assertNull(set.getSubset(Set.of(3)));
        assertNull(set.getSubset(Set.of(1, 4, 5)));
        assertNull(set.getSubset(Set.of(1, 4, 6)));
        assertNull(set.getSubset(Set.of(1, 5, 6)));
        assertNull(set.getSubset(Set.of(4, 5, 6)));
        assertNull(set.getSubset(Set.of(4, 5)));
        assertNull(set.getSubset(Set.of(4, 6)));
        assertNull(set.getSubset(Set.of(5, 6)));
        assertNull(set.getSubset(Set.of(5)));
        assertNull(set.getSubset(Set.of(6)));
        assertNull(set.getSubset(Set.of(3, 4, 5, 6)));
        assertNull(set.getSubset(Set.of(1, 4, 5, 7)));
        assertNull(set.getSubset(Set.of(1, 4, 6, 7)));
    }

    @Test
    public void getSubsetFindsSubsets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(Set.of(2), set.getSubset(Set.of(2, 7)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSubset(Set.of(1, 5, 6, 4, 8, 9)));
        assertEquals(Set.of(1, 3), set.getSubset(Set.of(3, 5, 4, 7, 1)));
        assertNotNull(set.getSubset(Set.of(3, 2)));
        assertNotNull(set.getSubset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void getDisjointNonDisjoint() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertNull(set.getDisjoint(Set.of(1, 2)));
        assertNull(set.getDisjoint(Set.of(3, 2, 5)));
        assertNull(set.getDisjoint(Set.of(1, 2, 5)));
        assertNull(set.getDisjoint(Set.of(2, 4, 3)));
        assertNull(set.getDisjoint(Set.of(2, 3, 6)));
        assertNull(set.getDisjoint(Set.of(2, 3, 6)));
    }

    @Test
    public void getDisjointFindsDisjoint() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(Set.of(1, 3), set.getDisjoint(Set.of(2, 5)));
        assertEquals(Set.of(1, 4, 5, 6), set.getDisjoint(Set.of(3, 2)));
        assertEquals(Set.of(2), set.getDisjoint(Set.of(1, 5, 6, 4, 8, 9)));
        assertEquals(Set.of(2), set.getDisjoint(Set.of(3, 5, 4, 7, 1)));
        assertEquals(Set.of(2), set.getDisjoint(Set.of(1, 5, 4, 6, 3)));
        assertEquals(Set.of(1, 3), set.getDisjoint(Set.of(2, 4, 5, 6)));
        assertEquals(Set.of(2), set.getDisjoint(Set.of(1, 3, 4, 5, 6)));
        assertNotNull(set.getDisjoint(Set.of(2)));
        assertNotNull(set.getDisjoint(Set.of(3)));
        assertNotNull(set.getDisjoint(Set.of(4, 5, 6)));
    }

    @Test
    public void getSupersetFindsKeys() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(Set.of(2), set.getSuperset(Set.of(2)));
        assertEquals(Set.of(1, 3), set.getSuperset(Set.of(1, 3)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void getSupersetNonSupersets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertNull(set.getSuperset(Set.of(3, 4, 5, 6)));
        assertNull(set.getSuperset(Set.of(1, 4, 5, 7)));
        assertNull(set.getSuperset(Set.of(1, 4, 6, 7)));
        assertNull(set.getSuperset(Set.of(2, 7)));
        assertNull(set.getSuperset(Set.of(3, 2)));
        assertNull(set.getSuperset(Set.of(1, 5, 6, 4, 8, 9)));
        assertNull(set.getSuperset(Set.of(3, 5, 4, 7, 1)));
        assertNull(set.getSuperset(Set.of(1, 5, 4, 6, 3)));
    }

    @Test
    public void getSupersetFindsSupersets() {
        var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(Set.of(1, 3), set.getSuperset(Set.of(3)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(1, 4, 5)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(1, 4, 6)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(1, 5, 6)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(4, 5, 6)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(4, 5)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(4, 6)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(5, 6)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(5)));
        assertEquals(Set.of(1, 4, 5, 6), set.getSuperset(Set.of(6)));
        assertNotNull(set.getSuperset(Set.of(1)));
    }
}
