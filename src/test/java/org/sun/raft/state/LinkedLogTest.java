package org.sun.raft.state;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.*;

public class LinkedLogTest {

    private LinkedLog log  = new LinkedLog();

    @Test
    public void testAppendLogEntry() {
        boolean result = log.appendLogEntry(new LogEntry(1L, "get sun"));
        assertEquals(result, true);
        String command = log.getLastLogEntry().getCommand();
        assertEquals(command, "get sun");
        assertEquals(log.getLogSize(), 1);
    }

    @Test
    public void testInsertLogEntry() {
        log.appendLogEntry(new LogEntry(0L, "get sun"));
        log.appendLogEntry(new LogEntry(1L, "get sun"));
        log.appendLogEntry(new LogEntry(2L, "get sun"));
        boolean result = log.insertLogEntry(1L, new LogEntry(11L, "set sun zin"));
        assertEquals(result, true);
        assertEquals(log.getLastLogEntry().getTerm(), 2L);
        assertEquals(log.getLogEntry(1L).getTerm(), 11L);
        assertEquals(log.getLogEntry(0L).getTerm(), 0L);
    }

}