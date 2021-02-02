package bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.minion;

import bg.sofia.uni.fmi.mjt.dungeonsonline.server.actor.Actor;

public interface IMinion extends Actor {

    /**
     * @return experience to be given to the minion's killer.
     */
    int giveExperience();

}
