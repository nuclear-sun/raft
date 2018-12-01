namespace java org.sun.raft.rpc

typedef i32 int
typedef i64 long

struct RVResult{
    1: required long term;
    2: required bool voteGranted;
}

struct APResult {
    1: required long term;
    2: required bool success;
}

service RaftService {
    RVResult requestVote(1:long term, 2:int candidateId, 3:long lastLogIndex, 4:long lastLogTerm);
    APResult appendEntries(1:long term, 2:int leaderId, 3:long prevLogIndex, 4:long prevLogTerm,
        5:list<string> entries, 6:long leaderCommit)
}