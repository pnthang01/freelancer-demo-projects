package org.iff.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by thangpham on 12/11/2017.
 */
public class IOChannelHandler implements Runnable {

    private static IOChannelHandler _instance;

    public synchronized static IOChannelHandler _load() {
        if(null == _instance) _instance = new IOChannelHandler();
        return _instance;
    }

    private List<ProcessControlBlock> ioQueue;

    public IOChannelHandler() {
        this.ioQueue = Collections.synchronizedList(new ArrayList());
    }

    public void addPCB(ProcessControlBlock pcb) {
        pcb.setState("ioready");
        ioQueue.add(pcb); //IO Queue is always FCFS
    }

    public void run() {
        CentralProcessingUnit cpu = CentralProcessingUnit._load();
        while(!ioQueue.isEmpty()) {
            ProcessControlBlock pcb = ioQueue.remove(0);
            pcb.setState("running");
            pcb.runIORequest();
            pcb.setState("completed");
        }
    }
}
