/*-
 * #%L
 * Eclipse Serializer Test Fixtures
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
module org.eclipse.serializer.test.fixtures
{
    requires transitive org.eclipse.serializer.base;
    requires java.sql;
    requires static org.junit.jupiter.api;

    exports test.eclipse.serializer.fixtures;
    exports test.eclipse.serializer.fixtures.types;
    exports test.eclipse.serializer.fixtures.types.help;
}
