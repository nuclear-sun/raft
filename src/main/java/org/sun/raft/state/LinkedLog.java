package org.sun.raft.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.io.Writer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Not thread safe
 *
 * An infinite list storing log entry, part of it is in memory.
 */
public class LinkedLog implements Log {

    private static final Logger logger = LoggerFactory.getLogger(LinkedLog.class);

    private final LinkedList<LogEntry> logList = new LinkedList<>();
    private long baseIndex = 0;

    private final Writer writer = new Writer();
    private static final String persistFile = "logList";

    private int realIndex(long index) {
        return (int)(index - baseIndex);
    }

    // TODO writer insert not implemented
    @Override
    public boolean insertLogEntry(long index, LogEntry logEntry) {
        int realIndex = realIndex(index);
        if(realIndex < 0 || realIndex > logList.size()) {
            return false;
        }
        try {
            writer.writeObject(persistFile, logEntry);
        } catch (IOException e) {
            logger.error("Persist {} failed: {}", logEntry.toString(), e.getMessage(), e);
            return false;
        }
        logList.add(realIndex, logEntry);
        return true;
    }

    @Override
    public boolean appendLogEntry(LogEntry entry) {
        return insertLogEntry(baseIndex + (long)logList.size(), entry);
    }

    @Override
    public LogEntry getLastLogEntry() {
        if (logList.size() < 1) {
            return null;
        }
        return logList.get(logList.size() - 1);
    }

    @Override
    public long getLogSize() {
        return baseIndex + logList.size();
    }

    @Override
    public LogEntry getLogEntry(long index) {
        int realIndex = (int)(index - baseIndex);
        if(realIndex < 0 || realIndex >= logList.size()){
            return null;
        }
        LogEntry logEntry = logList.get(realIndex);
        return logEntry;
    }

    @Override
    // TODO persistent
    public void removeLogEntryAfter(long index) {
        int beginIndex = realIndex(index);
        while (beginIndex < logList.size() - 1) {
            logList.removeLast();
        }
    }
}
