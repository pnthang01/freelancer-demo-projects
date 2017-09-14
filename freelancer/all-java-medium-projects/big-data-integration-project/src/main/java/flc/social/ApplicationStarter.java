package flc.social;

import com.ants.common.model.ProcessConfigs;
import com.ants.common.util.StringUtil;
import com.ants.common.util.ThreadPoolUtil;
import flc.social.dao.redis.MetadataRedisDao;
import flc.social.process.AbstractProcess;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.util.regexp.Base;
import scala.xml.MetaData;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by thangpham on 11/09/2017.
 */
public class ApplicationStarter {

    static final Logger LOGGER = LogManager.getLogger(ApplicationStarter.class);

    private ProcessConfigs processConfigs = new ProcessConfigs();

    public static void main(String[] args) {
        try {
            new ApplicationStarter().startToRun(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    public void startToRun(String[] args) throws ConfigurationException, InterruptedException {
        if (args.length < 1) {
            LOGGER.error("Parameters are missing, please check again.");
        }
        String clusterConfig = null;
        for (int i = 0; i < args.length; i += 2) {
            if ("config".equalsIgnoreCase(args[i])) {
//                ConfigurationConnectionUtil.setBaseConfig(args[i + 1]);
            } else if ("class".equalsIgnoreCase(args[i])) {
                processConfigs.setProcessClassPath(args[i + 1]);
            } else if ("param".equalsIgnoreCase(args[i])) {
                processConfigs.setParams(args[i + 1]);
            } else if ("name".equalsIgnoreCase(args[i])) {
                processConfigs.setProcessName(args[i + 1]);
            } else if ("delay".equalsIgnoreCase(args[i])) {
                processConfigs.setDelay(StringUtil.safeParseInt(args[i + 1]));
            } else if ("period".equalsIgnoreCase(args[i])) {
                processConfigs.setPeriod(StringUtil.safeParseInt(args[i + 1]));
            } else if ("cluster-config".equalsIgnoreCase(args[i])) {
                clusterConfig = args[i + 1];
            } else if ("scheduler".equalsIgnoreCase(args[i])) {
                processConfigs.setScheduler(args[i + 1]);
            }
        }
        //
        try {
            ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
            //Init plugins
            MetadataRedisDao.load().setStopJobToFalse(processConfigs.getProcessName());
            String[] classes = processConfigs.getProcessClassPath().split("-");
            Calendar cal = Calendar.getInstance();
            Date currTime = cal.getTime();
            for (String clazz : classes) {
                AbstractProcess p = (AbstractProcess) Class.forName(clazz).newInstance();
                //
                LOGGER.info("Start to run process: " + processConfigs.getProcessName() + " at class: "
                        + p.getClass().getSimpleName() + ", params: " + processConfigs.getParams());
                if (null == processConfigs.getScheduler()) {
                    if (processConfigs.getPeriod() == 0) {//Run once
                        scheduledThreadPool.schedule(p, processConfigs.getPeriod(), TimeUnit.SECONDS);
                    } else {//Repeatly
                        scheduledThreadPool.scheduleWithFixedDelay(p, processConfigs.getDelay(), processConfigs.getPeriod(), TimeUnit.SECONDS);
                    }
                } else {
                    long delay = 0, period = 0;
                    cal = Calendar.getInstance();
                    String[] split = processConfigs.getScheduler().split(":");
                    if (split.length == 1) {
                        cal.set(Calendar.SECOND, StringUtil.safeParseInt(split[0]));
                        period = (60);
                        if (cal.getTime().compareTo(currTime) <= 0) {
                            cal.add(Calendar.MINUTE, 1);
                        }
                        delay = (int) (cal.getTimeInMillis() - currTime.getTime()) / 1000;
                    }
                    if (split.length == 2) {
                        cal.set(Calendar.MINUTE, StringUtil.safeParseInt(split[0]));
                        cal.set(Calendar.SECOND, StringUtil.safeParseInt(split[1]));
                        period = 3600;
                        if (cal.getTime().compareTo(currTime) <= 0) {
                            cal.add(Calendar.HOUR, 1);
                        }
                        delay = (int) (cal.getTimeInMillis() - currTime.getTime()) / 1000;
                    }
                    if (split.length == 3) {
                        cal.set(Calendar.HOUR_OF_DAY, StringUtil.safeParseInt(split[0]));
                        cal.set(Calendar.MINUTE, StringUtil.safeParseInt(split[1]));
                        cal.set(Calendar.SECOND, StringUtil.safeParseInt(split[2]));
                        period = 86400;
                        if (cal.getTime().compareTo(currTime) <= 0) {
                            cal.add(Calendar.DATE, 1);
                        }
                        delay = (cal.getTimeInMillis() - currTime.getTime()) / 1000;
                    }
                    scheduledThreadPool.scheduleAtFixedRate(p, delay, period, TimeUnit.SECONDS);
                    processConfigs.setDelay(delay);
                    processConfigs.setPeriod(period);
                }
            }
            if (processConfigs.getPeriod() == 0) {
                Thread.sleep(30000);
            } else {
                while (true) {
                    LOGGER.info(String.format("FreeMemory %f M.B, UsedMemory: %f M.B, TotalMemory: %f M.B",
                            (double) (Runtime.getRuntime().freeMemory() / 1048576),
                            (double) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576),
                            (double) (Runtime.getRuntime().totalMemory() / 1048576)), true);
                    Thread.sleep(15000);
                    if (MetadataRedisDao.load().checkStopJob(processConfigs.getProcessName())) {
                        LOGGER.info("Received stop job command, stopping now...");
                        break;
                    }
                }
            }
            scheduledThreadPool.shutdown();
            while (!scheduledThreadPool.awaitTermination(15, TimeUnit.SECONDS)) {
                LOGGER.info("Wait for the process is finished...");
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot start to run " + processConfigs.getProcessClassPath() + " with error: ", ex);
        }
        Thread.sleep(5000);
        ThreadPoolUtil.load().shutdown();
        LOGGER.info("Finish run process: " + processConfigs.getProcessName() + " at class: " + processConfigs.getProcessClassPath());
    }


}
