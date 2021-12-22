package xyz.wagyourtail.launcher.gui.component.logging;

import xyz.wagyourtail.launcher.Logger;

import javax.swing.*;
import java.beans.JavaBean;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

@JavaBean
public class LoggingTextArea extends JTextPane implements Logger {
    private static final ThreadPoolExecutor loggerExecutor = new ThreadPoolExecutor(1, 1, 0L, java.util.concurrent.TimeUnit.MILLISECONDS, new java.util.concurrent.LinkedBlockingQueue<>());
    private static final Set<LoggingTextArea> loggers = new HashSet<>();

    static {
        Thread th = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        for (LoggingTextArea logger : loggers) {
                            if (logger.dirty) {
                                logger.setText(logger.head + logger.actual.toString() + logger.tail);
                                logger.dirty = false;
                            }
                        }
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }

    String head = "<html><body style='font-family: monospace;'><pre>";
    StringBuilder actual = new StringBuilder();
    String tail = "</pre></body></html>";
    boolean dirty = false;

    JScrollPane scrollBar;

    public LoggingTextArea(JScrollPane scrollBar) {
        super();
        this.scrollBar = scrollBar;
        this.setEditable(false);
        loggers.add(this);
    }

    public static void removeLogger(Logger logger) {
        loggers.remove(logger);
    }

    @Override
    public void info(String info) {
//        try {
            loggerExecutor.execute(() -> {
                actual.append(this.withColor(info, "white")).append("<br>");
                dirty = true;
//                //this.update(this.getGraphics());
            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void fatal(String fatal) {
//        try {
            loggerExecutor.execute(() -> {
                actual.append(this.withColor(fatal, "red", "bold")).append("<br>");
                dirty = true;
                //this.update(this.getGraphics());
            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void error(String error) {
//        try {
            loggerExecutor.execute(() -> {
                actual.append(this.withColor(error, "red")).append("<br>");
                dirty = true;
//                //this.update(this.getGraphics());
            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void warn(String warning) {
//        try {
            loggerExecutor.execute(() -> {
                actual.append(this.withColor(warning, "#FFD700")).append("<br>");
                dirty = true;
//                //this.update(this.getGraphics());
            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void debug(String debug) {
//        try {
            loggerExecutor.execute(() -> {
                actual.append(this.withColor(debug, "gray")).append("<br>");
                dirty = true;
                this.setText(head + actual.toString() + tail);
//                //this.update(this.getGraphics());
            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void trace(String trace) {
//        try {
            loggerExecutor.execute(() -> {
                actual.append(this.withColor(trace, "cyan")).append("<br>");
                dirty = true;
//                //this.update(this.getGraphics());
            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void log(LogLevel level, String message) {
        switch (level) {
            case FATAL -> this.fatal(message);
            case ERROR -> this.error(message);
            case WARN -> this.warn(message);
            case DEBUG -> this.debug(message);
            case TRACE -> this.trace(message);
            case INFO -> this.info(message);
        }
    }

    public void clear() {
        loggerExecutor.execute(() -> {actual = new StringBuilder(); dirty = true;});
    }

    @Override
    public void close() throws Exception {

    }

    public String withColor(String message, String color) {
        return "<font color=\"" + color + "\">" + message + "</font>";
    }

    public String withColor(String message, String color, String weight) {
        return "<font color=\"" + color + "\" weight=\"" + weight + "\">" + message + "</font>";
    }

}
