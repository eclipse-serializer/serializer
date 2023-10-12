
package org.eclipse.serializer.codegen.entity;

/*-
 * #%L
 * Eclipse Serializer Codegen for Entities
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.chars.XChars;
import org.eclipse.serializer.hashing.HashEqualator;
import org.eclipse.serializer.typing.Stateless;
import org.eclipse.serializer.util.X;

class TypeGeneratorHashEqualatorType extends TypeGenerator
{
	final static String SUFFIX = "HashEqualator";
	
	TypeGeneratorHashEqualatorType(
		final EntityProcessor processor,
		final TypeElement entityTypeElement,
		final List<Member> members)
	{
		super(
			processor,
			entityTypeElement,
			members,
			false,
			SUFFIX);
	}
	
	@Override
	void generateCode()
	{
		final List<? extends TypeParameterElement> typeParameters     =
			this.entityTypeElement.getTypeParameters();
		final String                               typeParametersCode = typeParameters.isEmpty()
			? ""
			: typeParameters.stream().map(tp -> "?").collect(Collectors.joining(", ", "<", ">"));
		
		this.add("public interface ").add(this.typeName)
			.add(" extends ").add(this.addImport(HashEqualator.class)).add("<")
			.add(this.entityName).add(typeParametersCode).add(">")
			.newline().add("{");
		
		// pseudo constructor
		this.newline().tab().add("public static ").add(this.typeName)
			.add(" New()").newline()
			.tab().add("{").newline()
			.tab(2).add("return new Default();").newline()
			.tab().add("}").newline();
		
		// default implementation class
		
		this.newline().tab().add("public static class Default implements ")
			.add(this.typeName).add(", ").add(this.addImport(Stateless.class)).newline()
			.tab().add("{").newline();
		
		final String varName  = XChars.decapitalize(this.entityName);
		final String varName1 = varName.concat("1");
		final String varName2 = varName.concat("2");
		
		// static methods
		this.addImport(X.class);
		this.tab(2).add("public static boolean equals(final ").add(this.entityName).add(typeParametersCode)
			.blank().add(varName1).add(", final ").add(this.entityName).add(typeParametersCode)
			.blank().add(varName2).add(")").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return ").add(this.members.stream()
				.map(m -> "X.equal(" + varName1 + "." + m.paddedMethodName + "(), "
					+ varName2 + "." + m.paddedMethodName + "())")
				.collect(Collectors.joining(VarString.New(System.lineSeparator()).tab(4).add("&& ").toString())))
			.newline().tab(3).add(";").newline()
			.tab(2).add("}").newline().newline();
		this.tab(2).add("public static int hashCode(final ").add(this.entityName).add(typeParametersCode)
			.blank().add(varName).add(")").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return ").add(this.addImport(Objects.class)).add(".hash(").newline().tab(3).tab().add("")
			.add(this.members.stream()
				.map(m -> varName + "." + m.paddedMethodName + "()")
				.collect(Collectors.joining(VarString.New(",").add(System.lineSeparator()).tab(4).toString())))
			.newline().tab(3).add(");").newline()
			.tab(2).add("}").newline().newline();
		
		// constructor
		this.tab(2).add("Default()").newline()
			.tab(2).add("{").newline()
			.tab(3).add("super();").newline()
			.tab(2).add("}").newline().newline();
		
		// override equal(entity1,entity2)
		this.tab(2).add("@Override").newline()
			.tab(2).add("public boolean equal(final ").add(this.entityName).add(typeParametersCode)
			.blank().add(varName1).add(", final ").add(this.entityName).add(typeParametersCode)
			.blank().add(varName2).add(")").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return equals(").add(varName1).add(", ").add(varName2).add(");").newline()
			.tab(2).add("}").newline().newline();
		
		// override hash(entity)
		this.tab(2).add("@Override").newline()
			.tab(2).add("public int hash(final ").add(this.entityName).add(typeParametersCode)
			.blank().add(varName).add(")").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return hashCode(").add(varName).add(");").newline()
			.tab(2).add("}");
		
		this.newline().tab().add("}");
		this.newline().add("}");
	}
}
