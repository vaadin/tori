package org.vaadin.tori.data.entity;

import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class Thread extends AbstractEntity {

    private String topic;

    @OneToMany(mappedBy = "thread")
    private List<Post> posts;

    public Thread() {
    }

    public Thread(final String topic) {
        this.topic = topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    @Transient
    public User getOriginalPoster() {
        if (posts != null && !posts.isEmpty()) {
            return posts.get(0).getAuthor();
        } else {
            return null;
        }
    }

    @Transient
    public int getPostCount() {
        if (posts != null) {
            return posts.size();
        } else {
            return 0;
        }
    }

    public void setPosts(final List<Post> posts) {
        this.posts = posts;
    }

    /**
     * Get all posts, in ascending time order
     */
    public List<Post> getPosts() {
        if (posts != null) {
            return posts;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get the newest post, or <code>null</code> if no posts are in thread.
     */
    @Transient
    public Post getLatestPost() {
        if (posts != null) {
            return posts.get(posts.size() - 1);
        } else {
            return null;
        }
    }

}
