package org.sun.raft.rpc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class RpcClientFactory {
    public RpcClientFactory() {
    }

    public static <T extends TServiceClient> T newRpcClient(String host, int port, Class<T> clazz) throws TTransportException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TTransport transport = new TSocket(host, port);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        Constructor constructor = clazz.getConstructor(TProtocol.class);
        T client = (T)constructor.newInstance(protocol);
        return client;
    }
}
