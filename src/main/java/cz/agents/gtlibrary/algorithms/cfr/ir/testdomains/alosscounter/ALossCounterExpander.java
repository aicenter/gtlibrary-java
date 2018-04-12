package cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alosscounter;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.Arrays;
import java.util.List;

public class ALossCounterExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public ALossCounterExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        ALossCounterGameState state = (ALossCounterGameState) gameState;

        if (state.getPlayerToMove().equals(ALossCounterGameInfo.FIRST_PLAYER))
            return getP1Actions(state);
        return getP2Actions(state);
    }

    private List<Action> getP1Actions(ALossCounterGameState state) {
        if (state.getSequenceFor(ALossCounterGameInfo.SECOND_PLAYER).isEmpty())
            return Arrays.asList(new Action[]{new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "a", new int[]{1, 1}),
                    new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "b", new int[]{1, 2})});
        return Arrays.asList(new Action[]{new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "g", new int[]{1, 1}),
                new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "h", new int[]{1, 1})});
    }

    private List<Action> getP2Actions(ALossCounterGameState state) {
        ALossCounterAction lastP1Action = (ALossCounterAction) state.getSequenceFor(ALossCounterGameInfo.FIRST_PLAYER).getLast();

        if (lastP1Action.getName().equals("a"))
            return Arrays.asList(new Action[]{new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "c", new int[]{1, 1}),
                    new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "d", new int[]{1, 1})});
        return Arrays.asList(new Action[]{new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "e", new int[]{1, 1}),
                new ALossCounterAction(getAlgorithmConfig().getInformationSetFor(state), "f", new int[]{1, 1})});
    }
}
