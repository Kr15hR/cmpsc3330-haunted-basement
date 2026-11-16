package com.example.haunted.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void testCapacityAddItemAndFullBoundary() {
        Inventory inventory = new Inventory(2);

        assertEquals(2, inventory.getCapacity());
        assertFalse(inventory.isFull());
        assertTrue(inventory.getItems().isEmpty());

        Potion first = new Potion("Coffee", "Warm coffee", 10);
        Weapon second = new Weapon("Ruler", "Wooden ruler", 3);
        Key third = new Key("Dean Key", "Brass key");

        // 1 of 2 -> not full yet (kills >= to > boundary mutant)
        assertTrue(inventory.addItem(first));
        assertFalse(inventory.isFull());

        // 2 of 2 -> exact capacity boundary is full
        assertTrue(inventory.addItem(second));
        assertTrue(inventory.isFull());

        // Exceeding capacity fails without modifying list
        assertFalse(inventory.addItem(third));
        assertEquals(2, inventory.getItems().size());
    }

    @Test
    void testFindContainsRemoveAndUnmodifiableItems() {
        Inventory inventory = new Inventory(3);
        Potion coffee = new Potion("Coffee Potion", "Warm coffee", 15);
        inventory.addItem(coffee);

        assertTrue(inventory.contains("coffee potion"));
        assertTrue(inventory.contains("COFFEE POTION"));
        assertFalse(inventory.contains("Ghost Key"));

        assertTrue(inventory.findItem("coffee potion").isPresent());
        assertTrue(inventory.findItem("Ghost Key").isEmpty());

        List<Item> snapshot = inventory.getItems();
        assertEquals(1, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(coffee));

        assertNull(inventory.removeItem("Ghost Key"));
        Item removed = inventory.removeItem("COFFEE POTION");
        assertSame(coffee, removed);
        assertFalse(inventory.contains("Coffee Potion"));
        assertFalse(inventory.isFull());
    }

    @Test
    void testZeroCapacityInventoryStartsFull() {
        Inventory emptyCapacity = new Inventory(0);

        assertEquals(0, emptyCapacity.getCapacity());
        assertTrue(emptyCapacity.isFull());
        assertFalse(emptyCapacity.addItem(new Potion("Tea", "Herbal tea", 5)));
    }
}