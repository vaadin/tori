/*
 * Copyright 2012 Vaadin Ltd.
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
