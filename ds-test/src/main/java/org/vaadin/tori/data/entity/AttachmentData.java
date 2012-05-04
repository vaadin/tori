package org.vaadin.tori.data.entity;

import javax.persistence.Entity;

@Entity
public class AttachmentData extends AbstractEntity {

    private byte[] data;
    private Attachment attachment;

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(final Attachment attachment) {
        this.attachment = attachment;
    }

}
