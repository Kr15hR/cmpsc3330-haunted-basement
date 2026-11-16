package com.example.haunted.rules;

import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTrackerTest {

    private QuestTracker tracker;
    private Quest quest;

    @BeforeEach
    void setUp() {
        tracker = new QuestTracker();
        quest = new Quest(
                "Escape the Basement",
                "Recover the Lost Gradebook and defeat the Final Exam Phantom."
        );
    }

    @Test
    void testUpdateQuestForItemIgnoresNullAndUnrelatedItems() {
        tracker.updateQuestForItem(quest, null);
        assertFalse(quest.isGradebookRecovered());

        tracker.updateQuestForItem(quest, new Potion("Coffee Potion", "Warm coffee", 15));
        assertFalse(quest.isGradebookRecovered());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Lost Gradebook", "lost gradebook", "LOST GRADEBOOK"})
    void testUpdateQuestForItemMarksGradebookRecoveredCaseInsensitively(String itemName) {
        QuestItem gradebook = new QuestItem(itemName, "The legendary missing gradebook.");

        tracker.updateQuestForItem(quest, gradebook);

        assertTrue(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void testUpdateQuestForMonsterIgnoresNullAndOtherMonsters() {
        tracker.updateQuestForMonster(quest, null);
        assertFalse(quest.isPhantomDefeated());

        Monster defeatedTa = new Monster("Sleep-Deprived TA", 18, 6, 1, List.of());
        defeatedTa.takeDamage(18);
        assertFalse(defeatedTa.isAlive());

        tracker.updateQuestForMonster(quest, defeatedTa);
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    void testUpdateQuestForMonsterDoesNotTriggerWhilePhantomIsAlive() {
        BossMonster alivePhantom = new BossMonster("Final Exam Phantom", 40, 10, 4, List.of(), 3);
        assertTrue(alivePhantom.isAlive());

        tracker.updateQuestForMonster(quest, alivePhantom);
        assertFalse(quest.isPhantomDefeated());

        // Partial damage still leaves phantom alive
        alivePhantom.takeDamage(39);
        assertTrue(alivePhantom.isAlive());

        tracker.updateQuestForMonster(quest, alivePhantom);
        assertFalse(quest.isPhantomDefeated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Final Exam Phantom", "final exam phantom", "FINAL EXAM PHANTOM"})
    void testUpdateQuestForMonsterMarksPhantomDefeatedWhenDead(String bossName) {
        BossMonster defeatedPhantom = new BossMonster(bossName, 40, 10, 4, List.of(), 3);
        defeatedPhantom.takeDamage(40);
        assertFalse(defeatedPhantom.isAlive());

        tracker.updateQuestForMonster(quest, defeatedPhantom);

        assertTrue(quest.isPhantomDefeated());
        assertFalse(quest.isGradebookRecovered());
    }
}