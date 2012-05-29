package org.vaadin.tori.indexing;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator.ApplicationView;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.util.PostFormatter;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ToriIndexableApplication {

    private static final String ESCAPED_FRAGMENT = "_escaped_fragment_";
    private static final String USER_AGENT = "User-Agent";
    private static final String[] BOT_USER_AGENTS = { // "firefox", // for
                                                      // testing
    "googlebot", // google
            "bingbot", "adidxbot", "msnbot", // bing
    // yahoo uses bing's crawlers
    };

    private final ToriApiLoader apiLoader = new ToriApiLoader();

    public ToriIndexableApplication(final PortletRequest request) {
        apiLoader.setRequest(request);
    }

    public ToriIndexableApplication(final HttpServletRequest request) {
        apiLoader.setRequest(request);
    }

    /** Get the resulting XHTML page (<code>&lt;html&gt;</code> tags and all) */
    public String getResultInHtml(
            @NonNull final HttpServletRequest servletRequest) {

        final ArrayList<String> fragmentArguments = getFragmentArguments(servletRequest);

        final String viewString = getViewString(fragmentArguments);
        final List<String> arguments = getArguments(fragmentArguments);
        final IndexableView view = getIndexableView(viewString, arguments, this);
        return view.getHtml();
    }

    @NonNull
    private static List<String> getArguments(
            @NonNull final ArrayList<String> fragmentArguments) {
        if (fragmentArguments.isEmpty()) {
            return fragmentArguments;
        }

        @SuppressWarnings("unchecked")
        final List<String> clone = (List<String>) fragmentArguments.clone();
        clone.remove(0);
        return clone;
    }

    private static String getViewString(final List<String> fragmentArguments) {
        if (fragmentArguments.isEmpty()) {
            return ApplicationView.DASHBOARD.getUrl().replace("/", "");
        } else {
            return fragmentArguments.get(0);
        }
    }

    @NonNull
    private static ArrayList<String> getFragmentArguments(
            @NonNull final HttpServletRequest servletRequest) {
        final String fragment = servletRequest.getParameter(ESCAPED_FRAGMENT);

        if (fragment == null) {
            return new ArrayList<String>();
        }

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

        return escapedFragmentParameter == null
                || escapedFragmentParameter.length == 1;
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
        return apiLoader.getDs();
    }

    public PostFormatter getPostFormatter() {
        return apiLoader.getPostFormatter();
    }
}
