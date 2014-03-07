package org.vaadin.tori;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.vaadin.server.RequestHandler;
import com.vaadin.server.UnsupportedBrowserHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;

public class UnsupportedDeviceHandler implements RequestHandler {
    private static final long serialVersionUID = 522576372014293512L;

    private static final Pattern IOS_REGEX = Pattern
            .compile("iPhone OS ([0-9]+)_");
    private static final Pattern ANDROID_REGEX = Pattern
            .compile("Android ([0-9]+)\\.([0-9]+)\\.[0-9]+;");

    private static final int FIRST_SUPPORTED_VERSION_IOS = 6;

    private static final int LAST_UNSUPPORTED_ANDROID_MAJOR_VERSION = 2;
    private static final int LAST_UNSUPPORTED_ANDROID_MINOR_VERSION = 3;

    @Override
    public boolean handleRequest(final VaadinSession session,
            final VaadinRequest request, final VaadinResponse response)
            throws IOException {

        @SuppressWarnings("deprecation")
        final WebBrowser browser = session.getBrowser();
        final String browserUserAgent = browser.getBrowserApplication();

        if (isUnsupportedDevice(browserUserAgent)
                && !userHasChosenToDisregardWarning(request)) {
            displayUnsupportedDevicePage(request, response);
            return true; // we handled this, done.
        }

        // pass to next handler
        return false;
    }

    private static boolean userHasChosenToDisregardWarning(
            final VaadinRequest request) {
        final String cookieHeader = request.getHeader("Cookie");
        final boolean disregarding = cookieHeader != null
                && cookieHeader
                        .contains(UnsupportedBrowserHandler.FORCE_LOAD_COOKIE);
        if (disregarding) {
            getLogger().debug("User has chosen to disregard any warnings");
        }
        return disregarding;
    }

    private static void displayUnsupportedDevicePage(
            final VaadinRequest request, final VaadinResponse response)
            throws IOException {

        getLogger().debug("outputting unsupported page.");

        final PrintWriter page = response.getWriter();

        // @formatter:off
        page.write("<html><body>" +
                "<p style='font-size: bigger; font-weight: bold'>I'm sorry, you seem to be using an unsupported device.</p>" +
                
                "<p>We've done our best to optimize Tori the best we can for every device, " +
                "but our tests have shown that your device would have a suboptimal viewing " +
                "experience. You're free to try it out still, but we really recommend to" +
                "try again with a more powerful device.</p>" +
                
                "<p>However, if you still want to try it out, " +
                "<a onclick=\"document.cookie='" + UnsupportedBrowserHandler.FORCE_LOAD_COOKIE + "';window.location.reload();return false;\" href=\"#\">just click here</a> " +
                "and let's hope for the best!.</p>"+
                
                "</body></html>");
        // @formatter:on

        page.close();
    }

    private static boolean isUnsupportedDevice(final String browserUserAgent) {
        if (browserUserAgent != null && !browserUserAgent.isEmpty()) {
            return isUnsupportedAppleDevice(browserUserAgent)
                    || isUnsupportedAndroidDevice(browserUserAgent);
        } else {
            return false;
        }
    }

    private static boolean isUnsupportedAndroidDevice(
            final String browserUserAgent) {
        final Matcher matcher = ANDROID_REGEX.matcher(browserUserAgent);
        if (matcher.find()) {
            try {
                final String androidMajorVersionString = matcher.group(1);
                final int androidMajorVersion = Integer
                        .parseInt(androidMajorVersionString);

                final String androidMinorVersionString = matcher.group(2);
                final int androidMinorVersion = Integer
                        .parseInt(androidMinorVersionString);

                return androidMajorVersion <= LAST_UNSUPPORTED_ANDROID_MAJOR_VERSION
                        && androidMinorVersion <= LAST_UNSUPPORTED_ANDROID_MINOR_VERSION;
            } catch (final NumberFormatException e) {
                getLogger()
                        .info("Device seems to be Android, but the version numbering is weird");
                return false;
            }
        } else {
            getLogger().debug("Device isn't running Android");
            return false;
        }
    }

    private static boolean isUnsupportedAppleDevice(
            final String browserUserAgent) {
        final Matcher matcher = IOS_REGEX.matcher(browserUserAgent);
        if (matcher.find()) {
            try {
                final String iosMajorVersionString = matcher.group(1);
                final int iosMajorVersion = Integer
                        .parseInt(iosMajorVersionString);

                final boolean isUnsupported = iosMajorVersion < FIRST_SUPPORTED_VERSION_IOS;
                getLogger().debug(
                        "Is device unsupported? " + isUnsupported
                                + ", ios version" + iosMajorVersion);
                return isUnsupported;
            } catch (final NumberFormatException e) {
                getLogger().info(
                        "Device seems to be an iOS device, but platform "
                                + "version is weird: " + browserUserAgent);
                return false;
            }
        } else {
            getLogger().debug("Device isn't running iOS");
            return false;
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(UnsupportedDeviceHandler.class);
    }

}
