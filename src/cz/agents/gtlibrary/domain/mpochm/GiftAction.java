package cz.agents.gtlibrary.domain.mpochm;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GiftAction extends ActionImpl {
    private MPoCHMGameState.GiftState state;
    private int hashCode = -1;

    public GiftAction(MPoCHMGameState.GiftState state, InformationSet informationSet) {
        super(informationSet);
        this.state = state;
    }

    @Override
    public void perform(GameState gameState) {
        ((MPoCHMGameState) gameState).setGiftState(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GiftAction)) return false;
        if (!super.equals(o)) return false;

        GiftAction that = (GiftAction) o;

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
