package com.wellnr.platform.common.validation;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationTest {

    @Test
    public void test() {
        var test = TestClass.apply();
        var testValidated = ValidationProxy.createProxy(test, TestClass.class);

        assertThrows(
            ValidationException.class,
            () -> {
                testValidated.testMethod("");
            },
            "Expected that assertion fails.");

        assertEquals(testValidated.testFunc("hello-test"), "hello-test");
    }

    @AllArgsConstructor(staticName = "apply")
    public static class TestClass {

        void testMethod(
            @ParameterName("hello") @Size(min = 5, max = 200) String hello
        ) {
            System.out.println(hello);
        }

        String testFunc(@ParameterName("hello") @Size(min = 5, max = 200) String hello) {
            return hello;
        }

    }

}
