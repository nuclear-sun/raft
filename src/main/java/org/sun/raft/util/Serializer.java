package org.sun.raft.util;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Serializer {

    ByteBuffer serialize(Object object) throws IOException;

    Object deserialize(ByteBuffer buffer) throws IOException;

}
