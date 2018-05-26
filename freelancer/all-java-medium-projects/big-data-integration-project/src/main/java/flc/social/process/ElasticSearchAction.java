package flc.social.process;

import com.ants.common.config.ElasticSearchConfiguration;
import com.ants.common.model.KafkaRecord;
import flc.social.model.CommentData;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Segnal on 9/16/17.
 */
public class ElasticSearchAction extends AbstractProcess {

    //Logger to log working step
    static final Logger LOGGER = LogManager.getLogger(ElasticSearchAction.class);

    //Load default Elasticsearch cluster
    private static final String CLUSTER_NAME = "social-app";

    private String index;
    private String type;

    /**
     * Initialize index and index's type to index comment data to
     * @throws ConfigurationException
     */
    public ElasticSearchAction() throws ConfigurationException {
        index = ElasticSearchConfiguration.load().getIndex(CLUSTER_NAME);
        type = ElasticSearchConfiguration.load().getType(CLUSTER_NAME);
    }

//    public static void main(String[] args) throws Exception {
//        new ElasticSearchAction().readAndCleanDataSource();
//    }

    /**
     * Retrieve all data from Kafka and index it to Elasticsearch
     * Method to be called by Application Starter.
     * @throws Exception
     */
    @Override
    public void readAndCleanDataSource() throws Exception {
        List<KafkaRecord> kafkaRecordList = getDataFromKafka(); // consumer data from kafka
        List<CommentData> commentDataList = new ArrayList<>();
        try(TransportClient client = ElasticSearchConfiguration.load().getClient(CLUSTER_NAME)) { // get TransportClient
            for (KafkaRecord kafkaRecord : kafkaRecordList) {
                LOGGER.info("Data receive: " + kafkaRecord.getValue());
                CommentData commentData = gson.fromJson(kafkaRecord.getValue(), CommentData.class);
                commentDataList.add(commentData);
            }
            putData(commentDataList, client, index, type);
        }
    }

    /**
     * Index list of #CommentData to #index and index #type with specified Elasticsearch client
     * @param models
     * @param client
     * @param index
     * @param type
     * @throws UnknownHostException
     */
    public void putData(List<CommentData> models, TransportClient client, String index, String type) throws UnknownHostException {
        try {
            LOGGER.info("Insert data to elasticsearch ...");
            for (CommentData model : models) {
                IndexRequest indexRequest = new IndexRequest(index, type, model.getCommentId());
                indexRequest.source(gson.toJson(model));
                IndexResponse response = client.index(indexRequest).actionGet(); // insert to elasticsearch
                LOGGER.info("Add success, see: "+response.toString());
            }
            LOGGER.info("Insert data to elasticsearch success, total "+models.size()+" records.");
        } catch (Exception e) {
            LOGGER.error("Error when insert data to elasticsearch, error: ", e);
        }
    }
}
