package cz.agents.gtlibrary.domain.ir.cfrcounterexample;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.Arrays;
import java.util.List;

public class CCExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public CCExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        CCGameState ccGameState = (CCGameState) gameState;

        if (ccGameState.isPlayerToMoveNature())
            return getNatureActions(ccGameState);
        return getPlayerActions(ccGameState);
    }

    private List<Action> getPlayerActions(CCGameState ccGameState) {
        if (ccGameState.getSequenceForPlayerToMove().isEmpty()) {
            CCAction lastNatureAction = (CCAction) ccGameState.getSequenceFor(CCGameInfo.NATURE).getLast();

            if (lastNatureAction.getActionType().equals("x"))
                return Arrays.asList(new Action[]{new CCAction("a", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                        new CCAction("b", getAlgorithmConfig().getInformationSetFor(ccGameState))});
            return Arrays.asList(new Action[]{new CCAction("c", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                    new CCAction("d", getAlgorithmConfig().getInformationSetFor(ccGameState))});
        }
        if (ccGameState.getSequenceFor(CCGameInfo.FIRST_PLAYER).size() == 1) {
            CCAction lastAction = (CCAction) ccGameState.getSequenceFor(CCGameInfo.FIRST_PLAYER).getLast();

            if (lastAction.getActionType().equals("b"))
                return Arrays.asList(new Action[]{new CCAction("t", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                        new CCAction("u", getAlgorithmConfig().getInformationSetFor(ccGameState))});
            return Arrays.asList(new Action[]{new CCAction("v", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                    new CCAction("w", getAlgorithmConfig().getInformationSetFor(ccGameState))});
        }
        CCAction lastAction = (CCAction) ccGameState.getSequenceFor(CCGameInfo.FIRST_PLAYER).getLast();

        if (lastAction.getActionType().equals("u"))
            return Arrays.asList(new Action[]{new CCAction("c", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                    new CCAction("d", getAlgorithmConfig().getInformationSetFor(ccGameState))});
        return Arrays.asList(new Action[]{new CCAction("a", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                new CCAction("b", getAlgorithmConfig().getInformationSetFor(ccGameState))});

    }

    private List<Action> getNatureActions(CCGameState ccGameState) {
        return Arrays.asList(new Action[]{new CCAction("x", getAlgorithmConfig().getInformationSetFor(ccGameState)),
                new CCAction("y", getAlgorithmConfig().getInformationSetFor(ccGameState))});
    }
}
