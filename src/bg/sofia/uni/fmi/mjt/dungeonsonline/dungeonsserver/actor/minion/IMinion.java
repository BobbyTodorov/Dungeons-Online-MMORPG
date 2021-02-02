package bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.minion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.dungeonsserver.actor.Actor;

public interface IMinion extends Actor {

    /**
     * @return experience to be given to the minion's killer.
     */
    int giveExperience();

}
