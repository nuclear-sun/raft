package org.sun.raft.io;

import org.sun.raft.util.JavaSerializer;
import org.sun.raft.util.Serializer;

import java.io.*;
import java.nio.channels.FileChannel;

public class JavaSerializationWriter implements TruncatableWriter {

    private final FileOutputStream fileOutputStream;
    private final ObjectOutputStream objectOutputStream;
    private final FileChannel channel;

    private final Serializer serializer;

    private long commitPosition;

    public JavaSerializationWriter(File file) throws IOException {
        this.fileOutputStream = new FileOutputStream(file);
        objectOutputStream = new ObjectOutputStream(fileOutputStream);
        channel = fileOutputStream.getChannel();
        serializer = new JavaSerializer();
        commitPosition = 0;
    }

    @Override
    public void write(Object object) throws IOException {
        channel.write(serializer.serialize(object));
        fileOutputStream.getFD().sync();
    }

    @Override
    public void reset(long offset) throws IOException {

        channel.truncate(offset);
    }
}
