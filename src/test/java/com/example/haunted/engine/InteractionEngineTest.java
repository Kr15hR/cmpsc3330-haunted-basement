package com.example.haunted.engine;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Key;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.QuestTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InteractionEngineTest {

    private InteractionEngine interactionEngine;
    private Player player;
    private Quest quest;
    private Room startRoom;

    @BeforeEach
    void setUp() {
        interactionEngine = new InteractionEngine(new QuestTracker());
        startRoom = new Room("lectureHall", "Abandoned Lecture Hall", "Rows of dusty desks.");
        player = new Player("Explorer", 50, 8, 2, new Inventory(2));
        player.setCurrentRoom(startRoom);
        quest = new Quest("Escape", "Recover the Lost Gradebook and defeat the Final Exam Phantom.");
    }

    // --- pickUpItem tests ---

    @Test
    void testPickUpItemNotFoundInRoom() {
        InteractionResult result = interactionEngine.pickUpItem(player, quest, "Ghost Key");

        assertFalse(result.isSuccess());
        assertEquals("Item not found in the room.", result.getMessage());
    }

    @Test
    void testPickUpItemWhenInventoryIsFullRollsBackItemToRoom() {
        player.getInventory().addItem(new Potion("Coffee 1", "Warm", 10));
        player.getInventory().addItem(new Potion("Coffee 2", "Hot", 10));
        Weapon groundWeapon = new Weapon("Heavy Ruler", "Wooden ruler", 3);
        startRoom.addItem(groundWeapon);

        InteractionResult result = interactionEngine.pickUpItem(player, quest, "Heavy Ruler");

        assertFalse(result.isSuccess());
        assertEquals("Inventory is full.", result.getMessage());
        // Item must remain in the room after failed pickup
        assertTrue(startRoom.getItems().contains(groundWeapon));
    }

    @Test
    void testPickUpQuestItemUpdatesQuestTracker() {
        QuestItem gradebook = new QuestItem("Lost Gradebook", "Missing faculty gradebook.");
        startRoom.addItem(gradebook);

        InteractionResult result = interactionEngine.pickUpItem(player, quest, "Lost Gradebook");

        assertTrue(result.isSuccess());
        assertEquals("Picked up Lost Gradebook.", result.getMessage());
        assertFalse(startRoom.getItems().contains(gradebook));
        assertTrue(player.getInventory().findItem("Lost Gradebook").isPresent());
        assertTrue(quest.isGradebookRecovered());
    }

    // --- useItem tests ---

    @Test
    void testUseItemNotInInventory() {
        InteractionResult result = interactionEngine.useItem(player, "Missing Potion");

        assertFalse(result.isSuccess());
        assertEquals("Item not found in inventory.", result.getMessage());
    }

    @Test
    void testUseNonPotionItemFails() {
        Weapon sword = new Weapon("Stapler", "Heavy stapler", 3);
        player.getInventory().addItem(sword);

        InteractionResult result = interactionEngine.useItem(player, "Stapler");

        assertFalse(result.isSuccess());
        assertEquals("That item cannot be used.", result.getMessage());
        assertTrue(player.getInventory().findItem("Stapler").isPresent());
    }

    @Test
    void testUsePotionHealsPlayerAndRemovesFromInventory() {
        player.takeDamage(25); // 50 -> 25 HP
        Potion coffee = new Potion("Espresso Shot", "Restores 15 HP", 15);
        player.getInventory().addItem(coffee);

        InteractionResult result = interactionEngine.useItem(player, "Espresso Shot");

        assertTrue(result.isSuccess());
        assertEquals("Used Espresso Shot.", result.getMessage());
        assertEquals(40, player.getHealth());
        assertTrue(player.getInventory().findItem("Espresso Shot").isEmpty());
    }

    // --- equipItem tests ---

    @Test
    void testEquipItemNotInInventory() {
        InteractionResult result = interactionEngine.equipItem(player, "Phantom Blade");

        assertFalse(result.isSuccess());
        assertEquals("Item not found in inventory.", result.getMessage());
    }

    @Test
    void testEquipWeaponArmorAndNonEquippableItem() {
        Weapon weapon = new Weapon("Laser Pointer", "High-powered beam", 5);
        Armor armor = new Armor("Lab Coat", "Thick protective coat", 4);
        player.getInventory().addItem(weapon);
        player.getInventory().addItem(armor);

        InteractionResult weaponResult = interactionEngine.equipItem(player, "Laser Pointer");
        assertTrue(weaponResult.isSuccess());
        assertEquals("Equipped weapon Laser Pointer.", weaponResult.getMessage());
        assertEquals(13, player.getAttackPower());

        InteractionResult armorResult = interactionEngine.equipItem(player, "Lab Coat");
        assertTrue(armorResult.isSuccess());
        assertEquals("Equipped armor Lab Coat.", armorResult.getMessage());
        assertEquals(6, player.getDefensePower());

        // Replace inventory contents with a non-equippable Potion
        player.getInventory().removeItem("Laser Pointer");
        player.getInventory().addItem(new Potion("Tea", "Herbal tea", 10));

        InteractionResult invalidEquip = interactionEngine.equipItem(player, "Tea");
        assertFalse(invalidEquip.isSuccess());
        assertEquals("That item cannot be equipped.", invalidEquip.getMessage());
    }

    // --- unlockRoom tests ---

    @Test
    void testUnlockRoomMissingExitAndAlreadyUnlockedRoom() {
        InteractionResult noExit = interactionEngine.unlockRoom(player, Direction.WEST);
        assertFalse(noExit.isSuccess());
        assertEquals("There is no room in that direction.", noExit.getMessage());

        Room openRoom = new Room("hallway", "Main Hallway", "Open corridor.");
        startRoom.connect(Direction.EAST, openRoom);

        InteractionResult alreadyOpen = interactionEngine.unlockRoom(player, Direction.EAST);
        assertTrue(alreadyOpen.isSuccess());
        assertEquals("The room is already unlocked.", alreadyOpen.getMessage());
    }

    @Test
    void testUnlockRoomWithNullRequiredKeyReturnsCannotBeUnlocked() {
        Room sealedRoom = new Room("sealed", "Sealed Chamber", "No keyhole.");
        sealedRoom.setLocked(true, null);
        startRoom.connect(Direction.SOUTH, sealedRoom);

        InteractionResult result = interactionEngine.unlockRoom(player, Direction.SOUTH);

        assertFalse(result.isSuccess());
        assertEquals("The room cannot be unlocked.", result.getMessage());
        assertTrue(sealedRoom.isLocked());
    }

    @Test
    void testUnlockRoomRequiresActualKeyInstanceAndUnlocksWithValidKey() {
        Room vault = new Room("vault", "Dean Vault", "Locked vault.");
        vault.setLocked(true, "Dean Key");
        startRoom.connect(Direction.NORTH, vault);

        // 1. Missing key completely
        InteractionResult missingKey = interactionEngine.unlockRoom(player, Direction.NORTH);
        assertFalse(missingKey.isSuccess());
        assertEquals("You do not have the correct key.", missingKey.getMessage());
        assertTrue(vault.isLocked());

        // 2. Non-Key item sharing the exact same name must still fail instanceof Key check
        player.getInventory().addItem(new QuestItem("Dean Key", "Fake paper replica."));
        InteractionResult fakeKeyResult = interactionEngine.unlockRoom(player, Direction.NORTH);
        assertFalse(fakeKeyResult.isSuccess());
        assertEquals("You do not have the correct key.", fakeKeyResult.getMessage());
        assertTrue(vault.isLocked());
        player.getInventory().removeItem("Dean Key");

        // 3. Real Key unlocks the room
        player.getInventory().addItem(new Key("Dean Key", "Heavy brass key."));
        InteractionResult validUnlock = interactionEngine.unlockRoom(player, Direction.NORTH);
        assertTrue(validUnlock.isSuccess());
        assertEquals("Unlocked Dean Vault.", validUnlock.getMessage());
        assertFalse(vault.isLocked());
    }
}