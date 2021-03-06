package dungeonsonline.server.storage;

import dungeonsonline.server.actor.minion.Minion;
import dungeonsonline.server.actor.minion.MinionDifficultyLevel;
import dungeonsonline.server.treasure.Treasure;
import dungeonsonline.server.treasure.potion.HealthPotion;
import dungeonsonline.server.treasure.potion.ManaPotion;
import dungeonsonline.server.treasure.potion.PotionSize;
import dungeonsonline.server.treasure.skill.Spell;
import dungeonsonline.server.treasure.skill.Weapon;
import dungeonsonline.server.validator.ArgumentValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaticObjectsStorage {

    private static final int NUMBER_OF_EASY_MINIONS = 4;
    private static final int NUMBER_OF_MEDIUM_MINIONS = 3;
    private static final int NUMBER_OF_HARD_MINIONS = 2;
    private static final int TOTAL_NUMBER_OF_MINIONS;

    static {
        TOTAL_NUMBER_OF_MINIONS =
            NUMBER_OF_EASY_MINIONS
                + NUMBER_OF_MEDIUM_MINIONS
                + NUMBER_OF_HARD_MINIONS;
    }

    private static final int NUMBER_OF_REGULAR_HEALTH_POTIONS = 4;
    private static final int NUMBER_OF_GREATER_HEALTH_POTIONS = 3;
    private static final int NUMBER_OF_SUPERIOR_HEALTH_POTIONS = 2;
    private static final int TOTAL_NUMBER_OF_HEALTH_POTIONS;

    static {
        TOTAL_NUMBER_OF_HEALTH_POTIONS =
            NUMBER_OF_REGULAR_HEALTH_POTIONS
                + NUMBER_OF_GREATER_HEALTH_POTIONS
                + NUMBER_OF_SUPERIOR_HEALTH_POTIONS;
    }

    private static final int NUMBER_OF_REGULAR_MANA_POTIONS = 3;
    private static final int NUMBER_OF_GREATER_MANA_POTIONS = 2;
    private static final int NUMBER_OF_SUPERIOR_MANA_POTIONS = 2;
    private static final int TOTAL_NUMBER_OF_MANA_POTIONS;

    static {
        TOTAL_NUMBER_OF_MANA_POTIONS =
            NUMBER_OF_REGULAR_MANA_POTIONS
                + NUMBER_OF_GREATER_MANA_POTIONS
                + NUMBER_OF_SUPERIOR_MANA_POTIONS;
    }

    private static final int NUMBER_OF_WEAPONS = 10;
    private static final Weapon woodenStick = new Weapon("Wooden Stick", 20, 1);
    private static final Weapon pole = new Weapon("Pole", 25, 1);
    private static final Weapon silverSword = new Weapon("Silver Sword", 30, 2);
    private static final Weapon rapier = new Weapon("Rapier", 40, 3);
    private static final Weapon dagger = new Weapon("Dagger", 50, 4);
    private static final Weapon reaper = new Weapon("The Reaper", 150, 10);

    private static final int NUMBER_OF_SPELLS = 4;
    private static final Spell iceArrow = new Spell("Ico Arrow", 30, 2, 20);
    private static final Spell celestialMagic = new Spell("Celestial Magic", 40, 3, 30);
    private static final Spell hurricane = new Spell("Hurricane", 50, 3, 50);
    private static final Spell poison = new Spell("Poison", 60, 4, 50);

    private static final int TOTAL_NUMBER_OF_TREASURES;

    static {
        TOTAL_NUMBER_OF_TREASURES =
            TOTAL_NUMBER_OF_HEALTH_POTIONS
                + TOTAL_NUMBER_OF_MANA_POTIONS
                + NUMBER_OF_WEAPONS
                + NUMBER_OF_SPELLS;
    }

    private static List<Minion> minions = null;
    private static int beatenMinions;
    private static List<Treasure> treasures = null;
    private static int takenTreasures;

    public StaticObjectsStorage() {
        minions = new ArrayList<>(TOTAL_NUMBER_OF_MINIONS);
        initializeMinions();
        Collections.shuffle(minions);


        treasures = new ArrayList<>(TOTAL_NUMBER_OF_TREASURES);
        initializeTreasures();
        Collections.shuffle(treasures);
    }

    public void addTreasure(Treasure treasure) {
        ArgumentValidator.checkForNullArguments(treasure);

        treasures.add(0, treasure);
    }

    public List<Minion> getMinions() {
        return minions;
    }

    public List<Treasure> getTreasures() {
        return treasures;
    }

    public Treasure getTreasure() {
        return treasures.get((takenTreasures++ % TOTAL_NUMBER_OF_TREASURES));
    }

    public Minion getMinion() {
        return minions.get((beatenMinions++ % TOTAL_NUMBER_OF_TREASURES));
    }

    private void initializeMinions() {
        initializeEasyMinions();
        initializeMediumMinions();
        initializeHardMinions();
    }

    private void initializeEasyMinions() {
        for (int i = 0; i < NUMBER_OF_EASY_MINIONS; ++i) {
            minions.add(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.EASY));
        }
    }

    private void initializeMediumMinions() {
        for (int i = 0; i < NUMBER_OF_MEDIUM_MINIONS; ++i) {
            minions.add(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.MEDIUM));
        }
    }

    private void initializeHardMinions() {
        for (int i = 0; i < NUMBER_OF_HARD_MINIONS; ++i) {
            minions.add(Minion.createMinionByDifficultyLevel(MinionDifficultyLevel.HARD));
        }
    }


    private void initializeTreasures() {
        initializePotions();
        initializeWeapons();
        initializeSpells();
    }

    private void initializePotions() {
        initializeHealthPotions();
        initializeManaPotions();
    }

    private void initializeHealthPotions() {
        initializeRegularHealthPotions();
        initializeGreaterHealthPotions();
        initializeSuperiorHealthPotions();
    }

    private void initializeRegularHealthPotions() {
        for (int i = 0; i < NUMBER_OF_REGULAR_HEALTH_POTIONS; ++i) {
            treasures.add(HealthPotion.createHealthPotionBySize(PotionSize.REGULAR));
        }
    }

    private void initializeGreaterHealthPotions() {
        for (int i = 0; i < NUMBER_OF_GREATER_HEALTH_POTIONS; ++i) {
            treasures.add(HealthPotion.createHealthPotionBySize(PotionSize.GREATER));
        }
    }

    private void initializeSuperiorHealthPotions() {
        for (int i = 0; i < NUMBER_OF_SUPERIOR_HEALTH_POTIONS; ++i) {
            treasures.add(HealthPotion.createHealthPotionBySize(PotionSize.SUPERIOR));
        }
    }

    private void initializeManaPotions() {
        initializeRegularManaPotions();
        initializeGreaterManaPotions();
        initializeSuperiorManaPotions();
    }

    private void initializeRegularManaPotions() {
        for (int i = 0; i < NUMBER_OF_REGULAR_MANA_POTIONS; ++i) {
            treasures.add(ManaPotion.createManaPotionBySize(PotionSize.REGULAR));
        }
    }

    private void initializeGreaterManaPotions() {
        for (int i = 0; i < NUMBER_OF_GREATER_MANA_POTIONS; ++i) {
            treasures.add(ManaPotion.createManaPotionBySize(PotionSize.GREATER));
        }
    }

    private void initializeSuperiorManaPotions() {
        for (int i = 0; i < NUMBER_OF_SUPERIOR_MANA_POTIONS; ++i) {
            treasures.add(ManaPotion.createManaPotionBySize(PotionSize.SUPERIOR));
        }
    }

    private void initializeWeapons() {
        treasures.add(woodenStick);
        treasures.add(woodenStick);
        treasures.add(pole);
        treasures.add(pole);
        treasures.add(silverSword);
        treasures.add(silverSword);
        treasures.add(rapier);
        treasures.add(dagger);
        treasures.add(reaper);
    }

    private void initializeSpells() {
        treasures.add(iceArrow);
        treasures.add(celestialMagic);
        treasures.add(hurricane);
        treasures.add(poison);
    }
}
