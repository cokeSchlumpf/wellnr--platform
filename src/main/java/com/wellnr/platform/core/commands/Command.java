package com.wellnr.platform.core.commands;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wellnr.platform.core.modules.users.values.users.User;
import com.wellnr.platform.core.context.PlatformContext;

import java.util.concurrent.CompletionStage;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "command")
public interface Command {

    CompletionStage<CommandResult> run(User user, PlatformContext runtime);

}
