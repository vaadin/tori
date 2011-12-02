// Copied from Liferay SVN @ revision 57916 (tagged Liferay 6.0.5)
/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.messageboards.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portlet.messageboards.model.MBMessage;

/**
 * @author Alexander Chow
 */
public class BBCodeUtil {

    static Map<Integer, String> fontSizes = new HashMap<Integer, String>();

    static Map<String, String> listStyles = new HashMap<String, String>();

    static String[][] emoticons = { { "angry.gif", ":angry:" },
            { "bashful.gif", ":bashful:" }, { "big_grin.gif", ":grin:" },
            { "blink.gif", ":blink:" }, { "blush.gif", ":*)" },
            { "bored.gif", ":bored:" }, { "closed_eyes.gif", "-_-" },
            { "cold.gif", ":cold:" }, { "cool.gif", "B)" },
            { "darth_vader.gif", ":vader:" }, { "dry.gif", "<_<" },
            { "exclamation.gif", ":what:" }, { "girl.gif", ":girl:" },
            { "glare.gif", ">_>" }, { "happy.gif", ":)" },
            { "huh.gif", ":huh:" }, { "in_love.gif", "<3" },
            { "karate_kid.gif", ":kid:" }, { "kiss.gif", ":#" },
            { "laugh.gif", ":lol:" }, { "mad.gif", ":mad:" },
            { "mellow.gif", ":mellow:" }, { "ninja.gif", ":ph34r:" },
            { "oh_my.gif", ":O" }, { "pac_man.gif", ":V" },
            { "roll_eyes.gif", ":rolleyes:" }, { "sad.gif", ":(" },
            { "sleep.gif", ":sleep:" }, { "smile.gif", ":D" },
            { "smug.gif", ":smug:" }, { "suspicious.gif", "8o" },
            { "tongue.gif", ":P" }, { "unsure.gif", ":unsure:" },
            { "wacko.gif", ":wacko:" }, { "wink.gif", ":wink:" },
            { "wub.gif", ":wub:" } };

    static {
        fontSizes.put(new Integer(1), "<span style='font-size: 0.7em;'>");
        fontSizes.put(new Integer(2), "<span style='font-size: 0.8em;'>");
        fontSizes.put(new Integer(3), "<span style='font-size: 0.9em;'>");
        fontSizes.put(new Integer(4), "<span style='font-size: 1.0em;'>");
        fontSizes.put(new Integer(5), "<span style='font-size: 1.1em;'>");
        fontSizes.put(new Integer(6), "<span style='font-size: 1.3em;'>");
        fontSizes.put(new Integer(7), "<span style='font-size: 1.5em;'>");

        listStyles.put("1", "<ol style='list-style: decimal inside;'>");
        listStyles.put("i", "<ol style='list-style: lower-roman inside;'>");
        listStyles.put("I", "<ol style='list-style: upper-roman inside;'>");
        listStyles.put("a", "<ol style='list-style: lower-alpha inside;'>");
        listStyles.put("A", "<ol style='list-style: upper-alpha inside;'>");

        for (final String[] emoticon2 : emoticons) {
            final String[] emoticon = emoticon2;

            final String image = emoticon[0];
            final String code = emoticon[1];

            emoticon[0] = "<img alt='emoticon' src='@theme_images_path@/emoticons/"
                    + image + "' />";
            emoticon[1] = HtmlUtil.escape(code);
        }
    }

    public static final String[][] EMOTICONS = emoticons;

    public static String getHTML(final MBMessage message) {
        String body = message.getBody();

        try {
            body = getHTML(body);
        } catch (final Exception e) {
            _log.error("Could not parse message " + message.getMessageId()
                    + " " + e.getMessage());
        }

        return body;
    }

    public static String getHTML(final String bbcode) {
        String html = HtmlUtil.escape(bbcode);

        html = StringUtil.replace(html, _BBCODE_TAGS, _HTML_TAGS);

        for (final String[] emoticon : emoticons) {
            html = StringUtil.replace(html, emoticon[1], emoticon[0]);
        }

        BBCodeTag tag = null;

        StringBundler sb = null;

        while ((tag = getFirstTag(html, "code")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            final String code = tag.getElement().replaceAll("\t",
                    StringPool.FOUR_SPACES);
            final String[] lines = code.split("\\n");
            final int digits = String.valueOf(lines.length + 1).length();

            sb = new StringBundler(preTag);

            sb.append("<div class='code'>");

            for (int i = 0; i < lines.length; i++) {
                final String index = String.valueOf(i + 1);
                final int ld = index.length();

                sb.append("<span class='code-lines'>");

                for (int j = 0; j < digits - ld; j++) {
                    sb.append("&nbsp;");
                }

                lines[i] = StringUtil.replace(lines[i], "   ", StringPool.NBSP
                        + StringPool.SPACE + StringPool.NBSP);
                lines[i] = StringUtil.replace(lines[i], "  ", StringPool.NBSP
                        + StringPool.SPACE);

                sb.append(index + "</span>");
                sb.append(lines[i]);

                if (index.length() < lines.length) {
                    sb.append("<br />");
                }
            }

            sb.append("</div>");
            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "color")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            if (tag.hasParameter()) {
                sb.append("<span style='color: ");
                sb.append(tag.getParameter() + ";'>");
                sb.append(tag.getElement() + "</span>");
            } else {
                sb.append(tag.getElement());
            }

            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "email")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            final String mailto = GetterUtil.getString(tag.getParameter(), tag
                    .getElement().trim());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            sb.append(preTag);
            sb.append("<a href='mailto: ");
            sb.append(mailto);
            sb.append("'>");
            sb.append(tag.getElement() + "</a>");
            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "font")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            if (tag.hasParameter()) {
                sb.append("<span style='font-family: ");
                sb.append(tag.getParameter() + ";'>");
                sb.append(tag.getElement() + "</span>");
            } else {
                sb.append(tag.getElement());
            }

            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "img")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            sb.append("<img alt='' src='");
            sb.append(tag.getElement().trim());
            sb.append("' />");
            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "list")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            final String[] items = _getListItems(tag.getElement());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            if (tag.hasParameter()
                    && listStyles.containsKey(tag.getParameter())) {

                sb.append(listStyles.get(tag.getParameter()));

                for (final String item : items) {
                    if (item.trim().length() > 0) {
                        sb.append("<li>" + item.trim() + "</li>");
                    }
                }

                sb.append("</ol>");
            } else {
                sb.append("<ul style='list-style: disc inside;'>");

                for (final String item : items) {
                    if (item.trim().length() > 0) {
                        sb.append("<li>" + item.trim() + "</li>");
                    }
                }

                sb.append("</ul>");
            }

            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "quote")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            if (tag.hasParameter()) {
                sb.append("<div class='quote-title'>");
                sb.append(tag.getParameter() + ":</div>");
            }

            sb.append("<div class='quote'>");
            sb.append("<div class='quote-content'>");
            sb.append(tag.getElement());
            sb.append("</div></div>");
            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "size")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            if (tag.hasParameter()) {
                Integer size = new Integer(GetterUtil.getInteger(tag
                        .getParameter()));

                if (size.intValue() > 7) {
                    size = new Integer(7);
                }

                if (fontSizes.containsKey(size)) {
                    sb.append(fontSizes.get(size));
                    sb.append(tag.getElement() + "</span>");
                } else {
                    sb.append(tag.getElement());
                }
            } else {
                sb.append(tag.getElement());
            }

            sb.append(postTag);

            html = sb.toString();
        }

        while ((tag = getFirstTag(html, "url")) != null) {
            final String preTag = html.substring(0, tag.getStartPos());
            final String postTag = html.substring(tag.getEndPos());

            final String url = GetterUtil.getString(tag.getParameter(), tag
                    .getElement().trim());

            if (sb == null) {
                sb = new StringBundler(preTag);
            } else {
                sb.setIndex(0);

                sb.append(preTag);
            }

            sb.append("<a href='");
            sb.append(HtmlUtil.escapeHREF(url));
            sb.append("'>");
            sb.append(tag.getElement());
            sb.append("</a>");
            sb.append(postTag);

            html = sb.toString();
        }

        html = StringUtil.replace(html, "\n", "<br />");

        return html;
    }

    public static BBCodeTag getFirstTag(final String bbcode, final String name) {
        final BBCodeTag tag = new BBCodeTag();

        final String begTag = "[" + name;
        final String endTag = "[/" + name + "]";

        final String preTag = StringUtil.extractFirst(bbcode, begTag);

        if (preTag == null) {
            return null;
        }

        if (preTag.length() != bbcode.length()) {
            tag.setStartPos(preTag.length());

            final String remainder = bbcode.substring(preTag.length()
                    + begTag.length());

            final int cb = remainder.indexOf("]");
            final int end = _getEndTagPos(remainder, begTag, endTag);

            if (cb > 0 && remainder.startsWith("=")) {
                tag.setParameter(remainder.substring(1, cb));
                tag.setElement(remainder.substring(cb + 1, end));
            } else if (cb == 0) {
                try {
                    tag.setElement(remainder.substring(1, end));
                } catch (final StringIndexOutOfBoundsException sioobe) {
                    _log.error(bbcode);

                    throw sioobe;
                }
            }
        }

        if (tag.hasElement()) {
            int length = begTag.length() + 1 + tag.getElement().length()
                    + endTag.length();

            if (tag.hasParameter()) {
                length += 1 + tag.getParameter().length();
            }

            tag.setEndPos(tag.getStartPos() + length);

            return tag;
        }

        return null;
    }

    private static int _getEndTagPos(final String remainder,
            final String begTag, final String endTag) {

        int nextBegTagPos = remainder.indexOf(begTag);
        int nextEndTagPos = remainder.indexOf(endTag);

        while ((nextBegTagPos < nextEndTagPos) && (nextBegTagPos >= 0)) {
            nextBegTagPos = remainder.indexOf(begTag,
                    nextBegTagPos + begTag.length());
            nextEndTagPos = remainder.indexOf(endTag,
                    nextEndTagPos + endTag.length());
        }

        return nextEndTagPos;
    }

    private static String[] _getListItems(final String tagElement) {
        final List<String> items = new ArrayList<String>();

        final StringBundler sb = new StringBundler();

        int nestLevel = 0;

        for (String item : StringUtil.split(tagElement, "[*]")) {
            item = item.trim();

            if (item.length() == 0) {
                continue;
            }

            final int begTagCount = StringUtil.count(item, "[list");

            if (begTagCount > 0) {
                nestLevel += begTagCount;
            }

            final int endTagCount = StringUtil.count(item, "[/list]");

            if (endTagCount > 0) {
                nestLevel -= endTagCount;
            }

            if (nestLevel == 0) {
                if ((begTagCount == 0) && (endTagCount == 0)) {
                    items.add(item);
                } else if (endTagCount > 0) {
                    if (sb.length() > 0) {
                        sb.append("[*]");
                    }

                    sb.append(item);

                    items.add(sb.toString());

                    sb.setIndex(0);
                }
            } else {
                if (sb.length() > 0) {
                    sb.append("[*]");
                }

                sb.append(item);
            }
        }

        return items.toArray(new String[items.size()]);
    }

    private static final String[] _BBCODE_TAGS = { "[b]", "[/b]", "[i]",
            "[/i]", "[u]", "[/u]", "[s]", "[/s]", "[img]", "[/img]", "[left]",
            "[center]", "[right]", "[indent]", "[/left]", "[/center]",
            "[/right]", "[/indent]", "[tt]", "[/tt]" };

    private static final String[] _HTML_TAGS = { "<b>", "</b>", "<i>", "</i>",
            "<u>", "</u>", "<strike>", "</strike>", "<img alt='' src='",
            "' />", "<div style='text-align: left;'>",
            "<div style='text-align: center;'>",
            "<div style='text-align: right;'>",
            "<div style='margin-left: 15px;'>", "</div>", "</div>", "</div>",
            "</div>", "<tt>", "</tt>" };

    private static Log _log = LogFactoryUtil.getLog(BBCodeUtil.class);

}