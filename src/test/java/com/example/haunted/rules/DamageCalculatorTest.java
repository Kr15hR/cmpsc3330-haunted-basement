package com.example.haunted.rules;

import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageCalculatorTest {

    private DamageCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DamageCalculator();
    }

    @ParameterizedTest
    @CsvSource({
        "10, 3, 7",   // Normal positive damage (10 - 3 = 7)
        "5, 4, 1",    // Exact boundary where difference equals 1
        "5, 5, 1",    // Zero difference clamps to minimum 1
        "4, 9, 1"     // Negative difference clamps to minimum 1
    })
    void testCalculatePlayerDamage(int playerAttack, int monsterDefense, int expectedDamage) {
        Player player = new Player("Tester", 50, playerAttack, 2, new Inventory(5));
        Monster monster = new Monster("Dummy", 20, 5, monsterDefense, List.of());

        assertEquals(expectedDamage, calculator.calculatePlayerDamage(player, monster));
    }

    @ParameterizedTest
    @CsvSource({
        "12, 4, 8",   // Standard monster normal damage (12 - 4 = 8)
        "6, 5, 1",    // Standard monster boundary difference equals 1
        "6, 6, 1",    // Standard monster zero difference clamps to 1
        "3, 8, 1"     // Standard monster negative difference clamps to 1
    })
    void testCalculateStandardMonsterDamage(int monsterAttack, int playerDefense, int expectedDamage) {
        Monster monster = new Monster("Ghoul", 20, monsterAttack, 1, List.of());
        Player player = new Player("Tester", 50, 5, playerDefense, new Inventory(5));

        assertEquals(expectedDamage, calculator.calculateMonsterDamage(monster, player));
    }

    @ParameterizedTest
    @CsvSource({
        "40, 40, 10, 3, 4, 6",   // Full HP boss uses base attack (10 - 4 = 6)
        "10, 40, 10, 3, 4, 9",   // Damaged/enraged boss uses boosted currentAttack (13 - 4 = 9)
        "40, 40, 5, 3, 5, 1",    // Full HP boss clamped at minimum 1
        "10, 40, 5, 3, 12, 1"    // Enraged boss clamped at minimum 1
    })
    void testCalculateBossMonsterDamage(int currentHp, int maxHp, int baseAttack, int enrageBonus,
                                        int playerDefense, int expectedDamage) {
        BossMonster boss = new BossMonster("Final Exam Phantom", maxHp, baseAttack, 2, List.of(), enrageBonus);
        if (currentHp < maxHp) {
            boss.takeDamage(maxHp - currentHp);
        }
        Player player = new Player("Tester", 50, 5, playerDefense, new Inventory(5));

        assertEquals(expectedDamage, calculator.calculateMonsterDamage(boss, player));
    }
}