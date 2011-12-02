// Copied from Liferay SVN @ revision 57916 (tagged Liferay 6.0.5)
/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.messageboards.util;

import com.liferay.portal.kernel.util.StringPool;

/**
 * @author Alexander Chow
 */
public class BBCodeTag {

    public BBCodeTag() {
    }

    public String getElement() {
        return _element;
    }

    public void setElement(final String element) {
        _element = element;
    }

    public boolean hasElement() {
        if (_element != null) {
            return true;
        } else {
            return false;
        }
    }

    public int getEndPos() {
        return _endPos;
    }

    public void setEndPos(final int pos) {
        _endPos = pos;
    }

    public String getParameter() {
        return _parameter;
    }

    public void setParameter(final String parameter) {
        _parameter = parameter.trim();

        if (_parameter.startsWith(StringPool.APOSTROPHE)
                || _parameter.startsWith(StringPool.QUOTE)) {

            _parameter = _parameter.substring(1);
        }

        if (_parameter.endsWith(StringPool.APOSTROPHE)
                || _parameter.endsWith(StringPool.QUOTE)) {

            _parameter = _parameter.substring(0, _parameter.length() - 1);
        }

        _parameter = _parameter.trim();
    }

    public boolean hasParameter() {
        if (_parameter != null) {
            return true;
        } else {
            return false;
        }
    }

    public int getStartPos() {
        return _startPos;
    }

    public void setStartPos(final int pos) {
        _startPos = pos;
    }

    private String _element;
    private int _endPos;
    private String _parameter;
    private int _startPos;

}