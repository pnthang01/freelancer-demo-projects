package org.iff.simulator;

/**
 * Created by thangpham on 12/11/2017.
 */
public class ProcessControlBlock {

    private int jobId;
    private int cpuBurstTime;
    private int ioRequestTime;
    private String state;
    private long arrivalTime = System.currentTimeMillis();
    private long readyTime;

    public ProcessControlBlock(int jobId) {
        this.jobId = jobId;
    }

    public void runBurstCPU() {
        MethodUtil.logInfo("Job " + jobId + " is about bursting cpu in " + cpuBurstTime + " milliseconds");
        try {
            Thread.sleep(cpuBurstTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        MethodUtil.logInfo("Job " + jobId + " just bursted CPU sucessfully.");
    }

    public void runIORequest() {
//        MethodUtil.logInfo("Job " + jobId + " requests IO in " + ioRequestTime + " miliseconds");
        try {
            Thread.sleep(ioRequestTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        MethodUtil.logInfo("Job " + jobId + " just requested IO sucessfully");
    }

    public long getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(long readyTime) {
        this.readyTime = readyTime;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getCpuBurstTime() {
        return cpuBurstTime;
    }

    public void setCpuBurstTime(int cpuBurstTime) {
        this.cpuBurstTime = cpuBurstTime;
    }

    public int getIoRequestTime() {
        return ioRequestTime;
    }

    public void setIoRequestTime(int ioRequestTime) {
        this.ioRequestTime = ioRequestTime;
    }

    public String getState() {
        return state;
    }

    public void waitCompleted() {

        while (!this.state.equalsIgnoreCase("completed")) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void setState(String state) {
        this.state = state;
        if (this.state.equalsIgnoreCase("completed")) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }


    @Override
    public String toString() {
        return "ProcessControlBlock{" +
                "jobId=" + jobId +
                ", cpuBurstTime=" + cpuBurstTime +
                ", state='" + state + '\'' +
                '}';
    }
}
