package flc.social.process;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import flc.social.config.MethodUtil;
import flc.social.dao.redis.MetadataRedisDao;
import flc.social.model.CommentData;
import flc.social.service.YoutubeDataService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by Segnal on 12/09/2017.
 */
public class YoutubeDataRetriever extends AbstractProcess {

    static final Logger LOGGER = LogManager.getLogger(YoutubeDataRetriever.class);

    public YoutubeDataRetriever() throws ConfigurationException {
    }


//    public static void main(String[] args) throws Exception {
//        new YoutubeDataRetriever().readAndCleanDataSource();
//    }

    /**
     * Main method to be executed every time
     *
     * @throws Exception
     */
    public void readAndCleanDataSource() throws Exception {
        //Initialize youtube client
        long beginJob = System.currentTimeMillis();
        YouTube youtube = YoutubeDataService.getYouTubeService();
        String regionCode = "GB";
        //Get last query token for youtube channel
        String nextPageToken = MetadataRedisDao.load().getPopularToken("GB");
        LOGGER.info("Start to retrieve youtube data at region: " + regionCode + " pagetoken = " + nextPageToken);
        //Prepare most popular videos query
        YouTube.Videos.List mostPopularQuery = youtube.videos().list("snippet,contentDetails,statistics")
                .setChart("mostPopular")
                .setRegionCode(regionCode)
                .setVideoCategoryId("")
                .setMaxResults(5l);
        //If last query tokent exists, query with it.
        if (null != nextPageToken) {
            mostPopularQuery.setPageToken(nextPageToken);
        }
        //Retrieve most popular videos from client
        long start = System.currentTimeMillis();
        VideoListResponse mostPopularResponse = mostPopularQuery.execute();
        List<Video> items = mostPopularResponse.getItems();
        long execTime = System.currentTimeMillis() - start;
        long videoInfo = MethodUtil.getObjectSize(items);
        LOGGER.info("Total time to get videos: " + execTime + " milliseconds has " + videoInfo + " bytes");
        for (Video video : items) {
            String cmtThreadToken = null;
            do {
                //Perform video's comment query
                start = System.currentTimeMillis();
                CommentThreadListResponse cmtThreadRS = youtube.commentThreads()
                        .list("snippet,replies")
                        .setVideoId(video.getId())
                        .setPageToken(cmtThreadToken)
                        .setMaxResults(10l).execute();
                execTime = System.currentTimeMillis() - start;
                List<CommentThread> commentItems = cmtThreadRS.getItems();
                long cmtThreadInfo = MethodUtil.getObjectSize(commentItems);
                LOGGER.info("Total time to get video's comments: " + execTime + " milliseconds has " + cmtThreadInfo + " bytes");
                //Loop through every comment to perform its replies query
                //With every video, retrieve all comment from its, then parse and clean comment data
                for (CommentThread cmt : commentItems) {
                    Comment mainCmt = cmt.getSnippet().getTopLevelComment();
                    //Cleaned and parse comment
                    addComment(buildCommentData(mainCmt.getSnippet(), video.getId(), mainCmt.getId(), "main")); // producer to kafka
                    //Check if comment has replies
                    if (null != cmt.getReplies()) {
                        List<Comment> comments = cmt.getReplies().getComments();
                        long commentsInfo = MethodUtil.getObjectSize(comments);
                        LOGGER.info("Total time to get comment's replies: " + execTime + " milliseconds has " + commentsInfo + " bytes");
                        for (Comment comment : comments) {
                            //Perform replies query
                            CommentSnippet snippet = comment.getSnippet();
                            //Cleaned and parse reply
                            addComment(buildCommentData(snippet, video.getId(), comment.getId(), "reply")); // producer to kafka
                        }
                    }
                }
                //Get last comment query token to make next query
                cmtThreadToken = cmtThreadRS.getNextPageToken();
            } while (null != cmtThreadToken);
        }
        //Get last video query token to set it to Redis
        nextPageToken = mostPopularResponse.getNextPageToken();
        MetadataRedisDao.load().setPopularToken(regionCode, nextPageToken);
        LOGGER.info("Total time to complete a batch Youtube data " +
                (System.currentTimeMillis() - beginJob) + " milliseconds");
    }

    /**
     * Parse and clean comment/reply data
     *
     * @param snippet
     * @param parentId
     * @param cmtId
     * @param type
     * @return
     */
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
