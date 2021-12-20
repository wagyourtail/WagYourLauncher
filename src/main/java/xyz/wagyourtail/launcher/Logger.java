package xyz.wagyourtail.launcher;

public interface Logger extends AutoCloseable {
    void info(String info);
    void fatal(String fatal);
    void error(String error);
    void warn(String warning);
    void debug(String debug);
    void trace(String trace);

    void log(LogLevel level, String message);

    public enum LogLevel {
        INFO,
        ERROR,
        WARN,
        DEBUG,
        TRACE,
        FATAL
    }
}
