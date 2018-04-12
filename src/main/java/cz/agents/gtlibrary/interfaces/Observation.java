package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;

public interface Observation extends Serializable{
    /**
     * Every imperfect recall action should have possibly different observation for each player excluding nature.
     * If one wants to avoid absent mindedness every action should have non-empty observation for the player playing this action.
     */

    public boolean isEmpty();
}
