package com.ants.common.config;

import com.ants.common.processor.KafkaLogHandler;
import com.ants.common.processor.KafkaLogReceiver;
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
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getKafkaProducersConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
        logHandlers = new ConcurrentHashMap();
        topicPartition = new HashMap();
        requestCount = new AtomicInteger();
        executor = Executors.newScheduledThreadPool(10);
        shutdownHook = ShutdownHookCleanUp.load();
        shutdownHook.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit(KafkaLogHandler.class.getName(), executor));
    }

    public KafkaLogReceiver getKafkaReceiver(String topicNaming) throws Exception {
        List<KafkaLogReceiver> handlerList = new ArrayList<>();
        handlerList = logHandlers.get(topicNaming);
        if (null == handlerList || handlerList.isEmpty()) {
            synchronized (logHandlers) {
                if (logHandlers.get(topicNaming) == null || logHandlers.get(topicNaming).isEmpty()) {
                    handlerList = new ArrayList();
                    String topic = config.getString("data.kafka.producer." + topicNaming + ".topic");
                    if (null == topic || topic.isEmpty()) {
                        LOGGER.error("Topic " + topic + " are not configured, please check your kafka-consumers-configs.properties");
                        return null;
                    }
                    int producerSize = config.getInt("data.kafka.consumer." + topicNaming + ".consumer.size", 5);
                    int delta = 400;
                    int handlerSize = (int) Math.ceil((double) producerSize / (double) 5);
                    for (int i = 0; i < handlerSize; ++i) {
                        KafkaLogReceiver handler = new KafkaLogReceiver(producerSize, topic, topicNaming);
                        handlerList.add(handler);
                        delta += 200;
                        executor.scheduleWithFixedDelay(handler, delta, 400, TimeUnit.MILLISECONDS);
                    }
                    logHandlers.put(topicNaming, handlerList);
                } else {
                    handlerList = logHandlers.get(topicNaming);
                }
            }
        }
        return handlerList.get(requestCount.getAndIncrement() % handlerList.size());
    }

    public KafkaConsumer<String, String> initNewConsumer(String topicNaming) {
        String brokers = config.getString("data.kafka.consumer." + topicNaming + ".brokers");
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", config.getString("data.kafka.group.id"));
        props.put("enable.auto.commit", config.getString("data.kafka.enable.auto.commit"));
        props.put("auto.commit.interval.ms", config.getString("data.kafka.auto.commit.interval.ms"));
        props.put("session.timeout.ms", config.getString("data.kafka.session.timeout.ms"));
        props.put("key.deserializer", config.getString("data.kafka.key.deserializer"));
        props.put("value.deserializer", config.getString("data.kafka.value.deserializer"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topicNaming));
        LOGGER.info("Subscribed to topic " + topicNaming);
        return consumer;
    }
}
