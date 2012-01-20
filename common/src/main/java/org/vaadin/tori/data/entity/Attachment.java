package org.vaadin.tori.data.entity;

import javax.persistence.Entity;

@Entity
public class Attachment extends AbstractEntity {

    private String downloadUrl;
    private String filename;
    private long fileSize;
    private Post post;

    public Attachment() {

    }

    public Attachment(final String filename, final long fileSize) {
        this.filename = filename;
        this.fileSize = fileSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * Returns the attachment file size in bytes.
     * 
     * @return attachment file size in bytes.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the attachment file size in bytes.
     * 
     * @param fileSize
     */
    public void setFileSize(final long fileSize) {
        this.fileSize = fileSize;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(final Post post) {
        this.post = post;
    }

}
