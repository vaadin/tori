package org.vaadin.tori.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.entity.Post;
import org.vaadin.tori.util.PostFormatter.FontsInfo;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontFace;
import org.vaadin.tori.util.PostFormatter.FontsInfo.FontSize;
import org.vaadin.tori.util.PostFormatter.FormatInfo;

import com.liferay.portlet.messageboards.util.BBCodeUtil;

public class LiferayPostFormatter implements PostFormatter {

    private static final Logger log = Logger
            .getLogger(LiferayPostFormatter.class);

    private static final String formattingHelp;
    private static Collection<FontFace> fontFaces;
    private static Collection<FontSize> fontSizes;
    private static Collection<FormatInfo> otherFormatInfos;
    private Map<String, String> postReplacements;

    static {
        final StringBuilder syntaxHelp = new StringBuilder();
        syntaxHelp.append("<strong>Tori syntax cheatsheet</strong><br />");
        syntaxHelp.append("[b]bold[/b]<br />");
        syntaxHelp.append("[i]italic[/i]<br />");
        syntaxHelp.append("[u]underline[/u]<br />");
        syntaxHelp.append("[s]strikethrough[/s]<br />");
        syntaxHelp.append("<hr />");
        syntaxHelp
                .append("[code]<br />// add your code block here<br />[/code]<br />");
        syntaxHelp.append("<hr />");
        syntaxHelp.append("[url=http://vaadin.com]link[/url]<br />");
        syntaxHelp.append("[img]http://vaadin.com/image.png[/img]<br />");
        syntaxHelp.append("[email]nospam@vaadin.com[/email]<br />");
        syntaxHelp.append("<hr />");
        syntaxHelp.append("[list=1]<br />");
        syntaxHelp.append("[*]ordered<br />[*]list<br />");
        syntaxHelp.append("[/list]");
        syntaxHelp.append("<hr />");
        syntaxHelp.append("[list]<br />");
        syntaxHelp.append("[*]unordered<br />[*]list<br />");
        syntaxHelp.append("[/list]<br />");
        formattingHelp = syntaxHelp.toString();

        fontFaces = new ArrayList<FontFace>(Arrays.asList(LiferayFontFace
                .values()));
        fontSizes = new ArrayList<FontSize>(Arrays.asList(LiferayFontSize
                .values()));

        otherFormatInfos = new ArrayList<FormatInfo>();
        otherFormatInfos.add(LiferayFormatInfo.STRIKETHROUGH);
        otherFormatInfos.add(LiferayFormatInfo.UNDERLINE);
    }

    @Override
    public String format(final String rawPostBody) {
        try {
            String body = BBCodeUtil.getHTML(rawPostBody.trim());
            if (postReplacements != null) {
                for (final Entry<String, String> entry : postReplacements
                        .entrySet()) {
                    try {
                        body = body
                                .replaceAll(entry.getKey(), entry.getValue());
                    } catch (PatternSyntaxException e) {
                        log.warn(
                                "Invalid replacement regex pattern: "
                                        + entry.getKey(), e);
                    }
                }
            }
            return body;
        } catch (final Exception e) {
            log.warn("Couldn't parse the given post body: " + rawPostBody);
        }
        return rawPostBody;
    }

    @Override
    public String getFormattingSyntaxXhtml() {
        return formattingHelp;
    }

    @Override
    public FontsInfo getFontsInfo() {
        return new FontsInfo() {
            @Override
            public Collection<FontFace> getFontFaces() {
                return fontFaces;
            }

            @Override
            public Collection<FontSize> getFontSizes() {
                return fontSizes;
            }
        };
    }

    @Override
    public FormatInfo getBoldInfo() {
        return LiferayFormatInfo.BOLD;
    }

    @Override
    public FormatInfo getItalicInfo() {
        return LiferayFormatInfo.ITALIC;
    }

    @Override
    public Collection<? extends FormatInfo> getOtherFormattingInfo() {
        return otherFormatInfos;
    }

    @Override
    public String getQuote(final Post postToQuote) {
        if (postToQuote == null) {
            return "";
        }
        return String.format("[quote=%s]%s[/quote]\n", postToQuote.getAuthor()
                .getDisplayedName(), postToQuote.getBodyRaw());
    }

    public final void setPostReplacements(
            final Map<String, String> postReplacements) {
        this.postReplacements = postReplacements;
    }
}
