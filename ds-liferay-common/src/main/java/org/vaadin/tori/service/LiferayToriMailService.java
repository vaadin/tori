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

package org.vaadin.tori.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.jsoup.Jsoup;
import org.vaadin.tori.PortletRequestAware;
import org.vaadin.tori.data.LiferayCommonDataSource;
import org.vaadin.tori.data.entity.LiferayCommonEntityFactoryUtil;
import org.vaadin.tori.util.DOMBuilder;
import org.vaadin.tori.util.ToriMailService;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.mail.Account;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.mail.SMTPAccount;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Subscription;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.SubscriptionLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portlet.messageboards.model.MBMailingList;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.model.MBMessageConstants;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBMailingListLocalServiceUtil;
import com.liferay.portlet.messageboards.service.MBMessageLocalServiceUtil;

public class LiferayToriMailService implements ToriMailService,
        PortletRequestAware {

    private String mailTemplateHtml;
    private String mailThemeCss;
    private String imagePath;
    private ServiceContext mbMessageServiceContext;
    private PortletRequest request;

    @Override
    public void setMailTheme(final String mailThemeCss) {
        this.mailThemeCss = mailThemeCss;
    }

    @Override
    public void setPostMailTemplate(final String mailTemplateHtml) {
        this.mailTemplateHtml = mailTemplateHtml;
    }

    private SMTPAccount getSMTPAccout(final MBMessage mbMessage) {
        SMTPAccount account = null;
        try {

            MBMailingList mailingList = MBMailingListLocalServiceUtil
                    .getCategoryMailingList(mbMessage.getGroupId(),
                            mbMessage.getCategoryId());
            if (mailingList.isOutCustom()) {
                String protocol = Account.PROTOCOL_SMTP;

                if (mailingList.isOutUseSSL()) {
                    protocol = Account.PROTOCOL_SMTPS;
                }

                account = (SMTPAccount) Account.getInstance(protocol,
                        mailingList.getOutServerPort());

                account.setHost(mailingList.getOutServerName());
                account.setUser(mailingList.getOutUserName());
                account.setPassword(mailingList.getOutPassword());
            }
        } catch (NestableException e) {
            getLogger().warn("Exception while determining SMTPAccount", e);
        }
        return account;
    }

    private String formMailBody(final MBMessage mbMessage,
            final String formattedPostBody) throws IOException, SAXException,
            PortalException, SystemException {

        User user = null;
        String avatarUrl = "";
        try {
            user = UserLocalServiceUtil.getUser(mbMessage.getUserId());
            String userAvatarUrl = LiferayCommonEntityFactoryUtil.getAvatarUrl(
                    user.getPortraitId(), imagePath, user.isFemale());
            if (userAvatarUrl != null) {
                avatarUrl = mbMessageServiceContext.getPortalURL()
                        + userAvatarUrl;
            }
        } catch (NestableException e) {
            // Ignore
        }

        MBMessage rootMessage = MBMessageLocalServiceUtil.getMessage(mbMessage
                .getThread().getRootMessageId());
        String threadTopic = rootMessage.getSubject();
        threadTopic = stripTags(threadTopic);

        String userDisplayName = user != null ? user.getFullName()
                : "Anonymous";
        if (user != null
                && LiferayCommonEntityFactoryUtil.usesScreennameOnTori(user)) {
            userDisplayName = user.getScreenName();
        }
        userDisplayName = stripTags(userDisplayName);

        String headerImage = getPreferenceValue(
                LiferayCommonDataSource.PREFS_EMAIL_HEADER_IMAGE_URL, null);

        String threadUrl = mbMessageServiceContext.getLayoutFullURL()
                + "#!/thread/" + mbMessage.getThreadId();

        String permaLink = threadUrl + "/" + mbMessage.getMessageId();

        String postHtml = populateTemplate(mailTemplateHtml, avatarUrl,
                threadTopic, userDisplayName, formattedPostBody, headerImage,
                threadUrl, permaLink);
        return formatInlineCSS(postHtml, mailThemeCss);

    }

    static String populateTemplate(final String htmlTemplate,
            final String avatarUrl, final String threadTopic,
            final String userDisplayName, final String bodyFormatted,
            final String headerImage, final String threadUrl,
            final String permaLink) {
        // @formatter:off
        return StringUtil.replace(htmlTemplate, new String[] {
            "[$MESSAGE_USER_AVATAR_URL$]",
            "[$MESSAGE_USER_NAME$]",
            "[$MESSAGE_BODY$]",
            "[$MESSAGE_THREAD_TOPIC$]",
            "[$MESSAGE_HEADER_IMAGE$]",
            "[$MESSAGE_THREAD_URL$]",
            "[$MESSAGE_PERMALINK$]",
            "[$MESSAGE_USER_ANONYMOUS$]",
            "[$MESSAGE_HEADER_DEFAULT_IMAGE$]"
        }, new String[] {
            avatarUrl,
            userDisplayName,
            bodyFormatted,
            threadTopic,
            headerImage != null ? headerImage : "",
            threadUrl,
            permaLink,
            Boolean.toString(avatarUrl == null || avatarUrl.isEmpty()),
            Boolean.toString(headerImage == null || headerImage.isEmpty())
        });
        // @formatter:on
    }

    static String formatInlineCSS(final String html, final String css)
            throws IOException, SAXException {
        org.jsoup.nodes.Document parsed = Jsoup.parse(html, "UTF-8");
        parsed.outputSettings().charset("UTF-8");
        Document doc = DOMBuilder.jsoup2DOM(parsed);

        DOMAnalyzer da = new DOMAnalyzer(doc);
        da.attributesToStyles();
        da.addStyleSheet(null, CSSNorm.stdStyleSheet(),
                DOMAnalyzer.Origin.AGENT);
        da.addStyleSheet(null, css, null);

        da.getStyleSheets();

        da.stylesToDomInherited();

        String result = toString(doc);

        result = result.replaceAll("class=\"topiclinkwrapper\" style=\"",
                "class=\"topiclinkwrapper\" style=\"text-overflow: ellipsis;");

        // Remove all line breaks
        result = result.replaceAll("\\n", "");

        return result;
    }

    public static String toString(final Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer
                    .setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    protected InternetAddress[] parseAddresses(final MBMessage mbMessage) {

        List<Subscription> subscriptions = new ArrayList<Subscription>();

        // Threads
        try {
            subscriptions.addAll(SubscriptionLocalServiceUtil.getSubscriptions(
                    mbMessage.getCompanyId(), MBThread.class.getName(),
                    mbMessage.getThreadId()));
        } catch (SystemException e1) {
            getLogger().warn(
                    "Unable to get thread subscriptions for thread "
                            + mbMessage.getThreadId(), e1);
        }

        Collection<Long> sent = new HashSet<Long>();
        List<InternetAddress> addresses = new ArrayList<InternetAddress>();

        for (Subscription subscription : subscriptions) {
            long subscribedUserId = subscription.getUserId();

            // Don't send email to the message author
            if (subscribedUserId == mbMessage.getUserId()
                    || sent.contains(subscribedUserId)) {
                continue;
            } else {
                sent.add(subscribedUserId);
            }

            try {
                User user = UserLocalServiceUtil.getUserById(subscribedUserId);
                if (user.isActive()) {
                    InternetAddress userAddress = new InternetAddress(
                            user.getEmailAddress(), user.getFullName());

                    addresses.add(userAddress);
                }
            } catch (NoSuchUserException nsue) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info(
                            "Subscription " + subscription.getSubscriptionId()
                                    + " is stale and will be deleted");
                }

                try {
                    SubscriptionLocalServiceUtil
                            .deleteSubscription(subscription
                                    .getSubscriptionId());
                } catch (NestableException e) {
                    getLogger().warn("Unable to delete subscription", e);
                }
            } catch (NestableException e) {
                getLogger().warn(
                        "Unable to parse address for userId "
                                + subscribedUserId, e);
            } catch (UnsupportedEncodingException e) {
                getLogger().warn(
                        "Unable to parse address for userId "
                                + subscribedUserId, e);
            }
        }

        return addresses.toArray(new InternetAddress[addresses.size()]);

    }

    @Override
    public void sendUserAuthored(final long postId,
            final String formattedPostBody) {
        try {
            MBMessage mbMessage = MBMessageLocalServiceUtil
                    .getMBMessage(postId);

            InternetAddress[] bulkAddresses = parseAddresses(mbMessage);

            if (bulkAddresses.length > 0) {
                String mailId = getMailId(mbMessage.getCompanyId(),
                        mbMessage.getCategoryId(), mbMessage.getMessageId());
                String body = formMailBody(mbMessage, formattedPostBody);

                String subject = "[" + mbMessage.getCategory().getName() + "] "
                        + mbMessage.getSubject();
                Company company = CompanyLocalServiceUtil.getCompany(mbMessage
                        .getCompanyId());
                String companyEmail = company.getEmailAddress();

                String fromAddress = getPreferenceValue(
                        LiferayCommonDataSource.PREFS_EMAIL_FROM_ADDRESS, null);
                if (fromAddress == null) {
                    fromAddress = companyEmail;
                }

                String fromName = getPreferenceValue(
                        LiferayCommonDataSource.PREFS_EMAIL_FROM_NAME, null);
                if (fromName == null) {
                    fromName = company.getName() + " forums";
                }

                String replyToAddress = getPreferenceValue(
                        LiferayCommonDataSource.PREFS_EMAIL_REPLY_TO_ADDRESS,
                        null);
                if (replyToAddress == null) {
                    replyToAddress = fromAddress;
                }

                SMTPAccount account = getSMTPAccout(mbMessage);

                String inReplyTo = null;
                if (mbMessage.getParentMessageId() != MBMessageConstants.DEFAULT_PARENT_MESSAGE_ID) {
                    inReplyTo = getMailId(mbMessage.getCompanyId(),
                            mbMessage.getCategoryId(),
                            mbMessage.getParentMessageId());
                }

                InternetAddress from = new InternetAddress(fromAddress,
                        fromName);

                InternetAddress to = new InternetAddress(replyToAddress,
                        replyToAddress);

                InternetAddress replyTo = new InternetAddress(replyToAddress,
                        replyToAddress);

                MailMessage message = new MailMessage(from, to, subject, body,
                        true);
                message.setBulkAddresses(bulkAddresses);
                message.setMessageId(mailId);
                message.setInReplyTo(inReplyTo);
                message.setReplyTo(new InternetAddress[] { replyTo });
                message.setSMTPAccount(account);

                MailServiceUtil.sendEmail(message);
            }
        } catch (Exception e) {
            getLogger().warn("Unable to form email notification", e);
        }
    }

    public static final String POP_PORTLET_PREFIX = "mb.";

    private static String getMailId(final long companyId,
            final long categoryId, final long messageId)
            throws PortalException, SystemException {

        Company company = CompanyLocalServiceUtil.getCompany(companyId);
        String mx = company.getMx();
        StringBundler sb = new StringBundler(10);

        sb.append(StringPool.LESS_THAN);
        sb.append(POP_PORTLET_PREFIX);
        sb.append(categoryId);
        sb.append(StringPool.PERIOD);
        sb.append(messageId);
        sb.append(StringPool.AT);

        String sd = PropsUtil.get(PropsKeys.POP_SERVER_SUBDOMAIN);
        if (sd != null && !"null".equals(sd.toLowerCase())) {
            sb.append(sd);
            sb.append(StringPool.PERIOD);
        }

        sb.append(mx);
        sb.append(StringPool.GREATER_THAN);

        return sb.toString();
    }

    private Logger getLogger() {
        return Logger.getLogger(LiferayToriMailService.class);
    }

    @Override
    public void setRequest(final PortletRequest request) {
        this.request = request;

        try {
            imagePath = ((ThemeDisplay) request
                    .getAttribute(WebKeys.THEME_DISPLAY)).getPathImage();
            mbMessageServiceContext = ServiceContextFactory.getInstance(
                    MBMessage.class.getName(), request);
        } catch (NestableException e) {
            getLogger().error("Unable to initialize mail service", e);
        } catch (NullPointerException e) {
            getLogger().error("Unable to initialize mail service", e);
        }
    }

    private String getPreferenceValue(final String preferenceKey,
            final String defaultValue) {
        String result = defaultValue;
        PortletPreferences portletPreferences;
        try {
            portletPreferences = PortletPreferencesFactoryUtil
                    .getPortletSetup(request);
            result = portletPreferences.getValue(preferenceKey, defaultValue);
        } catch (NestableException e) {
            getLogger().error("Unable to get PortletPreferences", e);
            e.printStackTrace();
        }
        return result;
    }

    private String stripTags(final String html) {
        return html.replaceAll("\\<.*?>", "");
    }

}
