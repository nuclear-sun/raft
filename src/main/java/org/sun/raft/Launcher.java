package org.sun.raft;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.common.TimedWaiter;
import org.sun.raft.rpc.RaftService;
import org.sun.raft.rpc.RaftServiceImpl;
import org.sun.raft.rpc.RpcServer;
import org.sun.raft.state.StateMachine;

import java.util.Random;

public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {

        //1. 实例化状态
        StateMachine stateMachine = new StateMachine();

        //2. 内部 RPC 服务
        RaftServiceImpl raftServiceImpl = new RaftServiceImpl(stateMachine);
        RaftService.Processor processor = new RaftService.Processor(raftServiceImpl);
        RpcServer rpcServer = new RpcServer();
        new Thread("Thread-rpc_server") {
            @Override
            public void run() {
                try {
                    rpcServer.serve();
                    logger.info("RPC Server started.");
                } catch (TException e){
                    logger.error("RPC server start failed. " + e.getMessage(), e);
                    System.exit(-1);
                }
            }
        }.start();

        //3. 面向 client 的服务

        //4. 主循环
        Random random = new Random();
        long candidateRandBase = 150;
        long candidateRandLength = 150;
        while (true) {
            StateMachine.Role role = stateMachine.getRole();

            if(role == StateMachine.Role.FOLLOWER) {
                TimedWaiter followerTimer = new TimedWaiter(300);
                try {
                    followerTimer.await();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    System.exit(-1);
                }
                stateMachine.setRole(StateMachine.Role.CANDIDATE);
            }
            else if (role == StateMachine.Role.CANDIDATE) {
                float rand = random.nextFloat();
                long timeout = candidateRandBase + (long)(candidateRandLength * rand);
                TimedWaiter candidateTimer = new TimedWaiter(timeout);
                try {
                    candidateTimer.await();
                } catch (InterruptedException e) {
                    logger.info("Elect canceled");
                    continue;
                }
                stateMachine.increCurrentTerm();
            }
            else if (role == StateMachine.Role.LEADER) {

            }
        }


    }
}
