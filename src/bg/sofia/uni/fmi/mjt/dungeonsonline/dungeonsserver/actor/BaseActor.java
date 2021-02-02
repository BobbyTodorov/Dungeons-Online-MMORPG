package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.attributes.Stats;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.skill.Spell;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.treasure.skill.Weapon;
import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.validator.ArgumentValidator;

public abstract class BaseActor implements Actor {

    protected String name;
    protected int level;
    protected Stats stats;
    protected Weapon weapon;
    protected Spell spell;

    protected BaseActor(String name) {
        ArgumentValidator.checkForNullArguments(name);

        this.name = name;
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

    @Override
    public int attack() {
        int weaponDamage = 0;
        int spellDamage = 0;

        if (getWeapon() != null) {
            weaponDamage = getWeapon().getDamage() + stats.getAttack();
        }
        if (getSpell() != null) {
            spellDamage = getSpell().getDamage() + stats.getAttack();
        }

        return (spellDamage > weaponDamage && castSpell(getSpell())) ? spellDamage : weaponDamage;
    }

    private boolean castSpell(Spell spell){
        ArgumentValidator.checkForNullArguments(spell);

        int spellManaCost = spell.getManaCost();
        if(stats.getCurrentMana() - spellManaCost > 0){
            stats.decreaseCurrentMana(spellManaCost);
            return true;
        }

        return false;
    }
}
