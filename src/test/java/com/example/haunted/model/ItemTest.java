package com.example.haunted.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testGettersAndToString() {
        Item item = new QuestItem("Lost Gradebook", "The missing faculty gradebook.");

        assertEquals("Lost Gradebook", item.getName());
        assertEquals("The missing faculty gradebook.", item.getDescription());
        assertEquals("Lost Gradebook", item.toString());
    }

    @Test
    void testConstructorRejectsNullNameOrDescription() {
        assertThrows(NullPointerException.class, () -> new QuestItem(null, "Valid description"));
        assertThrows(NullPointerException.class, () -> new QuestItem("Valid Name", null));
    }
}