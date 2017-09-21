package flc.social.process;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Comment;
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

    // input: access_token, version
    // output: CommentData data Object

    static final Logger LOGGER = LogManager.getLogger(FacebookDataRetriever.class);

    public FacebookDataRetriever() throws ConfigurationException {
    }

    public static void main(String[] args) throws Exception {
        new FacebookDataRetriever().readAndCleanDataSource();
    }

    // create a CommentData object with input params
    public void readAndCleanDataSource() throws Exception {
        String pageId = "bbcnews";
        FacebookClient facebookClient = FacebookDataService.getFacebookClient();
        String nextPageUrl = MetadataRedisDao.load().getNextPageUrl(pageId);
        Parameter limit = Parameter.with("limit", 50);
        LOGGER.info("Start to retrieve and clean data from Facebook/" + pageId + " nextPageUrl: " + nextPageUrl);
        Connection<Post> bbcnewsPost = nextPageUrl == null ? facebookClient.fetchConnection("bbcnews/posts", Post.class, Parameter.with("limit", 5))
                : facebookClient.fetchConnectionPage(nextPageUrl, Post.class);
        for (Post post : bbcnewsPost.getData()) {
            String postId = post.getId();
            Connection<Comment> comments = facebookClient.fetchConnection(postId + "/comments", Comment.class, limit);
            for (Comment comment : comments.getData()) {
                CommentData cmtData = new CommentData()
                        .setChannel("facebook")
                        .setCommentId(comment.getId())
                        .setContent(comment.getMessage())
                        .setOwnerId("-")
                        .setParentId(postId)
                        .setPublishedTime(comment.getCreatedTime().getTime())
                        .setType("main");
                addComment(cmtData); // producer to kafka
                Connection<Comment> replies = facebookClient.fetchConnection(comment.getId() + "/comments", Comment.class, limit);
                for (Comment reply : replies.getData()) {
                    CommentData replyData = new CommentData()
                            .setChannel("facebook")
                            .setCommentId(reply.getId())
                            .setContent(reply.getMessage())
                            .setOwnerId("-")
                            .setParentId(comment.getId())
                            .setPublishedTime(comment.getCreatedTime().getTime())
                            .setType("reply");
                    addComment(replyData); // producer to kafka
                }
            }
        }
        nextPageUrl = bbcnewsPost.getNextPageUrl();
        MetadataRedisDao.load().setNextPageUrl(pageId, nextPageUrl);

    }

}
