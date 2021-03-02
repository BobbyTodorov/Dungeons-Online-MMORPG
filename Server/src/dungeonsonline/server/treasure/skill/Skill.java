package dungeonsonline.server.treasure.skill;

import dungeonsonline.server.treasure.Treasure;

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
