package com.ants.common.processor;

import com.ants.common.config.KafkaProducerConfiguration;
import com.ants.common.model.KafkaRecord;
import com.ants.common.model.KafkaRecordCallback;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by thangpham on 12/09/2017.
 */
public class KafkaLogHandler implements Runnable {

    static final Logger LOGGER = LogManager.getLogger(KafkaLogHandler.class);
    private final List<Producer<String, String>> producers;
    private final Queue<KafkaRecord> logsQueue;
    private final String topicNaming;
    private final String topic;
    private long count = 0;

    public KafkaLogHandler(int producerSize, String topic, String topicNaming) throws ConfigurationException {
        this.producers = new ArrayList();
        for (int i = 0; i < producerSize; ++i) {
            Producer<String, String> tmp = KafkaProducerConfiguration.load().initNewProducer(topicNaming);
            this.producers.add(tmp);
        }
        this.logsQueue = new ConcurrentLinkedQueue<>();
        this.topicNaming = topicNaming;
        this.topic = topic;
    }

    public void addLog(KafkaRecord log) {
        logsQueue.add(log);
    }

    @Override
    public void run() {
        if (logsQueue.size() > 0) {
            try {
                //init the batch list with max capacity
                int index = 0;
                Producer<String, String> producer = null;
                while (true) {
                    KafkaRecord log = logsQueue.poll();
                    if (log == null) {
                        break;
                    } else {
                        index = (int) count % producers.size();
                        try {
                            producer = producers.get(index);
                            producer.send(new ProducerRecord<>(topic, log.getPartition(), log.getKey(), log.getValue()),
                                    new KafkaRecordCallback(log) {
                                        @Override
                                        public void onCompletion(KafkaRecord log, RecordMetadata metadata, Exception exception) {
                                            try {
                                                if (exception != null) {
                                                    LOGGER.error("Error ocurred when send log to kafka, message: %s"+ exception.getMessage(), exception);
//                                                    long firstFailed = log.getFirstFailed() == 0 ? System.currentTimeMillis() : log.getFirstFailed();
//                                                    if (System.currentTimeMillis() - firstFailed < 18000000) {
//                                                        String dateHour = DateTimeUtil.formatYYYYMMDDHH(System.currentTimeMillis(), DateTimeUtil.DASH);
//                                                        String logFile = StringUtil.toString(appConfig.getResyncFolder(),
//                                                                File.separator, dateHour, File.separator, appConfig.getNodeName(),
//                                                                "-", dateHour, '-', log.getPartition(), ".log");
//                                                        RawLogUtil.writeRawLog(logFile, null, Arrays.asList(firstFailed, topicNaming,
//                                                                log.getPartition(), log.getValue()), '\t');
//                                                    }
                                                }
                                            } catch (Exception ex) {
                                                LOGGER.error("Error when write data to disk, error: ", ex);
                                            }
                                        }
                                    });
                            count++;
                            if (count % 10000 == 0) {
                                LOGGER.info("Total logs have been injected in topic: " + topic + " is " + count
                                        + ", log remaining: " + logsQueue.size());
                            }
                        } catch (Exception ex) {
                            if (null != producer) {
                                producer.flush();
                                producer.close();
                            }
                            producer = KafkaProducerConfiguration.load().initNewProducer(topicNaming);
                            producers.set(index, producer);
                            LOGGER.info("New Kafka Producer has been initialized, at topic: " + topic + " with count: " + count);
                            LOGGER.error(topic, "Add log to producer fail : ", ex);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(topic, "sendToKafka fail : ", e);
            }
        }
    }
}