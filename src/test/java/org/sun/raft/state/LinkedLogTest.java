package org.sun.raft.state;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.*;

public class LinkedLogTest {

    private LinkedLog log  = new LinkedLog();

    @Test
    public void testInsertLogEntry() {
        //log.insertLogEntry(new LogEntry())
        //List<Integer> list = new LinkedList<>();
        //list.add(1, 2);
    }

    @Test
    public void testGetLastLogEntry() {
    }

    @Test
    public void testFindLogEntry() {
    }
}