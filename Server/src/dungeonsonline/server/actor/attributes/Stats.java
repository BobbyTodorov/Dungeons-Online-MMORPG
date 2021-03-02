package dungeonsonline.server.actor.attributes;

import dungeonsonline.server.validator.ArgumentValidator;

public final class Stats {

    private int maxHealthPoints, currentHealthPoints;
    private int maxManaPoints, currentManaPoints;
    private int attackPoints;
    private int defensePoints;

    public Stats(int healthPoints, int manaPoints, int attackPoints, int defensePoints) {
        ArgumentValidator.checkForPositiveArguments(healthPoints);
        ArgumentValidator.checkForNonNegativeArguments(manaPoints, attackPoints, defensePoints);

        this.maxHealthPoints = this.currentHealthPoints = healthPoints;
        this.maxManaPoints = this.currentManaPoints = manaPoints;
        this.attackPoints = attackPoints;
        this.defensePoints = defensePoints;
    }


    public int getMaxHealth() {
        return maxHealthPoints;
    }

    public int getMaxMana() {
        return maxManaPoints;
    }

    public int getCurrentHealth() {
        return currentHealthPoints;
    }

    public int getCurrentMana() {
        return currentManaPoints;
    }

    public int getAttack() {
        return attackPoints;
    }

    public int getDefense() {
        return defensePoints;
    }


    public void increaseMaxHealth(int amountOfHealthPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfHealthPoints);

        maxHealthPoints += amountOfHealthPoints;
    }

    public void increaseMaxMana(int amountOfManaPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfManaPoints);

        maxManaPoints += amountOfManaPoints;
    }

    public void increaseCurrentHealth(int amountOfHealthPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfHealthPoints);

        currentHealthPoints = Math.min(currentHealthPoints + amountOfHealthPoints, maxHealthPoints);
    }

    public void decreaseCurrentHealth(int amountOfHealthPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfHealthPoints);

        currentHealthPoints = Math.max(currentHealthPoints - amountOfHealthPoints, 0);
    }

    public void increaseCurrentMana(int amountOfManaPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfManaPoints);

        currentManaPoints = Math.min(currentManaPoints + amountOfManaPoints, maxManaPoints);
    }

    public void decreaseCurrentMana(int amountOfManaPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfManaPoints);

        currentManaPoints = Math.max(currentManaPoints - amountOfManaPoints, 0);
    }

    public void increaseAttack(int amountOfAttackPoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfAttackPoints);

        attackPoints += amountOfAttackPoints;
    }

    public void increaseDefense(int amountOfDefensePoints) {
        ArgumentValidator.checkForPositiveArguments(amountOfDefensePoints);

        defensePoints += amountOfDefensePoints;
    }

    @Override
    public String toString() {
        return "health=" + currentHealthPoints + "/" + maxHealthPoints +
            ", mana=" + currentManaPoints + "/" + maxManaPoints +
            ", attackPoints=" + attackPoints +
            ", defensePoints=" + defensePoints +
            '}';
    }
}
