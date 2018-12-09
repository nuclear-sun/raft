package org.sun.raft.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TimedWaiterTest {

    @Test
    public void testReset() throws Exception {
        TimedWaiter waiter = new TimedWaiter(300);
        Thread t= new Thread() {
            public void run() {
                for(int i=0;i<3;i++) {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    waiter.reset();
                }
            }
        };
        long begin = System.currentTimeMillis();
        waiter.start();
        waiter.reset();
        t.start();
        waiter.await();
        assertTrue(System.currentTimeMillis() - begin - 750 < 5);
    }

    @Test
    public void testAwait() throws Exception {
        TimedWaiter waiter = new TimedWaiter(300);
        waiter.start();
        long begin = System.currentTimeMillis();
        waiter.await();
        assertTrue((System.currentTimeMillis() - begin) % 300 < 5);
    }

    @Test
    public void testCancel() throws Exception {
        TimedWaiter waiter = new TimedWaiter(3000);
        waiter.start();
        long begin = System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waiter.cancel();
            }
        }.start();
        boolean isInteruptted = false;
        try {
            waiter.await();
        } catch (InterruptedException e) {
            isInteruptted = true;
        }
        assertTrue(System.currentTimeMillis() - begin < 1005);
        assertTrue(isInteruptted);

        TimedWaiter waiter2 = new TimedWaiter(3000);
        waiter2.start();
        begin = System.currentTimeMillis();
        waiter2.cancel();
        isInteruptted = false;
        InterruptedException ex = null;
        try {
            waiter2.await();
        } catch (InterruptedException e) {
            ex = e;
            isInteruptted = true;
        }
        assertTrue(System.currentTimeMillis() - begin < 5);
        assertTrue(isInteruptted);
        assertTrue(ex.getMessage().contains("already canceled"));
    }
}