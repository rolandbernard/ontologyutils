package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public class PreorderCacheTest {
    private int numCalls;

    private boolean compare(int a, int b) {
        numCalls += 1;
        return a < b;
    }

    @Test
    public void computeIfAbsentSimple() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(2, 3, this::compare));
        assertTrue(cache.computeIfAbsent(3, 4, this::compare));
        assertTrue(cache.computeIfAbsent(4, 5, this::compare));
        assertEquals(4, numCalls);
        assertTrue(cache.computeIfAbsent(1, 1, this::compare));
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(1, 3, this::compare));
        assertTrue(cache.computeIfAbsent(1, 4, this::compare));
        assertTrue(cache.computeIfAbsent(1, 5, this::compare));
        assertTrue(cache.computeIfAbsent(2, 2, this::compare));
        assertTrue(cache.computeIfAbsent(2, 3, this::compare));
        assertTrue(cache.computeIfAbsent(2, 4, this::compare));
        assertTrue(cache.computeIfAbsent(2, 5, this::compare));
        assertTrue(cache.computeIfAbsent(3, 3, this::compare));
        assertTrue(cache.computeIfAbsent(3, 4, this::compare));
        assertTrue(cache.computeIfAbsent(3, 5, this::compare));
        assertTrue(cache.computeIfAbsent(4, 4, this::compare));
        assertTrue(cache.computeIfAbsent(4, 5, this::compare));
        assertTrue(cache.computeIfAbsent(5, 5, this::compare));
        assertEquals(4, numCalls);
    }

    @Test
    public void computeIfAbsentPositive1() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(4, 5, this::compare));
        assertTrue(cache.computeIfAbsent(2, 4, this::compare));
        assertEquals(3, numCalls);
        assertTrue(cache.computeIfAbsent(1, 4, this::compare));
        assertTrue(cache.computeIfAbsent(1, 5, this::compare));
        assertTrue(cache.computeIfAbsent(2, 5, this::compare));
        assertEquals(3, numCalls);
    }

    @Test
    public void computeIfAbsentPositive2() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(3, 4, this::compare));
        assertTrue(cache.computeIfAbsent(5, 6, this::compare));
        assertTrue(cache.computeIfAbsent(4, 5, this::compare));
        assertTrue(cache.computeIfAbsent(2, 3, this::compare));
        assertEquals(5, numCalls);
        assertTrue(cache.computeIfAbsent(1, 3, this::compare));
        assertTrue(cache.computeIfAbsent(1, 4, this::compare));
        assertTrue(cache.computeIfAbsent(1, 5, this::compare));
        assertTrue(cache.computeIfAbsent(1, 6, this::compare));
        assertTrue(cache.computeIfAbsent(2, 4, this::compare));
        assertTrue(cache.computeIfAbsent(2, 5, this::compare));
        assertTrue(cache.computeIfAbsent(2, 6, this::compare));
        assertTrue(cache.computeIfAbsent(3, 5, this::compare));
        assertTrue(cache.computeIfAbsent(3, 6, this::compare));
        assertEquals(5, numCalls);
    }

    @Test
    public void computeIfAbsentPositive3() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.computeIfAbsent(4, 5, this::compare));
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(2, 3, this::compare));
        assertTrue(cache.computeIfAbsent(3, 4, this::compare));
        assertTrue(cache.computeIfAbsent(5, 6, this::compare));
        assertEquals(5, numCalls);
        assertTrue(cache.computeIfAbsent(1, 3, this::compare));
        assertTrue(cache.computeIfAbsent(1, 4, this::compare));
        assertTrue(cache.computeIfAbsent(1, 5, this::compare));
        assertTrue(cache.computeIfAbsent(1, 6, this::compare));
        assertTrue(cache.computeIfAbsent(2, 4, this::compare));
        assertTrue(cache.computeIfAbsent(2, 5, this::compare));
        assertTrue(cache.computeIfAbsent(2, 6, this::compare));
        assertTrue(cache.computeIfAbsent(3, 5, this::compare));
        assertTrue(cache.computeIfAbsent(3, 6, this::compare));
        assertEquals(5, numCalls);
    }

    @Test
    public void isKnownSuccessor() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertFalse(cache.isKnownSuccessor(4, 5));
        assertTrue(cache.computeIfAbsent(4, 5, this::compare));
        assertFalse(cache.isKnownSuccessor(1, 2));
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertFalse(cache.isKnownSuccessor(2, 3));
        assertTrue(cache.computeIfAbsent(2, 3, this::compare));
        assertFalse(cache.isKnownSuccessor(3, 4));
        assertTrue(cache.computeIfAbsent(3, 4, this::compare));
        assertFalse(cache.isKnownSuccessor(5, 6));
        assertTrue(cache.computeIfAbsent(5, 6, this::compare));
        assertEquals(5, numCalls);
        assertTrue(cache.isKnownSuccessor(1, 3));
        assertTrue(cache.isKnownSuccessor(1, 4));
        assertTrue(cache.isKnownSuccessor(1, 5));
        assertTrue(cache.isKnownSuccessor(1, 6));
        assertTrue(cache.isKnownSuccessor(2, 4));
        assertTrue(cache.isKnownSuccessor(2, 5));
        assertTrue(cache.isKnownSuccessor(2, 6));
        assertTrue(cache.isKnownSuccessor(3, 5));
        assertTrue(cache.isKnownSuccessor(3, 6));
        assertEquals(5, numCalls);
    }

    @Test
    public void computeIfAbsentNegative1() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(3, 4, this::compare));
        assertFalse(cache.computeIfAbsent(3, 2, this::compare));
        assertEquals(3, numCalls);
        assertFalse(cache.computeIfAbsent(3, 1, this::compare));
        assertFalse(cache.computeIfAbsent(4, 1, this::compare));
        assertFalse(cache.computeIfAbsent(4, 2, this::compare));
        assertEquals(3, numCalls);
    }

    @Test
    public void computeIfAbsentNegative2() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.computeIfAbsent(5, 6, this::compare));
        assertTrue(cache.computeIfAbsent(1, 4, this::compare));
        assertTrue(cache.computeIfAbsent(1, 3, this::compare));
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.computeIfAbsent(0, 2, this::compare));
        assertFalse(cache.computeIfAbsent(5, 2, this::compare));
        assertFalse(cache.computeIfAbsent(5, 3, this::compare));
        assertEquals(7, numCalls);
        assertFalse(cache.computeIfAbsent(5, 3, this::compare));
        assertFalse(cache.computeIfAbsent(5, 2, this::compare));
        assertFalse(cache.computeIfAbsent(5, 1, this::compare));
        assertFalse(cache.computeIfAbsent(5, 0, this::compare));
        assertFalse(cache.computeIfAbsent(6, 0, this::compare));
        assertFalse(cache.computeIfAbsent(6, 1, this::compare));
        assertFalse(cache.computeIfAbsent(6, 3, this::compare));
        assertFalse(cache.computeIfAbsent(6, 2, this::compare));
        assertEquals(7, numCalls);
    }

    @Test
    public void computeIfAbsentNegative3() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertFalse(cache.computeIfAbsent(2, 1, this::compare));
        assertTrue(cache.computeIfAbsent(2, 3, this::compare));
        assertEquals(2, numCalls);
        assertFalse(cache.computeIfAbsent(3, 1, this::compare));
        assertEquals(2, numCalls);
    }

    @Test
    public void isPossibleSuccessor() {
        var cache = new PreorderCache<Integer>();
        assertEquals(0, numCalls);
        assertTrue(cache.isPossibleSuccessor(5, 6));
        assertTrue(cache.computeIfAbsent(5, 6, this::compare));
        assertTrue(cache.isPossibleSuccessor(1, 4));
        assertTrue(cache.computeIfAbsent(1, 4, this::compare));
        assertTrue(cache.isPossibleSuccessor(1, 3));
        assertTrue(cache.computeIfAbsent(1, 3, this::compare));
        assertTrue(cache.isPossibleSuccessor(1, 2));
        assertTrue(cache.computeIfAbsent(1, 2, this::compare));
        assertTrue(cache.isPossibleSuccessor(0, 2));
        assertTrue(cache.computeIfAbsent(0, 2, this::compare));
        assertTrue(cache.isPossibleSuccessor(5, 2));
        assertFalse(cache.computeIfAbsent(5, 2, this::compare));
        assertTrue(cache.isPossibleSuccessor(5, 3));
        assertFalse(cache.computeIfAbsent(5, 3, this::compare));
        assertEquals(7, numCalls);
        assertFalse(cache.isPossibleSuccessor(5, 3));
        assertFalse(cache.isPossibleSuccessor(5, 2));
        assertFalse(cache.isPossibleSuccessor(5, 1));
        assertFalse(cache.isPossibleSuccessor(5, 0));
        assertFalse(cache.isPossibleSuccessor(6, 0));
        assertFalse(cache.isPossibleSuccessor(6, 1));
        assertFalse(cache.isPossibleSuccessor(6, 3));
        assertFalse(cache.isPossibleSuccessor(6, 2));
        assertEquals(7, numCalls);
    }

    @Test
    public void wrapPreorder() {
        var cache = PreorderCache.wrapPreorder(this::compare);
        assertTrue(cache.test(1, 1));
        assertTrue(cache.test(2, 2));
        assertTrue(cache.test(3, 3));
        assertTrue(cache.test(4, 4));
        assertTrue(cache.test(5, 5));
        assertEquals(0, numCalls);
        assertTrue(cache.test(1, 2));
        assertFalse(cache.test(2, 1));
        assertTrue(cache.test(2, 3));
        assertTrue(cache.test(4, 5));
        assertEquals(4, numCalls);
        assertTrue(cache.test(1, 2));
        assertTrue(cache.test(2, 3));
        assertTrue(cache.test(1, 3));
        assertTrue(cache.test(4, 5));
        assertFalse(cache.test(2, 1));
        assertFalse(cache.test(3, 1));
        assertEquals(4, numCalls);
        assertFalse(cache.test(3, 2));
        assertFalse(cache.test(4, 3));
        assertEquals(6, numCalls);
        assertFalse(cache.test(4, 1));
        assertFalse(cache.test(4, 2));
        assertFalse(cache.test(5, 1));
        assertFalse(cache.test(5, 2));
        assertFalse(cache.test(5, 3));
        assertEquals(6, numCalls);
        assertTrue(cache.test(3, 4));
        assertEquals(7, numCalls);
        assertTrue(cache.test(1, 4));
        assertTrue(cache.test(1, 5));
        assertTrue(cache.test(2, 4));
        assertTrue(cache.test(2, 5));
        assertTrue(cache.test(3, 4));
        assertTrue(cache.test(3, 5));
        assertEquals(7, numCalls);
    }
}
