package com.example.haunted.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestTest {

    private Quest quest;

    @BeforeEach
    void setUp() {
        quest = new Quest(
                "Escape the Basement",
                "Recover the Lost Gradebook and defeat the Final Exam Phantom."
        );
    }

    @Test
    void testInitialStateGettersAndNullGuards() {
        assertEquals("Escape the Basement", quest.getName());
        assertEquals("Recover the Lost Gradebook and defeat the Final Exam Phantom.", quest.getDescription());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
        assertFalse(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
        assertFalse(quest.isComplete());

        assertThrows(NullPointerException.class, () -> new Quest(null, "Valid description"));
        assertThrows(NullPointerException.class, () -> new Quest("Valid Name", null));
    }

    @Test
    void testGradebookFirstThenPhantomProgression() {
        quest.markGradebookRecovered();
        assertTrue(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());

        quest.markPhantomDefeated();
        assertTrue(quest.isGradebookRecovered());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        assertTrue(quest.isComplete());
    }

    @Test
    void testPhantomFirstThenGradebookProgression() {
        quest.markPhantomDefeated();
        assertFalse(quest.isGradebookRecovered());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.IN_PROGRESS, quest.getStatus());
        assertFalse(quest.isComplete());

        quest.markGradebookRecovered();
        assertTrue(quest.isGradebookRecovered());
        assertTrue(quest.isPhantomDefeated());
        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        assertTrue(quest.isComplete());
    }
}