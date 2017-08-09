package cz.agents.gtlibrary.domain.wichardtne;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class WichardtAction extends ActionImpl {

    private String type;

    public WichardtAction(String type, InformationSet informationSet) {
        super(informationSet);
        this.type = type;
    }

    @Override
    public void perform(GameState gameState) {
        ((WichardtGameState)gameState).increaseRound();
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WichardtAction)) return false;
        if (!super.equals(o)) return false;

        WichardtAction that = (WichardtAction) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public String toString() {
        return type;
    }
}
