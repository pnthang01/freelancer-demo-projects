package com.ants.common.processor;

import com.ants.common.config.KafkaConsumerConfiguration;
import com.ants.common.model.KafkaRecord;
import com.ants.common.util.MethodUtil;
import com.google.gson.Gson;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by dientt on 9/16/17.
 */
public class KafkaLogReceiver implements Runnable {

    static final Logger LOGGER = LogManager.getLogger(KafkaLogReceiver.class);
    private final List<KafkaConsumer<String, String>> kafkaConsumers;
    private final Queue<KafkaRecord> logsQueue;
    private final String topicNaming;
    private final String topic;
    private long count = 0;
    private int timeout = 100;

    public KafkaLogReceiver(int consumerSize, String topicNaming, String topic) throws ConfigurationException {
        kafkaConsumers = new ArrayList<>();
        this.logsQueue = new ConcurrentLinkedQueue<>();
        this.topicNaming = topicNaming;
        this.topic = topic;
        for (int i = 0; i < consumerSize; i++) {
            KafkaConsumer<String, String> consumer = KafkaConsumerConfiguration.load().initNewConsumer(topicNaming);
            kafkaConsumers.add(consumer);
        }
    }

    public Queue<KafkaRecord> getLog() {
        return logsQueue;
    }

    @Override
    public void run() {
        int index = 0;
        KafkaConsumer<String, String> consumer = null;
        try {
            index = (int) count % kafkaConsumers.size();
            consumer = kafkaConsumers.get(index);
            ConsumerRecords<String, String> records = consumer.poll(timeout);
            KafkaRecord model = null;
            for (ConsumerRecord<String, String> record : records) {
                LOGGER.info(String.format("Consumer data: offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value()));
//                KafkaRecord model = gson.fromJson(record.value(), KafkaRecord.class);
                model = new KafkaRecord(record.partition(), record.key(), record.value());
                logsQueue.add(model);
            }
            LOGGER.info("logsQueue.size() >>>>>> "+logsQueue.size());
        } catch (Exception e) {
            LOGGER.error("Error when get data from kafka, detail: "+e);
        }
    }

}
