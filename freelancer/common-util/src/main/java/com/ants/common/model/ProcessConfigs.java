package com.ants.common.model;

/**
 * Created by thangpham on 14/09/2017.
 */
public class ProcessConfigs {

    private String processClassPath;
    private String params;
    private String processName;
    private long delay = 3;
    private long period = 5;
    private String scheduler;

    public ProcessConfigs() {
    }

    public ProcessConfigs(String processClassPath, String params, String processName,
                          long delay, long period, String scheduler) {
        this.processClassPath = processClassPath;
        this.params = params;
        this.processName = processName;
        this.delay = delay;
        this.period = period;
        this.scheduler = scheduler;
    }

    public String getProcessClassPath() {
        return processClassPath;
    }

    public void setProcessClassPath(String processClassPath) {
        this.processClassPath = processClassPath;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

}