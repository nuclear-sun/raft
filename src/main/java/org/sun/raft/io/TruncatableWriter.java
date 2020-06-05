package org.sun.raft.io;


import java.io.IOException;

public interface TruncatableWriter {

    void write(Object object) throws IOException;

    void reset(long offset) throws IOException;
}
