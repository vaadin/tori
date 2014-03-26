/*
 * Copyright 2014 Vaadin Ltd.
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

import org.ocpsoft.prettytime.PrettyTime;
import org.vaadin.tori.util.ToriScheduler;
import org.vaadin.tori.util.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.view.thread.ThreadView.PostData;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class PostsLayout extends CssLayout {

    private static final int INITIAL_BATCH_MIN_SIZE = 5;
    private static final int INITIAL_BATCH_MAX_SIZE = 20;
    private static final int RENDER_BATCH_SIZE = 100;
    private final ThreadPresenter presenter;
    private static final String STYLE_READY = "ready";
    private final Map<Long, PostComponent> postComponents = new HashMap<Long, PostComponent>();

    public PostsLayout(final ThreadPresenter presenter) {
        this.presenter = presenter;
    }

    private List<PostData> posts;
    private int renderedIndex = -1;
    private Integer scrollToIndex;
    private final PrettyTime prettyTime = new PrettyTime();

    public void setPosts(final List<PostData> posts, final Integer selectedIndex) {
        removeAllComponents();
        removeStyleName(STYLE_READY);
        renderedIndex = -1;
        scrollToIndex = selectedIndex;
        this.posts = posts;

        int renderUntil = INITIAL_BATCH_MIN_SIZE;
        if (scrollToIndex != null && scrollToIndex < INITIAL_BATCH_MAX_SIZE) {
            renderUntil = scrollToIndex;
        } else if (posts.size() - 1 < INITIAL_BATCH_MAX_SIZE) {
            renderUntil = posts.size() - 1;
        }
        renderUntil(renderUntil);
    }

    private void renderUntil(final int untilIndex) {
        boolean postsAdded = false;
        while (renderedIndex <= untilIndex) {
            renderedIndex++;
            if (posts != null) {
                if (renderedIndex < posts.size()) {
                    postsAdded = true;
                    final Component component = new PostComponent(
                            posts.get(renderedIndex), presenter, prettyTime);
                    addComponent(component);
                    if (scrollToIndex != null && renderedIndex == scrollToIndex) {
                        // The component should be scrolled to
                        UI.getCurrent().scrollIntoView(component);
                        component.setId("scrollpostid");
                        JavaScript
                                .eval("window.setTimeout(\"document.getElementById('scrollpostid').scrollIntoView(true)\",10)");
                        scrollToIndex = null;
                    }
                } else {
                    break;
                }
            }
        }

        if (!postsAdded) {
            List<String> styles = getState(false).styles;
            if ((styles == null || !styles.contains(STYLE_READY))) {
                ToriScheduler.get().executeManualCommands();
                addStyleName(STYLE_READY);
            }
        }
    }

    @Override
    public void addComponent(final Component c) {
        if (c instanceof PostComponent) {
            PostComponent postComponent = (PostComponent) c;
            postComponents.put(postComponent.getPostId(), postComponent);
        }
        super.addComponent(c);
    }

    @Override
    public void beforeClientResponse(final boolean initial) {
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                renderUntil(renderedIndex + RENDER_BATCH_SIZE);
            }
        });
        if (initial && scrollToIndex == null) {
            // First render & no scroll to component specified -> scroll to
            // beginning
            UI.getCurrent().setScrollTop(0);
            JavaScript.eval("window.scrollTo(0,0)");
        }
        super.beforeClientResponse(initial);
    }

    public void updatePost(final PostData postData) {
        postComponents.get(postData.getId()).update(postData);
    }
}
