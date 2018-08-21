package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GadgetChanceAction extends ActionImpl {
    private final int actionIndex;

    public GadgetChanceAction(int actionIndex) {
        super(null);
        this.actionIndex = actionIndex;
    }

    @Override
    public void perform(GameState gameState) {
        throw new NotImplementedException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;

        int hashCode = 1;
        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + actionIndex;
        return hashCode;
    }

    @Override
    public String toString() {
        return "GadgetAction " + actionIndex ;
    }
}
