module org.eclipse.serializer.communication.binary
{
	exports org.eclipse.serializer.communication.binary.types;
	exports org.eclipse.serializer.communication.binarydynamic;
	exports org.eclipse.serializer.communication.tls;
	
	requires transitive org.eclipse.serializer.communication;
	requires transitive org.eclipse.serializer.persistence.binary;
	requires org.slf4j;
}
