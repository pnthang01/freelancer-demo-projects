package flc.social.process;

import com.ants.common.config.KafkaProducerConfiguration;
import com.ants.common.model.KafkaRecord;
import com.ants.common.processor.KafkaLogHandler;
import com.ants.druid.util.MethodUtil;
import flc.social.model.CommentData;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public abstract class AbstractProcess implements Runnable {

    static final Logger LOGGER = LogManager.getLogger(AbstractProcess.class);
    private KafkaProducerConfiguration kafkaLoader;
    List<CommentData> commentDataList;
    private String kafkaTopic = "social_retrieved";

    public AbstractProcess() throws ConfigurationException {
        this.kafkaLoader = KafkaProducerConfiguration.load();
        commentDataList = new ArrayList<CommentData>();
    }

    public abstract void readAndCleanDataSource() throws Exception;

    public void run() {
        try {
            readAndCleanDataSource();
        } catch (Exception ex) {
            LOGGER.error("Error when retrieving and cleaning social data.");
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
        int partition = kafkaLoader.getPartition(kafkaTopic);
        KafkaLogHandler handler = kafkaLoader.getKafkaHandler(kafkaTopic);
        for (CommentData data : dataList) {
            int dataPartition = (int) data.getPublishedTime() % partition;
            handler.addLog(new KafkaRecord(dataPartition, null, MethodUtil.toJson(data)));
        }
    }
}
