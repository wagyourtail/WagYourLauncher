package xyz.wagyourtail.launcher.nogui;

import xyz.wagyourtail.launcher.LogListener;

public class ConsoleLogListener implements LogListener {

    @Override
    public void onInfo(String info) {
        System.out.println(withColor(info, ConsoleColors.GRAY));
    }

    @Override
    public void onFatal(String fatal) {
        System.out.println(withColor(fatal, ConsoleColors.RED_BOLD));
    }

    @Override
    public void onError(String error) {
        System.out.println(withColor(error, ConsoleColors.RED));
    }

    @Override
    public void onWarning(String warning) {
        System.out.println(withColor(warning, ConsoleColors.YELLOW));
    }

    @Override
    public void onDebug(String debug) {
        System.out.println(withColor(debug, ConsoleColors.CYAN));
    }

    @Override
    public void onTrace(String trace) {
        System.out.println(withColor(trace, ConsoleColors.BLUE));
    }

    @Override
    public void log(LogLevel level, String message) {
        switch (level) {
            case INFO -> onInfo(message);
            case FATAL -> onFatal(message);
            case ERROR -> onError(message);
            case WARN -> onWarning(message);
            case DEBUG -> onDebug(message);
            case TRACE -> onTrace(message);
        }
    }

    public static String withColor(String str, ConsoleColors color) {
        return color.code + str + ConsoleColors.WHITE.code;
    }

    @Override
    public void close() throws Exception {

    }

    public enum ConsoleColors {
        WHITE("\033[0m"),
        BLACK("\033[0;30m"),
        RED("\033[0;31m"),
        GREEN("\033[0;32m"),
        YELLOW("\033[0;33m"),
        BLUE("\033[0;34m"),
        PURPLE("\033[0;35m"),
        CYAN("\033[0;36m"),
        GRAY("\033[0;37m"),

        BLACK_BOLD("\033[1;30m"),
        RED_BOLD("\033[1;31m"),
        GREEN_BOLD("\033[1;32m"),
        YELLOW_BOLD("\033[1;33m"),
        BLUE_BOLD("\033[1;34m"),
        PURPLE_BOLD("\033[1;35m"),
        CYAN_BOLD("\033[1;36m"),
        WHITE_BOLD("\033[1;37m");

        private final String code;

        ConsoleColors(String code) {
            this.code = code;
        }
    }

}
