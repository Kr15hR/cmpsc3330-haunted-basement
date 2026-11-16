package com.example.haunted.engine;

import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import com.example.haunted.rules.TrapResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementEngineTest {

    private MovementEngine movementEngine;
    private Player player;
    private Room startRoom;

    @BeforeEach
    void setUp() {
        movementEngine = new MovementEngine(new TrapResolver());
        startRoom = new Room("stairwell", "Maintenance Stairwell", "Dim stairwell.");
        player = new Player("Explorer", 50, 8, 2, new Inventory(8));
        player.setCurrentRoom(startRoom);
    }

    @Test
    void testMoveIntoMissingDirectionReturnsFailure() {
        MoveResult result = movementEngine.move(player, Direction.WEST);

        assertFalse(result.isSuccess());
        assertEquals("There is no room in that direction.", result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertSame(startRoom, player.getCurrentRoom());
    }

    @Test
    void testMoveIntoLockedRoomReturnsFailure() {
        Room vault = new Room("vault", "Dean Vault", "Locked chamber.");
        vault.setLocked(true, "Dean Key");
        startRoom.connect(Direction.NORTH, vault);

        MoveResult result = movementEngine.move(player, Direction.NORTH);

        assertFalse(result.isSuccess());
        assertEquals("The room is locked.", result.getMessage());
        assertFalse(result.isTrapTriggered());
        assertEquals(0, result.getTrapDamage());
        assertSame(startRoom, player.getCurrentRoom());
    }

    @Test
    void testMoveIntoSafeRoomAndDisarmedTrapRoomDoesNotTriggerTrap() {
        Room hallway = new Room("hallway", "Main Hallway", "Quiet corridor.");
        startRoom.connect(Direction.EAST, hallway);

        MoveResult safeMove = movementEngine.move(player, Direction.EAST);
        assertTrue(safeMove.isSuccess());
        assertEquals("Moved to Main Hallway.", safeMove.getMessage());
        assertFalse(safeMove.isTrapTriggered());
        assertEquals(0, safeMove.getTrapDamage());
        assertSame(hallway, player.getCurrentRoom());
        assertEquals(50, player.getHealth());

        // Room with an already disarmed trap must behave identically to a trap-free room
        Room lab = new Room("lab", "Computer Lab", "Rows of dark monitors.");
        Trap disarmedTrap = new Trap("Static Grid", TrapType.ELECTRIC, 10, false, true);
        lab.setTrap(disarmedTrap);
        hallway.connect(Direction.SOUTH, lab);

        MoveResult disarmedMove = movementEngine.move(player, Direction.SOUTH);
        assertTrue(disarmedMove.isSuccess());
        assertEquals("Moved to Computer Lab.", disarmedMove.getMessage());
        assertFalse(disarmedMove.isTrapTriggered());
        assertEquals(0, disarmedMove.getTrapDamage());
        assertSame(lab, player.getCurrentRoom());
        assertEquals(50, player.getHealth());
    }

    @Test
    void testMoveIntoArmedTrapRoomResolvesTrapAndUpdatesState() {
        Room elevator = new Room("elevator", "Broken Elevator", "Sparking control panel.");
        Trap looseWires = new Trap("Loose Wires Trap", TrapType.ELECTRIC, 8, true, true);
        elevator.setTrap(looseWires);
        startRoom.connect(Direction.SOUTH, elevator);

        MoveResult result = movementEngine.move(player, Direction.SOUTH);

        assertTrue(result.isSuccess());
        assertEquals(
                "Moved to Broken Elevator. Trap 'Loose Wires Trap' triggered for 8 damage.",
                result.getMessage()
        );
        assertTrue(result.isTrapTriggered());
        assertEquals(8, result.getTrapDamage());
        assertSame(elevator, player.getCurrentRoom());
        assertEquals(42, player.getHealth());
        assertFalse(looseWires.isArmed());
    }
}