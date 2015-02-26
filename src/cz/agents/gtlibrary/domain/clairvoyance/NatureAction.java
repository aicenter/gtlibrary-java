package cz.agents.gtlibrary.domain.clairvoyance;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class NatureAction extends ActionImpl {
    private boolean isWinningCard;

    public NatureAction(InformationSet informationSet, boolean isWinningCard) {
        super(informationSet);
        this.isWinningCard = isWinningCard;
    }

    @Override
    public void perform(GameState gameState) {
        ((ClairvoyanceGameState) gameState).setWinningCard(isWinningCard);
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int hashCode = 1;
        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + (isWinningCard ? 1 : 0);
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
        NatureAction other = (NatureAction) obj;

        if (isWinningCard != other.isWinningCard)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return isWinningCard ? "W" : "L";
    }
}
