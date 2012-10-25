/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
