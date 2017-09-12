package com.ants.common.config;

import com.ants.common.processor.KafkaLogHandler;
import com.ants.common.util.ShutdownHookCleanUp;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thangpham on 12/09/2017.
 */
public class KafkaProducerConfiguration {

    static final Logger LOGGER = LogManager.getLogger(KafkaProducerConfiguration.class);
    private static KafkaProducerConfiguration _instance = null;
    private Configuration config = null;

    private Map<String, Integer> topicPartition;
    private ConcurrentMap<String, List<KafkaLogHandler>> logHandlers;
    private AtomicInteger requestCount;
    private ShutdownHookCleanUp shutdownHook;

    private ScheduledExecutorService executor;

    public static KafkaProducerConfiguration load() throws ConfigurationException {
        if (null == _instance) {
            synchronized (KafkaProducerConfiguration.class) {
                _instance = new KafkaProducerConfiguration();
            }
        }
        return _instance;
    }

    public KafkaProducerConfiguration() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder
                = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties()
                        .setFileName(BaseConfiguration.getKafkaProducersConfigFile())
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
        config = builder.getConfiguration();
        logHandlers = new ConcurrentHashMap();
        topicPartition = new HashMap();
        executor = Executors.newScheduledThreadPool(10);
        shutdownHook = ShutdownHookCleanUp.load();
        shutdownHook.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit(KafkaLogHandler.class.getName(), executor));
    }


    public int getLogBatchSize() {
        return config.getInt("data.kafka.batch.size");
    }

    public int getPartition(String topicNaming) {
        Integer partition = topicPartition.get(topicNaming);
        if (null == partition || partition == 0) {
            partition = config.getInt("data.kafka.producer." + topicNaming + ".partition");
            topicPartition.put(topicNaming, partition);
        }
        return partition;
    }

    public KafkaLogHandler getKafkaHandler(String topicNaming) throws Exception {
        List<KafkaLogHandler> handlerList = new ArrayList<>();
        handlerList = logHandlers.get(topicNaming);
        if (null == handlerList || handlerList.isEmpty()) {
            synchronized (logHandlers) {
                if (logHandlers.get(topicNaming) == null || logHandlers.get(topicNaming).isEmpty()) {
                    handlerList = new ArrayList();
                    String topic = config.getString("data.kafka.producer." + topicNaming + ".topic");
                    if (null == topic || topic.isEmpty()) {
                        LOGGER.error("Topic " + topic + " are not configured, please check your kafka-producers-configs.properties");
                        return null;
                    }
                    int producerSize = config.getInt("data.kafka.producer." + topicNaming + ".producer.size", 5);
                    int delta = 400;
                    int handlerSize = (int) Math.ceil((double) producerSize / (double) 5);
                    for (int i = 0; i < handlerSize; ++i) {
                        KafkaLogHandler handler = new KafkaLogHandler(producerSize, topic, topicNaming);
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

    public Producer<String, String> initNewProducer(String topicNaming) {
        String brokers = config.getString("data.kafka.producer." + topicNaming + ".brokers");
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("acks", config.getString("data.kafka.acks"));
        props.put("retries", config.getInt("data.kafka.retries"));
        props.put("batch.size", config.getInt("data.kafka.batch.size"));
        props.put("linger.ms", config.getInt("data.kafka.linger.ms"));
        props.put("timeout.ms", config.getString("data.kafka.timeout.ms"));
        props.put("buffer.memory", config.getLong("data.kafka.buffer.memory"));
        props.put("key.serializer", config.getString("data.kafka.key.serializer"));
        props.put("value.serializer", config.getString("data.kafka.value.serializer"));
        props.put("producer.type", config.getString("data.kafka.producer.type"));
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        return producer;
    }
}
