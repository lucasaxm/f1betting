package com.sportygroup.f1betting.testutil;

import com.sportygroup.f1betting.entity.Driver;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static Driver sampleDriver() {
        Driver driver = new Driver();
        driver.setFullName("Driver 1");
        return driver;
    }
}
