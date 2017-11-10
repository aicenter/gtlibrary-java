package cz.agents.gtlibrary.domain.observationGame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 * Created by bbosansky on 11/3/17.
 */
public class ObsGameLeaderAction extends ActionImpl{

    private int fromRow;
    private int toRow;
    private int hashCode = -1;


    public ObsGameLeaderAction(int fromRow, int toRow, InformationSet informationSet) {
        super(informationSet);
        this.fromRow = fromRow;
        this.toRow = toRow;
    }

    @Override
    public void perform(GameState gameState) {
        ((ObsGameState)gameState).executeLeaderAction(this);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1) {
            final int prime = 31;

            hashCode = 1;
            hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
            hashCode = prime * hashCode + fromRow*17;
            hashCode = prime * hashCode + toRow*17;
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
        ObsGameLeaderAction a = (ObsGameLeaderAction)obj;
        if (this.fromRow != a.fromRow)
            return false;
        if (this.toRow != a.toRow)
            return false;

        return true;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getToRow() {
        return toRow;
    }

    @Override
    public String toString() {
        return "LA[" + fromRow + " -> " + toRow + "]";
    }
}
