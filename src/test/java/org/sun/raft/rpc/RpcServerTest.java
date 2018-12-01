package org.sun.raft.rpc;

import org.apache.thrift.TException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RpcServerTest {

    @Test
    public void testServer() {
        RpcServer server = new RpcServer();
        Calculate.Processor processor = new Calculate.Processor(new CalculateImpl());
        server.setProcessor(processor);
        new Thread("Thread-rpc_server") {
            @Override
            public void run() {
                try {
                    server.serve();
                    System.out.println("Rpc server started.");
                } catch (TException e){
                    e.printStackTrace();
                }
            }
        }.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        int result = -1;
        try {
            Calculate.Client client = RpcClientFactory.newRpcClient("localhost", 5055, Calculate.Client.class);
            result = client.calculate();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            server.stop();
        }
        assertEquals(result, 20);
    }

    private static class CalculateImpl implements Calculate.Iface {
        @Override
        public int calculate() throws TException {
            return 20;
        }
    }

}