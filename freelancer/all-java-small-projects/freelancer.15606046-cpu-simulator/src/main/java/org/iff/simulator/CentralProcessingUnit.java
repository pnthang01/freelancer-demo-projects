package org.iff.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by thangpham on 12/11/2017.
 */
public class CentralProcessingUnit implements Runnable {

    private static CentralProcessingUnit _instance;

    public synchronized static CentralProcessingUnit _init(String processAlgo) {
        _instance = new CentralProcessingUnit(processAlgo);
        return _instance;
    }

    public synchronized static CentralProcessingUnit _load() {
        if (null == _instance) _instance = new CentralProcessingUnit("FCFS");
        return _instance;
    }

    private boolean isBusy;
    private String processAlgo;
    private long busyTime = 0;
    private int processCount;
    private List<ProcessControlBlock> readyQueue;
    private List<Integer[]> ganttData;

    public CentralProcessingUnit(String processAlgo) {
        this.readyQueue = Collections.synchronizedList(new ArrayList());
        this.ganttData = new ArrayList<Integer[]>();
        this.processAlgo = processAlgo;
    }

    public long getBusyTime() {
        return busyTime;
    }

    public List<Integer[]> getGanttData() {
        return ganttData;
    }

    public int getProcessCount() {
        return processCount;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void addPCB(ProcessControlBlock pcb) {
        if ("SJF".equalsIgnoreCase(processAlgo)) { // SJF algorithm
            int burstTime = pcb.getCpuBurstTime();
            boolean isAdded = false;
            for (int i = 0; i < readyQueue.size(); i++) {
                ProcessControlBlock currPCB = readyQueue.get(i);
                if (burstTime < currPCB.getCpuBurstTime()) {
                    readyQueue.add(i, pcb);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) readyQueue.add(pcb);
        } else { // FCFS algorithm
            readyQueue.add(pcb);
        }
        pcb.setReadyTime(System.currentTimeMillis());
        pcb.setState("ready");
    }

    public void run() {
        while (!readyQueue.isEmpty()) {
            ProcessControlBlock pcb = readyQueue.remove(0);
            pcb.setState("running");
            long c = System.currentTimeMillis();
            isBusy = true;
            pcb.runBurstCPU();
            isBusy = false;
            pcb.setState("completed");
            long pt = System.currentTimeMillis() - c;
            busyTime += pt;
            processCount++;
            ganttData.add(new Integer[]{pcb.getJobId(), pcb.getCpuBurstTime()});
        }
    }

}
