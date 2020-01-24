package biz.paluch.logging.gelf.wildfly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jboss.logmanager.MDC;
import org.jboss.logmanager.NDC;
import org.jboss.logmanager.handlers.WriterHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import biz.paluch.logging.gelf.GelfTestSender;
import biz.paluch.logging.gelf.JsonUtil;
import biz.paluch.logging.gelf.LogMessageField;

/**
 * @author Mark Paluch
 */
class WildFlyGelfLogFormatterTests {

    private static final String LOG_MESSAGE = "foo bar test log message";
    private static final String EXPECTED_LOG_MESSAGE = LOG_MESSAGE;
    private WriterHandler handler = new WriterHandler();
    private StringWriter stringWriter = new StringWriter();

    @BeforeEach
    void before() {
        GelfTestSender.getMessages().clear();
        LogManager.getLogManager().reset();
        MDC.remove("mdcField1");
        NDC.clear();

        handler.setWriter(stringWriter);
    }

    @Test
    void testDefaults() {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        NDC.push("ndc message");
        logger.info(LOG_MESSAGE);
        NDC.clear();

        Map<String, Object> message = popMessage();

        assertThat(message.get("version")).isNull();
        assertThat(message.get("full_message")).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(message.get("short_message")).isEqualTo(EXPECTED_LOG_MESSAGE);
        assertThat(message.get("NDC")).isEqualTo("ndc message");
        assertThat(message.get("facility")).isEqualTo("logstash-gelf");
        assertThat(message.get("LoggerName")).isEqualTo(getClass().getName());
        assertThat(message.get("Thread")).isNotNull();
        assertThat(message.get("timestamp")).isNotNull();
        assertThat(message.get("MyTime")).isNotNull();
        assertThat(message.get("level")).isEqualTo("6");
        assertThat(message.get(LogMessageField.NamedLogField.SourceMethodName.name())).isEqualTo("testDefaults");
        assertThat(message.get(LogMessageField.NamedLogField.SourceClassName.name())).isEqualTo(getClass().getName());
    }

    @Test
    void testEmptyMessage() {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info("");
        Map<String, Object> message = popMessage();

        assertThat(message.get("full_message")).isNull();
        assertThat(message.get("short_message")).isNull();

    }

    @Test
    void testSimpleWithMsgFormatSubstitution() {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message {0}";
        String expectedMessage = "foo bar test log message aaa";
        logger.log(Level.INFO, logMessage, new String[] { "aaa" });

        Map<String, Object> message = popMessage();

        assertThat(message.get("full_message")).isEqualTo(expectedMessage);
        assertThat(message.get("short_message")).isEqualTo(expectedMessage);
    }

    @Test
    void testSimpleWithStringFormatSubstitution() {

        handler.setFormatter(new WildFlyJsonFormatter());
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        String logMessage = "foo bar test log message %s";
        String expectedMessage = "foo bar test log message aaa";

        logger.log(Level.INFO, logMessage, new String[] { "aaa" });

        Map<String, Object> message = popMessage();

        assertThat(message.get("full_message")).isEqualTo(expectedMessage);
        assertThat(message.get("short_message")).isEqualTo(expectedMessage);

    }

    @Test
    void testUnknownField() {

        assertThrows(IllegalArgumentException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
                formatter.setFields("dummy");
            }
        });
    }

    @Test
    void testNotSupportedField() {

        assertThrows(IllegalArgumentException.class, new Executable() {

            @Override
            public void execute() throws Throwable {
                WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
                formatter.setFields("Marker");
            }
        });
    }

    @Test
    void testFields() {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setFields("Time,Severity,ThreadName,SourceSimpleClassName,NDC");

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);

        Map<String, Object> message = popMessage();

        assertThat(message.get("SourceSimpleClassName")).isNotNull();
        assertThat(message.get("LoggerName")).isNull();
    }

    @Test
    void testLineBreak() {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setLineBreak("XxX");

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.info(LOG_MESSAGE);
        logger.info(LOG_MESSAGE);
        assertThat(stringWriter.getBuffer().toString().contains("}XxX{")).isTrue();
    }

    @Test
    void testMdcFields() {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setOriginHost("myhost");
        formatter.setAdditionalFields("fieldName1=fieldValue1,fieldName2=fieldValue2");
        formatter.setDynamicMdcFields(".*");
        formatter.setIncludeFullMdc(true);

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        MDC.put("mdcField1", "a value");

        logger.info(LOG_MESSAGE);

        Map<String, Object> message = popMessage();

        assertThat(message.get("host")).isEqualTo("myhost");
        assertThat(message.get("fieldName1")).isEqualTo("fieldValue1");
        assertThat(message.get("fieldName2")).isEqualTo("fieldValue2");
        assertThat(message.get("mdcField1")).isEqualTo("a value");
    }

    @Test
    void testIncludeLogMessageParameters() {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setOriginHost("myhost");
        formatter.setIncludeLogMessageParameters(true);

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.log(Level.INFO, "Foo {0}", "bar");

        Map<String, Object> message = popMessage();
        assertThat(message.get("full_message")).isEqualTo("Foo bar");
        assertThat(message).containsEntry("MessageParam0", "bar");

        formatter.setIncludeLogMessageParameters(false);

        logger.log(Level.INFO, "Foo {0}", "baz");
        message = popMessage();

        assertThat(message.get("full_message")).isEqualTo("Foo baz");
        assertThat(message).doesNotContainKey("MessageParam0");
    }

    @Test
    void testIncludeLocation() {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setIncludeLocation(false);

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.log(Level.INFO, "Foo {0}", "bar");

        Map<String, Object> message = popMessage();
        assertThat(message).doesNotContainKeys("SourceClassName", "SourceSimpleClassName", "SourceMethodName",
                "SourceLineNumber");
    }

    @Test
    void testException() {

        WildFlyJsonFormatter formatter = new WildFlyJsonFormatter();
        formatter.setOriginHost("myhost");
        formatter.setExtractStackTrace("true");

        handler.setFormatter(formatter);
        Logger logger = Logger.getLogger(getClass().getName());
        logger.addHandler(handler);

        logger.log(Level.WARNING, LOG_MESSAGE, new Exception("boom!"));

        Map<String, Object> message = popMessage();

        assertThat(message.get("StackTrace").toString()).contains("boom!");
    }

    Map<String, Object> popMessage() {
        try {
            return JsonUtil.parseToMap(stringWriter.getBuffer().toString());
        } finally {
            stringWriter.getBuffer().delete(0, stringWriter.getBuffer().length());
        }
    }
}
