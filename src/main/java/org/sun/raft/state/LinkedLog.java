package org.sun.raft.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.io.Writer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Not thread safe
 */
public class LinkedLog implements Log {

    private static final Logger logger = LoggerFactory.getLogger(LinkedLog.class);

    private final List<LogEntry> logList = new LinkedList<>();
    private long indexDiff = 0;

    private final Writer writer = new Writer();
    private static final String persistFile = "logList";

    @Override
    public boolean insertLogEntry(LogEntry logEntry) {
        try {
            writer.writeObject(persistFile, logEntry);
        } catch (IOException e) {
            logger.error("Persist {} failed: {}", logEntry.toString(), e.getMessage(), e);
            return false;
        }
        int realIndex = (int)(logEntry.getIndex() - indexDiff);
        logList.add(realIndex, logEntry);
        return true;
    }

    @Override
    public LogEntry getLastLogEntry() {
        if (logList.size() < 1) {
            return null;
        }
        return logList.get(logList.size() - 1);
    }

    @Override
    public LogEntry findLogEntry(long index, long term) {
        int realIndex = (int)(index - indexDiff);
        if(realIndex < 0 || realIndex >= logList.size()){
            return null;
        }
        LogEntry logEntry = logList.get(realIndex);
        if(logEntry.getTerm() != term) {
            return null;
        }
        return logEntry;
    }
}
