package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.*;

import java.util.List;

public abstract class ExpanderImpl<I extends InformationSet> implements Expander<I> {

    private static final long serialVersionUID = -2367393002316400229L;

    private AlgorithmConfig<I> algConfig;

    public ExpanderImpl(AlgorithmConfig<I> algConfig) {
        this.algConfig = algConfig;
    }

    @Override
    public AlgorithmConfig<I> getAlgorithmConfig() {
        return algConfig;
    }

    @Override
    public List<Action> getActionsForUnknownIS(GameState gameState) {
        return getActions(algConfig.createInformationSetFor(gameState));
    }

    public void setAlgConfig(AlgorithmConfig<I> algConfig) {
        this.algConfig = algConfig;
    }

    @Override
    public List<Action> getActions(I informationSet) {
        GameState state = informationSet.getAllStates().iterator().next();
        List<Action> actions = getActions(state);

        if (!state.isPlayerToMoveNature())
            for (Action a : actions) {
                a.setInformationSet(informationSet);
            }
        return actions;
    }
}
