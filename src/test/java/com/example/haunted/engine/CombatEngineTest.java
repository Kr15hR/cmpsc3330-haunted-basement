package com.example.haunted.engine;

import com.example.haunted.events.CombatResult;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Item;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CombatEngineTest {

    private CombatEngine combatEngine;
    private Player player;
    private Quest quest;
    private Room room;

    @BeforeEach
    void setUp() {
        combatEngine = new CombatEngine(new DamageCalculator(), new QuestTracker());
        room = new Room("lectureHall", "Abandoned Lecture Hall", "Dusty rows of desks.");
        player = new Player("Explorer", 50, 10, 2, new Inventory(8));
        player.setCurrentRoom(room);
        quest = new Quest("Escape", "Recover the gradebook and defeat the phantom.");
    }

    @Test
    void testAttackNullMonsterReturnsFailure() {
        CombatResult result = combatEngine.attack(player, quest, null);

        assertFalse(result.isSuccess());
        assertEquals("Monster not found.", result.getMessage());
        assertEquals(0, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertFalse(result.isMonsterDefeated());
        assertTrue(result.getDroppedItems().isEmpty());
    }

    @Test
    void testAttackWhenPlayerIsDefeatedReturnsFailure() {
        player.takeDamage(50);
        assertFalse(player.isAlive());
        Monster ghoul = new Monster("Ghoul", 20, 6, 1, List.of());

        CombatResult result = combatEngine.attack(player, quest, ghoul);

        assertFalse(result.isSuccess());
        assertEquals("Player is defeated.", result.getMessage());
        assertEquals(0, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertFalse(result.isMonsterDefeated());
        assertEquals(20, ghoul.getHealth());
    }

    @Test
    void testAttackAlreadyDefeatedMonsterReturnsFailure() {
        Monster defeatedGhoul = new Monster("Ghoul", 20, 6, 1, List.of());
        defeatedGhoul.takeDamage(20);
        assertFalse(defeatedGhoul.isAlive());

        CombatResult result = combatEngine.attack(player, quest, defeatedGhoul);

        assertFalse(result.isSuccess());
        assertEquals("Monster is already defeated.", result.getMessage());
        assertEquals(0, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertTrue(result.isMonsterDefeated());
    }

    @Test
    void testNonLethalAttackDamagesMonsterAndTriggersCounterAttack() {
        // Player attack 10 - monster defense 3 = 7 damage to monster (20 -> 13 HP)
        // Monster attack 8 - player defense 2 = 6 damage to player (50 -> 44 HP)
        Monster ghoul = new Monster("Library Ghoul", 20, 8, 3, List.of());

        CombatResult result = combatEngine.attack(player, quest, ghoul);

        assertTrue(result.isSuccess());
        assertEquals("Attacked Library Ghoul.", result.getMessage());
        assertEquals(7, result.getDamageToMonster());
        assertEquals(6, result.getDamageToPlayer());
        assertFalse(result.isMonsterDefeated());
        assertTrue(result.getDroppedItems().isEmpty());
        assertEquals(13, ghoul.getHealth());
        assertEquals(44, player.getHealth());
    }

    @Test
    void testLethalAttackDropsLootIntoRoomUpdatesQuestAndAvoidsCounterAttack() {
        Item lootPotion = new Potion("Espresso Shot", "Restores energy", 15);
        Item lootWeapon = new Weapon("Ruler of Doom", "Sharp wooden ruler", 5);
        BossMonster phantom = new BossMonster(
                "Final Exam Phantom", 8, 12, 2, List.of(lootPotion, lootWeapon), 4
        );

        CombatResult result = combatEngine.attack(player, quest, phantom);

        assertTrue(result.isSuccess());
        assertEquals("Defeated Final Exam Phantom.", result.getMessage());
        assertEquals(8, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
        assertTrue(result.isMonsterDefeated());
        assertFalse(phantom.isAlive());
        assertEquals(50, player.getHealth());

        // Verify loot was added both to CombatResult and the player's current room
        assertEquals(2, result.getDroppedItems().size());
        assertTrue(room.getItems().contains(lootPotion));
        assertTrue(room.getItems().contains(lootWeapon));

        // Verify QuestTracker updated the quest state
        assertTrue(quest.isPhantomDefeated());
    }
}