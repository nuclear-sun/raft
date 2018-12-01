package org.sun.raft.rpc;

import java.util.List;
import org.apache.thrift.TException;
import org.sun.raft.rpc.RaftService.Iface;

public class RaftServiceImpl implements Iface {
    public RaftServiceImpl() {
    }

    public RVResult requestVote(long term, int candidateId, long lastLogIndex, long lastLogTerm) throws TException {
        return null;
    }

    public APResult appendEntries(long term, int leaderId, long prevLogIndex, long prevLogTerm, List<String> entries, long leaderCommit) throws TException {
        return null;
    }
}
