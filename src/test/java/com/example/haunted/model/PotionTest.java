package com.example.haunted.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PotionTest {

    @Test
    void testGettersAndInheritedProperties() {
        Potion potion = new Potion("Coffee Potion", "Restores 15 HP.", 15);

        assertEquals("Coffee Potion", potion.getName());
        assertEquals("Restores 15 HP.", potion.getDescription());
        assertEquals(15, potion.getHealingAmount());
        assertTrue(potion instanceof Usable);
    }

    @ParameterizedTest
    @CsvSource({
        "15, 30, 35",   // 50 - 30 = 20 HP, heals +15 -> 35 HP
        "40, 30, 50",   // 50 - 30 = 20 HP, heals +40 -> clamps at 50 maxHealth
        "0, 20, 30"     // 50 - 20 = 30 HP, heals +0 -> remains 30 HP
    })
    void testUseHealsPlayerUpToMaxHealth(int healingAmount, int initialDamage, int expectedHealth) {
        Player player = new Player("Explorer", 50, 8, 2, new Inventory(5));
        player.takeDamage(initialDamage);

        Potion potion = new Potion("Brew", "Test brew", healingAmount);
        potion.use(player);

        assertEquals(expectedHealth, player.getHealth());
    }
}