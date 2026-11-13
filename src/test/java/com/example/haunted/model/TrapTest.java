package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TrapTest {

    @ParameterizedTest
    @CsvSource({
        "Loose Wires, ELECTRIC, 8, true, true",
        "Static Field, ELECTRIC, 0, false, false",
        "Arc Surge, ELECTRIC, -5, true, false",
        "Live Conduit, ELECTRIC, 15, false, true"
    })
    void testTrapConstructorAndGetters(String name, TrapType type, int damage, boolean armed, boolean oneTimeTrigger) {
        Trap trap = new Trap(name, type, damage, armed, oneTimeTrigger);

        assertEquals(name, trap.getName());
        assertEquals(type, trap.getType());
        assertEquals(damage, trap.getDamage());
        assertEquals(armed, trap.isArmed());
        assertEquals(oneTimeTrigger, trap.isOneTimeTrigger());
    }

    @Test
    void testConstructorRejectsNullArguments() {
        assertThrows(NullPointerException.class, () ->
                new Trap(null, TrapType.ELECTRIC, 10, true, true));

        assertThrows(NullPointerException.class, () ->
                new Trap("Arc Surge", null, 10, true, true));
    }

    @Test
    void testDisarmChangesArmedStateToFalse() {
        Trap armedTrap = new Trap("Laser Grid", TrapType.ELECTRIC, 12, true, false);
        assertTrue(armedTrap.isArmed());

        armedTrap.disarm();
        assertFalse(armedTrap.isArmed());

        // Disarming an already disarmed trap keeps it disarmed
        armedTrap.disarm();
        assertFalse(armedTrap.isArmed());
    }
}