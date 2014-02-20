/*
 * Copyright 2013 Vaadin Ltd.
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

package org.vaadin.tori.view.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.thread.ThreadView.PostData;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class PostsLayout extends CssLayout {

    private static final int RENDER_BATCH_SIZE = 30;
    private Component scrollToComponent;
    private final ThreadPresenter presenter;
    private static final String STYLE_READY = "ready";
    private final Map<Long, PostComponent> postComponents = new HashMap<Long, PostComponent>();

    public PostsLayout(ThreadPresenter presenter) {
        this.presenter = presenter;
    }

    private void renderBatch() {
        int rendered = 0;
        for (int index = 0; index < getComponentCount(); index++) {
            Component component = getComponent(index);
            if (!component.isVisible()) {
                component.setVisible(true);
                rendered++;
            }
            if (component == scrollToComponent) {
                // The component should be scrolled to
                component.setId("scrollpostid");
                UI.getCurrent().scrollIntoView(component);
                JavaScript
                        .eval("window.setTimeout(\"document.getElementById('scrollpostid').scrollIntoView()\",100)");
                scrollToComponent = null;
            }
            if (rendered > RENDER_BATCH_SIZE) {
                break;
            }
        }
        List<String> styles = getState(false).styles;
        if ((styles == null || !styles.contains(STYLE_READY)) && rendered == 0) {
            ToriScheduler.get().executeManualCommands();
            addStyleName(STYLE_READY);
        }
    }

    @Override
    public void addComponent(Component c) {
        if (c instanceof PostComponent) {
            PostComponent postComponent = (PostComponent) c;
            postComponents.put(postComponent.getPostId(), postComponent);
        }
        if (getComponentCount() > RENDER_BATCH_SIZE) {
            c.setVisible(false);
        }
        super.addComponent(c);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                renderBatch();
            }
        });
        if (initial && scrollToComponent == null) {
            // First render & no scroll to component specified -> scroll to
            // beginning
            UI.getCurrent().setScrollTop(0);
            JavaScript.eval("window.scrollTo(0,0)");
        }
        super.beforeClientResponse(initial);
    }

    public void setScrollToComponent(Component component) {
        this.scrollToComponent = component;
    }

    public void setPosts(List<PostData> posts) {
        removeAllComponents();
        removeStyleName(STYLE_READY);
        for (PostData post : posts) {
            PostComponent postComponent = new PostComponent(post, presenter);
            addComponent(postComponent);
            if (post.isSelected()) {
                setScrollToComponent(postComponent);
            }
        }

    }

    public void updatePost(PostData postData) {
        postComponents.get(postData.getId()).update(postData);
    }
}
