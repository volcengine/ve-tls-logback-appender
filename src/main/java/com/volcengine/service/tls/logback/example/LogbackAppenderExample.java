package com.volcengine.service.tls.logback.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogbackAppenderExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackAppenderExample.class);

    public static void main(String[] args) {
        MDC.put("MDC_KEY","MDC_VALUE");
        MDC.put("THREAD_ID", String.valueOf(Thread.currentThread().getId()));

        LOGGER.trace("This is a trace log.");
        LOGGER.debug("This is a debug log.");
        LOGGER.info("This is a info log.");
        LOGGER.warn("This is a warn log.");
        LOGGER.error("This is an error log.");
    }
}
