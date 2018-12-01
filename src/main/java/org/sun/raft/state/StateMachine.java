package org.sun.raft.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.io.Writer;

import java.io.IOException;
import java.util.List;

public class StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

    private long currentTerm = 0;
    private int votedFor = -1;
    private List<LogEntry> logList;
    private long commitIndex;
    private long lastApplied;
    private long[] nextIndex;
    private long[] matchIndex;

    private final Writer writer = new Writer();

    public boolean setCurrentTerm(long currentTerm) {
        if (currentTerm <= this.currentTerm) {
            return true;
        }
        String fileName = "currentTerm";
        try {
            writer.writeObject(fileName, Long.valueOf(currentTerm));
        } catch (IOException e) {
            logger.error("Persist currentTerm failed_" + e.getMessage(), e);
            return false;
        } finally {
            try {
                writer.close(fileName);
            } catch (IOException e) {
                logger.error("Close file currentTerm failed_" + e.getMessage(), e);
            }
        }
        this.currentTerm = currentTerm;
        return true;
    }

    public long getCurrentTerm() {
        return this.currentTerm;
    }

    public boolean setVoteFor(int votedFor){
        String fileName = "votedFor";
        try {
            writer.writeObject(fileName, Integer.valueOf(votedFor));
        } catch (IOException e) {
            logger.error("Persist votedFor failed_" + e.getMessage(), e);
            return false;
        } finally {
            try {
                writer.close(fileName);
            } catch (IOException e) {
                logger.error("Close file votedFor failed_" + e.getMessage(), e);
            }
        }
        this.votedFor = votedFor;
        return true;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public boolean appendLog(final LogEntry logEntry) {
        String fileName = "logList";
        try {
            writer.writeObject(fileName, logEntry);
        } catch (IOException e) {
            logger.error("Persist votedFor failed_" + e.getMessage(), e);
            return false;
        }
        logList.add(logEntry);
        return true;
    }
}
