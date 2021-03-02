package dungeonsonline.server.actor;

import dungeonsonline.server.actor.attributes.Stats;
import dungeonsonline.server.treasure.skill.Spell;
import dungeonsonline.server.treasure.skill.Weapon;
import dungeonsonline.server.validator.ArgumentValidator;

public abstract class BaseActor implements Actor {

    protected String name;
    protected int level;
    protected Stats stats;
    protected Weapon weapon;
    protected Spell spell;

    public BaseActor(String name, Stats stats) {
        ArgumentValidator.checkForNullArguments(name, stats);

        this.name = name;
        this.stats = stats;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Stats getStats() {
        return stats;
    }

    @Override
    public boolean isAlive() {
        return stats.getCurrentHealth() > 0;
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    @Override
    public void takeDamage(int damagePoints) {
        ArgumentValidator.checkForPositiveArguments(damagePoints);

        stats.decreaseCurrentHealth(damagePoints);
    }

    /**
     * @return The higher value between base attack+weapon attack and base attack+spell attack (if there is enough
     * mana to cast it) if the hero has equipped weapon and learnt spell. If there is no equipped weapon but there is
     * learnt spell, returns base attack+spell attack (if there is enough mana to cast it).
     * If there is no learnt spell but there is equipped weapon, returns base attack+weapon attack.
     * If there is no equipped weapon and no learnt spell, returns base attack.
     */
    @Override
    public int attack() {
        int baseDamage = stats.getAttack();
        int weaponDamage = baseDamage;
        int spellDamage = baseDamage;

        if (getWeapon() != null) {
            weaponDamage += getWeapon().getDamage();
        }
        if (getSpell() != null) {
            spellDamage += getSpell().getDamage();
        }

        return (spellDamage > weaponDamage && castSpell(getSpell())) ? spellDamage : weaponDamage;
    }

    private boolean castSpell(Spell spell) {
        ArgumentValidator.checkForNullArguments(spell);

        int spellManaCost = spell.getManaCost();
        if (stats.getCurrentMana() - spellManaCost > 0) {
            stats.decreaseCurrentMana(spellManaCost);
            return true;
        }

        return false;
    }
}
