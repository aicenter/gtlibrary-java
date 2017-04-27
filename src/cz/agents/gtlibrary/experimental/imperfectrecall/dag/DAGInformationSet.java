package cz.agents.gtlibrary.experimental.imperfectrecall.dag;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashSet;
import java.util.Set;

public class DAGInformationSet extends IRInformationSetImpl {

    protected Set<Action> actions;

    public DAGInformationSet(DAGGameState state) {
        super((GameState) state);
        actions = new HashSet<>();
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public Set<Action> getActions() {
        return actions;
    }

}
