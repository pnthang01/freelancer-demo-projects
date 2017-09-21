package flc.social.process;

import flc.social.model.CommentData;
import flc.social.service.TwitterDataService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.*;

/**
 * Created by dientt on 9/21/17.
 */
public class TwitterDataRetriever extends AbstractProcess{

    static final Logger LOGGER = LogManager.getLogger(TwitterDataRetriever.class);

    public TwitterDataRetriever() throws ConfigurationException {
    }

    public static void main(String[] args) throws Exception {
        new TwitterDataRetriever().readAndCleanDataSource();
    }

    @Override
    public void readAndCleanDataSource() throws Exception {
        Twitter twitter = TwitterDataService.getTwitterFactory().getInstance();
        Query query = new Query("lang:en");
        query.setCount(100);
        QueryResult result = twitter.search(query);
        LOGGER.info("Start to retrieve and clean data from Twitter, with max id: "+query.getMaxId());
        if(result.hasNext()) {
            for (Status status : result.getTweets()) {
                try {
                    CommentData cmtData = new CommentData()
                            .setChannel("twitter")
                            .setCommentId(String.valueOf(status.getId()))
                            .setContent(status.getText())
                            .setOwnerId(status.getUser().getScreenName())
                            .setParentId("-")
                            .setPublishedTime(status.getCreatedAt().getTime())
                            .setType("main");
                    addComment(cmtData);

                    if (status.getInReplyToStatusId() != -1) {
                        Status replyStatus = twitter.showStatus(status.getInReplyToStatusId());
                        if (replyStatus != null) {
                            CommentData reply = new CommentData()
                                    .setChannel("twitter")
                                    .setCommentId(String.valueOf(replyStatus.getId()))
                                    .setContent(replyStatus.getText())
                                    .setOwnerId(replyStatus.getUser().getScreenName())
                                    .setParentId(String.valueOf(status.getId()))
                                    .setPublishedTime(replyStatus.getCreatedAt().getTime())
                                    .setType("reply");
                            addComment(reply);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error when retrieve and clean data from Twitter, with: "+e.getMessage(), e);
                }
            }
        }
    }
}
