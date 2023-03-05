package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;

import java.util.concurrent.CompletionStage;

public interface RegisteredUserRootEntity extends RootEntity {

    CompletionStage<Done> updateSettings(String displayName);

    CompletionStage<Done> deleteUser();

}
