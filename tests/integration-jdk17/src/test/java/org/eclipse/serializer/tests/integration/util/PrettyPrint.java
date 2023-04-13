package org.eclipse.serializer.tests.integration.util;

/*-
 * #%L
 * integration
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

public final class PrettyPrint
{

    private PrettyPrint()
    {
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes)
    {
        StringBuilder result = new StringBuilder();

        StringBuilder visual = new StringBuilder();
        int idx = 0;

        for (byte aByte : bytes)
        {
            int v = aByte & 0xFF;
            visual.append((char) v);
            result.append(HEX_ARRAY[v >>> 4]);
            result.append(HEX_ARRAY[v & 0x0F]);
            result.append(" ");
            idx++;
            if (idx == 16)
            {
                result.append(useDotsWhereNeeded(visual));
                visual.setLength(0);
                result.append('\n');
                idx = 0;
            }
        }

        result.append("   ".repeat(Math.max(0, 16 - idx)));
        result.append(useDotsWhereNeeded(visual));

        return result.toString();
    }

    private static String useDotsWhereNeeded(StringBuilder visual)
    {
        return visual.toString()
                .replaceAll("[^\\x00-\\x7F]", ".")
                .replaceAll("\\p{C}", ".");
    }
}
