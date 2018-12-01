package org.sun.raft.rpc;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class RpcServer {
    private static final int DEFAULT_PORT = 5055;
    private int port;
    private TProcessor processor;

    public RpcServer(int port) {
        this.port = port;
    }

    public RpcServer() {
        this(5055);
    }

    public void setProcessor(TProcessor processor) {
        this.processor = processor;
    }

    public void serve() throws TException {
        TServerTransport transport = new TServerSocket(this.port);
        Args args = (Args)(new Args(transport)).processor(this.processor);
        TServer server = new TThreadPoolServer(args);
        server.serve();
    }
}
