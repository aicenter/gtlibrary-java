package cz.agents.gtlibrary.domain.ir.leftright;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class LRExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public LRExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().getId() == 0)
            return getP1Actions(gameState);
        return getP2Actions(gameState);
    }

    private List<Action> getP2Actions(GameState gameState) {
        Sequence sequence = gameState.getSequenceForPlayerToMove();

        if (sequence.size() == 0)
            return getFirstRoundP2Actions(gameState);
        else
            return getSecondRoundP2Actions(gameState);
    }

    private List<Action> getSecondRoundP2Actions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "L"));
        actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "R"));
        return actions;
    }

    private List<Action> getFirstRoundP2Actions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        if(((LRAction)gameState.getSequenceFor(LRGameInfo.FIRST_PLAYER).getLast()).getType().startsWith("R")) {
            actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "A'"));
            actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "P'"));
        } else {
            actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "A"));
            actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "P"));
        }
        return actions;
    }

    private List<Action> getP1Actions(GameState gameState) {
        List<Action> actions = new ArrayList<>(2);

        actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "L"));
        actions.add(new LRAction(getAlgorithmConfig().getInformationSetFor(gameState), "R"));
        return actions;
    }
}
