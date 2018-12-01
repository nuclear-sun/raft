package org.sun.raft.io;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class WriterTest {
    @Test
    public void testWriteObject() throws Exception {
        Writer writer = new Writer();
        String fileName = "target/testfile";
        for(int i=10;i<1000;i++) {
            writer.writeObject(fileName, Long.valueOf(12L));
        }

        Long value = (Long)writer.readObject(fileName);
        assertEquals(value, Long.valueOf(12L));
        writer.close();
    }

}