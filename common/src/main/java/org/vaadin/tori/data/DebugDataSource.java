package org.vaadin.tori.data;

/**
 * <p>
 * If the deployed {@link DataSource} is an instance of
 * <code>DebugDataSource</code>, the developer will be presented with additional
 * testing controls.
 * </p>
 */
public interface DebugDataSource {
    String CONTEXT = "/webapp";
    String ATTACHMENT_PREFIX = CONTEXT + "/attachments/";

    /**
     * Returns data as byte array for an attachment with the given id.
     * 
     * @param attachmentDataId
     * @return The data as byte array.
     */
    public byte[] getAttachmentData(final long attachmentDataId);

}
