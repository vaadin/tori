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

package org.vaadin.tori.component;

@SuppressWarnings("serial")
public class ReplyComponent extends AuthoringComponent {

    public interface ReplyListener extends AuthoringListener {
    }

    public ReplyComponent(final ReplyListener listener,
            final String formattingSyntaxXhtml, final String caption) {
        super(listener, formattingSyntaxXhtml, caption);
    }

    public ReplyComponent(final ReplyListener listener,
            final String formattingSyntaxXhtml, final String caption,
            final String inputPrompt) {
        super(listener, formattingSyntaxXhtml, caption, inputPrompt);
    }

    @Override
    public void setCompactMode(final boolean compact) {
        super.setCompactMode(compact);
    }

    @Override
    public void setCollapsible(final boolean collapsible) {
        super.setCollapsible(collapsible);
    }
}
