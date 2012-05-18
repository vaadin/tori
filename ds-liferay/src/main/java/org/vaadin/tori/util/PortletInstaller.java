package org.vaadin.tori.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.util.PortalUtil;

public class PortletInstaller {
    private static final Logger LOG = Logger.getLogger(PortletInstaller.class);
    private static final String TORI_WIDGETSET = "org.vaadin.tori.widgetset.ToriWidgetset";
    private static final String TORI_THEME = "tori-liferay";
    private static final String LIFERAY_V7_THEME = "liferay-v7";
    private static final String VAADIN_BOOTSTRAP = "vaadinBootstrap.js";
    private static final String DEPLOYMENT_NAME = "tori-liferay";

    public static void checkResources() {
        try {
            if (!getVaadinBootstrapFile().exists()) {
                LOG.info("Vaadin bootstrap not found. Copying...");
                copyVaadinBootstrap();
            }

            if (!getToriWidgetsetDir().exists()) {
                LOG.info("Tori widgetset not found. Copying...");
                copyToriWidgetset();
            }

            if (!getThemeDir(TORI_THEME).exists()) {
                LOG.info("Tori theme not found. Copying...");
                copyToriTheme();
            }

            if (!getThemeDir(LIFERAY_V7_THEME).exists()) {
                LOG.info("Liferay theme for Vaadin 7 not found. Copying...");
                copyLiferayV7Theme();
            }
        } catch (final ToriInstallException e) {
            LOG.warn(e.getMessage()
                    + " You'll need to install resources required by Tori manually.");
        }
    }

    private static void copyVaadinBootstrap() {
        final File vaadinJarFile = getDeploymentVaadinJarFile();
        if (vaadinJarFile != null) {
            try {
                final InputStream inputStream = new FileInputStream(
                        vaadinJarFile);
                final ZipInputStream zipInputStream = new ZipInputStream(
                        inputStream);
                try {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        if (zipEntry.getName().endsWith(VAADIN_BOOTSTRAP)) {
                            writeToFile(zipInputStream,
                                    getVaadinBootstrapFile());
                            zipInputStream.closeEntry();
                            break;
                        }
                    }
                } finally {
                    zipInputStream.close();
                    inputStream.close();
                }
            } catch (final IOException e) {
                throw new ToriInstallException(
                        "Exception while extracting Vaadin bootstrap file.", e);
            }
        }
    }

    private static void copyToriWidgetset() {
        final File deploymentWidgetset = new File(getDeploymentResourcesDir()
                + "/widgetsets/" + TORI_WIDGETSET);
        try {
            copyDirectory(deploymentWidgetset, getToriWidgetsetDir());
        } catch (final IOException e) {
            throw new ToriInstallException(
                    "Error while installing Tori widgetset.", e);
        }
    }

    private static void copyToriTheme() {
        final File deploymentTheme = new File(getDeploymentResourcesDir()
                + "/themes/" + TORI_THEME);
        try {
            copyDirectory(deploymentTheme, getThemeDir(TORI_THEME));
        } catch (final IOException e) {
            throw new ToriInstallException(
                    "Exception while installing Tori theme.", e);
        }
    }

    private static void copyLiferayV7Theme() {
        try {
            final InputStream inputStream = new FileInputStream(
                    getDeploymentVaadinJarFile());
            final ZipInputStream zipInputStream = new ZipInputStream(
                    inputStream);
            try {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    final String liferayThemeDir = "VAADIN/themes/liferay";
                    if (zipEntry.getName().startsWith(liferayThemeDir)) {
                        final File file = new File(getVaadinResourcePath()
                                + zipEntry.getName().replaceFirst(
                                        liferayThemeDir, "/themes/liferay-v7"));
                        if (zipEntry.isDirectory()) {
                            file.mkdir();
                        } else {
                            writeToFile(zipInputStream, file);
                        }
                    }
                    zipInputStream.closeEntry();
                }
            } finally {
                zipInputStream.close();
                inputStream.close();
            }

        } catch (final IOException e) {
            throw new ToriInstallException(
                    "Exception while extracting Liferay theme for Vaadin 7.", e);
        }
    }

    private static String getDeploymentPath() {
        final File webappsFolder = new File(PortalUtil.getPortalWebDir())
                .getParentFile();
        return webappsFolder.getAbsolutePath() + "/" + DEPLOYMENT_NAME;
    }

    private static File getDeploymentResourcesDir() {
        return new File(getDeploymentPath() + "/VAADIN/");
    }

    private static File getDeploymentVaadinJarFile() {
        final String libDir = getDeploymentPath() + "/WEB-INF/lib";
        final File libDirFile = new File(libDir);
        for (final String fileName : libDirFile.list()) {
            if (fileName.startsWith("vaadin-7") && fileName.endsWith(".jar")) {
                return new File(libDir + "/" + fileName);
            }
        }
        throw new ToriInstallException(
                "Vaadin jar package not found in Tori deployment", null);
    }

    private static String getVaadinResourcePath() {
        // return ".../webapps/ROOT/html/VAADIN/";
        return PortalUtil.getPortalWebDir()
                + PropsUtil.get("vaadin.resources.path") + "/VAADIN";
    }

    private static File getVaadinBootstrapFile() {
        return new File(getVaadinResourcePath() + "/" + VAADIN_BOOTSTRAP);
    }

    private static File getToriWidgetsetDir() {
        // ".../webapps/ROOT/html/VAADIN/widgetsets/org.vaadin.tori.widgetset.ToriWidgetset";
        return new File(getVaadinResourcePath() + "/widgetsets/"
                + TORI_WIDGETSET);
    }

    private static File getThemeDir(final String theme) {
        // return ".../webapps/ROOT/html/VAADIN/themes/theme";
        return new File(getVaadinResourcePath() + "/themes/" + theme);
    }

    private static void copyDirectory(final File source, final File target)
            throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdir();
            }

            for (final String element : source.list()) {
                copyDirectory(new File(source, element), new File(target,
                        element));
            }
        } else {
            writeToFile(new FileInputStream(source), target);
        }
    }

    private static void writeToFile(final InputStream inputStream,
            final File file) throws IOException {
        final byte[] buffer = new byte[2048];

        final FileOutputStream fileOutputStream = new FileOutputStream(file);
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                fileOutputStream, buffer.length);

        try {
            int size;
            while ((size = inputStream.read(buffer, 0, buffer.length)) != -1) {
                bufferedOutputStream.write(buffer, 0, size);
            }
            bufferedOutputStream.flush();
        } finally {
            bufferedOutputStream.close();
        }
    }

    @SuppressWarnings("serial")
    private static class ToriInstallException extends RuntimeException {
        public ToriInstallException(final String message,
                final Throwable throwable) {
            super(message, throwable);
        }
    }

}
