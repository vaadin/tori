package org.vaadin.tori.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource.UrlInfo;
import org.vaadin.tori.util.UrlConverter;

import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

public class LiferayUrlConverter implements UrlConverter {
    private static final Pattern LIFERAY_FORUM_URL_MESSAGE_PATTERN = Pattern
            .compile("/-/[^/]+/view_message/([0-9]+)");
    private static final Pattern LIFERAY_FORUM_URL_CATEGORY_PATTERN = Pattern
            .compile("/-/[^/]+");
    private static final Pattern LIFERAY_FORUM_URL_CATEGORYQUERY_PATTERN = Pattern
            .compile(".+mbCategoryId=([0-9]+)(&.*)?");

    @Override
    @CheckForNull
    public UrlInfo getToriFragment(@NonNull final String queryUrl,
            final String queryPart) throws Exception {
        final Matcher messageMatcher = LIFERAY_FORUM_URL_MESSAGE_PATTERN
                .matcher(queryUrl);
        if (messageMatcher.matches()) {
            final long id = Long.parseLong(messageMatcher.group(1));

            try {
                final MBMessage message = MBMessageServiceUtil.getMessage(id);
                final long threadId = message.getThreadId();

                return new UrlInfo() {
                    @Override
                    public Destination getDestination() {
                        return Destination.THREAD;
                    }

                    @Override
                    public long getId() {
                        return threadId;
                    }
                };

            } catch (final Exception e) {
                Logger.getLogger(getClass()).warn(
                        "Could not figure out a correct redirection", e);
                throw e;
            }
        }

        final Matcher categoryMatcher = LIFERAY_FORUM_URL_CATEGORY_PATTERN
                .matcher(queryUrl.trim());
        if (categoryMatcher.matches()) {
            final Matcher categoryIdMatcher = LIFERAY_FORUM_URL_CATEGORYQUERY_PATTERN
                    .matcher(queryPart);
            if (categoryIdMatcher.matches()) {
                final Long id = Long.parseLong(categoryIdMatcher.group(1));
                return new UrlInfo() {
                    @Override
                    public Destination getDestination() {
                        return Destination.CATEGORY;
                    }

                    @Override
                    public long getId() {
                        return id;
                    }
                };
            } else {
                return new UrlInfo() {
                    @Override
                    public Destination getDestination() {
                        return Destination.DASHBOARD;
                    }

                    @Override
                    public long getId() {
                        return -1;
                    }
                };
            }
        }

        return null;
    }
}
