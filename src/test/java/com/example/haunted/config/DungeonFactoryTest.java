package com.example.haunted.config;

import com.example.haunted.engine.GameEngine;
import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Quest;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DungeonFactoryTest {

    private GameEngine engine;
    private Player player;
    private Quest quest;

    @BeforeEach
    void setUp() {
        engine = DungeonFactory.createGame();
        assertNotNull(engine);
        player = engine.getPlayer();
        quest = engine.getQuest();
    }

    @Test
    void testInitialPlayerAndQuestState() {
        assertNotNull(player);
        assertEquals("Student Explorer", player.getName());
        assertEquals(50, player.getMaxHealth());
        assertEquals(50, player.getHealth());
        assertEquals(7, player.getBaseAttack());
        assertEquals(2, player.getBaseDefense());
        assertNotNull(player.getInventory());
        assertEquals(8, player.getInventory().getCapacity());

        assertNotNull(quest);
        assertEquals("Escape the Basement", quest.getName());
        assertEquals("Recover the Lost Gradebook and defeat the Final Exam Phantom.", quest.getDescription());
    }

    @Test
    void testDungeonTopologyAndConnections() {
        Room stairwell = player.getCurrentRoom();
        assertNotNull(stairwell);
        assertEquals("stairwell", stairwell.getId());
        assertEquals("Maintenance Stairwell", stairwell.getName());
        assertEquals("The flickering stairwell leading into the basement.", stairwell.getDescription());

        Room lectureHall = stairwell.getExit(Direction.EAST);
        assertNotNull(lectureHall);
        assertEquals("lectureHall", lectureHall.getId());
        assertEquals("Abandoned Lecture Hall", lectureHall.getName());
        assertEquals("Dusty desks and a broken projector.", lectureHall.getDescription());
        assertSame(stairwell, lectureHall.getExit(Direction.WEST));

        Room labStorage = lectureHall.getExit(Direction.EAST);
        assertNotNull(labStorage);
        assertEquals("labStorage", labStorage.getId());
        assertEquals("Lab Storage", labStorage.getName());
        assertEquals("Shelves of outdated hardware and chemicals.", labStorage.getDescription());
        assertSame(lectureHall, labStorage.getExit(Direction.WEST));

        Room brokenElevator = lectureHall.getExit(Direction.SOUTH);
        assertNotNull(brokenElevator);
        assertEquals("brokenElevator", brokenElevator.getId());
        assertEquals("Broken Elevator", brokenElevator.getName());
        assertEquals("The elevator doors hang over a dark shaft.", brokenElevator.getDescription());
        assertSame(lectureHall, brokenElevator.getExit(Direction.NORTH));

        Room examArchive = lectureHall.getExit(Direction.NORTH);
        assertNotNull(examArchive);
        assertEquals("examArchive", examArchive.getId());
        assertEquals("Exam Archive", examArchive.getName());
        assertEquals("Cabinets of old exams and paperwork.", examArchive.getDescription());
        assertSame(lectureHall, examArchive.getExit(Direction.SOUTH));

        Room serverCloset = labStorage.getExit(Direction.NORTH);
        assertNotNull(serverCloset);
        assertEquals("serverCloset", serverCloset.getId());
        assertEquals("Server Closet", serverCloset.getName());
        assertEquals("A warm humming room full of blinking servers.", serverCloset.getDescription());
        assertSame(labStorage, serverCloset.getExit(Direction.SOUTH));

        Room deanVault = serverCloset.getExit(Direction.EAST);
        assertNotNull(deanVault);
        assertEquals("deanVault", deanVault.getId());
        assertEquals("Dean Vault", deanVault.getName());
        assertEquals("A hidden chamber guarded by university secrets.", deanVault.getDescription());
        assertSame(serverCloset, deanVault.getExit(Direction.SOUTH));
        assertSame(examArchive, deanVault.getExit(Direction.WEST));
        assertSame(deanVault, examArchive.getExit(Direction.EAST));

        Room finalChamber = deanVault.getExit(Direction.NORTH);
        assertNotNull(finalChamber);
        assertEquals("finalChamber", finalChamber.getId());
        assertEquals("Final Chamber", finalChamber.getName());
        assertEquals("A spectral exam hall where the boss waits.", finalChamber.getDescription());
        assertSame(deanVault, finalChamber.getExit(Direction.SOUTH));
    }

    @Test
    void testLockedRoomsTrapsItemsAndMonsters() {
        Room stairwell = player.getCurrentRoom();
        Room lectureHall = stairwell.getExit(Direction.EAST);
        Room labStorage = lectureHall.getExit(Direction.EAST);
        Room brokenElevator = lectureHall.getExit(Direction.SOUTH);
        Room examArchive = lectureHall.getExit(Direction.NORTH);
        Room serverCloset = labStorage.getExit(Direction.NORTH);
        Room deanVault = serverCloset.getExit(Direction.EAST);
        Room finalChamber = deanVault.getExit(Direction.NORTH);

        // Locks
        assertFalse(stairwell.isLocked());
        assertTrue(examArchive.isLocked());
        assertEquals("Archive Key", examArchive.getRequiredKeyName());
        assertTrue(finalChamber.isLocked());
        assertEquals("Vault Key", finalChamber.getRequiredKeyName());

        // Trap
        Trap trap = brokenElevator.getTrap();
        assertNotNull(trap);
        assertEquals("Loose Wires Trap", trap.getName());
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(8, trap.getDamage());
        assertTrue(trap.isArmed());

        // Items per room
        assertEquals(1, lectureHall.getItems().size());
        assertEquals("Coffee Potion", lectureHall.getItems().get(0).getName());

        assertEquals(2, labStorage.getItems().size());
        assertEquals("Archive Key", labStorage.getItems().get(0).getName());
        assertEquals("Calculator Shield", labStorage.getItems().get(1).getName());

        assertEquals(1, serverCloset.getItems().size());
        assertEquals("Stapler of Justice", serverCloset.getItems().get(0).getName());

        assertEquals(1, examArchive.getItems().size());
        assertEquals("Lost Gradebook", examArchive.getItems().get(0).getName());

        assertEquals(1, deanVault.getItems().size());
        assertEquals("Vault Key", deanVault.getItems().get(0).getName());

        // Monsters per room
        assertEquals(1, lectureHall.getMonsters().size());
        Monster ta = lectureHall.getMonsters().get(0);
        assertEquals("Sleep-Deprived TA", ta.getName());
        assertEquals(18, ta.getHealth());
        assertEquals(6, ta.getAttack());
        assertEquals(1, ta.getDefense());
        assertEquals(1, ta.getLoot().size());

        assertEquals(1, serverCloset.getMonsters().size());
        Monster golem = serverCloset.getMonsters().get(0);
        assertEquals("Spreadsheet Golem", golem.getName());
        assertEquals(28, golem.getHealth());
        assertEquals(7, golem.getAttack());
        assertEquals(4, golem.getDefense());
        assertEquals(1, golem.getLoot().size());

        assertEquals(1, examArchive.getMonsters().size());
        Monster ghost = examArchive.getMonsters().get(0);
        assertEquals("Plagiarism Ghost", ghost.getName());
        assertEquals(22, ghost.getHealth());
        assertEquals(8, ghost.getAttack());
        assertEquals(2, ghost.getDefense());
        assertTrue(ghost.getLoot().isEmpty());

        assertEquals(1, deanVault.getMonsters().size());
        Monster wraith = deanVault.getMonsters().get(0);
        assertEquals("Registrar Wraith", wraith.getName());
        assertEquals(30, wraith.getHealth());
        assertEquals(9, wraith.getAttack());
        assertEquals(3, wraith.getDefense());
        assertEquals(1, wraith.getLoot().size());

        assertEquals(1, finalChamber.getMonsters().size());
        Monster boss = finalChamber.getMonsters().get(0);
        assertInstanceOf(BossMonster.class, boss);
        assertEquals("Final Exam Phantom", boss.getName());
        assertEquals(40, boss.getHealth());
        assertEquals(10, boss.getAttack());
        assertEquals(4, boss.getDefense());
        assertTrue(boss.getLoot().isEmpty());
    }
}