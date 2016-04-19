package cz.agents.gtlibrary.algorithms.cfr.ir;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.List;

public class IRCFRInformationSet extends IRInformationSetImpl {

    private OOSAlgorithmData data;

    public IRCFRInformationSet(GameState state) {
        super(state);
    }

    public IRCFRInformationSet(GameState state, List<Action> actions) {
        super(state);
        data = new OOSAlgorithmData(actions);
    }

    public IRCFRInformationSet(GameState state, OOSAlgorithmData data) {
        super(state);
        this.data = data;
    }

    public OOSAlgorithmData getData() {
        return data;
    }

    public void setData(OOSAlgorithmData data) {
        this.data = data;
    }
}
