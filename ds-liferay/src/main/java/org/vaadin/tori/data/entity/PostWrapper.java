package org.vaadin.tori.data.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.liferay.portlet.messageboards.model.MBMessage;

public class PostWrapper extends Post {

    public MBMessage liferayPost;

    private PostWrapper(final MBMessage message) {
        liferayPost = message;
    }

    @Override
    public long getId() {
        return liferayPost.getMessageId();
    }

    @Override
    public Date getTime() {
        return liferayPost.getCreateDate();
    }

    @Override
    public String getBodyRaw() {
        return liferayPost.getBody(false);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PostWrapper) {
            return liferayPost.equals(((PostWrapper) obj).liferayPost);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return liferayPost.hashCode();
    }

    public static Post wrap(final MBMessage message) {
        if (message != null) {
            return new PostWrapper(message);
        } else {
            return null;
        }
    }

    public static List<Post> wrap(final List<MBMessage> messagesToWrap) {
        if (messagesToWrap == null) {
            return Collections.emptyList();
        }

        final List<Post> result = new ArrayList<Post>(messagesToWrap.size());
        for (final MBMessage messageToWrap : messagesToWrap) {
            result.add(wrap(messageToWrap));
        }
        return result;
    }

}
