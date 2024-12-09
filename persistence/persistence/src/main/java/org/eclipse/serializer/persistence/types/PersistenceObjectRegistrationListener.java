package org.eclipse.serializer.persistence.types;

/**
 * Define an interface to collect all objects and their IDs that get registered
 * to be persisted by a storer.
 * <br>
 * The interface define only one method that accepts the object and its ID.
 * <br>
 * Any other logic like clearing between stores has to be done by the implementer.
 * 
 */
public interface PersistenceObjectRegistrationListener
{
	/**
	 * Can be called by {@link Storer} implementations when an object
	 * gets registered to be stored.
	 * <br>
	 * As this method may get invoked several times during one store
	 * implementers should minimize any logic here.
	 *
	 * @param objectID the object ID
	 * @param object the object instance
	 */
	void onObjectRegistration(long objectID, Object object);
}
