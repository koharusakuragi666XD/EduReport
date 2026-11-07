package com.mycompany.edureport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KillSwitchTest {

    @Test
    void shouldFindRequiredStartupResource() {
        assertTrue(KillSwitch.resourceExists("vaca.png"));
    }

    @Test
    void shouldNotThrowWhenStartupResourceIsPresent() {
        assertDoesNotThrow(KillSwitch::verifyStartup);
    }
}
