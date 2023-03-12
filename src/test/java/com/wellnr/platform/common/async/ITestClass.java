package com.wellnr.platform.common.async;

import com.wellnr.platform.common.tuples.Done;

import java.util.concurrent.CompletionStage;

public interface ITestClass {

    @AsyncMethod(pure = false)
    CompletionStage<Done> addAsync(int number);

    @AsyncMethod(pure = false)
    CompletionStage<Done> subAsync(int number);

    @AsyncMethod(pure = true)
    CompletionStage<Done> failCS();

    @AsyncMethod(pure = true)
    CompletionStage<Done> failAsync();

    @AsyncMethod(pure = false)
    Done add(int number);

    @AsyncMethod(pure = false)
    void sub(int number);

    @AsyncMethod(pure = true)
    int getValue();

    @AsyncMethod(pure = true)
    void fail();
}
