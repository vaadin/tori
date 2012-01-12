package org.vaadin.tori.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;

public abstract class JdbcUtil {

    private static final Logger log = Logger.getLogger(JdbcUtil.class);

    // Copied from LayoutModelImpl (see Liferay source code).
    private static final String DATA_SOURCE = "liferayDataSource";

    private JdbcUtil() {
        // do not instantiate
    }

    public static Connection getJdbcConnection() throws SQLException {
        return ((DataSource) PortalBeanLocatorUtil.getBeanLocator().locate(
                DATA_SOURCE)).getConnection();
    }

    public static void closeAndLogException(final Statement closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception e) {
                log.error(e);
            }
        }
    }

    public static void closeAndLogException(final ResultSet closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception e) {
                log.error(e);
            }
        }
    }

    public static void closeAndLogException(final Connection closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception e) {
                log.error(e);
            }
        }
    }

}
