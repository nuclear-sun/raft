package org.sun.raft.state;

import java.io.Serializable;

public class LogEntry implements Serializable,Cloneable{
    private long index;
    private long term;
    private String command;

    public LogEntry() {
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "index=" + index +
                ", term=" + term +
                ", command='" + command + '\'' +
                '}';
    }
}
