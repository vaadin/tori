package org.vaadin.tori.util;

import com.liferay.portlet.messageboards.util.BBCodeUtil;

public class LiferayPostFormatter implements PostFormatter {
    @Override
    public String format(final String rawPostBody) {
        return BBCodeUtil.getHTML(rawPostBody);
    }

    @Override
    public String getFormattingSyntaxXhtml() {
        // TODO
        return "<b>Not yet implemented for " + LiferayPostFormatter.class
                + ".</b>";
    }
}
