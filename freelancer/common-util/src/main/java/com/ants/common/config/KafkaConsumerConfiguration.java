package com.ants.common.config;

import com.ants.common.processor.KafkaLogReceiver;
import com.ants.common.util.MethodUtil;
import com.ants.common.util.ShutdownHookCleanUp;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dientt on 9/15/17.
 */
public class KafkaConsumerConfiguration {
    static final Logger LOGGER = LogManager.getLogger(KafkaConsumerConfiguration.class);
    private static KafkaConsumerConfiguration _instance = null;
    private Configuration config = null;

    private Map<String, Integer> topicPartition;
    private ConcurrentMap<String, List<KafkaLogReceiver>> logHandlers;
    private AtomicInteger requestCount;
    private ShutdownHookCleanUp shutdownHook;

    private ScheduledExecutorService executor;

    public static KafkaConsumerConfiguration load() throws ConfigurationException {
        if (null == _instance) {
            synchronized (KafkaConsumerConfiguration.class) {
                _instance = new KafkaConsumerConfiguration();
            }
        }
        return _instance;
    }

    public KafkaConsumerConfiguration() throws ConfigurationException {
        LOGGER.info("############ 2 - 1");
        Parameters params = new Parameters();
        LOGGER.info("############ 2 - 2");
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getKafkaProducersConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        LOGGER.info("############ 2 - 3");
        config = builder.getConfiguration();
        LOGGER.info("############ 2 - 4");
        logHandlers = new ConcurrentHashMap();
        LOGGER.info("############ 2 - 5");
        topicPartition = new HashMap();
        LOGGER.info("############ 2 - 6");
        requestCount = new AtomicInteger();
        LOGGER.info("############ 2 - 7");
        executor = Executors.newScheduledThreadPool(10);
        LOGGER.info("############ 2 - 8");
        shutdownHook = ShutdownHookCleanUp.load();
        LOGGER.info("############ 2 - 9");
        shutdownHook.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit(KafkaLogReceiver.class.getName(), executor));
        LOGGER.info("############ 2 - 10");
    }

    public KafkaLogReceiver getKafkaReceiver(String topicNaming) throws Exception {
        LOGGER.info("############ 1");
        List<KafkaLogReceiver> handlerList = new ArrayList<>();
        LOGGER.info("############ 2");
        handlerList = logHandlers.get(topicNaming);
        LOGGER.info("############ 3: "+ MethodUtil.toJson(logHandlers));
        if (null == handlerList || handlerList.isEmpty()) {
            LOGGER.info("############ 4");
            synchronized (logHandlers) {
                LOGGER.info("############ 5");
                if (logHandlers.get(topicNaming) == null || logHandlers.get(topicNaming).isEmpty()) {
                    LOGGER.info("############ 6");
                    handlerList = new ArrayList();
                    LOGGER.info("############ 7");
                    String topic = config.getString("data.kafka.producer." + topicNaming + ".topic");
                    LOGGER.info("############ 8: "+topic);
                    if (null == topic || topic.isEmpty()) {
                        LOGGER.error("Topic " + topic + " are not configured, please check your kafka-consumers-configs.properties");
                        return null;
                    }
                    LOGGER.info("############ 9");
                    int consumerSize = config.getInt("data.kafka.consumer." + topicNaming + ".size", 5);
                    LOGGER.info("############ 10: "+consumerSize);
                    int delta = 400;
                    int handlerSize = (int) Math.ceil((double) consumerSize / (double) 5);
                    LOGGER.info("############ 11: "+handlerSize);
                    for (int i = 0; i < handlerSize; ++i) {
                        LOGGER.info("############ 12: "+i);
                        KafkaLogReceiver handler = new KafkaLogReceiver(consumerSize, topic, topicNaming);
                        LOGGER.info("############ 13: "+i);
                        handlerList.add(handler);
                        LOGGER.info("############ 14: "+i);
                        delta += 200;
                        executor.scheduleWithFixedDelay(handler, delta, 400, TimeUnit.MILLISECONDS);
                    }
                    LOGGER.info("############ 15: ");
                    logHandlers.put(topicNaming, handlerList);
                    LOGGER.info("############ 16: ");
                } else {
                    LOGGER.info("############ 17: ");
                    handlerList = logHandlers.get(topicNaming);
                    LOGGER.info("############ 18: ");
                }
            }
        }
        LOGGER.info("############ 19: "+MethodUtil.toJson(handlerList));
        return handlerList.get(requestCount.getAndIncrement() % handlerList.size());
    }

    public KafkaConsumer<String, String> initNewConsumer(String topicNaming) {
        LOGGER.info("############ 01: "+topicNaming);
        String brokers = config.getString("data.kafka.consumer." + topicNaming + ".brokers");
        LOGGER.info("############ 02xxxx: bootstrap.servers: "+brokers);
        LOGGER.info("############ 02: group.id: "+config.getString("data.kafka.group.id"));
        LOGGER.info("############ 03: enable.auto.commit: "+config.getString("data.kafka.enable.auto.commit"));
        LOGGER.info("############ 04: auto.commit.interval.ms: "+config.getString("data.kafka.auto.commit.interval.ms"));
        LOGGER.info("############ 05: session.timeout.ms: "+config.getString("data.kafka.session.timeout.ms"));
        LOGGER.info("############ 06: key.deserializer: "+config.getString("data.kafka.key.deserializer"));
        LOGGER.info("############ 07: value.deserializer: "+config.getString("data.kafka.value.deserializer"));
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", config.getString("data.kafka.group.id"));
        props.put("enable.auto.commit", config.getString("data.kafka.enable.auto.commit"));
        props.put("auto.commit.interval.ms", config.getString("data.kafka.auto.commit.interval.ms"));
        props.put("session.timeout.ms", config.getString("data.kafka.session.timeout.ms"));
        props.put("key.deserializer", config.getString("data.kafka.key.deserializer"));
        props.put("value.deserializer", config.getString("data.kafka.value.deserializer"));

        LOGGER.info("############ 08: ");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        LOGGER.info("############ 09: ");
        consumer.subscribe(Arrays.asList(topicNaming));
        LOGGER.info("############ 10:Subscribed to topic " + topicNaming);

        return consumer;
    }
}
