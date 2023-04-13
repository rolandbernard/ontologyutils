package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class SetOfSetsTest {
    @Test
    public void emptySetHasEmpty() {
        final var set = new SetOfSets<Integer>();
        assertEquals(List.of(), set.stream().toList());
    }

    @Test
    public void emptySetContainsNoKey() {
        final var set = new SetOfSets<Integer>();
        assertFalse(set.contains(Set.of()));
        assertFalse(set.contains(Set.of(1)));
        assertFalse(set.contains(Set.of(2, 3)));
        assertFalse(set.contains(Set.of(4, 5, 6)));
    }

    @Test
    public void containsAfterAdd() {
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(
                Set.of(Set.of(), Set.of(2), Set.of(1, 3), Set.of(1, 4, 5, 6)),
                set.stream().collect(Collectors.toSet()));
    }

    @Test
    public void containsNoKeyAfterRemove() {
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsSubset(Set.of(2)));
        assertTrue(set.containsSubset(Set.of(1, 3)));
        assertTrue(set.containsSubset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSubsetNonSubsets() {
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
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
    public void containsSupersetFindsKeys() {
        final var set = new SetOfSets<Integer>();
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertTrue(set.containsSuperset(Set.of(2)));
        assertTrue(set.containsSuperset(Set.of(1, 3)));
        assertTrue(set.containsSuperset(Set.of(1, 4, 5, 6)));
    }

    @Test
    public void containsSupersetNonSupersets() {
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
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
        final var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(
                Set.of(Set.of(), Set.of(1, 3), Set.of(1, 4, 5, 6)),
                set.subsets(Set.of(1, 4, 5, 6, 3)).collect(Collectors.toSet()));
    }

    @Test
    public void entrySetForSupersets() {
        final var set = new SetOfSets<Integer>();
        set.add(Set.of());
        set.add(Set.of(2));
        set.add(Set.of(1, 3));
        set.add(Set.of(1, 4, 5, 6));
        assertEquals(
                Set.of(Set.of(1, 3), Set.of(1, 4, 5, 6)),
                set.supersets(Set.of(1)).collect(Collectors.toSet()));
        assertEquals(
                Set.of(Set.of(1, 4, 5, 6)),
                set.supersets(Set.of(1, 4)).collect(Collectors.toSet()));
    }
}
