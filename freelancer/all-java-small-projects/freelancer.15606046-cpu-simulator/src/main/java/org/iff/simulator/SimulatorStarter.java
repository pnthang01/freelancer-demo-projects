package org.iff.simulator;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by thangpham on 12/11/2017.
 */
public class SimulatorStarter {

    public static int[] CPU_BURST_TIME = new int[]{30, 35, 40, 45, 50, 55, 60, 65, 70, 75};
    public static int IO_REQUEST_TIME = 60;

    public static void main(String[] args) {
        String processAlgo = "FCFS";
        int totalJobs = 10, startedTime = 0, uniRand = 200;
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("pa")) {
                processAlgo = args[i + 1];
            } else if (args[i].equals("nj")) {
                totalJobs = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("st")) {
                startedTime = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("ur")) {
                uniRand = Integer.parseInt(args[i + 1]);
            }
        }
        try {
            CountDownLatch cd = new CountDownLatch(totalJobs);
            Random rand = new Random();
            ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(12);
            //
            CentralProcessingUnit cpu = CentralProcessingUnit._init(processAlgo);
            scheduled.scheduleAtFixedRate(cpu, 0, 1, TimeUnit.MILLISECONDS);
            scheduled.scheduleAtFixedRate(IOChannelHandler._load(), 0, 1, TimeUnit.MILLISECONDS);
            //
            List<ScheduledFuture> futureList = new ArrayList<ScheduledFuture>();
            long start = System.currentTimeMillis();
            for (int i = 0; i < totalJobs; i++) {
                long executedTime = rand.nextInt(uniRand) + startedTime;
                ProcessControlBlockWrapper pcbWrapper = new ProcessControlBlockWrapper(i + 1, cd);
                ScheduledFuture<Statistic> schedule = scheduled.schedule(pcbWrapper, executedTime, TimeUnit.MILLISECONDS);
                futureList.add(schedule);
            }
            while (!cd.await(1, TimeUnit.MILLISECONDS)) {
            }
            double totalTime = System.currentTimeMillis() - start;
            scheduled.shutdown();
            double throughput = (totalJobs * CPU_BURST_TIME.length) / totalTime;
            double cpuUtil = (double) cpu.getBusyTime() / totalTime;
            //
            long totalTAT = 0, totalWT = 0;
            StringBuilder sb = new StringBuilder("Process\tWT\tTAT\n");
            for (ScheduledFuture<Statistic> future : futureList) {
                Statistic statistic = future.get();
                totalTAT += statistic.tat;
                totalWT += statistic.wt;
                sb.append(statistic.jobId).append("\t")
                        .append(statistic.wt).append("\t")
                        .append(statistic.tat).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            String totalStatistic = "CPU_Utilization\tThroughput(per.ms)\tTurnaround_Time\tWaiting_Time\n" +
                    MethodUtil.nf.format(cpuUtil * 100) + "\t" + MethodUtil.nf.format(throughput) + "\t" +
                    totalTAT + "\t" + totalWT + "\n" +
                    "\n===========PROCESSES STATISTICS=============\n";
            sb.insert(0, totalStatistic);
            //
            String fileName = "./output/" + processAlgo + "-tabular-" + MethodUtil.df.format(start) + ".txt";
            MethodUtil.writeDataToFile(fileName, sb.toString());
            //Export Gantt data
            sb = new StringBuilder();
            List<Integer[]> ganttData = cpu.getGanttData();
            int totalDotTime = 0;
            StringBuilder sb2 = new StringBuilder("Process\tBurst_Time\n");
            for (Integer[] entry : ganttData) {
                int jobId = entry[0], bt = entry[1];
                sb2.append("P").append(jobId).append("\t").append(bt).append("\n");
                sb.append("P").append(jobId).append(":");
                int dotTime = bt / 5;
                for(int i = 0; i < totalDotTime; i++) sb.append(" ");
                for(int i = 0; i < dotTime; i++) sb.append(".");
                totalDotTime += dotTime;
                sb.append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb2.append("\n=============== Gantt chart =============\n");
            String ganttFileName = "./output/" + processAlgo + "-grantt-" + MethodUtil.df.format(start) + ".txt";
            MethodUtil.writeDataToFile(ganttFileName, sb2.append(sb.toString()).toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static class ProcessControlBlockWrapper implements Callable<Statistic> {

        private int jobId;
        private CountDownLatch cd;

        public ProcessControlBlockWrapper(int jobId, CountDownLatch cd) {
            this.jobId = jobId;
            this.cd = cd;
        }

        public Statistic call() {
            IOChannelHandler ioChannel = IOChannelHandler._load();
            CentralProcessingUnit cpu = CentralProcessingUnit._load();
            long tat = 0, wt = 0;
            for (int burstTime : CPU_BURST_TIME) {
                ProcessControlBlock pcb = new ProcessControlBlock(jobId);
                pcb.setCpuBurstTime(burstTime);
                pcb.setIoRequestTime(IO_REQUEST_TIME);
                long s = System.currentTimeMillis();
                MethodUtil.logInfo("Job " + jobId + " entered the system with burst time " + burstTime);
                //
                ioChannel.addPCB(pcb);
                pcb.waitCompleted();
                //
                cpu.addPCB(pcb);
                pcb.waitCompleted();
                MethodUtil.logInfo("Job " + jobId + " is done with burst time " + burstTime + " in " +
                        (System.currentTimeMillis() - s));
                wt += System.currentTimeMillis() - pcb.getReadyTime();
                tat += System.currentTimeMillis() - pcb.getArrivalTime();
            }
            cd.countDown();
            return new Statistic(jobId, tat, wt);
        }
    }

    private static class Statistic {
        private int jobId;
        private long tat;
        private long wt;

        public Statistic(int jobId, long tat, long wt) {
            this.jobId = jobId;
            this.tat = tat;
            this.wt = wt;
        }
    }
}
