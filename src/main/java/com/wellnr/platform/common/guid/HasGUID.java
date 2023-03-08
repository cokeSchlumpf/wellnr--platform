package com.wellnr.platform.common.guid;

/**
 * Markup interface for objects with a GUID.
 *
 * Usually (root-)entities should always have a GUID.
 */
public interface HasGUID {

    GUID getGUID();

}
