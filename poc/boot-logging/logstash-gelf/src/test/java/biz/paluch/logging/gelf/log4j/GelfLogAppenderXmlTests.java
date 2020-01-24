package biz.paluch.logging.gelf.log4j;

import org.apache.log4j.LogManager;
import org.apache.log4j.MDC;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.jupiter.api.BeforeEach;

import biz.paluch.logging.gelf.GelfTestSender;

/**
 * @author Mark Paluch
 * @since 27.09.13 07:47
 */
class GelfLogAppenderXmlTests extends AbstractGelfLogAppenderTests {

    @BeforeEach
    void before() throws Exception {
        LogManager.getLoggerRepository().resetConfiguration();
        GelfTestSender.getMessages().clear();
        DOMConfigurator.configure(getClass().getResource("/log4j/log4j.xml"));
        MDC.remove("mdcField1");
    }

}
