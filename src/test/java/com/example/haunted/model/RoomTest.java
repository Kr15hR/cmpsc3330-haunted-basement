package com.example.haunted.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("lectureHall", "Abandoned Lecture Hall", "Rows of dusty desks.");
    }

    @Test
    void testConstructorGettersAndNullGuards() {
        assertEquals("lectureHall", room.getId());
        assertEquals("Abandoned Lecture Hall", room.getName());
        assertEquals("Rows of dusty desks.", room.getDescription());
        assertFalse(room.isLocked());
        assertNull(room.getRequiredKeyName());
        assertNull(room.getTrap());

        assertThrows(NullPointerException.class, () -> new Room(null, "Name", "Desc"));
        assertThrows(NullPointerException.class, () -> new Room("id", null, "Desc"));
        assertThrows(NullPointerException.class, () -> new Room("id", "Name", null));
    }

    @Test
    void testExitsConnectGetAndUnmodifiableMap() {
        Room northRoom = new Room("archive", "Exam Archive", "Old cabinets.");
        room.connect(Direction.NORTH, northRoom);

        assertSame(northRoom, room.getExit(Direction.NORTH));
        assertNull(room.getExit(Direction.SOUTH));

        Map<Direction, Room> exits = room.getExits();
        assertEquals(1, exits.size());
        assertSame(northRoom, exits.get(Direction.NORTH));
        assertThrows(UnsupportedOperationException.class, () -> exits.put(Direction.EAST, northRoom));

        assertThrows(NullPointerException.class, () -> room.connect(null, northRoom));
        assertThrows(NullPointerException.class, () -> room.connect(Direction.EAST, null));
    }

    @Test
    void testItemFindRemoveCaseInsensitiveAndUnmodifiableList() {
        Potion potion = new Potion("Coffee Potion", "Warm coffee", 15);
        room.addItem(potion);

        assertTrue(room.findItem("coffee potion").isPresent());
        assertSame(potion, room.findItem("COFFEE POTION").get());
        assertTrue(room.findItem("Missing Item").isEmpty());

        List<Item> items = room.getItems();
        assertEquals(1, items.size());
        assertThrows(UnsupportedOperationException.class, () -> items.add(potion));

        assertNull(room.removeItemByName("Missing Item"));
        Item removed = room.removeItemByName("coffee potion");
        assertSame(potion, removed);
        assertTrue(room.getItems().isEmpty());

        assertThrows(NullPointerException.class, () -> room.addItem(null));
    }

    @Test
    void testMonstersFindLivingStatusAndUnmodifiableList() {
        assertFalse(room.hasLivingMonsters());

        Monster ghoul = new Monster("Library Ghoul", 15, 6, 1, List.of());
        room.addMonster(ghoul);

        assertTrue(room.hasLivingMonsters());
        assertTrue(room.findMonster("library ghoul").isPresent());
        assertTrue(room.findMonster("Ghost").isEmpty());

        List<Monster> monsters = room.getMonsters();
        assertEquals(1, monsters.size());
        assertThrows(UnsupportedOperationException.class, () -> monsters.add(ghoul));

        ghoul.takeDamage(15);
        assertFalse(room.hasLivingMonsters());

        assertThrows(NullPointerException.class, () -> room.addMonster(null));
    }

    @Test
    void testUnlockAllBranches() {
        // 1. Already unlocked room returns true immediately
        assertTrue(room.unlock("Any Key"));
        assertFalse(room.isLocked());

        // 2. Locked with null requiredKeyName cannot be unlocked
        room.setLocked(true, null);
        assertFalse(room.unlock("Dean Key"));
        assertFalse(room.unlock(null));
        assertTrue(room.isLocked());

        // 3. Locked with specific key rejects wrong/null key and accepts case-insensitive match
        room.setLocked(true, "Dean Key");
        assertEquals("Dean Key", room.getRequiredKeyName());
        assertFalse(room.unlock("Wrong Key"));
        assertFalse(room.unlock(null));
        assertTrue(room.isLocked());

        assertTrue(room.unlock("dean key"));
        assertFalse(room.isLocked());
    }

    @Test
    void testTrapGetterAndSetter() {
        Trap trap = new Trap("Loose Wires", TrapType.ELECTRIC, 8, true, true);
        room.setTrap(trap);
        assertSame(trap, room.getTrap());
    }
}