package com.example.haunted.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MoveResultTest {

    @ParameterizedTest
    @CsvSource({
        "true, Moved to Abandoned Lecture Hall., false, 0",
        "true, Moved to Broken Elevator and triggered trap!, true, 8",
        "false, That door is locked., false, 0",
        "true, Triggered zero-damage static trap., true, 0"
    })
    void testMoveResultGetters(boolean success, String message, boolean trapTriggered, int trapDamage) {
        MoveResult result = new MoveResult(success, message, trapTriggered, trapDamage);

        assertEquals(success, result.isSuccess());
        assertEquals(message, result.getMessage());
        assertEquals(trapTriggered, result.isTrapTriggered());
        assertEquals(trapDamage, result.getTrapDamage());
    }

    @Test
    void testMoveResultHandlesNullMessage() {
        MoveResult result = new MoveResult(false, null, false, 0);

        assertFalse(result.isSuccess());
        assertNull(result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
    }
}