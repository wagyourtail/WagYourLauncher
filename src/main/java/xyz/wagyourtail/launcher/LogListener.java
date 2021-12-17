package xyz.wagyourtail.launcher;

public interface LogListener extends AutoCloseable {
    void onInfo(String info);
    void onFatal(String fatal);
    void onError(String error);
    void onWarning(String warning);
    void onDebug(String debug);
    void onTrace(String trace);

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
