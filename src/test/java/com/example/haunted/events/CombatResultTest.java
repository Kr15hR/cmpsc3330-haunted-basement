package com.example.haunted.events;

import com.example.haunted.model.Item;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Weapon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatResultTest {

    @ParameterizedTest
    @CsvSource({
        "true, Hit monster for 7 damage., 7, 3, false",
        "true, Defeated monster!, 10, 0, true",
        "false, No monster to attack., 0, 0, false"
    })
    void testCombatResultGetters(boolean success, String message, int damageToMonster,
                                 int damageToPlayer, boolean monsterDefeated) {
        Item potion = new Potion("Coffee Potion", "Warm coffee", 15);
        List<Item> drops = List.of(potion);

        CombatResult result = new CombatResult(
                success, message, damageToMonster, damageToPlayer, monsterDefeated, drops
        );

        assertEquals(success, result.isSuccess());
        assertEquals(message, result.getMessage());
        assertEquals(damageToMonster, result.getDamageToMonster());
        assertEquals(damageToPlayer, result.getDamageToPlayer());
        assertEquals(monsterDefeated, result.isMonsterDefeated());
        assertEquals(1, result.getDroppedItems().size());
        assertSame(potion, result.getDroppedItems().get(0));
    }

    @Test
    void testDroppedItemsDefensiveCopyAndUnmodifiableList() {
        Item potion = new Potion("Coffee Potion", "Warm coffee", 15);
        Item weapon = new Weapon("Stapler of Justice", "Heavy stapler", 4);
        List<Item> mutableSource = new ArrayList<>();
        mutableSource.add(potion);

        CombatResult result = new CombatResult(true, "Victory", 12, 2, true, mutableSource);

        // Mutating the original list after construction must not affect CombatResult
        mutableSource.add(weapon);
        assertEquals(1, result.getDroppedItems().size());

        // Returned list must reject modification attempts
        List<Item> returnedDrops = result.getDroppedItems();
        assertThrows(UnsupportedOperationException.class, () -> returnedDrops.add(weapon));
    }
}