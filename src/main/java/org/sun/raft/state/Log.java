package org.sun.raft.state;

public interface Log {

    LogEntry getLastLogEntry();
    long getLogSize();
    LogEntry getLogEntry(long index);
    boolean insertLogEntry(long index, LogEntry entry);
    boolean appendLogEntry(LogEntry entry);
    void removeLogEntryAfter(long index);
}
