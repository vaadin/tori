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

package org.vaadin.tori.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.vaadin.tori.data.entity.Attachment;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.data.entity.User;
import org.vaadin.tori.data.util.PersistenceUtil;

public class TestDataGenerator {

    public static void generateTestData() {
        final EntityManager em = PersistenceUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        // Users
        User currentUser = createUser(
                "https://vaadin.com/vaadin-theme/images/company/personnel/virkki.png",
                "John doe");
        em.persist(currentUser);
        em.flush();
        TestDataSource.currentUserId = currentUser.getId();

        User janeDoe = createUser(null, "Jane Doe");
        em.persist(janeDoe);

        // Categories
        for (int i = 1; i <= 3; i++) {
            Category category = createCategory("Category " + i,
                    "Test category " + i + " description", null);
            em.persist(category);
            em.flush();
            if (i == 1) {
                // Sub-categories to category 1.
                for (int j = 1; j <= 2; j++) {
                    Category subCategory = createCategory("Sub Category " + j,
                            "Description for subcategory " + j, category);
                    em.persist(subCategory);
                    em.flush();
                }

                // Threads to category 1.
                for (int j = 1; j <= 300; j++) {
                    DiscussionThread thread = createThread("Discussion thread "
                            + j, category, currentUser);
                    em.persist(thread);

                    // Every thread must have at least 1 post
                    Post post = createPost(currentUser, thread);
                    em.persist(post);
                    em.flush();

                    if (j == 298) {
                        // Let's create a lot of posts to this thread
                        Post firstPost = thread.getPosts().get(0);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(firstPost.getTime());
                        boolean postByCurrentUser = false;
                        for (int k = 1; k <= 250; k++) {
                            User author = currentUser;
                            if (!postByCurrentUser) {
                                author = janeDoe;
                            }
                            postByCurrentUser = !postByCurrentUser;

                            Post newPost = createPost(author, thread);

                            Random random = new Random();
                            int addedMinutes = 20 + random.nextInt(300);

                            calendar.add(Calendar.MINUTE, addedMinutes);
                            Date time = calendar.getTime();
                            if (new Date().before(time)) {
                                time = new Date();
                            }
                            newPost.setTime(time);
                            em.persist(newPost);
                        }
                    }

                    if (j == 299) {
                        // Posts with lots of formatting in this thread
                        Post firstPost = thread.getPosts().get(0);
                        firstPost
                                .setBodyRaw("bbcode: [size=6]Large text[/size] Default text [b]Bold[/b] "
                                        + "[i]italic[/i] [u]undelined[/u] [s]triketrough[/s] "
                                        + "[url=http://vaadin.com]link[/url]\n[quote=Someone previously]"
                                        + "[b]Different[/b] [i]styles[/i] [size=1]in[/size]\n"
                                        + "[code]the\nquote[/code][/quote]\nA code block:\n"
                                        + "[code]int i = 0)\ni++)\nSystem.out.println(i))[/code]\n"
                                        + "[img]http://photos1.meetupstatic.com/photos/event/d/c/2/global_294903522.jpeg[/img]\n"
                                        + "[list=1]\n[*]List\n[list=1]\n[*]sublist\n[*]item\n[/list]\n"
                                        + "[/list]\n[list]\n[*]Bullet\n[*]list\n[list]\n[*]indented\n[/list]\n[/list]");

                        Post htmlFormattedPost = createPost(janeDoe, thread);
                        htmlFormattedPost.setFormatBBCode(false);
                        htmlFormattedPost
                                .setBodyRaw("html: <span style='font-size:32px)'>Large text</span> Default text "
                                        + "<strong>Bold</strong> <em>italic</em> <u>undelined</u> <s>triketrough</s>"
                                        + " <a data-cke-saved-href='http://vaadin.com' href='http://vaadin.com'>link</a>"
                                        + "<blockquote><cite>Someone previously</cite><div><strong>Different</strong> "
                                        + "<em>styles</em> <span style='font-size:10px)'>in</span><br>"
                                        + "<code>the<br>quote</code></div></blockquote>A code block:<br>"
                                        + "<code>int i = 0)<br>i++)<br>System.out.println(i))</code><br>"
                                        + "<img data-cke-saved-src='http://photos1.meetupstatic.com/photos/event/d/c/2/global_294903522.jpeg' "
                                        + "src='http://photos1.meetupstatic.com/photos/event/d/c/2/global_294903522.jpeg'>"
                                        + "<ol><li>List<ol><li>sublist</li><li>item</li></ol></li></ol><ul><li>Bullet</li>"
                                        + "<li>list<ul><li>indented</li></ul></li></ul>");
                        em.persist(htmlFormattedPost);
                    }

                    if (j == 300) {
                        // Post w/ attachments
                        Post firstPost = thread.getPosts().get(0);

                        Attachment attachment = new Attachment("README.md", 10);
                        attachment
                                .setDownloadUrl("https://raw.github.com/vaadin/tori/master/README.md");
                        attachment.setPost(firstPost);
                        firstPost.setAttachments(Arrays.asList(attachment));
                        em.persist(attachment);
                    }
                }
            }
        }
        tx.commit();
    }

    private static Post createPost(final User author,
            final DiscussionThread thread) {
        Post result = new Post();
        result.setThread(thread);
        if (thread.getPosts() == null) {
            thread.setPosts(new ArrayList<Post>());
        }
        thread.getPosts().add(result);

        result.setAuthor(author);
        result.setFormatBBCode(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -40);
        result.setTime(cal.getTime());
        result.setBodyRaw(getLorem());

        return result;
    }

    private static User createUser(final String avatarUrl,
            final String displayedName) {
        User result = new User();
        result.setAvatarUrl(avatarUrl);
        result.setDisplayedName(displayedName);
        return result;
    }

    protected static DiscussionThread createThread(final String topic,
            final Category category, final User user) {
        DiscussionThread result = new DiscussionThread();
        result.setTopic(topic);
        result.setCategory(category);
        return result;
    }

    private static Category createCategory(final String name,
            final String description, final Category parent) {
        Category result = new Category();
        result.setName(name);
        result.setDescription(description);
        result.setParentCategory(parent);
        return result;
    }

    private static String getLorem() {
        String lipsum = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor "
                + "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
                + "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure "
                + "dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
                + "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt "
                + "mollit anim id est laborum.";
        Random random = new Random();
        int begin = random.nextInt(lipsum.length() / 2);
        int end = begin + random.nextInt(lipsum.length() / 2);
        String result = lipsum.substring(begin,
                Math.min(lipsum.length() - 1, end)).trim();
        if (result.isEmpty()) {
            return getLorem();
        } else {
            result = String.valueOf(result.charAt(0)).toUpperCase()
                    + result.substring(1);
            return result + ".";
        }

    }
}
