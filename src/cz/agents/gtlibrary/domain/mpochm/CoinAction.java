package cz.agents.gtlibrary.domain.mpochm;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class CoinAction extends ActionImpl {

    private MPoCHMGameState.CoinState state;
    private int hashCode = -1;

    public CoinAction(MPoCHMGameState.CoinState state, InformationSet informationSet) {
        super(informationSet);
        this.state = state;
    }

    @Override
    public void perform(GameState gameState) {
        ((MPoCHMGameState) gameState).processCoinState(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoinAction)) return false;
        if (!super.equals(o)) return false;

        CoinAction that = (CoinAction) o;

        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode != -1)
            return hashCode;
        final int prime = 31;

        hashCode = 1;
        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + state.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "[" + state + "]";
    }
}
;