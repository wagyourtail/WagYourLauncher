package xyz.wagyourtail.launcher.gui.component.logging;

import xyz.wagyourtail.launcher.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.beans.JavaBean;
import java.io.IOException;

@JavaBean
public class LoggingTextArea extends JTextPane implements Logger {

    private static final String startHTMLTree = "<html><body style='font-family: monospace;'><p></p></body></html>";

    JScrollPane scrollBar;

    public LoggingTextArea(JScrollPane scrollBar) {
        super();
        this.scrollBar = scrollBar;
        this.setEditable(false);
        this.setContentType("text/html");
        this.setText(startHTMLTree);
    }

    @Override
    public void info(String info) {
        log(withColor(info.replaceAll(" ", "&nbsp;"), "white"));
    }

    @Override
    public void fatal(String fatal) {
        log(withColor(fatal.replaceAll(" ", "&nbsp;"), "red", "bold"));
    }

    @Override
    public void error(String error) {
        log(withColor(error.replaceAll(" ", "&nbsp;"), "red"));
    }

    @Override
    public void warn(String warning) {
        log(withColor(warning.replaceAll(" ", "&nbsp;"), "orange"));
    }

    @Override
    public void debug(String debug) {
        log(withColor(debug.replaceAll(" ", "&nbsp;"), "gray"));
    }

    @Override
    public void trace(String trace) {
        log(withColor(trace.replaceAll(" ", "&nbsp;"), "cyan"));
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

    public void log(String text) {
        text = text + "<br>";
        SwingUtilities.invokeLater(() -> {
            //                int max = scrollBar.getVerticalScrollBar().getMaximum();
            //                int size = scrollBar.getVerticalScrollBar().getVisibleAmount();
            //                int value = scrollBar.getVerticalScrollBar().getValue();
            //                if (value + size + 4 >= max) {
            //                    ((DefaultCaret)this.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            //                } else {
            //                    ((DefaultCaret)this.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            //                }
            try {
                ((HTMLDocument) this.getStyledDocument()).insertBeforeEnd(this.getDocument().getDefaultRootElement().getElement(1).getElement(0), text);
            } catch (BadLocationException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> this.setText(startHTMLTree));
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
