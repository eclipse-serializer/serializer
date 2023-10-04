
package org.eclipse.serializer.wrapping;

/*-
 * #%L
 * Eclipse Serializer Base
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Helper annotation for the wrapper annotation processor. List type names for arbitrary interfaces, for which wrappers
 * should be generated.
 * <pre>
 * &#64;GenerateWrapperFor({"com.myapp.MyType1","com.myapp.MyType2"})
 * public class WrapperGenerationDummy
 * {
 * }
 * </pre>
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateWrapperFor
{
	String[] value();
}
