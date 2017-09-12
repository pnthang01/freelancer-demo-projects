package flc.social.process;

import com.ants.common.config.KafkaProducerConfiguration;
import com.ants.common.model.KafkaRecord;
import com.ants.common.processor.KafkaLogHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public abstract class AbstractProcess implements Runnable {

    private KafkaProducerConfiguration kafkaLoader;
    private String kafkaTopic;

    public AbstractProcess() throws ConfigurationException {
        this.kafkaLoader = KafkaProducerConfiguration.load();
    }

    public abstract List<String> readDataSource();

    public void run(){

    }

    public void sendKafka(List<String> dataList) throws Exception {
        int partition = kafkaLoader.getPartition(kafkaTopic);
        KafkaLogHandler handler = null;

        handler = kafkaLoader.getKafkaHandler(kafkaTopic);
//        handler.addLog(new KafkaRecord(dataPartition, null, MethodUtil.toJson(obj)));
    }
}
