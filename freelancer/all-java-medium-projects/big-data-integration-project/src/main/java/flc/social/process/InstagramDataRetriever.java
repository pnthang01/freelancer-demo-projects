package flc.social.process;

import com.ants.common.util.HttpRequestClientUtil;
import com.google.api.services.youtube.model.CommentSnippet;
import flc.social.model.CommentData;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public class InstagramDataRetriever extends AbstractProcess {

    static final Logger LOGGER = LogManager.getLogger(InstagramDataRetriever.class);

    private static final String ACCESS_TOKEN = "6025652265.5e3f695.b09897a2e7264f4d986545a50bf8b101";

    private static final String MEDIA_SEARCH = "https://api.instagram.com/v1/media/search?lat=51.505244&lng=-0.018848&scope=public_content&distance=100000&access_token=";
//    private static final String TEST_MEDIA = "https://api.instagram.com/v1/tags/nofilter/media/recent?access_token=";
    private static final String TEST_MEDIA = "https://api.instagram.com/v1/users/self/?access_token=";

    public static void main(String[] args) throws Exception {
        new InstagramDataRetriever().readAndCleanDataSource();
    }

    public InstagramDataRetriever() throws ConfigurationException {
    }


    @Override
    public List<CommentData> readAndCleanDataSource() throws Exception {
        LOGGER.info("Start to read data from Instagram");
        System.out.println(HttpRequestClientUtil.load().executeGet(MEDIA_SEARCH + ACCESS_TOKEN));
        return null;
    }
    private CommentData buildCommentData(CommentSnippet snippet, String parentId, String cmtId, String type) {
        return new CommentData()
                .setCommentId(cmtId)
                .setParentId(parentId)
                .setOwnerId(snippet.getAuthorDisplayName())
                .setChannel("youtube")
                .setType(type)
                .setContent(snippet.getTextOriginal())
                .setPublishedTime(snippet.getPublishedAt().getValue());
    }
}
