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
    protected static class OptionType<T> {
        public static OptionType<Boolean> FLAG = new OptionType<>(null, s -> {
            if (s != null) {
                throw new IllegalArgumentException("option expects no arguments");
            }
            return true;
        });
        public static OptionType<Integer> INT = new OptionType<>("<integer>", s -> Integer.parseInt(s));
        public static OptionType<Integer> UINT = new OptionType<>("<non-negative>", s -> Integer.parseUnsignedInt(s));
        public static OptionType<File> FILE = new OptionType<>("<file>", s -> new File(s));

        public final String value;
        public final Function<String, T> parse;

        public OptionType(String help, Function<String, T> parse) {
            this.value = help;
            this.parse = parse;
        }

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

        public Option<T> create(Character abr, String name, Consumer<T> action, String desc, List<T> def) {
            return new Option<>(this, abr, name, action, desc, def);
        }

        public Option<T> createDefault(Consumer<T> action, String desc, List<T> def) {
            return new Option<>(this, null, null, action, desc, def);
        }
    }

    protected static class Option<T> {
        public final OptionType<T> type;
        public final Character abr;
        public final String name;
        public final Consumer<T> action;
        public final String desc;
        public final List<T> def;

        private Option(OptionType<T> type, Character abr, String name, Consumer<T> action, String desc, List<T> def) {
            this.type = type;
            this.abr = abr;
            this.name = name;
            this.action = action;
            this.desc = desc;
            this.def = def;
        }
    }

    protected abstract String appName();

    protected List<Option<?>> appOptions() {
        return List.of(
                OptionType.FLAG.create('h', "help", b -> {
                    printHelp(System.err);
                    System.exit(0);
                }, "print this help information and quit", null));
    }

    protected String getTimeStamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
    }

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
                            ((Consumer<Object>) option.action).accept(option.type.parse.apply(null));
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
                                ((Consumer<Object>) option.action).accept(option.type.parse.apply(null));
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
                        } else {
                            argsError("'-" + c + "'': unknown command line option");
                        }
                    }
                }
            } else {
                if (def != null) {
                    ((Consumer<Object>) def.action).accept(def.type.parse.apply(args[i]));
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
    protected void launch(String[] rawArgs) {
        parse(rawArgs);
        run();
    }
}
