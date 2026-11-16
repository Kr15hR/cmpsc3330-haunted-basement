package com.example.haunted.rules;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TrapResolverTest {

    private TrapResolver resolver;
    private Player player;

    @BeforeEach
    void setUp() {
        resolver = new TrapResolver();
        player = new Player("Student Explorer", 50, 7, 2, new Inventory(8));
    }

    @Test
    void testResolveNullTrapDoesNothing() {
        InteractionResult result = resolver.resolveTrap(player, null);

        assertFalse(result.isSuccess());
        assertEquals("No trap was triggered.", result.getMessage());
        assertEquals(50, player.getHealth());
    }

    @Test
    void testResolveDisarmedTrapDoesNothing() {
        Trap disarmedTrap = new Trap("Loose Wires Trap", TrapType.ELECTRIC, 8, false, true);

        InteractionResult result = resolver.resolveTrap(player, disarmedTrap);

        assertFalse(result.isSuccess());
        assertEquals("No trap was triggered.", result.getMessage());
        assertEquals(50, player.getHealth());
        assertFalse(disarmedTrap.isArmed());
    }

    @ParameterizedTest
    @CsvSource({
        "Loose Wires Trap, 8, true, 42, false",    // One-time trigger deals damage and disarms
        "Live Conduit, 15, false, 35, true",       // Persistent trap deals damage and stays armed
        "Static Shock, 0, true, 50, false"         // Zero-damage one-time trap still triggers and disarms
    })
    void testResolveArmedTrapHandlesDamageAndDisarmState(String trapName, int damage, boolean oneTime,
                                                         int expectedHealth, boolean expectedRemainingArmed) {
        Trap trap = new Trap(trapName, TrapType.ELECTRIC, damage, true, oneTime);

        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(String.format("Trap '%s' triggered for %d damage.", trapName, damage), result.getMessage());
        assertEquals(expectedHealth, player.getHealth());
        assertEquals(expectedRemainingArmed, trap.isArmed());
    }

    @Test
    void testOneTimeTrapDoesNotTriggerSecondTime() {
        Trap oneTimeTrap = new Trap("Loose Wires Trap", TrapType.ELECTRIC, 8, true, true);

        InteractionResult firstTrigger = resolver.resolveTrap(player, oneTimeTrap);
        assertTrue(firstTrigger.isSuccess());
        assertEquals(42, player.getHealth());
        assertFalse(oneTimeTrap.isArmed());

        InteractionResult secondTrigger = resolver.resolveTrap(player, oneTimeTrap);
        assertFalse(secondTrigger.isSuccess());
        assertEquals("No trap was triggered.", secondTrigger.getMessage());
        assertEquals(42, player.getHealth());
    }
}