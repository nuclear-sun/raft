package org.sun.raft.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.io.Writer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

    private Role role = Role.FOLLOWER;
    private long currentTerm = 0;
    private int votedFor = -1;
    private final Log log = new LinkedLog();
    private long commitIndex;
    private long lastApplied;
    private long[] nextIndex;
    private long[] matchIndex;

    private final Writer writer = new Writer();

    public Role getRole() {
        return this.role;
    }

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

    public Log getLog() {
        return this.log;
    }

    public long getCommitIndex() {
        return this.commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public enum Role {
        FOLLOWER, CANDIDATE, LEADER;
        public Role valueOf(int value) {
            return Role.values()[value];
        }
    }

}
