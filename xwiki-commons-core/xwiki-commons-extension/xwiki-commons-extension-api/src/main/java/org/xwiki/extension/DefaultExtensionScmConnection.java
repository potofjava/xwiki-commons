/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension;

/**
 * Default implementation of {@link ExtensionScmConnection}.
 *
 * @version $Id$
 * @since 6.3M1
 */
public class DefaultExtensionScmConnection extends AbstractExtensionScmConnection
{
    /**
     * @param system the system name
     * @param path the system specific path
     */
    public DefaultExtensionScmConnection(String system, String path)
    {
        super(system, path);
    }

    /**
     * @param str the serialized scm connection
     * @since 9.0RC1
     */
    public DefaultExtensionScmConnection(String str)
    {
        super(str);
    }
}
