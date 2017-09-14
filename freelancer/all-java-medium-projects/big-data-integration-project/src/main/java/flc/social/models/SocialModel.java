package flc.social.models;

/**
 * Created by dientt on 9/14/17.
 */
public class SocialModel {
    public String ownerId;
    public String parentId;
    public long publishedTime;

    public SocialModel() {
    }

    public SocialModel(String ownerId, String parentId, long publishedTime) {
        this.ownerId = ownerId;
        this.parentId = parentId;
        this.publishedTime = publishedTime;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(long publishedTime) {
        this.publishedTime = publishedTime;
    }
}
