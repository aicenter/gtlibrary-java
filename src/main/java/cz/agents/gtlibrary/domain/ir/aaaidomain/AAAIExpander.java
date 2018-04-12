package cz.agents.gtlibrary.domain.ir.aaaidomain;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.Arrays;
import java.util.List;

public class AAAIExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public AAAIExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        AAAIGameState aaaiGameState = (AAAIGameState) gameState;

        if (aaaiGameState.getPlayerToMove().equals(AAAIGameInfo.FIRST_PLAYER))
            return getP1Actions(aaaiGameState);
        return getP2Actions(aaaiGameState);
    }

    private List<Action> getP2Actions(AAAIGameState aaaiGameState) {
        AAAIAction lastAction = (AAAIAction) aaaiGameState.getSequenceFor(AAAIGameInfo.FIRST_PLAYER).getLast();

        if (lastAction.getActionType().equals("a"))
            return Arrays.asList(new Action[]{new AAAIAction("c", getAlgorithmConfig().getInformationSetFor(aaaiGameState)),
                    new AAAIAction("d", getAlgorithmConfig().getInformationSetFor(aaaiGameState))});
        return Arrays.asList(new Action[]{new AAAIAction("e", getAlgorithmConfig().getInformationSetFor(aaaiGameState)),
                new AAAIAction("f", getAlgorithmConfig().getInformationSetFor(aaaiGameState))});
    }

    private List<Action> getP1Actions(AAAIGameState aaaiGameState) {
        if (aaaiGameState.getSequenceForPlayerToMove().isEmpty())
            return Arrays.asList(new Action[]{new AAAIAction("a", getAlgorithmConfig().getInformationSetFor(aaaiGameState)),
                    new AAAIAction("b", getAlgorithmConfig().getInformationSetFor(aaaiGameState))});
        return Arrays.asList(new Action[]{new AAAIAction("g", getAlgorithmConfig().getInformationSetFor(aaaiGameState)),
                new AAAIAction("h", getAlgorithmConfig().getInformationSetFor(aaaiGameState))});
    }
}
