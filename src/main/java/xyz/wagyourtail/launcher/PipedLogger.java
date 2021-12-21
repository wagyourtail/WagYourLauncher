package xyz.wagyourtail.launcher;

import java.util.HashSet;
import java.util.Set;

public class PipedLogger implements Logger {
    private Set<Logger> loggers;

    public PipedLogger(Logger... loggers) {
        this.loggers = new HashSet<>(Set.of(loggers));
    }

    public void addLogger(Logger logger) {
        loggers.add(logger);
    }

    public void removeLogger(Logger logger) {
        loggers.remove(logger);
    }

    @Override
    public void info(String info) {
        log(LogLevel.INFO, info);
    }

    @Override
    public void fatal(String fatal) {
        log(LogLevel.FATAL, fatal);
    }

    @Override
    public void error(String error) {
        log(LogLevel.ERROR, error);
    }

    @Override
    public void warn(String warning) {
        log(LogLevel.WARN, warning);
    }

    @Override
    public void debug(String debug) {
        log(LogLevel.DEBUG, debug);
    }

    @Override
    public void trace(String trace) {
        log(LogLevel.TRACE, trace);
    }

    @Override
    public void log(LogLevel level, String message) {
        for (Logger logger : loggers) {
            logger.log(level, message);
        }
    }

    @Override
    public void close() throws Exception {
        for (Logger logger : loggers) {
            logger.close();
        }
        loggers.clear();
    }

}
