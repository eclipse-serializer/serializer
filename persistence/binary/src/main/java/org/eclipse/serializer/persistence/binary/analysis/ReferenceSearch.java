package org.eclipse.serializer.persistence.binary.analysis;

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

public class ReferenceSearch
{
	public static class ObjectReferenceInfo
	{
		private final long objectID;
		private final long objectTypeID;
		private final long memberIndex;
		private final String memberName;
		
		public ObjectReferenceInfo(
			final long objectID,
			final long objectTypeID,
			final long memberIndex,
			final String memberName)
		{
			super();
			this.objectID = objectID;
			this.objectTypeID = objectTypeID;
			this.memberIndex = memberIndex;
			this.memberName = memberName;
		}
	
		public final long getObjectID()
		{
			return this.objectID;
		}
		public final long getObjectTypeID()
		{
			return this.objectTypeID;
		}
		public final long getReferenceIndex()
		{
			return this.memberIndex;
		}
	
		@Override
		public String toString()
		{
			return "ObjectReferenceInfo ["
					+ "objectID=" + this.objectID
					+ ", objectTypeID=" + this.objectTypeID
					+ ", memberIndex=" + this.memberIndex
					+ ", memberName=" + this.memberName
					+ "]";
		}
		
	}

	public static class ObjectReferenceInfoMember extends ObjectReferenceInfo
	{
		Class<?> referenceType;
	
		public ObjectReferenceInfoMember(
			final long objectID,
			final long objectTypeID,
			final long memberIndex,
			final Class<?> referenceType,
			final String memberName)
		{
			super(objectID, objectTypeID, memberIndex, memberName);
			this.referenceType = referenceType;
		}
	
		public final Class<?> getReferenceTypeID()
		{
			return this.referenceType;
		}
	
		@Override
		public String toString()
		{
			return "ObjectReferenceInfoMember [referenceType=" + this.referenceType + ", toString()=" + super.toString()
					+ "]";
		}
	
	}

	public static class ObjectReferenceInfoVariable extends ObjectReferenceInfo
	{
		public ObjectReferenceInfoVariable(
			final long objectID,
			final long objectTypeID,
			final long memberIndex)
		{
			super(objectID, objectTypeID, memberIndex, null);
		}
	
		@Override
		public String toString()
		{
			return "ObjectReferenceInfoVariable [toString()=" + super.toString() + "]";
		}
				
	}

	public static ObjectReferenceInfo searchReferenceField(
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
	
	        		System.out.println(refId + " referenced by member: " + mtd.identifier()
	        			+ "\n\t member index: " + memberIndex
	        			+ "\n\t member name:  " + mtd.name()
	        			+ "\n\t member type:  " + mtd.type()
	        		);
	
	        		result.set(new ObjectReferenceInfoMember(
	        			objectId,
	        			typeId,
	        			memberIndex.get(),
	        			mtd.type(),
	        			mtd.name()
	        			));
	        	}
	        	else
	        	{
	        		System.out.println(refId + " reference list item "
	        			+ " index: " + memberIndex
	        			+ " member type:  " + "TODO"
	        		);
	
	        		result.set(new ObjectReferenceInfoVariable(objectId, typeId, memberIndex.get()));
	        	}
	        }
	        memberIndex.incrementAndGet();
	    });
		
		return result.get();
	}

}
