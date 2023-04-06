package www.ontologyutils.toolbox;

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
     * Used for exceptions that we don't want to or can't handle.
     *
     * @param e
     *            The exception that caused the panic.
     */
    public static void panic(final Exception e) {
        e.printStackTrace();
        System.exit(1);
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
}
