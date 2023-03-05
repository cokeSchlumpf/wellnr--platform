package com.wellnr.platform.core.modules.users.values.rbac;

import com.wellnr.platform.common.guid.GUID;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * A permission identifies a single allowance on a resource type. Permissions are defined by resource types.
 */
@Value
@AllArgsConstructor(staticName = "apply")
public class Permission {

    GUID guid;

}
