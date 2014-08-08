package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public interface StackelbergSolver {

    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander);

    public Double getResultForPlayer(Player player);

    public Map<Sequence,Double> getResultStrategiesForPlayer(Player player);

    public long getOverallConstraintGenerationTime();

    public long getOverallConstraintLPSolvingTime();

}
