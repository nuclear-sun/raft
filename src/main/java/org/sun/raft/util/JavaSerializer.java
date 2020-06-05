package org.sun.raft.util;

import java.io.*;
import java.nio.ByteBuffer;

public class JavaSerializer implements Serializer {

    public JavaSerializer() {
    }

    @Override
    public ByteBuffer serialize(Object object) throws IOException {

        ByteBufferOutputStream buffer = new ByteBufferOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(buffer);

        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return buffer.toByteBuffer();
    }

    /** TODO unfinished **/
    @Override
    public Object deserialize(ByteBuffer buffer) throws IOException {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer.array());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        return null;
    }

    private static class ByteBufferOutputStream extends ByteArrayOutputStream {

        private static final int DEFAULT_BUFFER_SIZE = 1024;

        public ByteBufferOutputStream() {
            super(DEFAULT_BUFFER_SIZE);
        }

        public ByteBufferOutputStream(int capacity) {
            super(capacity);
        }

        public ByteBuffer toByteBuffer() {
            return ByteBuffer.wrap(this.buf, 0, count);
        }
    }

}
