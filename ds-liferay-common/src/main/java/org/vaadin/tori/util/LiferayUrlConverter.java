package org.vaadin.tori.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;

public class LiferayUrlConverter implements UrlConverter {

    private static final String URL_PREFIX = "/#!/";
    private static final String CATEGORIES = URL_PREFIX + "category/";
    private static final String THREADS = URL_PREFIX + "thread/";

    @Override
    public String convertUrlToToriForm(final String url) {
        return convertAllUrlsToToriForm(url);
    }

    public static String convertAllUrlsToToriForm(final String text) {
        String result = replaceMessageBoardsLinksCategories(text);
        result = replaceMessageBoardsLinksMessages(result);
        return result;
    }

    private static final Pattern CATEGORY_LINK_60_PATTERN = Pattern.compile(
            "/-/message_boards\\?[_,\\d]+mbCategoryId=\\d+",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORY_LINK_62_PATTERN = Pattern.compile(
            "/-/message_boards/category/\\d+", Pattern.CASE_INSENSITIVE);

    private static String replaceMessageBoardsLinksCategories(
            final String bodyRaw) {
        String body = bodyRaw;
        // Liferay 6.0 pattern
        final Matcher matcher = CATEGORY_LINK_60_PATTERN.matcher(body);
        while (matcher.find()) {
            final String group = matcher.group();
            final String category = "mbCategoryId=";
            final String categoryIdString = group.substring(group
                    .indexOf(category) + category.length());
            final String fragment = CATEGORIES + categoryIdString;
            body = body
                    .replaceFirst(group.replaceAll("\\?", "\\\\?"), fragment);
        }

        // Liferay 6.1/6.2 pattern
        final Matcher matcher61 = CATEGORY_LINK_62_PATTERN.matcher(body);
        while (matcher61.find()) {
            final String group = matcher61.group();
            final String categoryIdString = group.substring(group
                    .lastIndexOf('/') + 1);
            final String fragment = CATEGORIES + categoryIdString;
            body = body.replaceFirst(group, fragment);
        }

        return body;
    }

    private static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile(
            "/-/message_boards/(view_)?message/\\d+(#[_,\\d]+message_\\d+)?",
            Pattern.CASE_INSENSITIVE);

    private static String replaceMessageBoardsLinksMessages(final String bodyRaw) {
        String body = bodyRaw;
        final Matcher matcher = MESSAGE_LINK_PATTERN.matcher(body);
        while (matcher.find()) {
            final String group = matcher.group();
            String messageIdString = group
                    .substring(group.lastIndexOf('/') + 1);
            if (messageIdString.contains("#")) {
                messageIdString = messageIdString.substring(0,
                        messageIdString.indexOf('#'));
            }
            long messageId = Long.parseLong(messageIdString);
            try {
                final MBMessage message = MBMessageLocalServiceUtil
                        .getMBMessage(messageId);

                final long threadId = message.getThreadId();
                final String messagePrefix = "_message_";
                final int messageIdIndex = group.indexOf(messagePrefix);
                if (messageIdIndex > -1) {
                    messageId = Long.parseLong(group.substring(messageIdIndex
                            + messagePrefix.length()));
                }

                final String fragment = THREADS + threadId + "/" + messageId;

                body = body.replaceFirst(group, fragment);
            } catch (final NestableException e) {
                getLogger()
                        .warn("Unable to get MBmessage for id: " + messageId);
            }
        }

        return body;
    }

    private static Logger getLogger() {
        return Logger.getLogger(LiferayUrlConverter.class);
    }
}
