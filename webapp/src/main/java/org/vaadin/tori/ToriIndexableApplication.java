package org.vaadin.tori;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.data.DataSource;

public class ToriIndexableApplication {

    private static final String ESCAPED_FRAGMENT = "_escaped_fragment_";
    private static final String USER_AGENT = "User-Agent";
    private static final String[] BOT_USER_AGENTS = { "firefox", // for testing
            "googlebot", // google
            "bingbot", "adidxbot", "msnbot", // bing
    // yahoo uses bing's crawlers
    };

    /** Get the resulting XHTML page (<code>&lt;html&gt;</code> tags and all) */
    public static String getResultInXhtml(
            final HttpServletRequest servletRequest) {

        final DataSource ds = new ToriApiLoader().createDataSource();

        return "<!DOCTYPE html>\n<html><body>HERPDERP!</body></html>";
    }

    /**
     * Checks whether the indexing query is done according to <a href=
     * "https://developers.google.com/webmasters/ajax-crawling/docs/specification"
     * >specifications</a> (See heading
     * "Mapping from _escaped_fragment_ format to #! format")
     */
    public static boolean isIndexableRequest(
            final HttpServletRequest servletRequest) {

        /*
         * Unfortunately there's no hacky way of checking if the parameter is
         * the last one or not. This seems to be the best way with a sane amount
         * of effort.
         * 
         * Or maybe just Liferay makes things hard by injecting stuff into
         * parameters that aren't originally there.
         */

        final String[] escapedFragmentParameter = (String[]) servletRequest
                .getParameterMap().get(ESCAPED_FRAGMENT);

        return escapedFragmentParameter != null
                && escapedFragmentParameter.length == 1;
    }

    public static boolean isIndexerBot(final HttpServletRequest servletRequest) {
        final String visitorUserAgent = servletRequest.getHeader(USER_AGENT)
                .toLowerCase();
        if (visitorUserAgent == null) {
            Logger.getLogger(ToriIndexableApplication.class).warn(
                    "User agent not detected. Assuming visitor is "
                            + "not an indexing bot");
            return false;
        }

        for (final String botUserAgent : BOT_USER_AGENTS) {
            if (visitorUserAgent.contains(botUserAgent)) {
                return true;
            }
        }
        return false;
    }
}
