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
