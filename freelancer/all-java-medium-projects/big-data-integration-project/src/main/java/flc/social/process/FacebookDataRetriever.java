package flc.social.process;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Comment;
import com.restfb.types.Comments;
import com.restfb.types.Post;
import flc.social.dao.redis.MetadataRedisDao;
import flc.social.model.CommentData;
import flc.social.service.FacebookDataService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public class FacebookDataRetriever extends AbstractProcess {

    static final Logger LOGGER = LogManager.getLogger(FacebookDataRetriever.class);

    public FacebookDataRetriever() throws ConfigurationException {
    }

    public List<CommentData> readAndCleanDataSource() throws Exception {
        List<CommentData> commentData = new ArrayList<CommentData>();
        String pageId = "bbcnews";
        FacebookClient facebookClient = FacebookDataService.getFacebookClient();
        String nextPageUrl = MetadataRedisDao.load().getNextPageUrl(pageId);
        LOGGER.info("Start to retrieve and clean data from Facebook/" + pageId + " nextPageUrl: " + nextPageUrl);
        Connection<Post> bbcnewsPost = facebookClient.fetchConnection("bbcnews/posts", Post.class,
                Parameter.with("limit", 10));

        for(Post post : bbcnewsPost.getData()) {
            String postId = post.getObjectId();
            Comments comments = post.getComments();
            if(null != comments) {
                for(Comment comment :comments.getData()) {
                    CommentData cmtData = new CommentData()
                            .setChannel("facebook")
                            .setCommentId(comment.getId())
                            .setContent(comment.getMessage())
                            .setOwnerId("-")
                            .setParentId(postId)
                            .setPublishedTime(comment.getCreatedTime().getTime())
                            .setType("main");
                    commentData.add(cmtData);
                }
            }
        }
//        System.out.println(bbcPage.getData().get(0).getComments().getData().get(0));
//        System.out.println(" d" + bbcPage.getNextPageUrl());
//        bbcPage = facebookClient.fetchConnectionPage(bbcPage.getNextPageUrl(), Post.class);
//
//        System.out.println(bbcPage.getData().size());
//        System.out.println(bbcPage.getData().get(0).getComments().getData().get(0));
        return commentData;
    }

}
