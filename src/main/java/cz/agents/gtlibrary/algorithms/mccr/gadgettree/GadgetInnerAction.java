package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GadgetInnerAction extends ActionImpl{
    private final boolean doFollow;

    public GadgetInnerAction(boolean doFollow, InformationSet informationSet) {
        super(informationSet);
        this.doFollow = doFollow;
    }

    @Override
    public void perform(GameState gameState) {

    }

    @Override
    public int hashCode() {
        return doFollow ? 1 : 0;
    }

    @Override
    public String toString() {
        return "gadget " + (doFollow ? "follow" : "terminate");
    }

    public boolean isFollow() {
        return doFollow;
    }
}
