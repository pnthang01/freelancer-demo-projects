package com.ants.common.model;

/**
 * Created by thangpham on 08/09/2017.
 */
public class FileStateModel {

    private String fileName;
    private long lastModified;
    private long lastMark;

    public FileStateModel(String fileName) {
        this.fileName = fileName;
        this.lastModified = 0;
        this.lastMark = 0;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastMark() {
        return lastMark;
    }

    public void setLastMark(long lastMark) {
        this.lastMark = lastMark;
    }

}