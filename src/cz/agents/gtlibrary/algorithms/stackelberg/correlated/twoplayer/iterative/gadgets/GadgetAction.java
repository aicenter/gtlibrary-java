package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 * Created by Jakub Cerny on 08/12/2017.
 */
public class GadgetAction extends ActionImpl {

    protected final int index;
    protected final int hash;
    protected final GameState state;

    public GadgetAction(InformationSet informationSet, ISKey key) {
        super(informationSet);
        index = -1;
        hash = key.hashCode();//informationSet.hashCode();
        this.state = null;
    }

    public GadgetAction(InformationSet informationSet, GameState state, int idx) {
        super(informationSet);
        index = idx;
        hash = 13*state.hashCode() + idx;
        this.state = state;
    }

    @Override
    public void perform(GameState gameState) {
        // no need
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof GadgetAction)
            return ((GadgetAction)o).hash == hash;
        return false;
    }

    @Override
    public String toString() {
        return "GA{" +
                "ix=" + index +
                ", hsh=" + hash +
                '}';
    }

    public int getIndex(){
        return index;
    }
    public GameState getState(){ return state; }
}
