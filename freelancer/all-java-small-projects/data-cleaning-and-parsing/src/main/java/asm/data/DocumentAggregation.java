package asm.data;

/**
 * Created by cchaitany on 19/09/2017.
 */
public class DocumentAggregation {

    private String category = "";
    private long bodyWordCount;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getBodyWordCount() {
        return bodyWordCount;
    }

    public void setBodyWordCount(long bodyWordCount) {
        this.bodyWordCount = bodyWordCount;
    }
}
