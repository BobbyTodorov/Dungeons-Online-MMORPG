package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.map.Visualizable;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill.Weapon;

public interface Actor extends Visualizable {
    /**
     * Returns Actor's name.
     */
    String getName();

    /**
     * Returns Actor's level.
     */
    int getLevel();

    /**
     * Returns Actor's stats.
     */
    Stats getStats();

    /**
     * Tells whether the Actor is alive or not.
     */
    boolean isAlive();

    /**
     * Returns Actor's current weapon.
     */
    Weapon getWeapon();

    /**
     * Returns Actor's current spell.
     * A Spell can be used only if the Actor has enough mana to do so
     * (the mana is then decreased according to the Spell's mana cost).
     */
    Spell getSpell();

    /**
     * Decreases Actor's health by given amount.
     *
     * @param damagePoints must be positive integer.
     */
    void takeDamage(int damagePoints);

    /**
     * Returns Actor's attack points (the stronger between the Spell and the Weapon).
     * At some point it is possible for an Actor to have weapon (or spell) only.
     */
    int attack();
}
