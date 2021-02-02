package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill;


import bg.sofia.uni.fmi.mjt.dungeonsonline.server.validator.ArgumentValidator;

public abstract class BaseSkill implements Skill {
    protected String name;
    protected int damage;
    protected int level;

    public BaseSkill(String name, int damage, int level){
        ArgumentValidator.checkForNullArguments(name);
        ArgumentValidator.checkForPositiveArguments(damage, level);

        this.name = name;
        this.damage = damage;
        this.level = level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
