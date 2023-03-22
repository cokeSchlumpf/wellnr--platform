package com.wellnr.platform.core.modules.users.commands;

import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.commands.CommandResult;
import com.wellnr.platform.core.commands.DataResult;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.values.users.User;

import java.util.concurrent.CompletionStage;

import static com.wellnr.platform.common.Operators.completed;


public class GetUserProfileCommand implements Command {

    @Override
    public CompletionStage<CommandResult> run(User user, PlatformContext runtime) {
        return completed(DataResult.apply("Ok"));
    }

}
