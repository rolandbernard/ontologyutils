package www.ontologyutils.toolbox;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * Utility class that contains static constants and methods that don't neatly
 * fit anywhere else.
 */
public final class Utils {
    /**
     * Prevents instantiation.
     */
    private Utils() {
    }

    /**
     * Used for exceptions that we don't want to or can't handle. This function is
     * guaranteed to never return.
     *
     * @param e
     *            The exception that caused the panic.
     * @return Return a runtime exception it is given, so that it can be thrown
     *         again to
     *         avoid control flow checks failing.
     */
    public static RuntimeException panic(final Exception e) {
        e.printStackTrace();
        System.exit(1);
        return new RuntimeException(e);
    }

    /**
     * Use this method for communicating data to the user that is not part of the
     * output.
     *
     * @param info
     *            The message to be communicated
     */
    public static void log(final String info) {
        System.err.println(info);
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> T randomChoice(final Stream<T> stream) {
        final Object[] flatArray = stream.toArray();
        final int randomIdx = ThreadLocalRandom.current().nextInt(flatArray.length);
        return (T) flatArray[randomIdx];
    }

    public static <T> T randomChoice(final Collection<T> collection) {
        return randomChoice(collection.stream());
    }
}
