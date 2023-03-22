package samples.services;

import com.wellnr.platform.core.modules.users.values.users.User;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public class CarServicesImpl implements CarServices {

    @Override
    public CompletionStage<String> someComplexMethod(User user, int value) {
        return CompletableFuture.completedFuture(String.valueOf(value));
    }

}
