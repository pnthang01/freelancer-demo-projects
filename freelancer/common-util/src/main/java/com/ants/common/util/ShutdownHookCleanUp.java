/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ants.common.util;

import com.ants.common.model.IService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author thangpham
 */
public class ShutdownHookCleanUp {

    static final Logger LOGGER = LogManager.getLogger(ShutdownHookCleanUp.class);
    private final Deque<ExecutorCleanUpUnit> cleanUpExecutor = new ArrayDeque<>();
    private final Deque<IService> cleanupService = new ArrayDeque();
    private static ShutdownHookCleanUp _instance;

    public void addExecutor(ExecutorCleanUpUnit executor) {
        cleanUpExecutor.add(executor);
    }

    public void addService(IService service) {
        cleanupService.add(service);
    }

    private ShutdownHookCleanUp() {
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutdownHook());
        LOGGER.info("Initialize shutdown hook cleanup");
    }

    public static ShutdownHookCleanUp load() {
        if (null == _instance) {
            synchronized (ShutdownHookCleanUp.class) {
                _instance = new ShutdownHookCleanUp();
            }
        }
        return _instance;
    }

    public class ShutdownHook extends Thread {

        @Override
        public void run() {
            while (!cleanupService.isEmpty()) {
                try {
                    IService poll = cleanupService.poll();
                    poll.shutdown();
                } catch (Exception ex) {
                    LOGGER.error("Failed to shutdown executor:", ex);
                }
            }
            //
            while (!cleanUpExecutor.isEmpty()) {
                try {
                    ExecutorCleanUpUnit unit = cleanUpExecutor.poll();
                    LOGGER.info("Start to shuting down executor: " + unit.name);
                    if (unit.waitOnShutdown > 0) {
                        Thread.sleep(unit.waitOnShutdown);
                    }
                    LOGGER.info("Start to shuwdown executor: " + unit.name);
                    unit.executor.shutdown();
                    while (!unit.executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    }
                    LOGGER.info("Shutdown the executor successfully.");
                } catch (Exception ex) {
                    LOGGER.error("Failed to shutdown executor:", ex);
                }
            }
        }
    }

    public static class ExecutorCleanUpUnit {

        private String name;
        private ExecutorService executor;
        private long waitOnShutdown = 0;

        public ExecutorCleanUpUnit(String name, ExecutorService executor, long waitOnShutdown) {
            this.name = name;
            this.executor = executor;
            this.waitOnShutdown = waitOnShutdown;
        }

        public ExecutorCleanUpUnit(String name, ExecutorService executor) {
            this.name = name;
            this.executor = executor;
        }

    }

}
