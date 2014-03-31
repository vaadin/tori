# Tori

Tori is a discussion forum portlet with a nice looking user interface. Features like real-time notifications and in-page navigation make it easy and comfortable to use. 

Tori has a back-end implementation for Liferay and also a development implementation on top of JPA.

It supports all modern browsers, including Internet Explorer 8 and newer.

![Tori Forum](https://vaadin.com/image/image_gallery?uuid=d1506121-5b14-4280-b2d2-8ae8a77ee8d5&groupId=10187&t=1396271871890)

## Tori features

* Clean looking, interactive message board
* Fast and fluent in-page navigation pattern
* Flashy real-time message notifications
* Uses the same backend and admin with Liferay's default message board
* Can be used simultaneously with Liferay's message board in a separate URI
* Page indexing and SEO support 
* Integrated Google Analytics tracking
* Supported Liferay versions: 6.0 upwards (CE / EE)
* Permission configuration from Liferay Control Panel
* Badge provider API for showing custom user badges
* Stand-alone portlet that includes Vaadin 7.1. built-in

## Installation

To install Tori from the Liferay Marketplace, follow [these](https://www.liferay.com/documentation/liferay-portal/6.2/user-guide/-/ai/downloading-and-installing-apps-liferay-portal-6-2-user-guide-14-en) instructions. Tori works fine on the default settings but the Portlet Preferences view allows you to define additional attributes such as Google Analytics tracker id for your portlet instance.

## Compiling Tori

In case you want to compile a deployable WAR yourself just check out this git repository and run "mvn install" in the root folder. This will compile a development/test deployment of the project and runs Vaadin TestBench tests against it (requires Firefox to be installed) on a Jetty server. The resulting .war file can be deployed to any servlet container.

To produce a deployable package for Liferay portal run "mvn -P liferay62 install" ('liferay62' profile). The resulting .war file (under /webapp/target) can be deployed on a Liferay 6.2 portal. 'vaadincom' -profile is mainly intended for the /forum site on vaadin.com (Liferay 6.0) but should work on any other Liferay 6.0 portal just as well.

## Notes on Portlet deployment

 * Tori doesn't currently have a dedicated user interface for managing the user permissions on individual categories, threads or posts. In order to set proper access rights, use the administrative UI provided by the Liferay Control Panel or Message Boards portlet.
 * Tori portlet doesn't use the global Vaadin library of the Liferay installation but comes bundled with a Vaadin jar and a pre-compiled widgetset. This might cause issues if Tori is placed on the same page with other Vaadin portlets.
 * On Liferay deployment Tori redirects URLs intended for the Message Boards portlet (get parameters) to a format used by Tori (fragment parameters). Because of this, Message Boards can't be used on the same page with Tori.
 * Currently Tori only stores new messages in bbcode format. HTML formatted messages (saved by other means such as using the Message Boards portlet in HTML mode) are displayed in the thread and can be edited but the reply-button is intentionally hidden for them.

## Modifying the theme

Tori theme is generated from a customisable SASS template during the project build process. To produce a build with appearance different from the default one you can fork the project and modify the SASS variables in /webapp/VAADIN/themes/tori/variables.scss to better suit your needs (or create a new maven build profile just like 'vaadincom' with 'tori-vaadin' theme). To compose a completely different theme you have the full power of SASS for your disposal.

## 3rd Party Libraries

 * [Vaadin](https://vaadin.com/home) -- Vaadin, Apache 2.0
 * [Apache log4j](http://logging.apache.org/log4j/1.2/) -- Apache, Apache 2.0
 * [CKEditor wrapper for Vaadin](http://vaadin.com/addon/ckeditor-wrapper-for-vaadin) -- David Wall, Apache 2.0
 * [Mockito](http://code.google.com/p/mockito/) -- Szczepan Faber & co, MIT
 * [PopupButton](http://vaadin.com/addon/popupbutton) -- Henri Kerola, Apache 2.0
 * [ConfirmDialog](http://vaadin.com/addon/confirmdialog) -- Sami Ekblad, Apache 2.0
 * [GoogleAnalyticsTracker](http://vaadin.com/addon/googleanalyticstracker) -- Sami Ekblad, Apache 2.0
 * [PrettyTime](http://ocpsoft.com/prettytime/) -- OCPsoft, LGPL3
 * [Vaadin TestBench](https://vaadin.com/home) -- Vaadin, CVAL 2.0
 * [EclipseLink](https://www.eclipse.org/eclipselink/) -- Eclipse Foundation, Eclipse Public License, Eclipse Distribution License
 * [Apache Derby](http://db.apache.org/derby/) -- Apache Software Foundation, Apache 2.0