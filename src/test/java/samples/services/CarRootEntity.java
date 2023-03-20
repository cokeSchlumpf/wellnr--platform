package samples.services;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.async.AsyncMethod;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public class CarRootEntity implements RootEntity {

    private final GUID guid;

    @Override
    public GUID getGUID() {
        return guid;
    }

    @AsyncMethod(pure = true)
    public CompletionStage<Done> inspect(String comment) {
        return CompletableFuture.supplyAsync(() -> {
            Operators.suppressExceptions(() -> Thread.sleep(1000));
            System.out.println(guid);
            System.out.println("Inspecting car: " + comment);
            return Done.getInstance();
        });
    }

    @AsyncMethod(pure = true)
    public CompletionStage<String> getCurrentLocation() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println(guid);
            Operators.suppressExceptions(() -> Thread.sleep(300));
            return "Merzig";
        });
    }

}
