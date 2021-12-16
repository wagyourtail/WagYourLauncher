package xyz.wagyourtail.launcher.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArgHandler {
    public Map<String, Arg> args = new HashMap<>();

    public ArgHandler(Arg... args) {
        for (Arg arg : args) {
            this.args.put(arg.name, arg);
            for (String alias : arg.aliases) {
                this.args.put(alias, arg);
            }
        }
    }

    public void printHelp() {
        System.out.println("Arguments:");
        for (Arg arg : args.values()) {
            System.out.println(arg.name + ": " + arg.description);
            System.out.println("\taliases: " + String.join(", ", arg.aliases));
        }
    }

    public ParsedArgs parseStringArgs(String[] args) {
        Map<Arg, String[]> argMap = new HashMap<>();
        for (int i = 0; i < args.length; ++i) {
            Arg a = this.args.get(args[i]);
            if (a == null) {
                throw new IllegalArgumentException("Unknown argument: " + args[i]);
            } else {
                if (argMap.containsKey(a)) {
                    throw new IllegalArgumentException("Argument " + a.name + " already specified");
                }
                String[] values = new String[a.length];
                System.arraycopy(args, i, values, 0, a.length);
                argMap.put(a, values);
                i += a.length - 1;
            }
        }
        return new ParsedArgs(this, argMap);
    }

    public Arg getArg(String name) {
        return args.get(name);
    }

    public static class ParsedArgs {
        public Map<Arg, String[]> argMap;
        public ArgHandler handler;

        public ParsedArgs(ArgHandler handler, Map<Arg, String[]> argMap) {
            this.handler = handler;
            this.argMap = argMap;
        }

        public boolean has(Arg arg) {
            return argMap.containsKey(arg);
        }

        public boolean has(String arg) {
            return has(handler.getArg(arg));
        }

        public Optional<String[]> get(Arg arg) {
            return Optional.ofNullable(argMap.get(arg));
        }

        public Optional<String[]> get(String arg) {
            return get(handler.getArg(arg));
        }
    }

    public record Arg(String name, String description, int length, String... aliases) {}
}
