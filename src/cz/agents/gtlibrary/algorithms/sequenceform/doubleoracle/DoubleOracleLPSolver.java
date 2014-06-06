package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.Map;

public interface DoubleOracleLPSolver {

    public void calculateStrategyForPlayer(int secondPlayerIndex, GameState root, DoubleOracleConfig algConfig, double currentBoundSize);
    public Double getResultForPlayer(Player player);
    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player);
    public void setDebugOutput(PrintStream debugOutput);

    public long getOverallGenerationTime();

    public long getOverallConstraintGenerationTime();

    public long getOverallConstraintLPSolvingTime();
}
