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

package org.vaadin.tori.data.entity;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.LiferayCommonDataSource;
import org.vaadin.tori.exception.DataSourceException;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBCategory;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBThread;

/**
 * A utility for mapping Liferay entities to Tori entities.
 * 
 * TODO: Map immutable DTOs/proxies instead of entities
 */
public class LiferayCommonEntityFactoryUtil {

    public static Category createCategory(final MBCategory liferayCategory,
            final DataSource dataSource) {
        final Category entity = new Category() {
            @Override
            public Category getParentCategory() {
                Category result = null;
                long parentCategoryId = liferayCategory.getParentCategoryId();
                if (parentCategoryId > 0) {
                    try {
                        result = dataSource.getCategory(parentCategoryId);
                    } catch (DataSourceException e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }
        };
        entity.setId(liferayCategory.getCategoryId());
        entity.setName(liferayCategory.getName());
        entity.setDescription(liferayCategory.getDescription());

        return entity;
    }

    public static List<Category> createCategories(
            final List<MBCategory> liferayCategories,
            final DataSource dataSource) {
        final List<Category> entities = new ArrayList<Category>(
                liferayCategories.size());
        for (final MBCategory liferayCategory : liferayCategories) {
            entities.add(createCategory(liferayCategory, dataSource));
        }
        return entities;
    }

    public static DiscussionThread createDiscussionThread(
            final Category category, final MBThread liferayThread,
            final MBMessage threadRootMessage, final User threadAuthor,
            final User lastPostAuthor, final boolean sticky,
            final LiferayCommonDataSource dataSource) {
        final DiscussionThread entity = new DiscussionThread() {
            @Override
            public Post getLatestPost() {
                // TODO workaround for this hack
                final Post fakedLastPost = new Post() {
                    @Override
                    public long getId() {
                        long result = 0;
                        try {
                            List<MBMessage> posts = dataSource
                                    .getLiferayPostsForThread(liferayThread
                                            .getThreadId());
                            MBMessage last = posts.get(posts.size() - 1);
                            result = last.getMessageId();
                        } catch (NestableException e) {
                            e.printStackTrace();
                        }
                        return result;
                    }
                };
                fakedLastPost.setTime(liferayThread.getLastPostDate());
                fakedLastPost.setAuthor(lastPostAuthor);
                return fakedLastPost;
            }

            @Override
            public User getOriginalPoster() {
                // TODO workaround for this hack
                return threadAuthor;
            }
        };
        entity.setSticky(sticky);
        entity.setCategory(category);
        entity.setId(liferayThread.getThreadId());
        entity.setTopic(threadRootMessage.getSubject());
        entity.setPostCount(liferayThread.getMessageCount());
        entity.setLocked(liferayThread.isLocked());
        entity.setViewCount(liferayThread.getViewCount());
        return entity;
    }

    public static Post createPost(final MBMessage liferayMessage,
            final String bodyRaw, final boolean formatBBCode,
            final User author, final DiscussionThread thread,
            final List<Attachment> attachments) {
        final Post entity = new Post();
        entity.setId(liferayMessage.getMessageId());
        entity.setTime(liferayMessage.getCreateDate());
        entity.setBodyRaw(bodyRaw);
        entity.setThread(thread);
        entity.setAuthor(author);
        entity.setFormatBBCode(formatBBCode);
        entity.setAttachments(attachments);
        return entity;
    }

    public static User createUser(
            final com.liferay.portal.model.User liferayUser,
            final String imagePath, final String userLink,
            final boolean isFemale, final boolean isBanned) {
        final User entity = new User();
        entity.setId(liferayUser.getUserId());
        entity.setDisplayedName(liferayUser.getFullName());
        if (usesScreennameOnTori(liferayUser)) {
            entity.setDisplayedName(liferayUser.getScreenName());
        }

        entity.setAvatarUrl(getAvatarUrl(liferayUser.getPortraitId(),
                imagePath, isFemale));
        entity.setBanned(isBanned);
        entity.setOriginalUserObject(liferayUser);

        entity.setUserLink(userLink);
        return entity;
    }

    private static final String SCREENNAME_EXPANDO_COLUMN_NAME = "use-screenname-on-tori";

    public static boolean usesScreennameOnTori(
            final com.liferay.portal.model.User liferayUser) {
        boolean result = false;
        try {
            if (liferayUser != null) {
                result = ExpandoValueLocalServiceUtil.getData(
                        liferayUser.getCompanyId(),
                        com.liferay.portal.model.User.class.getName(),
                        "CUSTOM_FIELDS", SCREENNAME_EXPANDO_COLUMN_NAME,
                        liferayUser.getUserId(), false);
            }
        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static User createAnonymousUser(final String imagePath) {
        final User entity = new User();
        entity.setAnonymous(true);
        entity.setId(0);
        entity.setDisplayedName("Anonymous");
        return entity;
    }

    public static String getAvatarUrl(final long liferayPortraidId,
            final String imagePath, final boolean isFemale) {
        String result = null;
        if (imagePath != null && liferayPortraidId > 0) {
            result = imagePath + "/user_" + (isFemale ? "female" : "male")
                    + "_portrait?img_id=" + liferayPortraidId;
        }
        return result;
    }

}
