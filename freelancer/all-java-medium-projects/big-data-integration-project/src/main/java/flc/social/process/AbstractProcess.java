package flc.social.process;

import com.ants.common.config.KafkaConsumerConfiguration;
import com.ants.common.config.KafkaProducerConfiguration;
import com.ants.common.model.KafkaRecord;
import com.ants.common.processor.KafkaLogHandler;
import com.ants.common.processor.KafkaLogReceiver;
import com.ants.druid.util.MethodUtil;
import com.google.gson.Gson;
import flc.social.model.CommentData;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by thangpham on 12/09/2017.
 */
public abstract class AbstractProcess implements Runnable {

    static final Logger LOGGER = LogManager.getLogger(AbstractProcess.class);
    private KafkaProducerConfiguration kafkaProducerLoader;
    private KafkaConsumerConfiguration kafkaConsumerLoader;
    List<CommentData> commentDataList;
    protected String kafkaTopic = "social_retrieved";
    protected Gson gson = new Gson();

    public AbstractProcess() throws ConfigurationException {
        this.kafkaProducerLoader = KafkaProducerConfiguration.load();
        this.kafkaConsumerLoader = KafkaConsumerConfiguration.load();
        commentDataList = new ArrayList<CommentData>();
    }

    public abstract void readAndCleanDataSource() throws Exception;

    public void run() {
        try {
            readAndCleanDataSource();
        } catch (Exception ex) {
            LOGGER.error("Error when retrieving and cleaning social data.", ex);
        }
    }

    public void addComment(CommentData commentData) throws Exception {
        commentDataList.add(commentData);
        if(commentDataList.size() >= 50) {
            LOGGER.info("Just receive 50 messages, start to send to Kafka");
            sendKafka(commentDataList);
            commentDataList.clear();
        }
    }

    public void sendKafka(List<CommentData> dataList) throws Exception {
        int partition = kafkaProducerLoader.getPartition(kafkaTopic);
        KafkaLogHandler handler = kafkaProducerLoader.getKafkaHandler(kafkaTopic);
        for (CommentData data : dataList) {
            int dataPartition = (int) data.getPublishedTime() % partition;
            handler.addLog(new KafkaRecord(dataPartition, null, MethodUtil.toJson(data)));
        }
    }

    public List<KafkaRecord> getDataFromKafka() throws Exception {
        List<KafkaRecord> kafkaRecordList = new ArrayList<>();
        LOGGER.info("Access to Kafka with topic: "+kafkaTopic);
        KafkaLogReceiver kafkaLogReceiver = kafkaConsumerLoader.getKafkaReceiver(kafkaTopic);
        Queue<KafkaRecord> recordQueue = kafkaLogReceiver.getLog();
        KafkaRecord kafkaRecord = null;
        while((kafkaRecord = recordQueue.poll()) != null) {
            kafkaRecordList.add(kafkaRecord);
        }
        return  kafkaRecordList;
    }
}
