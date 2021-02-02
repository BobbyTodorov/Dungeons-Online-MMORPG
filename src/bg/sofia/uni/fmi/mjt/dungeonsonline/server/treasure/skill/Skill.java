package bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.skill;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.treasure.Treasure;

public interface Skill extends Treasure {
    /**
     * @return Skill's name
     */
    String getName();

    /**
     * @return Skill's damage points
     */
    int getDamage();

    /**
     * @return Skill's level
     */
    int getLevel();
}
