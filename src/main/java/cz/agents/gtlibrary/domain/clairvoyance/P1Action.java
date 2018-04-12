package cz.agents.gtlibrary.domain.clairvoyance;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P1Action extends ActionImpl {

    private int value;

    public P1Action(InformationSet informationSet, int value) {
        super(informationSet);
        this.value = value;
    }

    @Override
    public void perform(GameState gameState) {
        ((ClairvoyanceGameState) gameState).addP1Money(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;

        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + value;
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
        P1Action other = (P1Action) obj;

        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "P1: " + value + (((ClairvoyanceGameState) informationSet.getAllStates().iterator().next()).isWinningCard() ? "W" : "L");
    }
}
