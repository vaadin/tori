package org.vaadin.tori.util;

public interface ToriMailService {

    void setMailTheme(String mailThemeCss);

    void setPostMailTemplate(String mailTemplateHtml);

    void sendUserAuthored(long postId, String formattedPostBody);

}
