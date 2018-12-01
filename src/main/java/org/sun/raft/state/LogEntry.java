package org.sun.raft.state;

public class LogEntry {
    private long index;
    private long term;
    private String command;

    public LogEntry() {
    }
}
