package cz.agents.gtlibrary.domain.metroTransport;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.List;

/**
 * Created by Jakub Cerny on 05/10/2017.
 */
public class MetroTransportExpander<I extends InformationSet> extends ExpanderImpl<I> {

    public MetroTransportExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        if (gameState.getPlayerToMove().equals(MetroTransportGameInfo.DEFENDER))
            return getDefenderActions((MetroTransportGameState)gameState);
        else
            return getAttackerActions((MetroTransportGameState)gameState);
    }

    public List<Action> getDefenderActions(MetroTransportGameState gameState) {
        return null;
    }

    public List<Action> getAttackerActions(MetroTransportGameState gameState) {
        return null;
    }

}
