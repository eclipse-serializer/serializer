package org.eclipse.serializer.persistence.types;

/**
 * Define an interface to collect all objects and their IDs that get registered
 * to be persisted by a storer.
 * 
 * The interface define only one method that accepts the object and its ID.
 * 
 * Any other logic like clearing between stores has to be done by the implementer.
 * 
 */
public interface PersistenceObjectCollector
{
	/**
	 * 
	 * 
	 * @param objectID the object ID
	 * @param object the object instance
	 */
	void collect(long objectID, Object object);
}
