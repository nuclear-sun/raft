package org.sun.raft;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.raft.common.TimedWaiter;
import org.sun.raft.rpc.*;
import org.sun.raft.state.StateMachine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {

        //0. 获取配置
        if (args.length < 1) {
            logger.error("No property specified.");
            System.exit(-1);
        }
        String fileName = args[0];
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(fileName));
        } catch (IOException e) {
            logger.error("No property correctly specified.");
            System.exit(-1);
        }

        //1. 实例化状态
        StateMachine stateMachine = new StateMachine();

        //2. 内部 RPC 服务
        RaftServiceImpl raftServiceImpl = new RaftServiceImpl(stateMachine);
        RaftService.Processor processor = new RaftService.Processor(raftServiceImpl);
        RpcServer rpcServer = new RpcServer();
        rpcServer.setProcessor(processor);
        new Thread("Thread-rpc_server") {
            @Override
            public void run() {
                try {
                    rpcServer.serve();
                    logger.info("RPC Server started.");
                } catch (TException e) {
                    logger.error("RPC server start failed. " + e.getMessage(), e);
                    System.exit(-1);
                }
            }
        }.start();

        List<String> hostPortList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String hostPort = properties.getProperty("server." + (i+1));
            if(hostPort != null) {
                hostPortList.add(hostPort);
            }
        }
        RaftClientsProxy clientsProxy = new RaftClientsProxy(hostPortList);


        //3. 面向 client 的服务

        //4. 主循环
        Random random = new Random();
        long candidateRandBase = 150;
        long candidateRandLength = 150;
        while (true) {
            StateMachine.Role role = stateMachine.getRole();

            if (role == StateMachine.Role.FOLLOWER) {
                TimedWaiter followerTimer = new TimedWaiter(300);
                try {
                    followerTimer.await();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    System.exit(-1);
                }
                stateMachine.setRole(StateMachine.Role.CANDIDATE);
            } else if (role == StateMachine.Role.CANDIDATE) {
                float rand = random.nextFloat();
                long timeout = candidateRandBase + (long) (candidateRandLength * rand);
                TimedWaiter candidateTimer = new TimedWaiter(timeout);
                try {
                    candidateTimer.await();
                } catch (InterruptedException e) {
                    logger.info("Elect canceled");
                    continue;
                }
                stateMachine.increCurrentTerm();
            } else if (role == StateMachine.Role.LEADER) {

            }
        }


    }
}
