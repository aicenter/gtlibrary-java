package cz.agents.gtlibrary.algorithms.sequenceform;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public interface FullSequenceFormLP {

    public void calculateBothPlStrategy(GameState rootState, SequenceFormConfig<SequenceInformationSet> algConfig);

    public Map<Sequence,Double> getResultStrategiesForPlayer(Player player);

    public Double getResultForPlayer(Player actingPlayer);
}
