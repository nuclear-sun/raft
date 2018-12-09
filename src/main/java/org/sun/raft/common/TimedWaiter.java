package org.sun.raft.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TimedWaiter {

    private static final Logger logger = LoggerFactory.getLogger(TimedWaiter.class);

    // duration in milliseconds
    private final long countDownDuration;

    private final Object condition = new Object();

    private Boolean isStopped = false;

    private boolean isCanceled = false;

    private final Thread countDownThread;

    private final List<Thread> waiterThreadList = new ArrayList<>();

    public TimedWaiter(long duration) {
        this.countDownDuration = duration;
        this.countDownThread = new Thread() {
            public void run() {
                while (!isStopped) {
                    try {
                        Thread.sleep(countDownDuration);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    synchronized (condition) {
                        isStopped = true;
                        condition.notifyAll();
                    }
                }
            }
        };
    }

    /**
     * 不可以重复调用
     */
    public void start() {
        countDownThread.start();
    }

    public void reset() {
        countDownThread.interrupt();
    }

    public void cancel() {
        synchronized (condition) {
            isCanceled = true;
            isStopped = true;
            for(Thread t: waiterThreadList) {
                t.interrupt();
            }
        }
    }

    public void await() throws InterruptedException{
        synchronized (condition) {
            if (isCanceled) {
                throw new InterruptedException(this.getClass().getName() + " is already canceled");
            }
            waiterThreadList.add(Thread.currentThread());
            while (!isStopped) {
                condition.wait();
            }
        }
    }
}
