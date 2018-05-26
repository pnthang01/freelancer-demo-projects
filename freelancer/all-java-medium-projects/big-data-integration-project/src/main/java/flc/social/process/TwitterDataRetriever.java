package flc.social.process;

import com.ants.common.util.StringUtil;
import flc.social.config.MethodUtil;
import flc.social.dao.redis.MetadataRedisDao;
import flc.social.model.CommentData;
import flc.social.service.TwitterDataService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;

import java.util.List;

/**
 * Created by Segnal on 9/21/17.
 */
public class TwitterDataRetriever extends AbstractProcess {

    static final Logger LOGGER = LogManager.getLogger(TwitterDataRetriever.class);

    public TwitterDataRetriever() throws ConfigurationException {
    }

    public static void main(String[] args) throws Exception {
//        new TwitterDataRetriever().readAndCleanDataSource();
    }

    /**
     * Main method to be executed every time
     *
     * @throws Exception
     */
    @Override
    public void readAndCleanDataSource() throws Exception {
        //Initialize twitter query client
        long beginJob = System.currentTimeMillis();
        Twitter twitter = TwitterDataService.getTwitterFactory().getInstance();
        long maxId = StringUtil.safeParseLong(MetadataRedisDao.load().getMaxId(1l));
        Query query = new Query("lang:en");
        query.setCount(100);
        if (maxId > 0) query.setMaxId(maxId);
        long start = System.currentTimeMillis();
        QueryResult result = twitter.search(query);
        long execTime = System.currentTimeMillis() - start;
        long tweetsSize = MethodUtil.getObjectSize(result);
        LOGGER.info("Total time to get tweets: " + execTime + " milliseconds has " + tweetsSize + " bytes");
        LOGGER.info("Start to retrieve and clean data from Twitter, with max id: " + query.getMaxId());
        if (result.hasNext()) {
            //Retrieve all tweets from the query
            for (Status status : result.getTweets()) {
                try {
                    //Clean and parse tweet to put it into Kafka
                    CommentData cmtData = new CommentData()
                            .setChannel("twitter")
                            .setCommentId(String.valueOf(status.getId()))
                            .setContent(status.getText())
                            .setOwnerId(status.getUser().getScreenName())
                            .setParentId("-")
                            .setPublishedTime(status.getCreatedAt().getTime())
                            .setType("main");
                    addComment(cmtData);
                    //Check whether the tweet has any reply
                    if (status.getInReplyToStatusId() != -1) {
                        //Get rely from the tweet
                        start = System.currentTimeMillis();
                        Status replyStatus = twitter.showStatus(status.getInReplyToStatusId());
                        execTime = System.currentTimeMillis() - start;
                        long replyBytes = MethodUtil.getObjectSize(replyStatus);
                        LOGGER.info("Total time to get Tweet's replies: " + execTime + " milliseconds has " + replyBytes + " bytes");
                        if (replyStatus != null) {
                            //Parse and clean reply
                            CommentData reply = new CommentData()
                                    .setChannel("twitter")
                                    .setCommentId(String.valueOf(replyStatus.getId()))
                                    .setContent(replyStatus.getText())
                                    .setOwnerId(replyStatus.getUser().getScreenName())
                                    .setParentId(String.valueOf(status.getId()))
                                    .setPublishedTime(replyStatus.getCreatedAt().getTime())
                                    .setType("reply");
                            addComment(reply);//Put reply into Kafka
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error when retrieve and clean data from Twitter, with: " + e.getMessage(), e);
                }
            }
            //Save last query Twitter id to Redis
            MetadataRedisDao.load().setMaxId("1", result.getMaxId() + "");
            LOGGER.info("Total time to complete a batch Twitter data " +
                    (System.currentTimeMillis() - beginJob) + " milliseconds");
        }
    }
}
