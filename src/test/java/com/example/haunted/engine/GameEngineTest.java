package com.example.haunted.engine;

import com.example.haunted.events.CombatResult;
import com.example.haunted.events.InteractionResult;
import com.example.haunted.events.MoveResult;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Key;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;
import com.example.haunted.rules.TrapResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private Player player;
    private Quest quest;
    private Room startRoom;
    private Room northRoom;
    private GameEngine gameEngine;

    @BeforeEach
    void setUp() {
        startRoom = new Room("stairwell", "Maintenance Stairwell", "Dim stairwell.");
        northRoom = new Room("vault", "Dean Vault", "Hidden chamber.");
        northRoom.setLocked(true, "Dean Key");
        startRoom.connect(Direction.NORTH, northRoom);

        player = new Player("Explorer", 50, 10, 2, new Inventory(8));
        player.setCurrentRoom(startRoom);
        quest = new Quest("Escape", "Recover the Lost Gradebook and defeat the Final Exam Phantom.");

        QuestTracker questTracker = new QuestTracker();
        MovementEngine movementEngine = new MovementEngine(new TrapResolver());
        CombatEngine combatEngine = new CombatEngine(new DamageCalculator(), questTracker);
        InteractionEngine interactionEngine = new InteractionEngine(questTracker);

        gameEngine = new GameEngine(player, quest, movementEngine, combatEngine, interactionEngine);
    }

    @Test
    void testGettersAndInitialGameState() {
        assertSame(player, gameEngine.getPlayer());
        assertSame(quest, gameEngine.getQuest());
        assertSame(startRoom, gameEngine.getCurrentRoom());
        assertFalse(gameEngine.isGameOver());
        assertFalse(gameEngine.isGameWon());
    }

    @Test
    void testUnlockRoomAndMoveDelegation() {
    	player.getInventory().addItem(new Key("Dean Key", "Opens the Dean Vault."));
        InteractionResult unlockResult = gameEngine.unlockRoom(Direction.NORTH);
        assertTrue(unlockResult.isSuccess());
        assertFalse(northRoom.isLocked());

        MoveResult moveResult = gameEngine.move(Direction.NORTH);
        assertTrue(moveResult.isSuccess());
        assertSame(northRoom, gameEngine.getCurrentRoom());
    }

    @Test
    void testPickUpEquipAndUseItemDelegation() {
        Weapon sword = new Weapon("Stapler of Justice", "Heavy stapler", 4);
        Potion coffee = new Potion("Coffee Potion", "Restores 15 HP", 15);
        startRoom.addItem(sword);
        startRoom.addItem(coffee);
        player.takeDamage(20); // HP -> 30

        InteractionResult pickUpWeapon = gameEngine.pickUpItem("Stapler of Justice");
        assertTrue(pickUpWeapon.isSuccess());

        InteractionResult equipResult = gameEngine.equipItem("Stapler of Justice");
        assertTrue(equipResult.isSuccess());
        assertEquals(14, player.getAttackPower());

        InteractionResult pickUpPotion = gameEngine.pickUpItem("Coffee Potion");
        assertTrue(pickUpPotion.isSuccess());

        InteractionResult useResult = gameEngine.useItem("Coffee Potion");
        assertTrue(useResult.isSuccess());
        assertEquals(45, player.getHealth());
    }

    @Test
    void testAttackDelegationFoundAndNotFound() {
        CombatResult missingResult = gameEngine.attack("Nonexistent Ghost");
        assertFalse(missingResult.isSuccess());
        assertEquals("Monster not found.", missingResult.getMessage());

        Monster ghoul = new Monster("Hallway Ghoul", 15, 6, 1, List.of());
        startRoom.addMonster(ghoul);

        CombatResult hitResult = gameEngine.attack("Hallway Ghoul");
        assertTrue(hitResult.isSuccess());
        assertEquals(9, hitResult.getDamageToMonster());
        assertEquals(6, ghoul.getHealth());
    }

    @Test
    void testGameOverAndGameWonConditions() {
        // 1. Quest incomplete, player alive -> neither won nor over
        assertFalse(gameEngine.isGameWon());
        assertFalse(gameEngine.isGameOver());

        // 2. Complete quest while player is alive -> game won, not over
        startRoom.addItem(new QuestItem("Lost Gradebook", "The missing gradebook."));
        gameEngine.pickUpItem("Lost Gradebook");
        BossMonster phantom = new BossMonster("Final Exam Phantom", 5, 10, 1, List.of(), 3);
        startRoom.addMonster(phantom);
        gameEngine.attack("Final Exam Phantom");

        assertTrue(quest.isComplete());
        assertTrue(gameEngine.isGameWon());
        assertFalse(gameEngine.isGameOver());

        // 3. Quest complete, but player dies -> game over, NOT won
        player.takeDamage(100);
        assertFalse(player.isAlive());
        assertTrue(gameEngine.isGameOver());
        assertFalse(gameEngine.isGameWon());
    }
}