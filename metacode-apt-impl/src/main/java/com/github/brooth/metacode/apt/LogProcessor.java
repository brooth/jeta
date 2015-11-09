package com.github.brooth.metacode.apt;

/**
 * -AmcLoggerMethodFormat="setName(\"%s\")"
 */
public class LogProcessor extends SimpleProcessor {

    public LogProcessor() {
        super(Log.class);
    }

    @Override
    public boolean process(ProcessorContext ctx, int round) {
        return false;
    }
}
