package uk.gov.hmpo.dropwizard.smartLogging.json;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonEncoder extends PatternLayoutEncoder {
    private boolean immediateFlush = true;
    @Override
    public void doEncode(ILoggingEvent event) throws IOException {
        outputStream.write(convertToBytes(event, event.getMessage()));
        outputStream.write("\n".getBytes());

        if (immediateFlush) {
            outputStream.flush();
        }
    }

    private byte[] convertToBytes(ILoggingEvent event, String message) throws JsonProcessingException {
        Map<String, String> mdc = event.getMDCPropertyMap();

        String applicationName = mdc.get("applicationName");
        String sessionId = mdc.get("X-REQ-ID");
        String environment = mdc.get("environment");
        String host = mdc.get("host");

        HashMap<String, Object> jsonContent = new HashMap<>();
        jsonContent.put("timestamp", new Date());
        jsonContent.put("message", message);
        jsonContent.put("level", event.getLevel().toString());
        jsonContent.put("sessionID", sessionId);
        jsonContent.put("environment", environment);
        jsonContent.put("label", applicationName);
        jsonContent.put("host", host);
        jsonContent.put("logger", event.getLoggerName());
        jsonContent.put("thread", event.getThreadName());
        addExceptionMessage(event, jsonContent);

        return new ObjectMapper().writeValueAsBytes(jsonContent);
    }

    private void addExceptionMessage(ILoggingEvent event, Map<String, Object> jsonContent) {
        if (event.getThrowableProxy() != null) {
            jsonContent.put("exceptionMessage", ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }
    }
}