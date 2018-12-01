package org.sun.raft.state;

import java.util.List;

public class StateMachine {
    private long currentTerm;
    private int votedFor;
    private List<LogEntry> logList;
    private long commitIndex;
    private long lastApplied;
    private long[] nextIndex;
    private long[] matchIndex;

    public StateMachine() {
    }

    public void setCurrentTerm(long currentTerm) {
    }
}
