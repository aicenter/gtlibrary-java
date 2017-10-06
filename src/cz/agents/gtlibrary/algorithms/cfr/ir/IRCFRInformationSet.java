package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;

public class IRCFRInformationSet extends IRInformationSetImpl {

    private FixedForIterationData data;

    public IRCFRInformationSet(GameState state) {
        super(state);
    }

    public IRCFRInformationSet(GameState state, ImperfectRecallISKey isKey) {
        super(state, isKey);
    }

    public FixedForIterationData getData() {
        return data;
    }

    public void setData(FixedForIterationData data) {
        this.data = data;
    }
}
