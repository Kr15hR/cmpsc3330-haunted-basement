package com.example.haunted.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Inventory inventory;
    private Player player;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(5);
        player = new Player("Krish", 50, 8, 3, inventory);
    }

    @Test
    void testConstructorGettersAndNullGuards() {
        assertEquals("Krish", player.getName());
        assertEquals(50, player.getMaxHealth());
        assertEquals(50, player.getHealth());
        assertEquals(8, player.getBaseAttack());
        assertEquals(3, player.getBaseDefense());
        assertSame(inventory, player.getInventory());
        assertNull(player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
        assertNull(player.getCurrentRoom());
        assertTrue(player.isAlive());

        assertThrows(NullPointerException.class, () -> new Player(null, 50, 8, 3, inventory));
        assertThrows(NullPointerException.class, () -> new Player("Krish", 50, 8, 3, null));
        assertThrows(NullPointerException.class, () -> player.setCurrentRoom(null));
    }

    @ParameterizedTest
    @CsvSource({
        "15, 35, true",    // Normal damage reduces HP
        "49, 1, true",     // Exact 1 HP remaining keeps player alive (kills > to >= mutant)
        "50, 0, false",    // Exact lethal damage drops HP to 0 and sets alive to false
        "99, 0, false",    // Overkill damage clamps at 0
        "-10, 50, true"    // Negative damage clamps to 0 (no accidental healing)
    })
    void testTakeDamageBoundariesAndIsAlive(int damage, int expectedHealth, boolean expectedAlive) {
        player.takeDamage(damage);

        assertEquals(expectedHealth, player.getHealth());
        assertEquals(expectedAlive, player.isAlive());
    }

    @ParameterizedTest
    @CsvSource({
        "10, 30",    // Normal heal from 20 -> 30
        "30, 50",    // Exact maxHealth boundary (20 + 30 = 50)
        "50, 50",    // Overheal clamps at maxHealth (50)
        "-15, 20"    // Negative heal clamps to 0 (no accidental damage)
    })
    void testHealBoundaries(int healAmount, int expectedHealth) {
        player.takeDamage(30); // Drop HP from 50 to 20
        assertEquals(20, player.getHealth());

        player.heal(healAmount);
        assertEquals(expectedHealth, player.getHealth());
    }

    @Test
    void testEquipWeaponAndArmorUpdatesGettersAndCombatStats() {
        assertEquals(8, player.getAttackPower());
        assertEquals(3, player.getDefensePower());

        Weapon weapon = new Weapon("Stapler of Justice", "Heavy stapler", 5);
        Armor armor = new Armor("Lab Coat", "Protective coat", 4);

        player.equipWeapon(weapon);
        player.equipArmor(armor);

        assertSame(weapon, player.getEquippedWeapon());
        assertSame(armor, player.getEquippedArmor());
        assertEquals(13, player.getAttackPower());
        assertEquals(7, player.getDefensePower());

        assertThrows(NullPointerException.class, () -> player.equipWeapon(null));
        assertThrows(NullPointerException.class, () -> player.equipArmor(null));
    }

    @Test
    void testSetAndGetCurrentRoom() {
        Room room = new Room("stairwell", "Stairwell", "Dim stairwell.");
        player.setCurrentRoom(room);
        assertSame(room, player.getCurrentRoom());
    }
}