package cz.agents.gtlibrary.domain.clairvoyance;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class P2Action extends ActionImpl {
    private boolean isCall;

    public P2Action(InformationSet informationSet, boolean isCall) {
        super(informationSet);
        this.isCall = isCall;
    }

    @Override
    public void perform(GameState gameState) {
        if (isCall)
            ((ClairvoyanceGameState) gameState).call();
        else
            ((ClairvoyanceGameState) gameState).fold();
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int hashCode = 1;
        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + (isCall ? 1 : 0);
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
        P2Action other = (P2Action) obj;

        if (isCall != other.isCall)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "P2: " + (isCall ? "c" : "f") + ((ClairvoyanceGameState)informationSet.getAllStates().iterator().next()).getP1Money();
    }
}
