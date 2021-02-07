package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.hero;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.Actor;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Weapon;

public interface IHero extends Actor {

    /**
     * @return Hero's backpack.
     */
    Backpack backpack();

    /**
     * Hero gains given amount of experience.
     * @return
     */
    int gainExperience(int amountOfExperience);

    /**
     * Hero collects given treasure to its backpack.
     */
    void collectTreasure(Treasure treasure);

    /**
     * Hero heals - increases current health by given amount.
     * @param healingPoints must be positive integer
     */
    void takeHealing(int healingPoints);

    /**
     * Hero recharges - increases current mana by given amount.
     * @param manaPoints must be positive integer
     */
    void takeMana(int manaPoints);

    /**
     * Hero equips a weapon.
     * A weapon can be equipped only if:
     * 1. Hero's level is greater or equal to weapon's level.
     * 2. Weapon's damage is greater than hero's current weapon's damage.
     */
    boolean equip(Weapon weapon);

    /**
     * Hero learns a spell.
     * A spell can be learnt only if:
     * 1. Hero's level is greater or equal to spell's level.
     * 2. Spell's damage is greater than hero's current spell's damage.
     */
    boolean learn(Spell spell);
}
