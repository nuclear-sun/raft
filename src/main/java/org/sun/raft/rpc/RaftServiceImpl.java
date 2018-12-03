package org.sun.raft.rpc;

import java.util.List;
import org.apache.thrift.TException;
import org.sun.raft.rpc.RaftService.Iface;
import org.sun.raft.state.StateMachine;

public class RaftServiceImpl implements Iface {

    private final StateMachine stateMachine;

    public RaftServiceImpl(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public RVResult requestVote(long term, int candidateId, long lastLogIndex, long lastLogTerm) throws TException {
        synchronized (stateMachine) {
            RVResult result = new RVResult();
            if (term < stateMachine.getCurrentTerm()) {
                result.term = stateMachine.getCurrentTerm();
                result.voteGranted = false;
                return result;
            }
            // TODO Warning: stateMachine.getVotedFor()==candidateId
            if(stateMachine.getVotedFor()<0 || stateMachine.getVotedFor()==candidateId){
                if(!moreUpdated(stateMachine.getCurrentTerm(), stateMachine.getLastLog().getIndex(),
                        lastLogTerm, lastLogIndex)){
                    result.term = term;
                    result.voteGranted = true;
                    return result;
                }
            }
            result.term = term;
            result.voteGranted = false;
            return result;
        }
    }

    private static boolean moreUpdated(long term1, long index1, long term2, long index2) {
        if(term1>term2) {
            return true;
        }
        if(term1<term2) {
            return false;
        }
        if(index1>index2) {
            return true;
        }
        return false;
    }

    public APResult appendEntries(long term, int leaderId, long prevLogIndex, long prevLogTerm, List<String> entries, long leaderCommit) throws TException {
        synchronized (stateMachine) {
            APResult result = new APResult();
            if(term < stateMachine.getCurrentTerm()) {
                result.term = stateMachine.getCurrentTerm();
                result.success = false;
                return result;
            }

        }
        return null;
    }

    private boolean matchLog(long term, long index) {
        if(
    }


}
