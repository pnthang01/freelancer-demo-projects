package flc.social.process;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import flc.social.dao.redis.MetadataRedisDao;
import flc.social.model.CommentData;
import flc.social.service.YoutubeDataService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thangpham on 12/09/2017.
 */
public class YoutubeDataRetriever extends AbstractProcess {

    static final Logger LOGGER = LogManager.getLogger(YoutubeDataRetriever.class);

    public YoutubeDataRetriever() throws ConfigurationException {
    }


    public static void main(String[] args) throws Exception {
        new YoutubeDataRetriever().readAndCleanDataSource();
    }

    public void readAndCleanDataSource() throws Exception {
        YouTube youtube = YoutubeDataService.getYouTubeService();
//        List<CommentData> commentData = new ArrayList<CommentData>();
        String regionCode = "GB";
        String nextPageToken = MetadataRedisDao.load().getPopularToken("GB");
        LOGGER.info("Start to retrieve youtube data at region: " + regionCode + " pagetoken = " + nextPageToken);
        YouTube.Videos.List mostPopularQuery = youtube.videos().list("snippet,contentDetails,statistics")
                .setChart("mostPopular")
                .setRegionCode(regionCode)
                .setVideoCategoryId("")
                .setMaxResults(5l);
        if (null != nextPageToken) {
            mostPopularQuery.setPageToken(nextPageToken);
        }
        VideoListResponse mostPopularResponse = mostPopularQuery.execute();
        for (Video video : mostPopularResponse.getItems()) {
            String cmtThreadToken = null;
            do {
                CommentThreadListResponse cmtThreadRS = youtube.commentThreads()
                        .list("snippet,replies")
                        .setVideoId(video.getId())
                        .setPageToken(cmtThreadToken)
                        .setMaxResults(10l).execute();
                for (CommentThread cmt : cmtThreadRS.getItems()) {
                    Comment mainCmt = cmt.getSnippet().getTopLevelComment();
                    addComment(buildCommentData(mainCmt.getSnippet(), video.getId(), mainCmt.getId(), "main"));
                    if(null != cmt.getReplies())
                    for (Comment comment : cmt.getReplies().getComments()) {
                        CommentSnippet snippet = comment.getSnippet();
                        addComment(buildCommentData(snippet, video.getId(), comment.getId(), "reply"));
                    }
                }
                cmtThreadToken = cmtThreadRS.getNextPageToken();
            } while(null != cmtThreadToken);
        }
        nextPageToken = mostPopularResponse.getNextPageToken();
        MetadataRedisDao.load().setPopularToken(regionCode, nextPageToken);
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
