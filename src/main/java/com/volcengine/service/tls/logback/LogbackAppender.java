package com.volcengine.service.tls.logback;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import com.volcengine.model.tls.LogItem;
import com.volcengine.model.tls.exception.LogException;
import com.volcengine.model.tls.producer.ProducerConfig;
import com.volcengine.service.tls.Producer;
import com.volcengine.service.tls.ProducerImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

public class LogbackAppender<E> extends UnsynchronizedAppenderBase<E> {
    private String endpoint;
    private String region;
    private String accessKeyId;
    private String accessKeySecret;
    private String userAgent = "logback";

    private String projectID;
    private String topicID;
    private String source;
    private String filename;

    protected ProducerConfig producerConfig;
    protected Producer producer;

    private int totalSizeInBytes;
    private int maxThreadCount;
    private long maxBlockMs;
    private int maxBatchSizeBytes;
    private int maxBatchCount;
    private int lingerMs;
    private int retryCount;
    private int maxReservedAttempts;

    protected Encoder<E> encoder;
    protected String timeZone = "UTC";
    protected String timeFormat = "yyyy-MM-dd'T'HH:mmZ";
    protected DateTimeFormatter formatter;
    protected java.time.format.DateTimeFormatter defaultFormatter;
    private String mdcFields;
    protected int maxThrowable = 500;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getTopicID() {
        return topicID;
    }

    public void setTopicID(String topicID) {
        this.topicID = topicID;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(int totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public long getMaxBlockMs() {
        return maxBlockMs;
    }

    public void setMaxBlockMs(long maxBlockMs) {
        this.maxBlockMs = maxBlockMs;
    }

    public int getMaxBatchSizeBytes() {
        return maxBatchSizeBytes;
    }

    public void setMaxBatchSizeBytes(int maxBatchSizeBytes) {
        this.maxBatchSizeBytes = maxBatchSizeBytes;
    }

    public int getMaxBatchCount() {
        return maxBatchCount;
    }

    public void setMaxBatchCount(int maxBatchCount) {
        this.maxBatchCount = maxBatchCount;
    }

    public int getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(int lingerMs) {
        this.lingerMs = lingerMs;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxReservedAttempts() {
        return maxReservedAttempts;
    }

    public void setMaxReservedAttempts(int maxReservedAttempts) {
        this.maxReservedAttempts = maxReservedAttempts;
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    public String getMdcFields() {
        return mdcFields;
    }

    public void setMdcFields(String mdcFields) {
        this.mdcFields = mdcFields;
    }

    @Override
    public void start() {
        try {
            try {
                formatter = DateTimeFormat.forPattern(timeFormat).withZone(DateTimeZone.forID(timeZone));
            } catch (Exception e) {
                defaultFormatter = java.time.format.DateTimeFormatter.ofPattern(timeFormat).withZone(ZoneId.of(timeZone));
            }

            producerConfig = new ProducerConfig(endpoint, region, accessKeyId, accessKeySecret);
            setProducerConfig();
            producer = new ProducerImpl(producerConfig);
            producer.start();
            super.start();
        } catch (Exception e) {
            addError("Failed to start LogbackAppender.", e);
        }
    }

    private void setProducerConfig() throws LogException {
        if (totalSizeInBytes > 0) {
            this.producerConfig.setTotalSizeInBytes(totalSizeInBytes);
        }
        if (maxThreadCount > 0) {
            this.producerConfig.setMaxThreadCount(maxThreadCount);
        }
        if (maxBlockMs > 0) {
            this.producerConfig.setMaxBlockMs(maxBlockMs);
        }
        if (maxBatchSizeBytes > 0) {
            this.producerConfig.setMaxBatchSizeBytes(maxBatchSizeBytes);
        }
        if (maxBatchCount > 0) {
            this.producerConfig.setMaxBatchCount(maxBatchCount);
        }
        if (lingerMs > 0) {
            this.producerConfig.setLingerMs(lingerMs);
        }
        if (retryCount > 0) {
            this.producerConfig.setRetryCount(retryCount);
        }
        if (maxReservedAttempts > 0) {
            this.producerConfig.setMaxReservedAttempts(maxReservedAttempts);
        }
    }

    @Override
    public void stop() {
        try {
            if (!isStarted()) {
                return;
            }
            super.stop();
            producer.close();
        } catch (Exception e) {
            addError("Failed to stop LogbackAppender.", e);
        }
    }

    @Override
    public void append(E eventObject) {
        try {
            appendEvent(eventObject);
        } catch (Exception e) {
            addError("Failed to append event.", e);
        }
    }

    private void appendEvent(E eventObject) {
        if (!(eventObject instanceof LoggingEvent)) {
            return;
        }
        LoggingEvent event = (LoggingEvent) eventObject;

        LogItem logItem = new LogItem();
        logItem.setTime(event.getTimeStamp() / 1000);

        if (formatter != null) {
            DateTime dateTime = new DateTime(event.getTimeStamp());
            logItem.addContent("time", dateTime.toString(formatter));
        } else {
            Instant instant = Instant.ofEpochMilli(event.getTimeStamp());
            logItem.addContent("time", defaultFormatter.format(instant));
        }

        logItem.addContent("level", event.getLevel().toString());
        logItem.addContent("thread", event.getThreadName());
        logItem.addContent("message", event.getFormattedMessage());
        StackTraceElement[] caller = event.getCallerData();
        if (caller != null && caller.length > 0) {
            logItem.addContent("location", caller[0].toString());
        }

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            StringBuilder throwable = new StringBuilder(this.getExceptionInfo(throwableProxy));

            do {
                throwable.append(this.fullDump(throwableProxy.getStackTraceElementProxyArray()));
                throwableProxy = throwableProxy.getCause();
                if (throwableProxy != null) {
                    throwable.append("\n\nCaused by:").append(this.getExceptionInfo(throwableProxy));
                }
            } while (throwableProxy != null);

            String throwableSub;
            if (throwable.length() > maxThrowable) {
                throwableSub = throwable.substring(0, maxThrowable);
            } else {
                throwableSub = throwable.toString();
            }

            logItem.addContent("throwable", throwableSub);
        }

        if (this.encoder != null) {
            logItem.addContent("log", new String(this.encoder.encode(eventObject)));
        }

        // mdcFields can be "*" or format of "fieldA,FieldB,fieldC"
        if (mdcFields != null && mdcFields.trim().equals("*")) {
            // "*" matches all fields, add all fields to item
            event.getMDCPropertyMap().forEach((key, value) -> logItem.addContent(key, value));
        } else {
            Optional.ofNullable(mdcFields).ifPresent(f -> event.getMDCPropertyMap().entrySet().stream()
                    .filter(v -> Arrays.stream(f.split(",")).anyMatch(i -> i.equals(v.getKey())))
                    .forEach(map -> logItem.addContent(map.getKey(), map.getValue()))
            );
        }

        try {
            producer.sendLogV2("", topicID, source, filename, logItem,
                    new LogbackAppenderCallback<E>(this, projectID, topicID, source, filename, logItem));
        } catch (Exception ignored) {
        }
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();

        return (message != null) ? (s + ": " + message) : s;
    }

    private String fullDump(StackTraceElementProxy[] stackTraceElementProxyArray) {
        StringBuilder builder = new StringBuilder();

        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            builder.append(CoreConstants.TAB).append(step.toString());
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }

        return builder.toString();
    }
}
