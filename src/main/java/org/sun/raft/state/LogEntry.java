package org.sun.raft.state;

import java.io.Serializable;

public final class LogEntry implements Serializable,Cloneable{
    private final long term;
    private final String command;

    public LogEntry(long term, String command) {
        this.term = term;
        this.command = command;
    }

    public long getTerm() {
        return term;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "term=" + term +
                ", command='" + command + '\'' +
                '}';
    }
}
