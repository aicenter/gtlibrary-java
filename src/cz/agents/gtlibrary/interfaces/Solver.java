package cz.agents.gtlibrary.interfaces;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;

import java.util.Map;

/**
 * Created by Jakub Cerny on 01/09/2017.
 */
public interface Solver {

    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander);
    public Double getResultForPlayer(Player leader);
    public Map<Sequence,Double> getResultStrategiesForPlayer(Player player);
    public long getOverallConstraintGenerationTime();
    public long getOverallConstraintLPSolvingTime();
    public String getInfo();
}
