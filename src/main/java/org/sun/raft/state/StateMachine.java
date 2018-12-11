package org.sun.raft.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.io.Writer;

import java.io.IOException;

public class StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

    private int id = -1;
    private volatile Role role = Role.FOLLOWER;
    private long currentTerm = 0;
    //private AtomicLong currentTerm = new AtomicLong(0);
    private int votedFor = -1;
    private final Log log = new LinkedLog();
    private long commitIndex;
    private long lastApplied;
    private long[] nextIndex;
    private long[] matchIndex;

    private final Writer writer = new Writer();

    public void setId(int id) {
        if(this.id < 0 && id >= 0) {
            this.id = id;
        }
    }

    public int getId() {
        return this.id;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean setCurrentTerm(long currentTerm) {
        synchronized (this) {
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
    }

    public long getCurrentTerm() {
        synchronized (this) {
            return this.currentTerm;
        }
    }

    public long increCurrentTerm() {
        synchronized (this) {
            return ++this.currentTerm;
        }
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
