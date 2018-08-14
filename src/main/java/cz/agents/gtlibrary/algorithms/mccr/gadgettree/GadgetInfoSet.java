package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.GameState;

public class GadgetInfoSet extends MCTSInformationSet {

    private final GadgetISKey isKey;

    public GadgetInfoSet(GameState state, GadgetISKey isKey) {
        super(state);
        this.playerHistory = isKey.getSequence();
        this.isKey = isKey;
        this.hashCode = isKey.getHash();
    }

    @Override
    public String toString() {
        return "Gadget "+super.toString();
    }

    @Override
    public ISKey getISKey() {
        return isKey;
    }
}
