package org.eclipse.serializer.persistence.binary.analysis;

/*-
 * #%L
 * Eclipse Serializer Persistence Binary
 * %%
 * Copyright (C) 2023 - 2025 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.serializer.memory.XMemory;
import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryReferenceTraverser;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinition;
import org.eclipse.serializer.persistence.types.PersistenceTypeDefinitionMember;
import org.eclipse.serializer.persistence.types.PersistenceTypeDictionary;
import org.eclipse.serializer.reference.Swizzling;
import org.eclipse.serializer.util.logging.Logging;
import org.slf4j.Logger;


/**
 * Class contains helper identify reference in the storage.
 */
public class ReferenceSearch
{
	/**
	 * Basic desription of the source of a reference.
	 */
	public static class ObjectReferenceInfo
	{
		private final long referenceID;
		private final long objectID;
		private final long objectTypeID;
		private final long memberIndex;
		private final String memberName;
		
		public ObjectReferenceInfo(
			final long referenceID,
			final long objectID,
			final long objectTypeID,
			final long memberIndex,
			final String memberName)
		{
			super();
			this.referenceID = referenceID;
			this.objectID = objectID;
			this.objectTypeID = objectTypeID;
			this.memberIndex = memberIndex;
			this.memberName = memberName;
		}
	
		
		/**
		 * Get the referenced object's Id.
		 * 
		 * @return the referenced object's id.
		 */
		public final long getReferenceID()
		{
			return this.referenceID;
		}

		/**
		 * Get the objectId of the object that holds the reference.
		 * 
		 * @return The refercing object's id.
		 */
		public final long getObjectID()
		{
			return this.objectID;
		}
		
		/**
		 * Get the typeId of the object that holds the reference.
		 * 
		 * @return The refercing object's typeId.
		 */
		public final long getObjectTypeID()
		{
			return this.objectTypeID;
		}
		
		/**
		 * Get the index of the member that references.
		 * 
		 * @return the member index that references.
		 */
		public final long getReferenceIndex()
		{
			return this.memberIndex;
		}
	
		@Override
		public String toString()
		{
			return "ObjectReferenceInfo ["
					+ "referenceID=" + this.referenceID
					+ ", objectID=" + this.objectID
					+ ", objectTypeID=" + this.objectTypeID
					+ ", memberIndex=" + this.memberIndex
					+ ", memberName=" + this.memberName
					+ "]";
		}
		
	}

	/**
	 * Desribes a reference from a the fixed references of a serialized object.
	 * These are usualy fiels.
	 */
	public static class ObjectReferenceInfoMember extends ObjectReferenceInfo
	{
		Class<?> referenceType;
	
		public ObjectReferenceInfoMember(
			final long referenceID,
			final long objectID,
			final long objectTypeID,
			final long memberIndex,
			final Class<?> referenceType,
			final String memberName)
		{
			super(referenceID, objectID, objectTypeID, memberIndex, memberName);
			this.referenceType = referenceType;
		}
	
		/**
		 * Get the type of the referencing member.
		 * 
		 * @return type of the referencing member.
		 */
		public final Class<?> getReferenceTypeID()
		{
			return this.referenceType;
		}
	
		@Override
		public String toString()
		{
			return "ObjectReferenceInfoMember [referenceType=" + this.referenceType + super.toString()
					+ "]";
		}
	
	}

	/*
	 * Describes a reference from a the variable sized references of a serialized object.
	 * Most likely those are "collections".
	 */
	public static class ObjectReferenceInfoVariable extends ObjectReferenceInfo
	{
		public ObjectReferenceInfoVariable(
			final long referenceID,
			final long objectID,
			final long objectTypeID,
			final long memberIndex)
		{
			super(referenceID, objectID, objectTypeID, memberIndex, null);
		}
	
		@Override
		public String toString()
		{
			return "ObjectReferenceInfoVariable [" + super.toString() + "]";
		}
				
	}

	private final static Logger logger = Logging.getLogger(ReferenceSearch.class);
	
	public static ObjectReferenceInfo searchReferencingField(
		final PersistenceTypeDictionary typeDictionary,
		final byte[] data,
		final long referenceObjectID,
		final boolean switchByteOrder)
	{
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
		buffer.put(data);
		long address = XMemory.getDirectByteBufferAddress(buffer);
		
		long typeId = Binary.getEntityTypeIdRawValue(address);
		long objectId = Binary.getEntityObjectIdRawValue(address);
		
		
		PersistenceTypeDefinition typeDefinition = typeDictionary.allTypeDefinitions().get(typeId);
		BinaryReferenceTraverser[] traversers = BinaryReferenceTraverser.Static.deriveReferenceTraversers(typeDefinition.allMembers(), switchByteOrder);
		BinaryReferenceTraverser traverser = traversers[0];
		
		AtomicInteger memberIndex = new AtomicInteger();
		AtomicReference<ObjectReferenceInfo> result = new AtomicReference<>(null);
		traverser.apply(Binary.toEntityContentOffset(address), refId ->
	    {
	        if (Swizzling.isProperId(refId) && refId == referenceObjectID)
	        {
	        	if(memberIndex.get() < typeDefinition.instanceReferenceMembers().size())
	        	{
	        		PersistenceTypeDefinitionMember mtd = typeDefinition.allMembers().at(memberIndex.get());
	
	        		logger.debug("Object {} referenced by member {}. "
	        				+ "\n\t member index: {}"
	        				+ "\n\t member type:  {}",
	        				refId, mtd.identifier(), memberIndex, mtd.type());
	
	        		result.set(new ObjectReferenceInfoMember(
	        			refId,
	        			objectId,
	        			typeId,
	        			memberIndex.get(),
	        			mtd.type(),
	        			mtd.identifier()
	        			));
	        	}
	        	else
	        	{
	        		logger.debug("Object {} referenced form reference list, index {} ", refId, memberIndex);
	
	        		result.set(new ObjectReferenceInfoVariable(refId, objectId, typeId, memberIndex.get()));
	        	}
	        }
	        memberIndex.incrementAndGet();
	    });
		
		return result.get();
	}

}
