package com.wellnr.platform.common;

import com.wellnr.platform.common.async.AsyncBoundaryProxy;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.common.tuples.Tuple;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class AsyncBoundaryProxyTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        var orig = TestClass.apply();
        var test = AsyncBoundaryProxy.<ITestClass>createProxy(orig);

        test.add(1);

        try {
            test.failAsync();
        } catch (Exception ex) {
            try {
                orig.failAsync();
            } catch (Exception oex) {
                assertEquals(ex.toString(), oex.toString());
            }
        }

        var cs1 = test.addAsync(3).thenApply(done -> test.subAsync(3));

        var cs3 = test.failCS()
            .handle((done, ex) -> {
                assertNotNull(ex);
                return ex;
            })
            .thenCompose(ex -> orig.failCS().handle((done, oex) -> Tuple.apply(ex, oex)))
            .thenApply(exceptions -> {
                assertEquals(exceptions._2.toString(), exceptions._1.toString());
                return Done.getInstance();
            });

        var cs2 = test.subAsync(1);
        test.sub(5);

        try {
            test.fail();
        } catch (Exception ex) {
            assertInstanceOf(RuntimeException.class, ex);
            assertEquals("Some exception", ex.getMessage());
        }

        test.add(5);

        var finalResult = Operators.compose(cs1, cs2, cs3, (d1, d2, d3) -> test.getValue()).toCompletableFuture().get();
        assertEquals(finalResult, 0);
    }

    /*
     * Commenting test as this test can also fail by accident. It can be used for manually testing effects
     * when not using Async Boundary.
     *
    @Test
    public void testWithoutBoundary() throws ExecutionException, InterruptedException {
        var test = TestClass.apply();

        test.add(1);
        var cs1 = test.addAsync(3).thenApply(done -> test.subAsync(3));
        var cs2 = test.subAsync(1);
        test.sub(5);
        test.add(5);

        var finalResult = Operators.compose(cs1, cs2, (d1, d2) -> test.getValue()).toCompletableFuture().get();
        assertNotEquals(finalResult, 0);
    }
     */

    @AllArgsConstructor(staticName = "apply")
    public static final class TestClass implements ITestClass {

        int value;

        ExecutorService executors;

        Random random;

        public static TestClass apply() {
            return apply(0, Executors.newFixedThreadPool(42), new Random());
        }

        @Override
        public CompletionStage<Done> addAsync(int number) {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("Adding " + number);
                var currentValue = this.value;
                Operators.suppressExceptions(() -> Thread.sleep(1000 + random.nextInt(2000)));
                this.value = currentValue + number;

                return Done.getInstance();
            }, executors);
        }

        @Override
        public CompletionStage<Done> subAsync(int number) {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("Subtracting " + number);
                var currentValue = this.value;
                Operators.suppressExceptions(() -> Thread.sleep(1000 + random.nextInt(2000)));
                this.value = currentValue - number;

                return Done.getInstance();
            }, executors);
        }

        @Override
        public CompletionStage<Done> failCS() {
            return CompletableFuture.supplyAsync(() -> {
                Operators.suppressExceptions(() -> Thread.sleep(1000 + random.nextInt(2000)));
                throw new RuntimeException("Some exception");
            });
        }

        @Override
        public CompletionStage<Done> failAsync() {
            throw new RuntimeException("Some exception");
        }

        @Override
        public Done add(int number) {
            System.out.println("Adding " + number);
            this.value = this.value + number;
            return Done.getInstance();
        }

        @Override
        public void sub(int number) {
            System.out.println("Subtracting " + number);
            this.value = this.value - number;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public void fail() {
            throw new RuntimeException("Some exception");
        }

    }


}