package www.ontologyutils.apps;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public abstract class App {
    /**
     * Class for representing type of command line options.
     */
    protected static class OptionType<T> {
        /**
         * Flag type that does not expect any value.
         */
        public static OptionType<Boolean> FLAG = new OptionType<>(null, s -> {
            if (s != null) {
                throw new IllegalArgumentException("option expects no arguments");
            }
            return true;
        });
        /**
         * Signed integer type.
         */
        public static OptionType<Integer> INT = new OptionType<>("<integer>", s -> Integer.parseInt(s));
        /**
         * Unsigned integer type.
         */
        public static OptionType<Integer> UINT = new OptionType<>("<non-negative>", s -> Integer.parseUnsignedInt(s));
        /**
         * File type. Small convenience around string, no validation is performed.
         */
        public static OptionType<File> FILE = new OptionType<>("<file>", s -> new File(s));

        private final String value;
        private final Function<String, T> parse;

        private OptionType(String help, Function<String, T> parse) {
            this.value = help;
            this.parse = parse;
        }

        /**
         * @param <T>
         *            The type of the options.
         * @param options
         *            The options the user can choose from.
         * @return A new type for the given options.
         */
        public static <T> OptionType<T> options(Map<String, T> options) {
            return new OptionType<>("{" + options.keySet().stream().sorted().collect(Collectors.joining("|")) + "}",
                    s -> {
                        if (options.containsKey(s)) {
                            return options.get(s);
                        } else {
                            throw new IllegalArgumentException("value is not any of "
                                    + options.keySet().stream().sorted().collect(Collectors.joining(", ")));
                        }
                    });
        }

        /**
         * @param abr
         *            Single character name of the option.
         * @param name
         *            Full name of the option.
         * @param action
         *            Action to be performed if the option is present.
         * @param desc
         *            A description of the option for the help output.
         * @param def
         *            The default value to call the option with.
         * @return The new option.
         */
        public Option<T> create(Character abr, String name, Consumer<T> action, String desc, List<T> def) {
            return new Option<>(this, abr, name, action, desc, def);
        }

        /**
         * @param abr
         *            Single character name of the option.
         * @param name
         *            Full name of the option.
         * @param action
         *            Action to be performed if the option is present.
         * @param desc
         *            A description of the option for the help output.
         * @return The new option.
         */
        public Option<T> create(Character abr, String name, Consumer<T> action, String desc) {
            return create(abr, name, action, desc, null);
        }

        /**
         * @param name
         *            Full name of the option.
         * @param action
         *            Action to be performed if the option is present.
         * @param desc
         *            A description of the option for the help output.
         * @return The new option.
         */
        public Option<T> create(String name, Consumer<T> action, String desc) {
            return create(null, name, action, desc, null);
        }

        /**
         * @param action
         *            Action to be performed if the option is present.
         * @param desc
         *            A description of the option for the help output.
         * @param def
         *            The default value to call the option with.
         * @return The new option.
         */
        public Option<T> createDefault(Consumer<T> action, String desc, List<T> def) {
            return new Option<>(this, null, null, action, desc, def);
        }

        /**
         * @param action
         *            Action to be performed if the option is present.
         * @param desc
         *            A description of the option for the help output.
         * @return The new option.
         */
        public Option<T> createDefault(Consumer<T> action, String desc) {
            return createDefault(action, desc, null);
        }
    }

    /**
     * Class for representing command line options.
     */
    protected static class Option<T> {
        private final OptionType<T> type;
        private final Character abr;
        private final String name;
        private final Consumer<T> action;
        private final String desc;
        private final List<T> def;

        private Option(OptionType<T> type, Character abr, String name, Consumer<T> action, String desc, List<T> def) {
            this.type = type;
            this.abr = abr;
            this.name = name;
            this.action = action;
            this.desc = desc;
            this.def = def;
        }
    }

    /**
     * @return The name of this application.
     */
    protected String appName() {
        return this.getClass().getName();
    }

    /**
     * @return The list of options accepted by this application.
     */
    protected List<Option<?>> appOptions() {
        return List.of(
                OptionType.FLAG.create('h', "help", b -> {
                    printHelp(System.err);
                    System.exit(0);
                }, "print this help information and quit"));
    }

    /**
     * @return Current timestamp for use in log messages.
     */
    protected String getTimeStamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
    }

    /**
     * @param msg
     *            The message to be printed.
     */
    protected void logMessage(String msg) {
        System.err.println("[" + getTimeStamp() + "] " + msg);
    }

    /**
     * Execute the actual application;
     */
    protected abstract void run();

    private void printOptionHelp(PrintStream out, Option<?> option) {
        out.print("  ");
        int col = 2;
        if (option.abr != null) {
            out.print("-");
            out.print(option.abr);
            col += 2;
        }
        if (option.name != null) {
            if (option.abr != null) {
                out.print(" ");
                col += 1;
            }
            out.print("--");
            out.print(option.name);
            col += option.name.length() + 2;
        }
        if (option.type.value != null) {
            if (option.abr != null || option.name != null) {
                out.print("=");
                col += 1;
            }
            out.print(option.type.value);
            col += option.type.value.length();
        }
        if (col >= 27) {
            out.println();
            col = 0;
        }
        while (col < 27) {
            out.print(" ");
            col++;
        }
        out.print(option.desc);
        out.println();
    }

    private void printHelp(PrintStream out) {
        var options = appOptions();
        Option<?> def = null;
        for (var option : options) {
            if (option.abr == null && option.name == null) {
                def = option;
            }
        }
        out.println("Usage: " + appName() + " [options]"
                + (def != null && def.type.value != null ? " " + def.type.value : ""));
        out.println("Options:");
        for (var option : options) {
            if (option != def) {
                printOptionHelp(out, option);
            }
        }
        printOptionHelp(out, def);
    }

    private void argsError(String msg) {
        System.err.println("error: " + appName() + ": " + msg);
        System.exit(1);
    }

    @SuppressWarnings("unchecked")
    private void parse(String[] args) {
        var options = appOptions();
        var byAbr = new HashMap<Character, Option<?>>();
        var byName = new HashMap<String, Option<?>>();
        Option<?> def = null;
        for (var option : options) {
            if (option.abr != null) {
                byAbr.put(option.abr, option);
            }
            if (option.name != null) {
                byName.put(option.name, option);
            }
            if (option.abr == null && option.name == null && option.type.value != null) {
                def = option;
            }
        }
        var unused = new HashSet<>(options);
        var onlyDef = false;
        for (int i = 0; i < args.length; i++) {
            if (!onlyDef && args[i].charAt(0) == '-') {
                if (args[i].charAt(1) == '-') {
                    if (args[i].length() == 2) {
                        onlyDef = true;
                        continue;
                    }
                    int len = 0;
                    while (len + 2 < args[i].length() && args[i].charAt(len + 2) != '=') {
                        len++;
                    }
                    String name = args[i].substring(2, len + 2);
                    var option = byName.get(name);
                    if (option != null) {
                        if (option.type.value == null) {
                            if (len + 2 < args[i].length()) {
                                argsError("'--" + name + "'': '" + args[i].substring(len + 3)
                                        + "': option does not expect a value");
                            }
                            try {
                                ((Consumer<Object>) option.action).accept(option.type.parse.apply(null));
                            } catch (IllegalArgumentException ex) {
                                argsError("'--" + name + "'': " + ex.getMessage());
                            }
                        } else {
                            String value = null;
                            if (len + 2 < args[i].length()) {
                                value = args[i].substring(len + 3);
                            } else if (i + 1 < args.length && args[i + 1].charAt(0) != '-') {
                                value = args[i + 1];
                                i++;
                            }
                            try {
                                ((Consumer<Object>) option.action).accept(option.type.parse.apply(value));
                            } catch (IllegalArgumentException ex) {
                                argsError("'--" + name + "'': '" + value + "': " + ex.getMessage());
                            }
                        }
                        unused.remove(option);
                    } else {
                        argsError("'--" + name + "'': unknown command line option");
                    }
                } else {
                    var arg = args[i];
                    for (int j = 1; j < arg.length(); j++) {
                        char c = arg.charAt(j);
                        var option = byAbr.get(c);
                        if (option != null) {
                            if (option.type.value == null) {
                                try {
                                    ((Consumer<Object>) option.action).accept(option.type.parse.apply(null));
                                } catch (IllegalArgumentException ex) {
                                    argsError("'-" + c + "'': " + ex.getMessage());
                                }
                            } else {
                                String value = null;
                                if (j + 1 < arg.length() && arg.charAt(j + 1) == '=') {
                                    value = arg.substring(j + 2);
                                    j = arg.length();
                                } else if (i + 1 < args.length && args[i + 1].charAt(0) != '-') {
                                    value = args[i + 1];
                                    i++;
                                }
                                try {
                                    ((Consumer<Object>) option.action).accept(option.type.parse.apply(value));
                                } catch (IllegalArgumentException ex) {
                                    argsError("'-" + c + "'': '" + value + "': " + ex.getMessage());
                                }
                            }
                            unused.remove(option);
                        } else {
                            argsError("'-" + c + "'': unknown command line option");
                        }
                    }
                }
            } else {
                if (def != null) {
                    try {
                        ((Consumer<Object>) def.action).accept(def.type.parse.apply(args[i]));
                    } catch (IllegalArgumentException ex) {
                        argsError("'" + args[i] + "': " + ex.getMessage());
                    }
                    unused.remove(def);
                } else {
                    argsError("'" + args[i] + "'': unknown command line option");
                }
            }
        }
        for (var option : unused) {
            if (option.def != null) {
                for (var value : option.def) {
                    ((Consumer<Object>) option.action).accept(value);
                }
            }
        }
    }

    /**
     * @param args
     *            The arguments to the application.
     */
    protected void launch(String[] args) {
        parse(args);
        run();
    }
}
