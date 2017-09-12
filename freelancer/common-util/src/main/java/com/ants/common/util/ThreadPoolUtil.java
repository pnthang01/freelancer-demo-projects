package com.ants.common.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by thangpham on 08/09/2017.
 */
public class ThreadPoolUtil {
    private static ThreadPoolUtil _instance;
    private ScheduledExecutorService threadPool;

    public ThreadPoolUtil() {
        threadPool = Executors.newScheduledThreadPool(20);
        ShutdownHookCleanUp shutdownHook = ShutdownHookCleanUp.load();
        shutdownHook.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit("ThreadPoolUtil", threadPool));
    }

    public static ThreadPoolUtil load() {
        if (null == _instance) {
            synchronized (ThreadPoolUtil.class) {
                _instance = new ThreadPoolUtil();
            }
        }
        return _instance;
    }

    public static void shutdown() {
        if (null != _instance) {
            _instance.threadPool.shutdown();
        }
    }

    public void addThread(Runnable runnable, long delay, long period, TimeUnit tu) {
        if (period < 0) {
            threadPool.schedule(runnable, delay, tu);
        } else {
            threadPool.scheduleWithFixedDelay(runnable, delay, delay, tu);
        }
    }
}
