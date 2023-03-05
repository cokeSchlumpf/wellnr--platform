package com.wellnr.platform.common;

import com.wellnr.platform.common.async.ReadsOnly;
import com.wellnr.platform.common.async.Writes;
import com.wellnr.platform.common.tuples.Done;

import java.util.concurrent.CompletionStage;

public interface ITestClass {
    @Writes
    CompletionStage<Done> addAsync(int number);

    @Writes
    CompletionStage<Done> subAsync(int number);

    @Writes
    CompletionStage<Done> failCS();

    @Writes
    CompletionStage<Done> failAsync();

    @Writes
    Done add(int number);

    @Writes
    void sub(int number);

    @ReadsOnly
    int getValue();

    @Writes
    void fail();
}
