package org.eclipse.serializer.persistence.types;

/*-
 * #%L
 * Eclipse Serializer Persistence
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

import static org.eclipse.serializer.math.XMath.notNegative;

import java.util.function.Consumer;

import org.eclipse.serializer.chars.VarString;
import org.eclipse.serializer.collections.types.XGettingSequence;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionary.Symbols;


/**
 * Visitor that renders {@link PersistenceTypeDescriptionMember} instances back into the textual form used
 * by the {@link PersistenceTypeDictionary}.
 * <p>
 * The appender is the write-side counterpart of the {@link PersistenceTypeDictionaryParser}: it receives a
 * member via {@link #accept(PersistenceTypeDescriptionMember)} (or one of the type-specific
 * {@code appendTypeMemberDescription} overloads), and writes the corresponding textual entry to its backing
 * {@link VarString}, including the proper indentation, padding and member terminator.
 * <p>
 * Each {@link PersistenceTypeDescriptionMember} subtype implements
 * {@link PersistenceTypeDescriptionMember#assembleTypeDescription(PersistenceTypeDescriptionMemberAppender)}
 * to dispatch to the matching overload here, forming a double-dispatch visitor pattern.
 *
 * @see PersistenceTypeDictionaryAssembler
 * @see PersistenceTypeDescriptionMember#assembleTypeDescription(PersistenceTypeDescriptionMemberAppender)
 */
public interface PersistenceTypeDescriptionMemberAppender extends Consumer<PersistenceTypeDescriptionMember>
{
	/**
	 * Renders the passed member by indenting, dispatching it via
	 * {@link PersistenceTypeDescriptionMember#assembleTypeDescription(PersistenceTypeDescriptionMemberAppender)}
	 * and emitting the member terminator.
	 *
	 * @param typeMember the member to render.
	 */
	@Override
	public void accept(PersistenceTypeDescriptionMember typeMember);

	/**
	 * Renders a generic {@linkplain PersistenceTypeDescriptionMemberField field-style} member: type name,
	 * optional qualifier, and field name.
	 *
	 * @param typeMember the field member to render.
	 */
	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberField typeMember);

	/**
	 * Renders a {@linkplain PersistenceTypeDescriptionMemberFieldGenericVariableLength variable-length}
	 * generic field. By default the rendering is identical to a plain field; subclasses can specialize.
	 *
	 * @param typeMember the variable-length field member to render.
	 */
	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberFieldGenericVariableLength typeMember);

	/**
	 * Renders a {@linkplain PersistenceTypeDescriptionMemberFieldGenericComplex complex} (nested-member-bearing)
	 * generic field, including the bracketed nested-member block at the appropriate indentation level.
	 *
	 * @param typeMember the complex field member to render.
	 */
	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberFieldGenericComplex typeMember);

	/**
	 * Renders a {@linkplain PersistenceTypeDescriptionMemberPrimitiveDefinition primitive bit-layout
	 * definition} entry, prefixed with the {@code primitive} keyword.
	 *
	 * @param typeMember the primitive-definition member to render.
	 */
	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberPrimitiveDefinition typeMember);

	/**
	 * Renders a {@linkplain PersistenceTypeDescriptionMemberEnumConstant enum constant} entry, prefixed
	 * with the {@code enum} keyword.
	 *
	 * @param typeMember the enum-constant member to render.
	 */
	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberEnumConstant typeMember);



	/**
	 * Default {@link PersistenceTypeDescriptionMemberAppender} that writes into a
	 * {@link VarString}, applying tab-based indentation and right-padding
	 * type names, qualifiers and field names to user-supplied widths so that emitted dictionary text
	 * is column-aligned and easy to read.
	 */
	public final class Default
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDescriptionMemberAppender
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		// primitive definition special case char sequence
		private static final char[] PRIMITIVE_ = (KEYWORD_PRIMITIVE + ' ').toCharArray();
		private static final char[] ENUM_      = (KEYWORD_ENUM + ' ')     .toCharArray();



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final VarString vs;
		private final int       maxFieldTypeNameLength;
		private final int       maxDeclaringTypeNameLength;
		private final int       maxFieldNameLength;
		private final int       level;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final VarString vs                        ,
			final int       level                     ,
			final int       maxFieldTypeNameLength    ,
			final int       maxDeclaringTypeNameLength,
			final int       maxFieldNameLength
		)
		{
			super();
			this.vs                         =             vs                         ;
			this.level                      =             level                      ;
			this.maxFieldTypeNameLength     = notNegative(maxFieldTypeNameLength    );
			this.maxDeclaringTypeNameLength = notNegative(maxDeclaringTypeNameLength);
			this.maxFieldNameLength         = notNegative(maxFieldNameLength        );
		}

		private void indentMember()
		{
			this.vs.repeat(this.level, '\t');
		}

		private void terminateMember()
		{
			this.vs.add(MEMBER_TERMINATOR).lf();
		}

		private void appendField(final PersistenceTypeDescriptionMemberField member)
		{
			// field type name gets assembled in any case
			this.vs.padRight(member.typeName(), this.maxFieldTypeNameLength, ' ').blank();
			
			// field qualifier (e.g. declaring type name) is optional
			final String qualifier = member.qualifier();
			if(qualifier != null)
			{
				this.vs
				.padRight(qualifier, this.maxDeclaringTypeNameLength, ' ')
				.add(Symbols.MEMBER_FIELD_QUALIFIER_SEPERATOR)
				;
			}
			
			this.vs.padRight(member.name(), this.maxFieldNameLength, ' ');
		}

		

		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final void accept(final PersistenceTypeDescriptionMember typeMember)
		{
			this.indentMember();
			typeMember.assembleTypeDescription(this);
			this.terminateMember();
		}
		
		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberField typeMember)
		{
			this.appendField(typeMember);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberFieldGenericVariableLength typeMember)
		{
			this.appendField(typeMember);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberFieldGenericComplex typeMember)
		{
			this.appendField(typeMember);
			this.vs.add(MEMBER_COMPLEX_DEF_START).lf();
			final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> members = typeMember.members();
			final PersistenceTypeDescriptionMemberAppender appender = members.iterate(
				new TypeDictionaryAppenderBuilder(this.vs, this.level + 1)
			).yield();
			members.iterate(appender);
			this.indentMember();
			this.vs.add(MEMBER_COMPLEX_DEF_END);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberPrimitiveDefinition typeMember)
		{
			this.vs.add(PRIMITIVE_).add(typeMember.primitiveDefinition());
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberEnumConstant typeMember)
		{
			this.vs.add(ENUM_).add(typeMember.name());
		}

	}


}
