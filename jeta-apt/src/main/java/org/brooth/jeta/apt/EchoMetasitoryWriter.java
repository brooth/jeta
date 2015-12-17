package org.brooth.jeta.apt;

import com.google.common.base.Joiner;
import org.brooth.jeta.apt.metasitory.MetasitoryEnvironment;
import org.brooth.jeta.apt.metasitory.MetasitoryWriter;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * for debugging
 *
 * @author Oleg Khalidov (brooth@gmail.com)
 */
class EchoMetasitoryWriter implements MetasitoryWriter {

    private ProcessingEnvironment env;
    protected Logger logger;

    @Override
    public void open(MetasitoryEnvironment env) {
        this.env = env.processingEnv();
        this.logger = env.logger();
        logger.debug("open()");
    }

    @Override
    public void write(MetacodeContext context) {
        String master = context.masterElement().toString();
        logger.debug(String.format("master: %1$s, metacode: %2$s, annotations: {%3$s}",
                master, MetacodeUtils.getMetacodeOf(env.getElementUtils(), master),
                Joiner.on(", ").join(context.metacodeAnnotations())));
    }

    @Override
    public void close() {
        logger.debug("close()");
    }
}
