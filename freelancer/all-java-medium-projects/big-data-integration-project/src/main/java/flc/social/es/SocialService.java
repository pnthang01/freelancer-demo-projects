package flc.social.es;

import com.google.gson.Gson;
import flc.social.models.SocialModel;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by dientt on 9/14/17.
 */
public class SocialService implements ISocialService {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress host = InetAddress.getLocalHost();
        Settings settings = Settings.builder()
                .put("node.name", "node-1")
                .put("cluster.name", "social-app").build();
        TransportClient client = new PreBuiltTransportClient(settings).addTransportAddresses(new InetSocketTransportAddress(host, 9300));
        SocialModel model = new SocialModel();
        model.setOwnerId("owner_id_test");
        model.setParentId("parent_id_test");
        model.setPublishedTime(System.currentTimeMillis());
        IndexRequest indexRequest = new IndexRequest("social","facebook", model.getOwnerId());
        indexRequest.source(new Gson().toJson(model));
        IndexResponse response = client.index(indexRequest).actionGet();
        client.close();
        System.out.println(">>>>>>>>>>>> <<<<<<<<<<<<<<");
    }

    public void putData(List<SocialModel> socialModels) throws UnknownHostException {

    }
}
