package flc.social.process;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Comment;
import com.restfb.types.Post;
import flc.social.config.MethodUtil;
import flc.social.dao.redis.MetadataRedisDao;
import flc.social.model.CommentData;
import flc.social.service.FacebookDataService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by Segnal on 12/09/2017.
 */
public class FacebookDataRetriever extends AbstractProcess {

    //Logger to log working step
    static final Logger LOGGER = LogManager.getLogger(FacebookDataRetriever.class);

    public FacebookDataRetriever() throws ConfigurationException {
    }

//    public static void main(String[] args) throws Exception {
//        new FacebookDataRetriever().readAndCleanDataSource();
//    }

    /**
     * Main method to be executed every time
     *
     * @throws Exception
     */
    public void readAndCleanDataSource() throws Exception {
        //Set default data to retrieve is bbcnews fanpage
        long beginJob = System.currentTimeMillis();
        String pageId = "bbcnews";
        //Initialize facebook client to retrieve data from
        FacebookClient facebookClient = FacebookDataService.getFacebookClient();
        //Load last query id from Redis
        String nextPageUrl = MetadataRedisDao.load().getNextPageUrl(pageId);
        //Set limit size to 50
        Parameter limit = Parameter.with("limit", 50);
        LOGGER.info("Start to retrieve and clean data from Facebook/" + pageId + " nextPageUrl: " + nextPageUrl);
        //If last query id exists, query from the last id, otherwise query the lastest posts
        long start = System.currentTimeMillis();
        Connection<Post> bbcnewsPost = nextPageUrl == null ? facebookClient.fetchConnection("bbcnews/posts", Post.class, Parameter.with("limit", 5))
                : facebookClient.fetchConnectionPage(nextPageUrl, Post.class);
        List<Post> postData = bbcnewsPost.getData();
        long execTime = System.currentTimeMillis() - start;
        long bbcnewsPostInfo = MethodUtil.getObjectSize(postData);
        LOGGER.info("Total time to get FB Post: " + execTime + " milliseconds has " + bbcnewsPostInfo + " bytes");
        //Loop through retrieved posts
        for (Post post : postData) {
            String postId = post.getId();
            //For every post, retrieve all comments from the post
            start = System.currentTimeMillis();
            Connection<Comment> comments = facebookClient.fetchConnection(postId + "/comments", Comment.class, limit);
            List<Comment> commentData = comments.getData();
            execTime = System.currentTimeMillis() - start;
            long commentsInfo = MethodUtil.getObjectSize(commentData);
            LOGGER.info("Total time to get Post's comments: " + execTime + " milliseconds has " + commentsInfo + " bytes");
            for (Comment comment : commentData) {
                //Parse and clean retrieved comments data
                CommentData cmtData = new CommentData()
                        .setChannel("facebook")
                        .setCommentId(comment.getId())
                        .setContent(comment.getMessage())
                        .setOwnerId("-")
                        .setParentId(postId)
                        .setPublishedTime(comment.getCreatedTime().getTime())
                        .setType("main");
                addComment(cmtData); //Put parsed and clean comment to Kafka
                //For every comment, retrieve all replies from the comment
                start = System.currentTimeMillis();
                Connection<Comment> replies = facebookClient.fetchConnection(comment.getId() + "/comments", Comment.class, limit);
                List<Comment> repliesData = replies.getData();
                execTime = System.currentTimeMillis() - start;
                long repliesInfo = MethodUtil.getObjectSize(repliesData);
                LOGGER.info("Total time to get Comment's replies: " + execTime + " milliseconds has " + repliesInfo + " bytes");
                for (Comment reply : repliesData) {
                    //Parse and clean retrieved replies data
                    CommentData replyData = new CommentData()
                            .setChannel("facebook")
                            .setCommentId(reply.getId())
                            .setContent(reply.getMessage())
                            .setOwnerId("-")
                            .setParentId(comment.getId())
                            .setPublishedTime(comment.getCreatedTime().getTime())
                            .setType("reply");
                    addComment(replyData); //Put parsed and clean reply to Kafka
                }
            }
        }
        nextPageUrl = bbcnewsPost.getNextPageUrl();
        //Set last query id to Redis
        MetadataRedisDao.load().setNextPageUrl(pageId, nextPageUrl);
        LOGGER.info("Total time to complete a batch Facebook data " +
                (System.currentTimeMillis() - beginJob) + " milliseconds");
    }

}
