package org.vaadin.tori.data.entity;

import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class DiscussionThread extends AbstractEntity {

    @Column(nullable = false)
    private String topic;

    @OneToMany(mappedBy = "thread", cascade = { CascadeType.ALL }, orphanRemoval = true)
    @JoinColumn(nullable = false)
    private List<Post> posts;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Category category;

    @Column(nullable = false)
    private boolean sticky;

    public DiscussionThread() {
    }

    public DiscussionThread(final String topic) {
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public void setSticky(final boolean sticky) {
        this.sticky = sticky;
    }

    public boolean isSticky() {
        return sticky;
    }

}
