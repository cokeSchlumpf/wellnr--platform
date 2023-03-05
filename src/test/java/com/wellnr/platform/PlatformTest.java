package com.wellnr.platform;

import org.junit.jupiter.api.Test;

public class PlatformTest {

    @Test
    public void test() {
        var p = Platform.apply();
        p.start();
    }

}
