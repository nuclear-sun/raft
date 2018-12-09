package org.sun.raft.rpc;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.rpc.RaftService.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RaftClientsProxy implements RaftService.Iface {

    private static final Logger logger = LoggerFactory.getLogger(RaftClientsProxy.class);

    private final List<Client> clientList = new ArrayList<>();

    private final List<RequestVoteCallable> requestVoteCallableList = new ArrayList<>();

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

    public RaftClientsProxy(List<String> hostPortList) {
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

    // 代理各个客户端的 rpc 请求，始终返回 null
    @Override
    public RVResult requestVote(long term, int candidateId, long lastLogIndex, long lastLogTerm) throws TException {
        RequestVoteParams params = new RequestVoteParams(term, candidateId, lastLogIndex, lastLogTerm);
        List<Future<RVResult>> futureList = new ArrayList<>();
        // 1. 异步发送请求
        for (RequestVoteCallable callable : requestVoteCallableList) {
            callable.setParams(params);
            Future<RVResult> rvResultFuture = executorService.submit(callable);
            futureList.add(rvResultFuture);
        }
        // 2. 处理请求结果
        for (Future<RVResult> future : futureList) {

        }
        return null;
    }

    @Override
    public APResult appendEntries(long term, int leaderId, long prevLogIndex, long prevLogTerm, List<String> entries, long leaderCommit) throws TException {
        return null;
    }
}
