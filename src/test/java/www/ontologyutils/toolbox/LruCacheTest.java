package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.*;

@Execution(ExecutionMode.CONCURRENT)
public class LruCacheTest {
    @Test
    public void keepAllEntriesUntilLimit() {
        var cache = new LruCache<Integer, Integer>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        assertEquals(Set.of(
                new SimpleEntry<>(1, 1),
                new SimpleEntry<>(2, 2),
                new SimpleEntry<>(3, 3)), cache.entrySet());
    }

    @Test
    public void removeOldestEntry() {
        var cache = new LruCache<Integer, Integer>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        assertEquals(Set.of(
                new SimpleEntry<>(4, 4),
                new SimpleEntry<>(2, 2),
                new SimpleEntry<>(3, 3)), cache.entrySet());
    }

    @Test
    public void removeLastAccessedEntry() {
        var cache = new LruCache<Integer, Integer>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);
        cache.get(2);
        cache.put(4, 4);
        assertEquals(Set.of(
                new SimpleEntry<>(1, 1),
                new SimpleEntry<>(2, 2),
                new SimpleEntry<>(4, 4)), cache.entrySet());
    }

    @Test
    public void canOverwriteEntries() {
        var cache = new LruCache<Integer, Integer>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);
        cache.get(2);
        cache.put(2, 4);
        cache.put(1, 2);
        assertEquals(Set.of(
                new SimpleEntry<>(1, 2),
                new SimpleEntry<>(2, 4),
                new SimpleEntry<>(3, 3)), cache.entrySet());
    }

    @Test
    public void computeIfAbsent() {
        var cache = new LruCache<Integer, Integer>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);
        cache.get(2);
        cache.put(2, 4);
        cache.put(1, 2);
        assertEquals(5, cache.computeIfAbsent(4, k -> 5));
        assertEquals(4, cache.computeIfAbsent(2, k -> 5));
        assertEquals(5, cache.computeIfAbsent(3, k -> 5));
        assertEquals(5, cache.computeIfAbsent(1, k -> 5));
        assertEquals(Set.of(
                new SimpleEntry<>(1, 5),
                new SimpleEntry<>(3, 5),
                new SimpleEntry<>(2, 4)), cache.entrySet());
    }

    @Test
    public void wrappedFunction() {
        var func = LruCache.wrapFunction(new Function<Integer, Integer>() {
            private int call = 0;

            @Override
            public Integer apply(Integer argument) {
                call += 1;
                return call;
            }
        }, 3);
        assertEquals(1, func.apply(1));
        assertEquals(2, func.apply(2));
        assertEquals(3, func.apply(3));
        assertEquals(3, func.apply(3));
        assertEquals(2, func.apply(2));
        assertEquals(1, func.apply(1));
        assertEquals(4, func.apply(4));
        assertEquals(4, func.apply(4));
        assertEquals(1, func.apply(1));
        assertEquals(2, func.apply(2));
        assertEquals(5, func.apply(3));
        assertEquals(6, func.apply(4));
        assertEquals(7, func.apply(5));
    }
}
