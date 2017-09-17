package flc.social.model;

import java.io.Serializable;

/**
 * Created by thangpham on 12/09/2017.
 */
public class CommentData implements Serializable {

    private String commentId;
    private String channel;
    private String ownerId;
    private String content;
    private String parentId;
    private String type;
    private long publishedTime;

    public String getType() {
        return type;
    }

    public CommentData setType(String type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }

    public CommentData setContent(String content) {
        this.content = content;
        return this;
    }

    public String getCommentId() {
        return commentId;
    }

    public CommentData setCommentId(String commentId) {
        this.commentId = commentId;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public CommentData setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public CommentData setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public CommentData setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public long getPublishedTime() {
        return publishedTime;
    }

    public CommentData setPublishedTime(long publishedTime) {
        this.publishedTime = publishedTime;
        return this;
    }

    @Override
    public String toString() {
        return "CommentData{" +
                "commentId='" + commentId + '\'' +
                ", channel='" + channel + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", content='" + content + '\'' +
                ", parentId='" + parentId + '\'' +
                ", type='" + type + '\'' +
                ", publishedTime=" + publishedTime +
                '}';
    }
}
