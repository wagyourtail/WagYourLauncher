package xyz.wagyourtail.launcher.nogui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandManager {
    public Map<String, Command> commands = new HashMap<>();

    public CommandManager() {
        registerCommand("help", "[command]", "Prints help info", this::help);
        registerCommand("exit", "", "Exits", s -> false);
    }

    public CommandManager registerCommand(String name, String usage, String desc, Function<String[], Boolean> command) {
        commands.put(name, new Command(name, usage, desc, command));
        return this;
    }

    public CommandManager registerCommand(String name, String usage, String desc, Consumer<String[]> command) {
        return registerCommand(name, usage, desc, s -> {command.accept(s); return true;});
    }

    public boolean run(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 0) {
            return true;
        }
        String cmd = parts[0];
        if (commands.containsKey(cmd)) {
            Command c = commands.get(cmd);
            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
            return c.action.apply(args);
        } else {
            System.err.println("Unknown command: " + cmd);
        }
        return true;
    }

    public boolean help(String[] args) {
        System.out.println("Available commands:");
        if (args.length == 1) {
            for (Command c : commands.values()) {
                System.out.println("    " + c.name + " " + c.usage + "\n        " + c.desc);
            }
        } else {
            String cmd = args[1];
            if (commands.containsKey(cmd)) {
                System.out.println("    " + cmd + " - " + commands.get(cmd).usage + "\n        " + commands.get(cmd).desc);
            }
        }
        return true;
    }


    public record Command(String name, String usage, String desc, Function<String[], Boolean> action) {}
}
