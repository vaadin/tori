package org.vaadin.tori.data.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Category extends AbstractEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;
    private int displayOrder;

    @ManyToOne(optional = true)
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE })
    private List<Category> subCategories;

    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE })
    private List<DiscussionThread> threads;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setParentCategory(final Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    protected void setSubCategories(final List<Category> subCategories) {
        this.subCategories = subCategories;
    }

    protected List<Category> getSubCategories() {
        return subCategories;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(final int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
