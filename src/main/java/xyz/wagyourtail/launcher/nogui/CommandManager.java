package xyz.wagyourtail.launcher.nogui;

import xyz.wagyourtail.launcher.main.Main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandManager {
    public LauncherNoGui launcher;
    public Map<String, Command> commands = new HashMap<>();

    public CommandManager(LauncherNoGui launcher) {
        this.launcher = launcher;
        registerCommand("help", "[command]", "Prints help info", this::help);
        registerCommand("exit", "", "Exits", s -> false);
    }

    public CommandManager registerCommand(String name, String usage, String desc, Function<String[], Boolean> command) {
        commands.put(name, new Command(name, usage, desc, command));
        return this;
    }

    public boolean run(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 0) {
            return true;
        }
        String cmd = parts[0];
        if (commands.containsKey(cmd)) {
            Command c = commands.get(cmd);
            return c.action.apply(parts);
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