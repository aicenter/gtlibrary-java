package cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alossgame;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.Arrays;
import java.util.List;

public class ALossExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public ALossExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        ALossGameState state = (ALossGameState) gameState;

        if (state.getPlayerToMove().equals(ALossGameInfo.FIRST_PLAYER))
            return getP1Actions(state);
        return getP2Actions(state);
    }

    private List<Action> getP1Actions(ALossGameState state) {
        if (state.getSequenceFor(ALossGameInfo.SECOND_PLAYER).isEmpty())
            return Arrays.asList(new Action[]{new ALossAction(getAlgorithmConfig().getInformationSetFor(state), "a"),
                    new ALossAction(getAlgorithmConfig().getInformationSetFor(state), "b")});
        return Arrays.asList(new Action[]{new ALossAction(getAlgorithmConfig().getInformationSetFor(state), "g"),
                new ALossAction(getAlgorithmConfig().getInformationSetFor(state), "h")});
    }

    private List<Action> getP2Actions(ALossGameState state) {
        return Arrays.asList(new Action[]{new ALossAction(getAlgorithmConfig().getInformationSetFor(state), "c"),
                new ALossAction(getAlgorithmConfig().getInformationSetFor(state), "d")});
    }
}
