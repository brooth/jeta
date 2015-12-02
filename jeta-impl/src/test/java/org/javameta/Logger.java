package org.javameta;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class Logger {

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized void debug(String msg) {
        System.out.println(String.format("debug [%s] %s", name, msg));
    }

    public void debug(String msg, Object... params) {
        debug(String.format(msg, params));
    }

    public synchronized void error(String msg) {
        System.err.println(String.format("error [%s] %s", name, msg));
    }

    public void error(String msg, Object... params) {
        error(String.format(msg, params));
    }
}
