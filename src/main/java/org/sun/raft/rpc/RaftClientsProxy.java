package org.sun.raft.rpc;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.rpc.RaftService.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class RaftClientsProxy {

    private static final Logger logger = LoggerFactory.getLogger(RaftClientsProxy.class);
    private final List<Client> clientList = new ArrayList<>();
    private final List<RequestVoteCallable> requestVoteCallableList = new ArrayList<>();
    private final List<AppendEntriesCallable> appendEntriesCallableList = new ArrayList<>();

    private static final int REQUEST_VOTE_TIMEOUT = 1000;
    private static final int APPEND_ENTRY_TIMEOUT = 1000;

    private int numClients = -1;

    // TPS 过高可能导致 OOM
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    private static class RequestVoteCallable implements Callable<RVResult> {
        private final RaftService.Client client;

        private RequestVoteParams params;

        public RequestVoteCallable(RaftService.Client client) {
            this.client = client;
        }

        public void setParams(final RequestVoteParams params) {
            this.params = params;
        }

        @Override
        public RVResult call() throws Exception {
            return client.requestVote(params.term, params.candidateId, params.lastLogIndex, params.lastLogTerm);
        }
    }

    private static class RequestVoteParams {
        public final long term;
        public final int candidateId;
        public final long lastLogIndex;
        public final long lastLogTerm;

        public RequestVoteParams(long term, int candidateId, long lastLogIndex, long lastLogTerm) {
            this.term = term;
            this.candidateId = candidateId;
            this.lastLogIndex = lastLogIndex;
            this.lastLogTerm = lastLogTerm;
        }
    }
    private static class AppendEntriesCallable implements Callable<APResult> {
        private final RaftService.Client client;
        private AppendEntriesParams params;

        public AppendEntriesCallable(RaftService.Client client) {
            this.client = client;
        }

        public void setParams(final AppendEntriesParams params) {
            this.params = params;
        }

        @Override
        public APResult call() throws Exception {
            return this.client.appendEntries(params.term, params.leaderId, params.prevLogIndex,
                    params.prevLogTerm, params.entries, params.leaderCommit);
        }
    }
    private static class AppendEntriesParams {
        private final long term;
        private final int leaderId;
        private final long prevLogIndex;
        private final long prevLogTerm;
        private final List<String> entries;
        private final long leaderCommit;

        public AppendEntriesParams(long term, int leaderId, long prevLogIndex, long prevLogTerm, List<String> entries,
                                   long leaderCommit) {
            this.term = term;
            this.leaderId = leaderId;
            this.prevLogIndex = prevLogIndex;
            this.prevLogTerm = prevLogTerm;
            this.entries = entries;
            this.leaderCommit = leaderCommit;
        }
    }

    public RaftClientsProxy(List<String> hostPortList) {
        this.numClients = hostPortList.size();
        for (String hostPort : hostPortList) {
            String[] hostPortPair = hostPort.split(":");
            try {
                RaftService.Client client = RpcClientFactory.newRpcClient(hostPortPair[0],
                        Integer.valueOf(hostPortPair[1]), RaftService.Client.class);
                clientList.add(client);
                requestVoteCallableList.add(new RequestVoteCallable(client));
            } catch (Exception e) {
                // 个别某个 client 连接失败是正常的！
                logger.error("Initializing raft client failed. {}", e.getMessage(), e);
                System.exit(-1);
            }
        }
    }

    public int getNumClients() {
        return this.numClients;
    }

    // 代理各个客户端的 rpc 请求，始终返回 null
    public boolean requestVote(long term, int candidateId, long lastLogIndex, long lastLogTerm) throws TException,
            InterruptedException {
        RequestVoteParams params = new RequestVoteParams(term, candidateId, lastLogIndex, lastLogTerm);
        CompletionService<RVResult> completionService = new ExecutorCompletionService<>(executorService);
        for (RequestVoteCallable callable : requestVoteCallableList) {
            callable.setParams(params);
        }
        List<Future<RVResult>> resultList = executorService.invokeAll(requestVoteCallableList,
                REQUEST_VOTE_TIMEOUT, TimeUnit.MILLISECONDS);
        int agreeCount = 0;
        int rejectCount = 0;
        int majority = numClients / 2 + 1;
        for (Future<RVResult> future : resultList) {
            try {
                RVResult result = future.get();
                if (result.voteGranted) {
                    if (++agreeCount >= majority) {
                        break;
                    }
                } else {
                    if (++rejectCount >= majority) {
                        break;
                    }
                }
            } catch (CancellationException e) {
            } catch (ExecutionException e) {
                // 如何向外层继续抛出 ？
            }
        }
        if (agreeCount >= majority) {
            return true;
        }
        return false;
    }

    public List<Future<APResult>> appendEntries(long term, int leaderId, long prevLogIndex, long prevLogTerm,
                                                List<String> entries, long leaderCommit) throws TException {
        AppendEntriesParams params = new AppendEntriesParams(term, leaderId, prevLogIndex, prevLogTerm,
                entries, leaderCommit);
        List<Future<APResult>> futureList = new ArrayList<>();
        for (AppendEntriesCallable callable : appendEntriesCallableList) {
            callable.setParams(params);
            Future<APResult> apResultFuture = executorService.submit(callable);
            futureList.add(apResultFuture);
        }
        return futureList;
    }
}
