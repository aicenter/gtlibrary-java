package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameState;

public class IRCFRInformationSet extends IRInformationSetImpl {

    private FixedForIterationData data;

    public IRCFRInformationSet(GameState state) {
        super(state);
    }

    public FixedForIterationData getData() {
        return data;
    }

    public void setData(FixedForIterationData data) {
        this.data = data;
    }
}
