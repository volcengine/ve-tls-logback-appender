package com.volcengine.service.tls.logback;

import com.volcengine.model.tls.LogItem;
import com.volcengine.model.tls.producer.CallBack;
import com.volcengine.model.tls.producer.Result;

public class LogbackAppenderCallback<E> implements CallBack {
    protected LogbackAppender<E> logbackAppender;
    protected String projectID;
    protected String topicID;
    protected String source;
    protected String filename;
    protected LogItem logItem;

    public LogbackAppenderCallback(LogbackAppender<E> logbackAppender, String projectID, String topicID,
                                   String source, String filename, LogItem logItem) {
        super();
        this.logbackAppender = logbackAppender;
        this.projectID = projectID;
        this.topicID = topicID;
        this.source = source;
        this.filename = filename;
        this.logItem = logItem;
    }

    @Override
    public void onComplete(Result result) {
    }
}
