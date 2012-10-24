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

package org.vaadin.tori.component.breadcrumbs;

import org.vaadin.tori.data.entity.DiscussionThread;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
class ThreadCrumb extends CustomComponent {
    public interface ThreadSelectionListener {
        void selectThread(DiscussionThread selectedThread);
    }

    // private final ThreadSelectionListener listener;
    private final Button crumb;

    // private final Logger log = Logger.getLogger(getClass());

    public ThreadCrumb(final DiscussionThread thread,
            final ThreadSelectionListener listener) {
        // this.listener = listener;

        if (thread == null) {
            throw new RuntimeException("Trying to render the thread part of "
                    + "the breadcrumbs, but the given thread was null");
        }

        setStyleName(Breadcrumbs.STYLE_CRUMB);
        addStyleName(Breadcrumbs.STYLE_THREAD);
        addStyleName(Breadcrumbs.STYLE_UNCLICKABLE);

        crumb = new Button(thread.getTopic());
        /*-
        crumb.addPopupVisibilityListener(new SplitButton.PopupVisibilityListener() {
            @Override
            public void splitButtonPopupVisibilityChange(
                    final PopupVisibilityEvent event) {
                event.getSplitButton().setComponent(getThreadPopup(thread));
            }
        });
         */

        setCompositionRoot(crumb);
    }

    /*-
    private Component getThreadPopup(final DiscussionThread thread) {
        final ListSelect root = new ListSelect();
        root.setImmediate(true);
        root.setNullSelectionAllowed(false);

        List<DiscussionThread> threads = null;
        try {
            threads = ToriUI.getCurrent().getDataSource()
                    .getThreads(thread.getCategory());
        } catch (final DataSourceException e) {
            log.error(e);
            e.printStackTrace();
            return new Label("Something went wrong :(");
        }

        for (final DiscussionThread t : threads) {
            root.addItem(t);

            String topic = t.getTopic();
            if (t.equals(thread)) {
                topic = "> " + topic;
            }

            root.setItemCaption(t, topic);
        }

        root.setValue(thread);
        root.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                if (listener != null) {
                    final DiscussionThread value = (DiscussionThread) event
                            .getProperty().getValue();
                    listener.selectThread(value);
                }
                crumb.setPopupVisible(false);
            }
        });

        return root;
    }
     */
}
