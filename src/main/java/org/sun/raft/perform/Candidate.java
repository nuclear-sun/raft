package org.sun.raft.perform;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.rpc.RVResult;
import org.sun.raft.rpc.RaftClientsProxy;
import org.sun.raft.state.Log;
import org.sun.raft.state.StateMachine;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Candidate {
    private static final Logger logger = LoggerFactory.getLogger(Candidate.class);

    private final StateMachine stateMachine;
    private final RaftClientsProxy proxy;
    private volatile Thread electThread;

    ExecutorService service = Executors.newFixedThreadPool(20);

    public Candidate(final StateMachine stateMachine, final RaftClientsProxy proxy) {
        this.stateMachine = stateMachine;
        this.proxy = proxy;
    }

    // is it safe to retrieve the state ?
    public void elect() {
        this.electThread = Thread.currentThread();
        long term = stateMachine.increCurrentTerm();
        int id = stateMachine.getId();
        Log log = stateMachine.getLog();
        long lastLogIndex = -1L;
        long lastLogTerm = -1L;
        synchronized (log) {
            lastLogIndex = log.getLogSize() - 1;
            lastLogTerm = log.getLastLogEntry().getTerm();
        }
        try {
            List<Future<RVResult>> futureList = proxy.requestVote(term, id, lastLogIndex, lastLogTerm);
            AtomicInteger vote = new AtomicInteger(0);
            AtomicInteger reject = new AtomicInteger(0);
            int majority = proxy.getNumClients()/2 + 1;
            CountDownLatch voteCountDownLatch = new CountDownLatch(majority);
            CountDownLatch rejectCountDownLatch = new CountDownLatch(majority);
            for(Future<RVResult> future: futureList) {
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RVResult result = future.get(1, TimeUnit.SECONDS);
                            if (result != null) {
                                // 多线程异步更新
                                if(result.term > stateMachine.getCurrentTerm()) {
                                    stateMachine.setCurrentTerm(term);
                                }
                                if(result.voteGranted) {
                                    vote.incrementAndGet();
                                    voteCountDownLatch.countDown();
                                } else {
                                    reject.incrementAndGet();
                                    //rejectCountDownLatch.countDown();
                                }
                            }
                        } catch (ExecutionException | TimeoutException | InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                });
            }
            // 等待一段时间
            try {
                voteCountDownLatch.await();
                // 获得多数投票
                stateMachine.setRole(StateMachine.Role.LEADER);
            } catch (InterruptedException e) {
                logger.info("Elect interrupted. {}", e.getMessage(), e);
            }
        } catch (TException e) {
            logger.error(e.getMessage());
        }
    }

    public void interrupt() {
        this.electThread.interrupt();
    }
}
