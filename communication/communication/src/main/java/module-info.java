module org.eclipse.serializer.communication
{
	exports org.eclipse.serializer.communication.types;
		
	requires transitive org.eclipse.serializer.persistence;
	requires org.slf4j;
	requires org.eclipse.serializer.base;
}
