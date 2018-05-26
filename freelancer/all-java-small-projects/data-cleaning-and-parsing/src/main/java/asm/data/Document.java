package asm.data;

/**
 * Created by cchaitany on 19/09/2017.
 */
public class Document {
    private String category = " ";
    private String sender = " ";
    private String senderAff = "N/A";
    private String subject = " ";
    private String content = " ";

    @Override
    public String toString() {
        return category + "\t" + sender + "\t" + senderAff + "\t" + subject + "\t" + content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderAff() {
        return senderAff;
    }

    public void setSenderAff(String senderAff) {
        this.senderAff = senderAff == null || senderAff.isEmpty() ? "N/A" : senderAff;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
