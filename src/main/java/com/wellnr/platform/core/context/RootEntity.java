package com.wellnr.platform.core.context;

import com.wellnr.platform.common.guid.GUID;

/**
 * Interface root entities/ aggregate roots of the application. Operations on read entities must happen within
 * asynchronous boundaries - Only a single operation is executed in one moment of time. If multiple calls
 * are made to entities, the calls are queued using {@link com.wellnr.platform.common.async.AsyncBoundaryProxy}.
 *
 * Each operation may transform a Root entity and its child entities so that the entity is always consistent at
 * the end of the operation.
 */
public interface RootEntity {

    GUID getGUID();

}
