package org.vaadin.tori.indexing;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.HttpServletRequestAware;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator.ApplicationView;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.util.PostFormatter;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ToriIndexableApplication {

    private static final String ESCAPED_FRAGMENT = "_escaped_fragment_";
    private static final String USER_AGENT = "User-Agent";
    private static final String[] BOT_USER_AGENTS = {
            // "firefox", // for testing
            "googlebot", // google
            "bingbot", "adidxbot", "msnbot", // bing
    // yahoo uses bing's crawlers
    };

    private PortletRequest portletRequest = null;
    private HttpServletRequest servletRequest = null;
    private DataSource ds;
    private PostFormatter postFormatter;

    public ToriIndexableApplication(final PortletRequest request) {
        this.portletRequest = request;
    }

    public ToriIndexableApplication(final HttpServletRequest request) {
        this.servletRequest = request;
    }

    /** Get the resulting XHTML page (<code>&lt;html&gt;</code> tags and all) */
    public String getResultInXhtml(
            @NonNull final HttpServletRequest servletRequest) {

        final ArrayList<String> fragmentArguments = getFragmentArguments(servletRequest);

        if (fragmentArguments.size() > 0) {
            final ToriApiLoader apiLoader = new ToriApiLoader();
            ds = apiLoader.createDataSource();
            postFormatter = apiLoader.createPostFormatter();
            injectRequestIntoDataSource(ds);

            final String viewString = getViewString(fragmentArguments);
            final List<String> arguments = getArguments(fragmentArguments);
            final IndexableView view = getIndexableView(viewString, arguments,
                    this);
            return view.getXhtml();
        } else {
            return "<!DOCTYPE html>\n<html><body>There was some unsightly error</body></html>";
        }
    }

    private void injectRequestIntoDataSource(final DataSource ds) {
        if (ds instanceof PortletRequestAware) {
            ((PortletRequestAware) ds).setRequest(portletRequest);
        } else if (ds instanceof HttpServletRequestAware) {
            ((HttpServletRequestAware) ds).setRequest(servletRequest);
        }
    }

    private static List<String> getArguments(
            final ArrayList<String> fragmentArguments) {
        @SuppressWarnings("unchecked")
        final List<String> clone = (List<String>) fragmentArguments.clone();
        clone.remove(0);
        return clone;
    }

    private static String getViewString(final List<String> fragmentArguments) {
        if (fragmentArguments.isEmpty()) {
            return null;
        } else {
            return fragmentArguments.get(0);
        }
    }

    @NonNull
    private static ArrayList<String> getFragmentArguments(
            @NonNull final HttpServletRequest servletRequest) {
        final String fragment = servletRequest.getParameter(ESCAPED_FRAGMENT);
        final ArrayList<String> args = new ArrayList<String>();
        for (final String bit : fragment.split("/")) {
            args.add(bit);
        }
        if (!args.isEmpty()) {
            // because we start with a slash, the first index is always empty.
            // Or should be.
            args.remove(0);
        }
        return args;
    }

    @NonNull
    private static IndexableView getIndexableView(@NonNull final String view,
            final List<String> arguments,
            final ToriIndexableApplication application) {

        final ApplicationView viewToIndex;

        ApplicationView tempView = null;
        for (final ApplicationView appView : ApplicationView.values()) {
            final String untrimmedAppUrl = appView.getUrl();
            final String appUrl = untrimmedAppUrl.substring(ApplicationView
                    .getUrlPrefix().length());

            if (appUrl.equals(view)) {
                tempView = appView;
                break;
            }
        }

        if (tempView != null) {
            viewToIndex = tempView;
        } else {
            final ApplicationView defaultView = ApplicationView.getDefault();
            getLogger().debug(
                    String.format("\"%s\" is not an explicit view name. "
                            + "Defaulting to %s", view, defaultView));
            viewToIndex = defaultView;
        }

        return IndexableView.newInstance(viewToIndex.getIndexableView(),
                arguments, application);
    }

    private static Logger getLogger() {
        return Logger.getLogger(ToriIndexableApplication.class);
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

    public DataSource getDataSource() {
        return ds;
    }

    public PostFormatter getPostFormatter() {
        return postFormatter;
    }
}
