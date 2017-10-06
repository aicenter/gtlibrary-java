package cz.agents.gtlibrary.experimental.imperfectrecall.dag.ttt;

import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.imperfectrecall.IRTTTState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.dag.DAGInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.BitSet;

public class DAGTTTState extends IRTTTState implements DAGGameState {
    protected DAGConfig config;

    public static void main(String[] args) {

        DAGConfig config = new DAGConfig();
        GameState root = new DAGTTTState(config);
        Expander<DAGInformationSet> expander = new TTTExpander<>(config);

        BasicGameBuilder.build(root, config, expander);
        System.out.println(config.getAllInformationSets().size());
    }

    public DAGTTTState(DAGConfig config) {
        this.config = config;
    }

    public DAGTTTState(BitSet s, char toMove, byte moveNum, DAGConfig config) {
        super(s, toMove, moveNum);
        this.config = config;
    }

    public DAGTTTState(DAGTTTState state) {
        super(state);
        this.config = state.config;
    }

    @Override
    public GameState performAction(Action action) {
        GameState successor = config.getSuccessor(this, action);

        if (successor == null) {
            successor = super.performAction(action);
            config.setSuccessor(this, action, successor);
        }
        return successor;
    }

    @Override
    public Object getDAGKey() {
        return s.clone();
    }

    @Override
    public GameState copy() {
        return new DAGTTTState(this);
    }

    @Override
    public boolean equals(Object obj) {
        DAGTTTState other = (DAGTTTState) obj;

        return s.equals(other.s);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }
}
