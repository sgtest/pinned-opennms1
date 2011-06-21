/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.api.reporting;

/**
 * <p>ReportException class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportException extends Exception {

    private static final long serialVersionUID = 4117625493115237316L;

    /**
     * <p>Constructor for ReportException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for ReportException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public ReportException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for ReportException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public ReportException(Throwable cause) {
        super(cause);
    }
}
