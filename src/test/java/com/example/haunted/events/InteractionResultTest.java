package com.example.haunted.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class InteractionResultTest {

    @ParameterizedTest
    @CsvSource({
        "true, Picked up Coffee Potion.",
        "false, Inventory is full.",
        "true, ''",
        "false, No trap was triggered."
    })
    void testInteractionResultGetters(boolean success, String message) {
        InteractionResult result = new InteractionResult(success, message);

        assertEquals(success, result.isSuccess());
        assertEquals(message, result.getMessage());
    }

    @Test
    void testInteractionResultHandlesNullMessage() {
        InteractionResult result = new InteractionResult(false, null);

        assertFalse(result.isSuccess());
        assertNull(result.getMessage());
    }
}