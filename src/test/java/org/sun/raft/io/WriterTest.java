package org.sun.raft.io;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class WriterTest {
    @Test
    public void testWriteObject() throws Exception {
        Writer writer = new Writer();
        String fileName = "testfile";
        writer.writeObject(fileName, Long.valueOf(12L));
        Long value = (Long)writer.readObject(fileName);
        assertEquals(value, Long.valueOf(12L));
    }

}