package cz.agents.gtlibrary.domain.observationGame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 * Created by bbosansky on 11/3/17.
 */
public class ObsGameFollowerAction extends ActionImpl {

    public enum FollowerActionType {ATTACK, OBSERVE, WAIT};
    private FollowerActionType type;
    private int row;
    private int hashCode = -1;

    public ObsGameFollowerAction(FollowerActionType type, int row, InformationSet informationSet) {
        super(informationSet);
        this.type = type;
        this.row = row;
    }

    @Override
    public void perform(GameState gameState) {
        ((ObsGameState)gameState).executeFollowerAction(this);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1) {
            final int prime = 31;

            hashCode = 1;
            hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
            hashCode = prime * hashCode + ((type == null) ? 0 : type.hashCode());
            hashCode = prime * hashCode + row*17;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObsGameFollowerAction a = (ObsGameFollowerAction)obj;
        if (this.type != a.type)
            return false;
        if (this.row != a.row)
            return false;

        return true;
    }

    public FollowerActionType getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    @Override
    public String toString() {
        return "FA[" + type + ", " + row + ']';
    }
}
