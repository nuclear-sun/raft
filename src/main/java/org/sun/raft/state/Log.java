package org.sun.raft.state;

public interface Log {

    LogEntry getLastLogEntry();
    LogEntry findLogEntry(long index, long term);
    boolean insertLogEntry(LogEntry entry);
}
