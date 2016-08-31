package cz.agents.gtlibrary.experimental.imperfectrecall.dag;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.HashMap;
import java.util.Map;

public class DAGConfig extends ConfigImpl<DAGInformationSet> {
    protected Map<DAGGameState, Double> actualUtilityValuesInLeafs = new HashMap<>();
    protected Map<GameState, Map<Action, GameState>> successors = new HashMap<>();

    @Override
    public DAGInformationSet createInformationSetFor(GameState gameState) {
        return new DAGInformationSet((DAGGameState) gameState);
    }

    public void addInformationSetFor(GameState state) {
        if (state.isGameEnd()) {
            setUtility(state);
        } else {
            super.addInformationSetFor(state);
        }
        if (state.isPlayerToMoveNature())
            return;
    }

    private void setUtility(GameState state) {
        actualUtilityValuesInLeafs.put((DAGGameState) state, state.getUtilities()[0]);
    }

    public GameState getSuccessor(GameState state, Action action) {
        Map<Action, GameState> stateSuccessors = successors.get(state);

        if(stateSuccessors == null)
            return null;
        return stateSuccessors.get(action);
    }

    public void setSuccessor(GameState state, Action action, GameState successor) {
        Map<Action, GameState> stateSuccessors = successors.get(state);

        if(stateSuccessors == null)
            stateSuccessors = new HashMap<>();
        stateSuccessors.put(action, successor);
        successors.put(state, stateSuccessors);
        DAGInformationSet informationSet = getInformationSetFor(state);

        if(informationSet != null)
            informationSet.addAction(action);
    }

    public Map<GameState, Map<Action, GameState>> getSuccessors() {
        return successors;
    }

    @Override
    public Double getActualNonzeroUtilityValues(GameState leaf) {
        return actualUtilityValuesInLeafs.get(leaf);
    }
}
