package com.wellnr.platform.core.context;

import java.util.Map;

public interface PlatformContextInternal extends PlatformContext {

    Map<Class<?>, Object> getInstances();

}
