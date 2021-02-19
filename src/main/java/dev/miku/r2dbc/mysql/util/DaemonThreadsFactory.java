package dev.miku.r2dbc.mysql.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * a Thread factory that generates daemon threads with given name
 */
public class DaemonThreadsFactory implements ThreadFactory {

    private AtomicInteger threadNumber = new AtomicInteger(1);
    private String name;

    public DaemonThreadsFactory(String name){
        this.name = name;
    }

    public Thread newThread(Runnable r) {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setDaemon(true);
        String threadName = name + "-thread-" + threadNumber.getAndIncrement();
        thread.setName(threadName);
        return thread;
    }
}