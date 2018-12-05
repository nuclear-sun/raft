package org.sun.raft.rpc;

import java.util.List;

import org.apache.thrift.TException;
import org.sun.raft.rpc.RaftService.Iface;
import org.sun.raft.state.Log;
import org.sun.raft.state.LogEntry;
import org.sun.raft.state.StateMachine;

public class RaftServiceImpl implements Iface {

    private final StateMachine stateMachine;

    public RaftServiceImpl(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public RVResult requestVote(long term, int candidateId, long lastLogIndex, long lastLogTerm) throws TException {
        synchronized (stateMachine) {
            // If the receiving node hasn't voted yet in this term then it votes for the candidate
            RVResult result = new RVResult();
            if (term <= stateMachine.getCurrentTerm()) {
                result.term = stateMachine.getCurrentTerm();
                result.voteGranted = false;
                return result;
            }
            // TODO Warning vote condition to be verified
            // Now term > stateMachine.getCurrentTerm()
            stateMachine.setCurrentTerm(term);
            // 并不是只要 term 比较大就投票，有限制条件: 谁的 log 更新？
            if (!moreUpdated(stateMachine.getCurrentTerm(), stateMachine.getLog().getLogSize() - 1,
                    lastLogTerm, lastLogIndex)) {
                result.term = term;
                result.voteGranted = true;
                return result;
            }
            result.term = term;
            result.voteGranted = false;
            return result;
        }
    }

    private static boolean moreUpdated(long term1, long index1, long term2, long index2) {
        if (term1 > term2) {
            return true;
        }
        if (term1 < term2) {
            return false;
        }
        if (index1 > index2) {
            return true;
        }
        return false;
    }

    public APResult appendEntries(long term, int leaderId, long prevLogIndex, long prevLogTerm,
                                  List<String> entries, long leaderCommit) throws TException {
        synchronized (stateMachine) {
            APResult result = new APResult();
            if (term < stateMachine.getCurrentTerm()) {
                result.term = stateMachine.getCurrentTerm();
                result.success = false;
                return result;
            }
            // TODO 更新 term ? may be problematic
            if (term > stateMachine.getCurrentTerm()) {
                stateMachine.setCurrentTerm(term);
            }
            // 自己是什么角色？
            if (stateMachine.getRole() != StateMachine.Role.FOLLOWER) {
                result.term = term;
                result.success = false;
                return result;
            }
            // 自己是 follower
            // 1. 重置选举定时器 TODO
            // 2. 添加 log
            Log log = stateMachine.getLog();
            LogEntry logEntry = log.getLogEntry(prevLogIndex);
            if (logEntry != null && logEntry.getTerm() == prevLogTerm) {
                log.removeLogEntryAfter(prevLogIndex);
                if (entries != null) {
                    for (int i = 0, size = entries.size(); i < size; i++) {
                        log.insertLogEntry(prevLogIndex + 1 + i, new LogEntry(term, entries.get(i)));
                    }
                }
                result.term = term;
                result.success = true;
            } else {
                result.term = term;
                result.success = false;
            }

            if (leaderCommit > stateMachine.getCommitIndex()) {
                stateMachine.setCurrentTerm(Math.min(leaderCommit, log.getLogSize() - 1));
            }

            return result;
        }
    }

}
